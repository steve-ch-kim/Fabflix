package com.github.klefstad_teaching.cs122b.idm.model.request;

import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.IDMResults;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RefreshRequest {
    String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public RefreshRequest setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    public void validate() {
        if (refreshToken.length() != 36) {
            throw new ResultError(IDMResults.REFRESH_TOKEN_HAS_INVALID_LENGTH);
        }

        String token = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}$";
        Pattern token_pattern = Pattern.compile(token);
        Matcher token_matcher = token_pattern.matcher(this.refreshToken);

        if (!token_matcher.matches()) {
            throw new ResultError(IDMResults.REFRESH_TOKEN_HAS_INVALID_FORMAT);
        }
    }
}
