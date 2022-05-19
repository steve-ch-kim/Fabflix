package com.github.klefstad_teaching.cs122b.billing.repo;

import com.github.klefstad_teaching.cs122b.billing.model.data.Item;
import com.github.klefstad_teaching.cs122b.billing.model.data.Movie;
import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.BillingResults;
import com.github.klefstad_teaching.cs122b.core.security.JWTManager;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Types;
import java.text.ParseException;
import java.util.List;

@Component
public class BillingRepo
{
    private final NamedParameterJdbcTemplate template;

    private static final String RETRIEVE_CART =
            "SELECT c.movie_id, quantity, unit_price, premium_discount, title, backdrop_path, poster_path " +
            "FROM billing.cart c " +
            "JOIN billing.movie_price mp ON mp.movie_id = c.movie_id " +
            "JOIN movies.movie m ON m.id = c.movie_id ";

    private static final String RETRIEVE_SALE =
            "SELECT m.id, quantity, unit_price, premium_discount, title, backdrop_path, poster_path " +
            "FROM billing.sale s " +
            "JOIN billing.sale_item si ON s.id = si.sale_id " +
            "JOIN billing.movie_price mp ON mp.movie_id = si.movie_id " +
            "JOIN movies.movie m ON m.id = mp.movie_id ";

    @Autowired
    public BillingRepo(NamedParameterJdbcTemplate template)
    {
        this.template = template;
    }

    public NamedParameterJdbcTemplate getTemplate() {
        return this.template;
    }

    public BigDecimal getDiscountedPrice(BigDecimal unit_price, Integer discount) {
        BigDecimal discountedUnitPrice = unit_price.multiply((BigDecimal.valueOf(1)
                .subtract(BigDecimal.valueOf(discount/100.0))));

        discountedUnitPrice = discountedUnitPrice.setScale(2, RoundingMode.DOWN);
        return discountedUnitPrice;
    }

    public Long getUserId(@AuthenticationPrincipal SignedJWT user) {
        Long user_id = 0L;
        try {
            user_id = user.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);
        } catch (ParseException e) {
            e.getStackTrace();
        }

        return user_id;
    }

    public String getRole(@AuthenticationPrincipal SignedJWT user) {
        String role = "";
        try {
            if (user.getJWTClaimsSet().getStringArrayClaim(JWTManager.CLAIM_ROLES).length != 0) {
                role = user.getJWTClaimsSet().getStringArrayClaim(JWTManager.CLAIM_ROLES)[0];
            }
        } catch (ParseException | ArrayIndexOutOfBoundsException e) {
            e.getStackTrace();
        }

        return role;
    }

    public List<Item> getItems(Long user_id, String role) {
        // build sql, source
        StringBuilder sql = new StringBuilder(RETRIEVE_CART);
        MapSqlParameterSource source = new MapSqlParameterSource();
        sql.append(" WHERE c.user_id = :user_id ");
        source.addValue("user_id", user_id, Types.LONGNVARCHAR);

        List<Item> items;
        if (role.equals("PREMIUM")) {
            items =
                this.template.query(
                        sql.toString(),
                        source,
                        (rs, rowNum) ->
                                new Item()
                                        .setUnitPrice(this.getDiscountedPrice(rs.getBigDecimal("unit_price"),
                                                rs.getInt("premium_discount")))
                                        .setQuantity(rs.getInt("quantity"))
                                        .setMovieId(rs.getLong("movie_id"))
                                        .setMovieTitle(rs.getString("title"))
                                        .setBackdropPath(rs.getString("backdrop_path"))
                                        .setPosterPath(rs.getString("poster_path"))
                                        .setDiscount(rs.getInt("premium_discount"))
                );
        } else {
            items =
                this.template.query(
                        sql.toString(),
                        source,
                        (rs, rowNum) ->
                                new Item()
                                        .setUnitPrice(rs.getBigDecimal("unit_price"))
                                        .setQuantity(rs.getInt("quantity"))
                                        .setMovieId(rs.getLong("movie_id"))
                                        .setMovieTitle(rs.getString("title"))
                                        .setBackdropPath(rs.getString("backdrop_path"))
                                        .setPosterPath(rs.getString("poster_path"))
                                        .setDiscount(rs.getInt("premium_discount"))
                );
        }

        // if items is empty throw error
        if (items.size() == 0) {
            throw new ResultError(BillingResults.CART_EMPTY);
        }

        return items;
    }

    public List<Item> getSaleItems(Long user_id, Long sale_id, String role) {
        // build sql, source
        StringBuilder sql = new StringBuilder(RETRIEVE_SALE);
        MapSqlParameterSource source = new MapSqlParameterSource();
        sql.append(" WHERE si.sale_id = :sale_id ");
        sql.append(" AND s.user_id = :user_id ");
        source.addValue("sale_id", sale_id, Types.LONGNVARCHAR);
        source.addValue("user_id", user_id, Types.LONGNVARCHAR);

        List<Item> items;
        if (role.equals("PREMIUM")) {
            items =
                this.template.query(
                        sql.toString(),
                        source,
                        (rs, rowNum) ->
                                new Item()
                                        .setUnitPrice(this.getDiscountedPrice(rs.getBigDecimal("unit_price"),
                                                rs.getInt("premium_discount")))
                                        .setQuantity(rs.getInt("quantity"))
                                        .setMovieId(rs.getLong("id"))
                                        .setMovieTitle(rs.getString("title"))
                                        .setBackdropPath(rs.getString("backdrop_path"))
                                        .setPosterPath(rs.getString("poster_path"))
                                        .setDiscount(rs.getInt("premium_discount"))
                );
        } else {
            items =
                this.template.query(
                        sql.toString(),
                        source,
                        (rs, rowNum) ->
                                new Item()
                                        .setUnitPrice(rs.getBigDecimal("unit_price"))
                                        .setQuantity(rs.getInt("quantity"))
                                        .setMovieId(rs.getLong("id"))
                                        .setMovieTitle(rs.getString("title"))
                                        .setBackdropPath(rs.getString("backdrop_path"))
                                        .setPosterPath(rs.getString("poster_path"))
                                        .setDiscount(rs.getInt("premium_discount"))
                );
        }

        // if items is empty throw error
        if (items.size() == 0) {
            throw new ResultError(BillingResults.ORDER_DETAIL_NOT_FOUND);
        }

        return items;
    }

    public BigDecimal getTotal(List<Item> items, String role) {
        // calculate the total price
        BigDecimal total = BigDecimal.valueOf(0).setScale(2, RoundingMode.DOWN);
        for (Item item : items) {
            if (!role.equals("PREMIUM")) {
                BigDecimal price = item.getUnitPrice().setScale(2, RoundingMode.DOWN);
                total = total.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
            } else {
                total = total.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()).setScale(2, RoundingMode.DOWN)));
            }
        }

        return total;
    }

    public void clearCart(Long user_id) {
         // delete all items in user's cart
        String sql = "DELETE FROM billing.cart WHERE user_id = :user_id ";

        MapSqlParameterSource source =
                new MapSqlParameterSource()
                        .addValue("user_id", user_id, Types.INTEGER);

        this.template.update(sql, source);
    }

    public void verifyMovieExistsInCart(Long movie_id, Long user_id) {
        // check to see if item exists in cart
        String sql = "SELECT movie_id " +
                "FROM billing.cart " +
                "WHERE movie_id = :movieId AND user_id = :userId ";

        MapSqlParameterSource source =
                new MapSqlParameterSource()
                        .addValue("movieId", movie_id, Types.LONGNVARCHAR)
                        .addValue("userId", user_id, Types.INTEGER);

        List<Movie> movies =
                this.template.query(
                        sql,
                        source,
                        (rs, rowNum) ->
                                new Movie()
                                        .setId(rs.getLong("movie_id"))
                );

        // if item doesn't exist throw an error
        if (movies.size() == 0) {
            throw new ResultError(BillingResults.CART_ITEM_DOES_NOT_EXIST);
        }
    }
}
