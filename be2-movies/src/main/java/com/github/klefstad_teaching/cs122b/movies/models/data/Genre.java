package com.github.klefstad_teaching.cs122b.movies.models.data;

public class Genre {
    private Integer id;
    private String name;

    public Integer getId() {
        return id;
    }

    public Genre setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Genre setName(String name) {
        this.name = name;
        return this;
    }
}
