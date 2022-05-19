package com.github.klefstad_teaching.cs122b.billing.model.response;

import com.github.klefstad_teaching.cs122b.billing.model.data.Item;
import com.github.klefstad_teaching.cs122b.core.base.ResponseModel;
import com.github.klefstad_teaching.cs122b.core.result.Result;
import java.math.BigDecimal;
import java.util.List;

public class DetailResponse extends ResponseModel {
    private Result result;
    private BigDecimal total;
    List<Item> items;

    public Result getResult() {
        return result;
    }

    public DetailResponse setResult(Result result) {
        this.result = result;
        return this;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public DetailResponse setTotal(BigDecimal total) {
        this.total = total;
        return this;
    }

    public List<Item> getItems() {
        return items;
    }

    public DetailResponse setItems(List<Item> items) {
        this.items = items;
        return this;
    }
}
