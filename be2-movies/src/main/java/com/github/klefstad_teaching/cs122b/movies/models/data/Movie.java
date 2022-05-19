package com.github.klefstad_teaching.cs122b.movies.models.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

public class Movie {
    private Long id;
    private String title;
    private Integer year;
    private String director;
    private Double rating;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long numVotes;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long budget;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long revenue;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String overview;
    private String backdropPath;
    private String posterPath;
    private Boolean hidden;

    public Long getId() {
        return id;
    }

    public Movie setId(Long id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Movie setTitle(String title) {
        this.title = title;
        return this;
    }

    public Integer getYear() {
        return year;
    }

    public Movie setYear(Integer year) {
        this.year = year;
        return this;
    }

    public String getDirector() {
        return director;
    }

    public Movie setDirector(String director) {
        this.director = director;
        return this;
    }

    public Double getRating() {
        return rating;
    }

    public Movie setRating(Double rating) {
        this.rating = rating;
        return this;
    }

    @JsonIgnore(false)
    public Long getNumVotes() {
        return numVotes;
    }

    @JsonIgnore(false)
    public Movie setNumVotes(Long numVotes) {
        this.numVotes = numVotes;
        return this;
    }

    public Long getBudget() {
        return budget;
    }

    public Movie setBudget(Long budget) {
        this.budget = budget;
        return this;
    }

    public Long getRevenue() {
        return revenue;
    }

    public Movie setRevenue(Long revenue) {
        this.revenue = revenue;
        return this;
    }

    public String getOverview() {
        return overview;
    }

    public Movie setOverview(String overview) {
        this.overview = overview;
        return this;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public Movie setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
        return this;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public Movie setPosterPath(String posterPath) {
        this.posterPath = posterPath;
        return this;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public Movie setHidden(Boolean hidden) {
        this.hidden = hidden;
        return this;
    }
}
