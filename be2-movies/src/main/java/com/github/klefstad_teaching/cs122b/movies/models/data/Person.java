package com.github.klefstad_teaching.cs122b.movies.models.data;

import java.util.Date;

public class Person {
    private Long id;
    private String name;
    private Date birthday;
    private String biography;
    private String birthplace;
    private float popularity;
    private String profilePath;

    public Long getId() {
        return id;
    }

    public Person setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Person setName(String name) {
        this.name = name;
        return this;
    }

    public Date getBirthday() {
        return birthday;
    }

    public Person setBirthday(Date birthday) {
        this.birthday = birthday;
        return this;
    }

    public String getBiography() {
        return biography;
    }

    public Person setBiography(String biography) {
        this.biography = biography;
        return this;
    }

    public String getBirthplace() {
        return birthplace;
    }

    public Person setBirthplace(String birthplace) {
        this.birthplace = birthplace;
        return this;
    }

    public float getPopularity() {
        return popularity;
    }

    public Person setPopularity(float popularity) {
        this.popularity = popularity;
        return this;
    }

    public String getProfilePath() {
        return profilePath;
    }

    public Person setProfilePath(String profilePath) {
        this.profilePath = profilePath;
        return this;
    }
}
