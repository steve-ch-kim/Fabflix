package com.github.klefstad_teaching.cs122b.movies.models.request;

import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.MoviesResults;

public class PersonSearchRequest {
    private Long id;
    private String name;
    private String birthday;
    private String movieTitle;
    private Integer limit = 10;
    private Integer page = 1;
    private String orderBy = "name";
    private String direction = "asc";

    public Long getId() {
        return id;
    }

    public PersonSearchRequest setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public PersonSearchRequest setName(String name) {
        this.name = name;
        return this;
    }

    public String getBirthday() {
        return birthday;
    }

    public PersonSearchRequest setBirthday(String birthday) {
        this.birthday = birthday;
        return this;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public PersonSearchRequest setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public PersonSearchRequest setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public Integer getPage() {
        return page;
    }

    public PersonSearchRequest setPage(Integer page) {
        this.page = page;
        return this;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public PersonSearchRequest setOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public String getDirection() {
        return direction;
    }

    public PersonSearchRequest setDirection(String direction) {
        this.direction = direction;
        return this;
    }

    public void validate() {
        if (this.limit != 10 && this.limit != 25 && this.limit != 50 && this.limit != 100) {
            throw new ResultError(MoviesResults.INVALID_LIMIT);
        }

        if (this.page <= 0) {
            throw new ResultError(MoviesResults.INVALID_PAGE);
        }

        if (!this.orderBy.equals("name") && !this.orderBy.equals("popularity") && !this.orderBy.equals("birthday")) {
            throw new ResultError(MoviesResults.INVALID_ORDER_BY);
        }

        if (!this.direction.equals("asc") && !this.direction.equals("desc")) {
            throw new ResultError(MoviesResults.INVALID_DIRECTION);
        }
    }
}
