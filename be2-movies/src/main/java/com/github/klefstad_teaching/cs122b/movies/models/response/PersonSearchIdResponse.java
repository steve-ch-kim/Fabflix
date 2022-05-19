package com.github.klefstad_teaching.cs122b.movies.models.response;

import com.github.klefstad_teaching.cs122b.core.base.ResponseModel;
import com.github.klefstad_teaching.cs122b.core.result.Result;
import com.github.klefstad_teaching.cs122b.movies.models.data.Person;

public class PersonSearchIdResponse extends ResponseModel {
    Result result;
    private Person person;

    public Result getResult() {
        return result;
    }

    public PersonSearchIdResponse setResult(Result result) {
        this.result = result;
        return this;
    }

    public Person getPerson() {
        return person;
    }

    public PersonSearchIdResponse setPerson(Person person) {
        this.person = person;
        return this;
    }
}
