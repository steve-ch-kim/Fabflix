package com.github.klefstad_teaching.cs122b.movies.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.klefstad_teaching.cs122b.movies.models.data.Movie;
import com.github.klefstad_teaching.cs122b.movies.models.data.Person;
import com.github.klefstad_teaching.cs122b.movies.models.data.Genre;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.List;

@Component
public class MovieRepo
{
    private final NamedParameterJdbcTemplate template;
    private final ObjectMapper objectMapper;

    @Autowired
    public MovieRepo(ObjectMapper objectMapper, NamedParameterJdbcTemplate template)
    {
        this.template = template;
        this.objectMapper = objectMapper;
    }

    public NamedParameterJdbcTemplate getTemplate() {
        return template;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public String findDirectorName(Integer director_id) {
        String sql = "SELECT p.name " +
                "FROM movies.movie m " +
                "JOIN movies.person p ON p.id = m.director_id " +
                "WHERE p.id = :director_id ";

        MapSqlParameterSource source =
                new MapSqlParameterSource()
                        .addValue("director_id", director_id, Types.INTEGER);

        List<Movie> movies =
                this.template.query(
                        sql,
                        source,
                        (rs, rowNum) ->
                                new Movie()
                                        .setDirector(rs.getString("p.name"))
                );

        return movies.get(0).getDirector();
    }

    public Long findDirectorId(String director_name) {
        String sql = "SELECT p.id " +
                "FROM movies.movie m " +
                "JOIN movies.person p ON p.id = m.director_id " +
                "WHERE p.name LIKE :director_name ";

        String wildcardSearch = '%' + director_name + '%';

        MapSqlParameterSource source =
                new MapSqlParameterSource()
                        .addValue("director_name", wildcardSearch, Types.VARCHAR);

        List<Person> persons =
                this.template.query(
                        sql,
                        source,
                        (rs, rowNum) ->
                                new Person()
                                        .setId(rs.getLong("p.id"))
                );

        return persons.get(0).getId();
    }

    public Integer findGenreId(String genre_name) {
        String sql = "SELECT g.id " +
                "FROM movies.genre g " +
                "WHERE g.name LIKE :genre_name ";

        String wildcardSearch = '%' + genre_name + '%';

        MapSqlParameterSource source =
                new MapSqlParameterSource()
                        .addValue("genre_name", wildcardSearch, Types.VARCHAR);

        List<Genre> genres =
                this.template.query(
                        sql,
                        source,
                        (rs, rowNum) ->
                                new Genre()
                                        .setId(rs.getInt("g.id"))
                );

        return genres.get(0).getId();
    }

    public Boolean isMovieValidId(Long movieId, String role) {
        String sql = "SELECT m.id " +
                "FROM movies.movie m " +
                "WHERE m.id = :movieId ";

        // hidden
        if (!role.equals("ADMIN") && !role.equals("EMPLOYEE")) {
            sql += " AND m.hidden != true ";
        }

        MapSqlParameterSource source =
                new MapSqlParameterSource()
                        .addValue("movieId", movieId, Types.LONGVARCHAR);

        List<Movie> movies =
                this.template.query(
                        sql,
                        source,
                        (rs, rowNum) ->
                                new Movie()
                                        .setId(rs.getLong("m.id"))
                );

        return movies.size() == 1;
    }
}
