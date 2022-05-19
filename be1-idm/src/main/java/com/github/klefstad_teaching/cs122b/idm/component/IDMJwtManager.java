package com.github.klefstad_teaching.cs122b.idm.component;

import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.IDMResults;
import com.github.klefstad_teaching.cs122b.core.security.JWTManager;
import com.github.klefstad_teaching.cs122b.idm.config.IDMServiceConfig;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.RefreshToken;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.User;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.type.TokenStatus;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class IDMJwtManager
{
    private final JWTManager jwtManager;
    private final IDMServiceConfig config;

    @Autowired
    public IDMJwtManager(IDMServiceConfig serviceConfig, IDMServiceConfig config)
    {
        this.jwtManager =
            new JWTManager.Builder()
                .keyFileName(serviceConfig.keyFileName())
                .accessTokenExpire(serviceConfig.accessTokenExpire())
                .maxRefreshTokenLifeTime(serviceConfig.maxRefreshTokenLifeTime())
                .refreshTokenExpire(serviceConfig.refreshTokenExpire())
                .build();
        this.config = config;
    }

    private SignedJWT buildAndSignJWT(JWTClaimsSet claimsSet, JWSHeader header)
        throws JOSEException
    {
        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        signedJWT.sign(this.jwtManager.getSigner());

        return signedJWT;
    }

    private void verifyJWT(SignedJWT jwt)
        throws JOSEException, BadJOSEException
    {
        try {
            jwt.verify(this.jwtManager.getVerifier());
            this.jwtManager.getJwtProcessor().process(jwt, null);

            jwt.getJWTClaimsSet().getExpirationTime();

        } catch (IllegalStateException | JOSEException | BadJOSEException | ParseException e) {
            e.printStackTrace();
        }
    }

    public String buildAccessToken(User user) throws JOSEException {
        Instant current_time = Instant.now();

        JWTClaimsSet claimsSet =
                new JWTClaimsSet.Builder()
                        .subject(user.getEmail())
                        .expirationTime(Date.from(current_time.plus(jwtManager.getAccessTokenExpire())))
                        .claim(JWTManager.CLAIM_ID, user.getId())
                        .claim(JWTManager.CLAIM_ROLES, user.getRoles())
                        .issueTime(Date.from(Instant.now()))
                        .build();

        JWSHeader header =
                new JWSHeader.Builder(JWTManager.JWS_ALGORITHM)
                        .keyID(this.jwtManager.getEcKey().getKeyID())
                        .type(JWTManager.JWS_TYPE)
                        .build();

        SignedJWT signedJWT = buildAndSignJWT(claimsSet, header);

        try {
            verifyJWT(signedJWT);
        } catch (JOSEException | BadJOSEException e) {
            e.printStackTrace();
        }

        return signedJWT.serialize();
    }

    public void verifyAccessToken(String jws)
    {
        try {
            SignedJWT signedJWT = SignedJWT.parse(jws);

            // These two functions are what is checking your JWT to make sure it is valid
            // If it is not it will throw and error and take you to that catch
            // clause in this try and catch.
            signedJWT.verify(jwtManager.getVerifier());
            jwtManager.getJwtProcessor().process(signedJWT, null);

            // So this is to get the expire time but you have to check if the
            // current time is past this.
            Date expired_time = signedJWT.getJWTClaimsSet().getExpirationTime();
            Date current_time = Date.from(Instant.now());

            if (current_time.after(expired_time)) {
                throw new ResultError(IDMResults.ACCESS_TOKEN_IS_EXPIRED);
            }

        } catch (IllegalStateException | JOSEException | BadJOSEException e) {
            // If you get here that means the token is not valid and
            // you should throw the right Result error to show that
            throw new ResultError(IDMResults.ACCESS_TOKEN_IS_INVALID);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public RefreshToken buildRefreshToken(User user)
    {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUserId(user.getId());
        refreshToken.setTokenStatus(TokenStatus.ACTIVE);
        refreshToken.setExpireTime(Instant.now().plus(jwtManager.getRefreshTokenExpire()));
        refreshToken.setMaxLifeTime(Instant.now().plus(jwtManager.getMaxRefreshTokenLifeTime()));
        return refreshToken;
    }

    private UUID generateUUID()
    {
        return UUID.randomUUID();
    }
}
