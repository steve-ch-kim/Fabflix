package com.github.klefstad_teaching.cs122b.billing.model.response;

import com.github.klefstad_teaching.cs122b.core.base.ResponseModel;
import com.github.klefstad_teaching.cs122b.core.result.Result;

public class ItemResponse extends ResponseModel {
    private Result result;

    public Result getResult() {
        return this.result;
    }

    public ItemResponse setResult(Result result) {
        this.result = result;
        return this;
    }
}
