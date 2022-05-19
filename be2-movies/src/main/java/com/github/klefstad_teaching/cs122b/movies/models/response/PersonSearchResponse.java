package com.github.klefstad_teaching.cs122b.movies.models.response;

import com.github.klefstad_teaching.cs122b.core.base.ResponseModel;
import com.github.klefstad_teaching.cs122b.core.result.Result;
import com.github.klefstad_teaching.cs122b.movies.models.data.Person;
import java.util.List;

public class PersonSearchResponse extends ResponseModel {
    Result result;
    private List<Person> persons;

    public Result getResult() {
        return result;
    }

    public PersonSearchResponse setResult(Result result) {
        this.result = result;
        return this;
    }

    public List<Person> getPersons() {
        return persons;
    }

    public PersonSearchResponse setPersons(List<Person> persons) {
        this.persons = persons;
        return this;
    }
}
