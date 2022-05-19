package com.github.klefstad_teaching.cs122b.idm.model.response;

import com.github.klefstad_teaching.cs122b.core.base.ResponseModel;
import com.github.klefstad_teaching.cs122b.core.result.Result;

public class LoginResponse extends ResponseModel{
    Result result;
    String accessToken;
    String refreshToken;

    public Result getResult() {
        return result;
    }

    public LoginResponse setResult(Result result) {
        this.result = result;
        return this;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public LoginResponse setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public LoginResponse setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }
}