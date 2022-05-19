package com.github.klefstad_teaching.cs122b.idm.component;

import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.BasicResults;
import com.github.klefstad_teaching.cs122b.core.result.IDMResults;
import com.github.klefstad_teaching.cs122b.idm.config.IDMServiceConfig;
import com.github.klefstad_teaching.cs122b.idm.repo.IDMRepo;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.RefreshToken;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.User;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.type.TokenStatus;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.type.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.transform.Result;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.Ref;
import java.sql.Types;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
public class IDMAuthenticationManager
{
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String       HASH_FUNCTION = "PBKDF2WithHmacSHA512";

    private static final int ITERATIONS     = 10000;
    private static final int KEY_BIT_LENGTH = 512;

    private static final int SALT_BYTE_LENGTH = 4;

    public final IDMRepo repo;
    private final IDMServiceConfig config;

    @Autowired
    public IDMAuthenticationManager(IDMRepo repo, IDMServiceConfig config)
    {
        this.repo = repo;
        this.config = config;
    }

    private static byte[] hashPassword(final char[] password, String salt)
    {
        return hashPassword(password, Base64.getDecoder().decode(salt));
    }

    private static byte[] hashPassword(final char[] password, final byte[] salt)
    {
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(HASH_FUNCTION);

            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_BIT_LENGTH);

            SecretKey key = skf.generateSecret(spec);

            return key.getEncoded();

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] genSalt()
    {
        byte[] salt = new byte[SALT_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(salt);
        return salt;
    }

    public User selectAndAuthenticateUser(String email, char[] password)
    {
        String sql = "SELECT id, email, user_status_id, salt, hashed_password " +
                     "FROM idm.user " +
                     "WHERE email = :email;";

        MapSqlParameterSource source =
                new MapSqlParameterSource()
                        .addValue("email", email, Types.VARCHAR);

        List<User> users =
                repo.getTemplate().query(
                        sql,
                        source,
                        (rs, rowNum) ->
                                new User()
                                        .setId(rs.getInt("id"))
                                        .setEmail(rs.getString("email"))
                                        .setUserStatus(UserStatus.fromId(rs.getInt(("user_status_id"))))
                                        .setSalt(rs.getString("salt"))
                                        .setHashedPassword(rs.getString("hashed_password"))
                );

        if (users.size() != 1) {
            throw new ResultError(IDMResults.USER_NOT_FOUND);
        }

        byte[] hash_password = hashPassword(password, users.get(0).getSalt());
        String base64EncodedHashedPassword = Base64.getEncoder().encodeToString(hash_password);

        if (users.get(0).getUserStatus() == UserStatus.BANNED) {
            throw new ResultError(IDMResults.USER_IS_BANNED);
        }

        if (users.get(0).getUserStatus() == UserStatus.LOCKED) {
            throw new ResultError(IDMResults.USER_IS_LOCKED);
        }

        if (!users.get(0).getHashedPassword().equals(base64EncodedHashedPassword)) {
            throw new ResultError(IDMResults.INVALID_CREDENTIALS);
        }

        return users.get(0);
    }

    public void createAndInsertUser(String email, char[] password)
    {
        byte[] salt = genSalt();
        byte[] hash_password = hashPassword(password, salt);
        String base64EncodedHashedSalt = Base64.getEncoder().encodeToString(salt);
        String base64EncodedHashedPassword = Base64.getEncoder().encodeToString(hash_password);
        int user_status_id = 1;

        String sql =
                "INSERT INTO idm.user (email, user_status_id, salt, hashed_password) " +
                "VALUES (:email, :user_status_id, :salt, :hash_password);";

        MapSqlParameterSource source =
                new MapSqlParameterSource()
                        .addValue("email", email, Types.VARCHAR)
                        .addValue("user_status_id", user_status_id, Types.INTEGER)
                        .addValue("salt", base64EncodedHashedSalt, Types.CHAR)
                        .addValue("hash_password", base64EncodedHashedPassword, Types.CHAR);

        try {
            repo.getTemplate().update(sql, source);
        } catch (DuplicateKeyException e) {
            throw new ResultError(IDMResults.USER_ALREADY_EXISTS);
        }
    }

    public void insertRefreshToken(RefreshToken refreshToken)
    {
        User user = getUserFromRefreshToken(refreshToken);

        String sql = "INSERT INTO idm.refresh_token (token, user_id, token_status_id, expire_time, max_life_time) " +
                     "VALUES (:token, :user_id, :token_status_id, :expire_time, :max_life_time)";

        MapSqlParameterSource source =
                new MapSqlParameterSource()
                        .addValue("token", refreshToken.getToken())
                        .addValue("user_id", user.getId())
                        .addValue("token_status_id", refreshToken.getTokenStatus().id())
                        .addValue("expire_time", Date.from(refreshToken.getExpireTime()))
                        .addValue("max_life_time", Date.from(refreshToken.getMaxLifeTime()));

        repo.getTemplate().update(sql, source);
    }

    public RefreshToken verifyRefreshToken(String token)
    {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

        String sql =
                "SELECT id, token, user_id, token_status_id, expire_time, max_life_time " +
                        "FROM idm.refresh_token " +
                        "WHERE token = :token;";

        MapSqlParameterSource source =
                new MapSqlParameterSource()
                        .addValue("token", token, Types.VARCHAR);

        List<RefreshToken> tokens =
                repo.getTemplate().query(
                        sql,
                        source,
                        (rs, rowNum) ->
                                new RefreshToken()
                                        .setId(rs.getInt("id"))
                                        .setToken(rs.getString("token"))
                                        .setUserId(rs.getInt("user_id"))
                                        .setTokenStatus(TokenStatus.fromId(rs.getInt("token_status_id")))
                                        .setExpireTime(Instant.from(format.parse(rs.getString("expire_time"))))
                                        .setMaxLifeTime(Instant.from(format.parse(rs.getString("max_life_time"))))
                );

        if (tokens.get(0).getTokenStatus() == TokenStatus.EXPIRED) {
            throw new ResultError(IDMResults.REFRESH_TOKEN_IS_EXPIRED);
        }

        if (tokens.get(0).getTokenStatus() == TokenStatus.REVOKED) {
            throw new ResultError(IDMResults.REFRESH_TOKEN_IS_REVOKED);
        }

        return tokens.get(0);
    }

    public void updateRefreshTokenExpireTime(RefreshToken token)
    {
        if (token.getToken().length() != 36) {
            throw new ResultError(IDMResults.REFRESH_TOKEN_HAS_INVALID_LENGTH);
        }

        int id = token.getId();
        Instant new_expire_time = Instant.now().plus(config.refreshTokenExpire());
        Date expire_time = Date.from(new_expire_time);

        String sql = "UPDATE idm.refresh_token " +
                "SET expire_time=:expire_time " +
                "WHERE id=:id;";

        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("id", id, Types.INTEGER)
                .addValue("expire_time", expire_time, Types.TIMESTAMP);

        repo.getTemplate().update(sql, source);
        token.setExpireTime(new_expire_time);
    }

    public void updateStatus(RefreshToken refreshToken, int token_status_id) {
        if (refreshToken.getToken().length() != 36) {
            throw new ResultError(IDMResults.REFRESH_TOKEN_HAS_INVALID_LENGTH);
        }

        int id = refreshToken.getId();

        String sql = "UPDATE idm.refresh_token " +
                "SET token_status_id=:token_status_id " +
                "WHERE id=:id;";

        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("id", id, Types.INTEGER)
                .addValue("token_status_id", token_status_id, Types.INTEGER);

        repo.getTemplate().update(sql, source);
        refreshToken.setTokenStatus(TokenStatus.fromId(token_status_id));
    }

    public User getUserFromRefreshToken(RefreshToken refreshToken)
    {
        String sql = "SELECT id " +
                     "FROM idm.user " +
                     "WHERE id = :user_id";

        MapSqlParameterSource source =
                new MapSqlParameterSource()
                        .addValue("user_id", refreshToken.getUserId(), Types.VARCHAR);

        List<User> users =
                repo.getTemplate().query(
                        sql,
                        source,
                        (rs, rowNum) ->
                                new User()
                                        .setId(rs.getInt("id"))
                );

        return users.get(0);
    }
}
