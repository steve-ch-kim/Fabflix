package com.github.klefstad_teaching.cs122b.idm.model.request;

public class AccessRequest {
    String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public AccessRequest setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }
}
