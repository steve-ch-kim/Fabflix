package com.github.klefstad_teaching.cs122b.movies.models.response;

import com.github.klefstad_teaching.cs122b.core.base.ResponseModel;
import com.github.klefstad_teaching.cs122b.core.result.Result;
import com.github.klefstad_teaching.cs122b.movies.models.data.Movie;

import java.util.List;

public class MovieSearchResponse extends ResponseModel {
    Result result;
    private List<Movie> movies;

    public Result getResult() {
        return result;
    }

    public MovieSearchResponse setResult(Result result) {
        this.result = result;
        return this;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public MovieSearchResponse setMovies(List<Movie> movies) {
        this.movies = movies;
        return this;
    }
}
