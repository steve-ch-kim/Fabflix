package com.github.klefstad_teaching.cs122b.billing.rest;

import com.github.klefstad_teaching.cs122b.billing.model.data.Item;
import com.github.klefstad_teaching.cs122b.billing.model.data.Sale;
import com.github.klefstad_teaching.cs122b.billing.model.request.PaymentRequest;
import com.github.klefstad_teaching.cs122b.billing.model.response.DetailResponse;
import com.github.klefstad_teaching.cs122b.billing.model.response.ItemResponse;
import com.github.klefstad_teaching.cs122b.billing.model.response.OrderResponse;
import com.github.klefstad_teaching.cs122b.billing.model.response.SaleResponse;
import com.github.klefstad_teaching.cs122b.billing.repo.BillingRepo;
import com.github.klefstad_teaching.cs122b.billing.util.Validate;
import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.BillingResults;
import com.nimbusds.jwt.SignedJWT;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@RestController
public class OrderController
{
    private final BillingRepo repo;
    private final Validate    validate;

    @Autowired
    public OrderController(BillingRepo repo,Validate validate)
    {
        this.repo = repo;
        this.validate = validate;
    }

    @GetMapping("/order/payment")
    public ResponseEntity<OrderResponse> orderPayment(@AuthenticationPrincipal SignedJWT user) {
        // get user id and roles
        Long user_id = repo.getUserId(user);
        String role = repo.getRole(user);

        // get items and total
        List<Item> items = repo.getItems(user_id, role);
        BigDecimal total = repo.getTotal(items, role);

        // get list of movie titles
        List<String> movie_titles = new ArrayList<>();

        for (Item item : items) {
            movie_titles.add(item.getMovieTitle());
        }

        Long amountInTotalCents = total.multiply(BigDecimal.valueOf(100)).longValue();
        String description = movie_titles.toString();
        String userId = Long.toString(user_id);

        PaymentIntentCreateParams paymentIntentCreateParams =
            PaymentIntentCreateParams
                .builder()
                .setCurrency("USD") // This will always be the same for our project
                .setDescription(description)
                .setAmount(amountInTotalCents)
                // We use MetaData to keep track of the user that should pay for the order
                .putMetadata("userId", userId)
                .setAutomaticPaymentMethods(
                    // This will tell stripe to generate the payment methods automatically
                    // This will always be the same for our project
                    PaymentIntentCreateParams.AutomaticPaymentMethods
                        .builder()
                        .setEnabled(true)
                        .build()
                )
                .build();

        String paymentIntentId = "";
        String clientSecret = "";
        try {
            PaymentIntent paymentIntent = PaymentIntent.create(paymentIntentCreateParams);
            paymentIntentId = paymentIntent.getId();
            clientSecret = paymentIntent.getClientSecret();
        } catch (StripeException e) {
            throw new ResultError(BillingResults.STRIPE_ERROR);
        }

        OrderResponse response = new OrderResponse()
                .setResult(BillingResults.ORDER_PAYMENT_INTENT_CREATED)
                .setPaymentIntentId(paymentIntentId)
                .setClientSecret(clientSecret);

        return ResponseEntity
                .status(response.getResult().status())
                .body(response);
    }

    @PostMapping("/order/complete")
    public ResponseEntity<ItemResponse> orderComplete(@AuthenticationPrincipal SignedJWT user,
                                                      @RequestBody PaymentRequest request) {
        // get user id and roles
        Long user_id = repo.getUserId(user);
        String role = repo.getRole(user);

        // get payment intent
        PaymentIntent retrievedPaymentIntent;
        try {
            retrievedPaymentIntent = PaymentIntent.retrieve(request.getPaymentIntentId());
        } catch (StripeException e) {
            throw new ResultError(BillingResults.STRIPE_ERROR);
        }

        // check if payment status succeeded and if the user is correct
        if (!retrievedPaymentIntent.getStatus().equals("succeeded")) {
            throw new ResultError(BillingResults.ORDER_CANNOT_COMPLETE_NOT_SUCCEEDED);
        } else if (!retrievedPaymentIntent.getMetadata().get("userId").equals(user_id.toString())) {
            throw new ResultError(BillingResults.ORDER_CANNOT_COMPLETE_WRONG_USER);
        }

        // get items and total
        List<Item> items = repo.getItems(user_id, role);
        BigDecimal total = repo.getTotal(items, role);

        // create entry in billing sale table
        String billing_sql = "INSERT INTO billing.sale (user_id, total, order_date) " +
                "VALUES (:user_id, :total, :order_date) ";

        MapSqlParameterSource billing_source =
                new MapSqlParameterSource()
                        .addValue("user_id", user_id, Types.LONGNVARCHAR)
                        .addValue("total", total, Types.DECIMAL)
                        .addValue("order_date", new Timestamp(System.currentTimeMillis()), Types.TIMESTAMP);

        repo.getTemplate().update(billing_sql, billing_source);

        // get id of the latest order for the user id and create billing.sale_item entries
        String find_sql = "SELECT id " +
                "FROM billing.sale " +
                "WHERE user_id = :user_id " +
                "ORDER BY ID DESC LIMIT 1 ";

        MapSqlParameterSource find_source =
                new MapSqlParameterSource()
                        .addValue("user_id", user_id, Types.LONGNVARCHAR);

        List<Item> billing_items =
                repo.getTemplate().query(
                        find_sql,
                        find_source,
                        (rs, rowNum) ->
                                new Item()
                                        .setMovieId(rs.getLong("id"))
                );

        // get id
        Integer sale_id = Math.toIntExact(billing_items.get(0).getMovieId());

        // create billing.sale_item entries
        for (Item item : items) {
            String sale_sql = "INSERT INTO billing.sale_item (sale_id, movie_id, quantity) " +
                    "VALUES (:sale_id, :movie_id, :quantity) ";

            MapSqlParameterSource sale_source =
                new MapSqlParameterSource()
                        .addValue("sale_id", sale_id, Types.INTEGER)
                        .addValue("movie_id", item.getMovieId(), Types.INTEGER)
                        .addValue("quantity", item.getQuantity(), Types.INTEGER);

            repo.getTemplate().update(sale_sql, sale_source);
        }

        // clear cart
        repo.clearCart(user_id);

        ItemResponse response = new ItemResponse()
                .setResult(BillingResults.ORDER_COMPLETED);

        return ResponseEntity
                .status(response.getResult().status())
                .body(response);
    }

    @GetMapping("/order/list")
    public ResponseEntity<SaleResponse> orderList(@AuthenticationPrincipal SignedJWT user) {
        // get user id
        Long user_id = repo.getUserId(user);

        // get all sales from user
        String sql = "SELECT id, total, order_date " +
                "FROM billing.sale " +
                "WHERE user_id = :user_id " +
                "ORDER BY id DESC LIMIT 5 ";

        MapSqlParameterSource source =
                new MapSqlParameterSource()
                        .addValue("user_id", user_id, Types.INTEGER);

        List<Sale> sales =
                repo.getTemplate().query(
                        sql,
                        source,
                        (rs, rowNum) ->
                                new Sale()
                                        .setSaleId(rs.getLong("id"))
                                        .setTotal(rs.getBigDecimal("total"))
                                        .setOrderDate(rs.getTimestamp("order_date").toInstant())
                );

        // check if sales is empty
        if (sales.size() == 0) {
            throw new ResultError(BillingResults.ORDER_LIST_NO_SALES_FOUND);
        }

        SaleResponse response = new SaleResponse()
                .setResult(BillingResults.ORDER_LIST_FOUND_SALES)
                .setSales(sales);

        return ResponseEntity
                .status(response.getResult().status())
                .body(response);
    }

    @GetMapping("/order/detail/{saleId}")
    public ResponseEntity<DetailResponse> orderDetail(@AuthenticationPrincipal SignedJWT user,
                                                      @PathVariable Long saleId) {
        // get user id and role
        Long user_id = repo.getUserId(user);
        String role = repo.getRole(user);

        // get items
        List<Item> items = repo.getSaleItems(user_id, saleId, role);

        // get total
        BigDecimal total = repo.getTotal(items, role);

        DetailResponse response = new DetailResponse()
                .setResult(BillingResults.ORDER_DETAIL_FOUND)
                .setItems(items)
                .setTotal(total);

        return ResponseEntity
                .status(response.getResult().status())
                .body(response);
    }
}
