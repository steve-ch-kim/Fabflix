package com.github.klefstad_teaching.cs122b.billing.model.response;

import com.github.klefstad_teaching.cs122b.core.base.ResponseModel;
import com.github.klefstad_teaching.cs122b.core.result.Result;

public class OrderResponse extends ResponseModel {
    private Result result;
    private String PaymentIntentId;
    private String clientSecret;

    public Result getResult() {
        return result;
    }

    public OrderResponse setResult(Result result) {
        this.result = result;
        return this;
    }

    public String getPaymentIntentId() {
        return PaymentIntentId;
    }

    public OrderResponse setPaymentIntentId(String paymentIntentId) {
        PaymentIntentId = paymentIntentId;
        return this;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public OrderResponse setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }
}
