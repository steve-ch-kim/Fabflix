package com.github.klefstad_teaching.cs122b.billing.model.request;

public class PaymentRequest {
    private String paymentIntentId;

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public PaymentRequest setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
        return this;
    }
}
