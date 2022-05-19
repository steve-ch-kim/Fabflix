package com.github.klefstad_teaching.cs122b.movies.models.response;

import com.github.klefstad_teaching.cs122b.core.base.ResponseModel;
import com.github.klefstad_teaching.cs122b.core.result.Result;
import com.github.klefstad_teaching.cs122b.movies.models.data.Genre;
import com.github.klefstad_teaching.cs122b.movies.models.data.Movie;
import com.github.klefstad_teaching.cs122b.movies.models.data.Person;
import com.github.klefstad_teaching.cs122b.movies.models.data.SimplePerson;

import java.util.List;

public class MovieSearchIdResponse extends ResponseModel {
    Result result;
    private Movie movie;
    private List<Genre> genres;
    private List<SimplePerson> persons;

    public Result getResult() {
        return result;
    }

    public MovieSearchIdResponse setResult(Result result) {
        this.result = result;
        return this;
    }

    public Movie getMovie() {
        return movie;
    }

    public MovieSearchIdResponse setMovie(Movie movie) {
        this.movie = movie;
        return this;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public MovieSearchIdResponse setGenres(List<Genre> genres) {
        this.genres = genres;
        return this;
    }

    public List<SimplePerson> getPersons() {
        return persons;
    }

    public MovieSearchIdResponse setPersons(List<SimplePerson> persons) {
        this.persons = persons;
        return this;
    }
}
