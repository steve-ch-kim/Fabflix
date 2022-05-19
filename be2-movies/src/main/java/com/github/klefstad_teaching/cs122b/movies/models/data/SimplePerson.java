package com.github.klefstad_teaching.cs122b.movies.models.data;

public class SimplePerson {
    private Long id;
    private String name;

    public Long getId() {
        return id;
    }

    public SimplePerson setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public SimplePerson setName(String name) {
        this.name = name;
        return this;
    }
}
