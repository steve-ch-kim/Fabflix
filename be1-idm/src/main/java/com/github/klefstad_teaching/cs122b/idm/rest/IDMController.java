package com.github.klefstad_teaching.cs122b.idm.rest;

import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.IDMResults;
import com.github.klefstad_teaching.cs122b.idm.component.IDMAuthenticationManager;
import com.github.klefstad_teaching.cs122b.idm.component.IDMJwtManager;
import com.github.klefstad_teaching.cs122b.idm.model.request.AccessRequest;
import com.github.klefstad_teaching.cs122b.idm.model.request.LoginRequest;
import com.github.klefstad_teaching.cs122b.idm.model.request.RefreshRequest;
import com.github.klefstad_teaching.cs122b.idm.model.request.RegisterRequest;
import com.github.klefstad_teaching.cs122b.idm.model.response.LoginResponse;
import com.github.klefstad_teaching.cs122b.idm.model.response.RegisterResponse;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.RefreshToken;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.User;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.type.TokenStatus;
import com.github.klefstad_teaching.cs122b.idm.util.Validate;
import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.transform.Result;
import java.time.Instant;
import java.util.List;

@RestController
public class IDMController
{
    private final IDMAuthenticationManager authManager;
    private final IDMJwtManager            jwtManager;
    private final Validate                 validate;

    @Autowired
    public IDMController(IDMAuthenticationManager authManager,
                         IDMJwtManager jwtManager,
                         Validate validate)
    {
        this.authManager = authManager;
        this.jwtManager = jwtManager;
        this.validate = validate;
    }


    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        request.validate();
        authManager.createAndInsertUser(request.getEmail(), request.getPassword());

        RegisterResponse register = new RegisterResponse()
                .setResult(IDMResults.USER_REGISTERED_SUCCESSFULLY);

        return ResponseEntity
                .status(register.getResult().status())
                .body(register);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        request.validate();
        User user = authManager.selectAndAuthenticateUser(request.getEmail(), request.getPassword());
        LoginResponse login = new LoginResponse();

        try {
            String accessToken = jwtManager.buildAccessToken(user);
            RefreshToken refreshToken = jwtManager.buildRefreshToken(user);

            authManager.insertRefreshToken(refreshToken);

            login.setResult(IDMResults.USER_LOGGED_IN_SUCCESSFULLY);
            login.setAccessToken(accessToken);
            login.setRefreshToken(refreshToken.getToken());
        } catch (JOSEException e) {
            e.printStackTrace();
        }

        return ResponseEntity
                .status(login.getResult().status())
                .body(login);
    }


    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestBody RefreshRequest request) throws JOSEException {
        request.validate();
        LoginResponse refresh = new LoginResponse();
        RefreshToken refreshToken = authManager.verifyRefreshToken(request.getRefreshToken());
        User user = authManager.getUserFromRefreshToken(refreshToken);

        if (Instant.now().isAfter(refreshToken.getExpireTime()) || Instant.now().isAfter(refreshToken.getMaxLifeTime())) {
            authManager.updateStatus(refreshToken, 2);
            throw new ResultError(IDMResults.REFRESH_TOKEN_IS_EXPIRED);
        } else {
            authManager.updateRefreshTokenExpireTime(refreshToken);
            if (refreshToken.getExpireTime().isAfter(refreshToken.getMaxLifeTime())) {
                RefreshToken new_token = this.jwtManager.buildRefreshToken(user);
                authManager.updateStatus(refreshToken, 3);
                authManager.insertRefreshToken(new_token);
                String accessToken = jwtManager.buildAccessToken(user);

                refresh.setResult(IDMResults.RENEWED_FROM_REFRESH_TOKEN)
                        .setAccessToken(accessToken)
                        .setRefreshToken(new_token.getToken());
            } else {
                authManager.updateRefreshTokenExpireTime(refreshToken);
                String accessToken = jwtManager.buildAccessToken(user);

                refresh.setResult(IDMResults.RENEWED_FROM_REFRESH_TOKEN)
                        .setAccessToken(accessToken)
                        .setRefreshToken(refreshToken.getToken());
            }

            return ResponseEntity
                    .status(refresh.getResult().status())
                    .body(refresh);
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<RegisterResponse> authenticate(@RequestBody AccessRequest request) {
        jwtManager.verifyAccessToken(request.getAccessToken());

        RegisterResponse auth = new RegisterResponse();
        auth.setResult(IDMResults.ACCESS_TOKEN_IS_VALID);

        return ResponseEntity
                .status(auth.getResult().status())
                .body(auth);
    }
}
