package com.github.klefstad_teaching.cs122b.idm.model.response;

import com.github.klefstad_teaching.cs122b.core.base.ResponseModel;
import com.github.klefstad_teaching.cs122b.core.result.Result;

public class RegisterResponse extends ResponseModel {
    private Result result;

    public Result getResult() {
        return this.result;
    }

    public RegisterResponse setResult(Result result) {
        this.result = result;
        return this;
    }
}
