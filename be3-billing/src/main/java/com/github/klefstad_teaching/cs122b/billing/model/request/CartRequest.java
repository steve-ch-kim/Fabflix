package com.github.klefstad_teaching.cs122b.billing.model.request;

import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.BillingResults;

public class CartRequest {
    private Long movieId;
    private Integer quantity;

    public Long getMovieId() {
        return movieId;
    }

    public CartRequest setMovieId(Long movieId) {
        this.movieId = movieId;
        return this;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public CartRequest setQuantity(Integer quantity) {
        this.quantity = quantity;
        return this;
    }

    public void validate() {
        if (this.quantity <= 0) {
            throw new ResultError(BillingResults.INVALID_QUANTITY);
        } else if (this.quantity > 10) {
            throw new ResultError(BillingResults.MAX_QUANTITY);
        }
    }
}
