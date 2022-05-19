package com.github.klefstad_teaching.cs122b.billing.model.response;

import com.github.klefstad_teaching.cs122b.billing.model.data.Sale;
import com.github.klefstad_teaching.cs122b.core.base.ResponseModel;
import com.github.klefstad_teaching.cs122b.core.result.Result;

import java.util.List;

public class SaleResponse extends ResponseModel {
    private Result result;
    List<Sale> sales;

    public Result getResult() {
        return result;
    }

    public SaleResponse setResult(Result result) {
        this.result = result;
        return this;
    }

    public List<Sale> getSales() {
        return sales;
    }

    public SaleResponse setSales(List<Sale> sales) {
        this.sales = sales;
        return this;
    }
}
