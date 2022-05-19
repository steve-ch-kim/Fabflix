package com.github.klefstad_teaching.cs122b.billing.rest;

import com.github.klefstad_teaching.cs122b.billing.model.data.Item;
import com.github.klefstad_teaching.cs122b.billing.model.data.Movie;
import com.github.klefstad_teaching.cs122b.billing.model.request.CartRequest;
import com.github.klefstad_teaching.cs122b.billing.model.response.DetailResponse;
import com.github.klefstad_teaching.cs122b.billing.model.response.ItemResponse;
import com.github.klefstad_teaching.cs122b.billing.repo.BillingRepo;
import com.github.klefstad_teaching.cs122b.billing.util.Validate;
import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.BillingResults;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.List;

@RestController
public class CartController
{
    private final BillingRepo repo;
    private final Validate    validate;

    @Autowired
    public CartController(BillingRepo repo, Validate validate)
    {
        this.repo = repo;
        this.validate = validate;
    }

    @PostMapping("/cart/insert")
    public ResponseEntity<ItemResponse> insertCart(@AuthenticationPrincipal SignedJWT user,
                                                   @RequestBody CartRequest request) {
        request.validate();

        // get user id
        Long user_id = repo.getUserId(user);

        // insert into cart
        String sql = "INSERT INTO billing.cart (user_id, movie_id, quantity) " +
                "VALUES (:user_id, :movie_id, :quantity) ";

        MapSqlParameterSource source =
                new MapSqlParameterSource()
                        .addValue("user_id", user_id, Types.LONGNVARCHAR)
                        .addValue("movie_id", request.getMovieId(), Types.LONGNVARCHAR)
                        .addValue("quantity", request.getQuantity(), Types.INTEGER);

        try {
            repo.getTemplate().update(sql, source);
        } catch (DuplicateKeyException e) {
            throw new ResultError(BillingResults.CART_ITEM_EXISTS);
        }

        ItemResponse response = new ItemResponse()
                .setResult(BillingResults.CART_ITEM_INSERTED);

        return ResponseEntity
                .status(response.getResult().status())
                .body(response);
    }

    @PostMapping("/cart/update")
    public ResponseEntity<ItemResponse> updateCart(@AuthenticationPrincipal SignedJWT user,
                                                   @RequestBody CartRequest request) {
        request.validate();

        // get user id
        Long user_id = repo.getUserId(user);

        // verify movie exists in cart
        repo.verifyMovieExistsInCart(request.getMovieId(), user_id);

        // update cart
        String sql = "UPDATE billing.cart " +
                "SET quantity = :quantity " +
                "WHERE movie_id = :movie_id AND user_id = :user_id ";

        MapSqlParameterSource source =
                new MapSqlParameterSource()
                        .addValue("quantity", request.getQuantity(), Types.INTEGER)
                        .addValue("movie_id", request.getMovieId(), Types.LONGNVARCHAR)
                        .addValue("user_id", user_id, Types.LONGNVARCHAR);

        repo.getTemplate().update(sql, source);

        ItemResponse response = new ItemResponse()
                .setResult(BillingResults.CART_ITEM_UPDATED);

        return ResponseEntity
                .status(response.getResult().status())
                .body(response);
    }

    @DeleteMapping("/cart/delete/{movieId}")
    public ResponseEntity<ItemResponse> deleteCart(@AuthenticationPrincipal SignedJWT user,
                                                   @PathVariable Long movieId) {
        // get user id
        Long user_id = repo.getUserId(user);

        // verify movie exists in cart
        repo.verifyMovieExistsInCart(movieId, user_id);

        // delete from cart
        String sql = "DELETE FROM billing.cart WHERE movie_id = :movieId AND user_id = :userId ";

        MapSqlParameterSource source =
                new MapSqlParameterSource()
                        .addValue("movieId", movieId, Types.LONGNVARCHAR)
                        .addValue("userId", user_id, Types.INTEGER);

        repo.getTemplate().update(sql, source);

        ItemResponse response = new ItemResponse()
                .setResult(BillingResults.CART_ITEM_DELETED);

        return ResponseEntity
                .status(response.getResult().status())
                .body(response);
    }

    @GetMapping("/cart/retrieve")
    public ResponseEntity<DetailResponse> retrieveCart(@AuthenticationPrincipal SignedJWT user) {
        // get user id and roles
        Long user_id = repo.getUserId(user);
        String role = repo.getRole(user);

        // get items and total
        List<Item> items = repo.getItems(user_id, role);
        BigDecimal total = repo.getTotal(items, role);

        DetailResponse response = new DetailResponse()
                .setResult(BillingResults.CART_RETRIEVED)
                .setTotal(total)
                .setItems(items);

        return ResponseEntity
                .status(response.getResult().status())
                .body(response);
    }

    @PostMapping("/cart/clear")
    public ResponseEntity<ItemResponse> clearCart(@AuthenticationPrincipal SignedJWT user) {
        // get user id
        Long user_id = repo.getUserId(user);

        // find movies in user's cart
        String movie_sql = "SELECT movie_id " +
                "FROM billing.cart " +
                "WHERE user_id = :user_id ";

        MapSqlParameterSource movie_source =
                new MapSqlParameterSource()
                        .addValue("user_id", user_id, Types.INTEGER);

        List<Movie> movies =
                repo.getTemplate().query(
                        movie_sql,
                        movie_source,
                        (rs, rowNum) ->
                                new Movie()
                                        .setId(rs.getLong("movie_id"))
                );

        // if cart is empty throw an error
        if (movies.size() == 0) {
            throw new ResultError(BillingResults.CART_EMPTY);
        }

        // clear cart
        repo.clearCart(user_id);

        ItemResponse response = new ItemResponse()
                .setResult(BillingResults.CART_CLEARED);

        return ResponseEntity
                .status(response.getResult().status())
                .body(response);
    }
}
