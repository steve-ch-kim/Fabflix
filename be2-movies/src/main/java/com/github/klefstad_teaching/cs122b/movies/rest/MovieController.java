package com.github.klefstad_teaching.cs122b.movies.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.MoviesResults;
import com.github.klefstad_teaching.cs122b.core.security.JWTManager;
import com.github.klefstad_teaching.cs122b.movies.models.data.*;
import com.github.klefstad_teaching.cs122b.movies.models.request.MovieSearchRequest;
import com.github.klefstad_teaching.cs122b.movies.models.request.PersonSearchRequest;
import com.github.klefstad_teaching.cs122b.movies.models.response.MovieSearchIdResponse;
import com.github.klefstad_teaching.cs122b.movies.models.response.MovieSearchResponse;
import com.github.klefstad_teaching.cs122b.movies.models.response.PersonSearchIdResponse;
import com.github.klefstad_teaching.cs122b.movies.models.response.PersonSearchResponse;
import com.github.klefstad_teaching.cs122b.movies.repo.MovieRepo;
import com.github.klefstad_teaching.cs122b.movies.util.Validate;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class MovieController
{
    private final MovieRepo repo;
    private final Validate validate;
    private final ObjectMapper objectMapper;

    private static final String ORDER_BY_MOVIE_TITLE = " ORDER BY TITLE ";
    private static final String ORDER_BY_MOVIE_RATING = " ORDER BY RATING ";
    private static final String ORDER_BY_MOVIE_YEAR = " ORDER BY YEAR ";
    private static final String MOVIE_ASC = " ASC, m.id ASC ";
    private static final String MOVIE_DESC = " DESC, m.id ASC ";
    private static final String ORDER_BY_PERSON_NAME = " ORDER BY NAME ";
    private static final String ORDER_BY_PERSON_POPULARITY = " ORDER BY POPULARITY ";
    private static final String ORDER_BY_PERSON_BIRTHDAY = " ORDER BY BIRTHDAY ";
    private static final String PERSON_ASC = " ASC, p.id";
    private static final String PERSON_DESC = " DESC, p.id";

    private static final String MOVIE_NO_GENRE =
            "SELECT m.id, m.title, m.year, m.director_id, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
            "FROM movies.movie m ";

    private static final String MOVIE_WITH_GENRE =
            "SELECT m.id, mg.genre_id, m.title, m.year, m.director_id, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
            "FROM movies.movie m " +
            "JOIN movies.movie_genre mg ON m.id = mg.movie_id ";

    private static final String MOVIE_PERSON_ID =
            " SELECT m.id, m.title, m.year, m.director_id, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
            " FROM movies.movie m " +
            " JOIN movies.movie_person mp ON m.id = mp.movie_id ";

    private static final String PERSON_WITH_TITLE =
            " SELECT p.id, p.name, p.birthday, p.biography, p.birthplace, p.popularity, p.profile_path " +
            " FROM movies.person p " +
            " JOIN movies.movie_person mp ON p.id = mp.person_id " +
            " JOIN movies.movie m ON m.id = mp.movie_id ";

    private static final String PERSON_NO_TITLE =
            " SELECT p.id, p.name, p.birthday, p.biography, p.birthplace, p.popularity, p.profile_path " +
            " FROM movies.person p ";

    private static final String PERSON_ID =
            " SELECT p.id, p.name, p.birthday, p.biography, p.birthplace, p.popularity, p.profile_path " +
            " FROM movies.person p ";

    private static final String MOVIE_SEARCH =
            "SELECT m.id, m.title, m.year, m.director_id, m.rating, m.num_votes, m.budget, m.revenue, m.overview, m.backdrop_path, m.poster_path, m.hidden, " +
                    "(SELECT JSON_ARRAYAGG(JSON_OBJECT('id', p.id, 'name', p.name)) " +
                    "FROM (SELECT DISTINCT p.id, p.name, p.popularity " +
                    "    FROM movies.person p " +
                    "        JOIN movies.movie_person mp ON mp.person_id = p.id " +
                    "        JOIN movies.movie m ON m.id = mp.movie_id " +
                    "    WHERE mp.movie_id = :movieId " +
                    "    ORDER BY p.popularity DESC, p.id) as p) AS persons, " +
                    "(SELECT JSON_ARRAYAGG(JSON_OBJECT('id', g.id, 'name', g.name)) " +
                    "FROM (SELECT DISTINCT g.id, g.name " +
                    "    FROM movies.genre g " +
                    "        JOIN movies.movie_genre mg ON mg.genre_id = g.id " +
                    "        JOIN movies.movie m on m.id = mg.movie_id " +
                    "    WHERE mg.movie_id = :movieId " +
                    "    ORDER BY g.name) as g) AS genres " +
                    "FROM movies.movie m " +
                    "WHERE m.id = :movieId ";

    @Autowired
    public MovieController(MovieRepo repo, Validate validate, ObjectMapper objectMapper)
    {
        this.repo = repo;
        this.validate = validate;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/movie/search")
    public ResponseEntity<MovieSearchResponse> endpoint(@AuthenticationPrincipal SignedJWT user,
                                                        MovieSearchRequest request)
    {
        // get role of user
        String role = "";
        try {
            if (user.getJWTClaimsSet().getStringArrayClaim(JWTManager.CLAIM_ROLES).length != 0) {
                role = user.getJWTClaimsSet().getStringArrayClaim(JWTManager.CLAIM_ROLES)[0];
            }
        } catch (ParseException | ArrayIndexOutOfBoundsException e) {
            e.getStackTrace();
        }

        // validate request parameters
        request.validate();

        // build sql, source, whereAdded
        StringBuilder         sql;
        MapSqlParameterSource source     = new MapSqlParameterSource();
        boolean               whereAdded = false;

        // genre
        if (request.getGenre() != null) {
            sql = new StringBuilder(MOVIE_WITH_GENRE);
            sql.append(" WHERE ");
            whereAdded = true;

            String genre_id = repo.findGenreId(request.getGenre()).toString();
            sql.append(" mg.genre_id = :genre_id ");
            source.addValue("genre_id", genre_id, Types.INTEGER);
        } else {
            sql = new StringBuilder(MOVIE_NO_GENRE);
        }

        // title
        if (request.getTitle() != null) {
            System.out.println(request.getTitle());
            if (whereAdded) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
                whereAdded = true;
            }

            String wildcardSearch = '%' + request.getTitle() + '%';
            sql.append(" m.title LIKE :movieTitle ");
            source.addValue("movieTitle", wildcardSearch, Types.VARCHAR);
        }

        // year
        if (request.getYear() != null) {
            if (whereAdded) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
                whereAdded = true;
            }

            sql.append(" m.year = :year ");
            source.addValue("year", request.getYear(), Types.INTEGER);
        }

        // director
        if (request.getDirector() != null) {
            if (whereAdded) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
                whereAdded = true;
            }

            String director_name = repo.findDirectorId(request.getDirector()).toString();
            sql.append(" m.director_id = :director_id ");
            source.addValue("director_id", director_name, Types.VARCHAR);
        }

        // hidden
        if (!role.equals("ADMIN") && !role.equals("EMPLOYEE")) {
            if (whereAdded) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
            }

            sql.append(" m.hidden != true ");
        }

        // order by
        switch (request.getOrderBy()) {
            case "title":
                sql.append(ORDER_BY_MOVIE_TITLE);
                break;
            case "rating":
                sql.append(ORDER_BY_MOVIE_RATING);
                break;
            case "year":
                sql.append(ORDER_BY_MOVIE_YEAR);
                break;
        }

        // direction
        switch (request.getDirection()) {
            case "desc":
                sql.append(MOVIE_DESC);
                break;
            case "asc":
                sql.append(MOVIE_ASC);
                break;
        }

        // limit/offset
        int offset = (request.getPage() - 1) * request.getLimit();
        sql.append(" LIMIT :limit OFFSET :offset ");
        source.addValue("limit", request.getLimit(), Types.INTEGER);
        source.addValue("offset", offset, Types.INTEGER);

        // get full list of movies
        List<Movie> movies = repo.getTemplate().query(
                sql.toString(),
                source,
                (rs, rowNum) ->
                        new Movie()
                                .setId(rs.getLong("id"))
                                .setTitle(rs.getString("title"))
                                .setYear(rs.getInt("year"))
                                .setDirector(repo.findDirectorName(rs.getInt("director_id")))
                                .setRating(rs.getDouble("rating"))
                                .setBackdropPath(rs.getString("backdrop_path"))
                                .setPosterPath(rs.getString("poster_path"))
                                .setHidden(rs.getBoolean("hidden"))
        );

        // error checking
        if (movies.size() == 0) {
            throw new ResultError(MoviesResults.NO_MOVIES_FOUND_WITHIN_SEARCH);
        }

        MovieSearchResponse response = new MovieSearchResponse()
                .setResult(MoviesResults.MOVIES_FOUND_WITHIN_SEARCH)
                .setMovies(movies);

        return ResponseEntity
                .status(response.getResult().status())
                .body(response);
    }

    @GetMapping("/movie/search/person/{personId}")
    public ResponseEntity<MovieSearchResponse> moviePerson(@AuthenticationPrincipal SignedJWT user,
                                                            @PathVariable Long personId,
                                                            MovieSearchRequest request) {
        // get role of user
        String role = "";
        try {
            role = user.getJWTClaimsSet().getStringArrayClaim(JWTManager.CLAIM_ROLES)[0];
        } catch (java.text.ParseException e) {
            e.getStackTrace();
        }

        // validate request parameters
        request.validate();

        // build sql, source
        StringBuilder         sql;
        MapSqlParameterSource source     = new MapSqlParameterSource();

        sql = new StringBuilder(MOVIE_PERSON_ID);
        sql.append(" WHERE mp.person_id = :person_id ");
        source.addValue("person_id", personId, Types.LONGVARCHAR);

        // hidden
        if (!role.equals("ADMIN") && !role.equals("EMPLOYEE")) {
            sql.append(" AND m.hidden != true ");
        }

        // order by
        switch (request.getOrderBy()) {
            case "title":
                sql.append(ORDER_BY_MOVIE_TITLE);
                break;
            case "rating":
                sql.append(ORDER_BY_MOVIE_RATING);
                break;
            case "year":
                sql.append(ORDER_BY_MOVIE_YEAR);
                break;
        }

        // direction
        switch (request.getDirection()) {
            case "desc":
                sql.append(MOVIE_DESC);
                break;
            case "asc":
                sql.append(MOVIE_ASC);
                break;
        }

        // limit/offset
        int offset = (request.getPage() - 1) * request.getLimit();
        sql.append(" LIMIT :limit OFFSET :offset ");
        source.addValue("limit", request.getLimit(), Types.INTEGER);
        source.addValue("offset", offset, Types.INTEGER);

        // get full list of movies
        List<Movie> movies = repo.getTemplate().query(
                sql.toString(),
                source,
                (rs, rowNum) ->
                        new Movie()
                                .setId(rs.getLong("id"))
                                .setTitle(rs.getString("title"))
                                .setYear(rs.getInt("year"))
                                .setDirector(repo.findDirectorName(rs.getInt("director_id")))
                                .setRating(rs.getDouble("rating"))
                                .setBackdropPath(rs.getString("backdrop_path"))
                                .setPosterPath(rs.getString("poster_path"))
                                .setHidden(rs.getBoolean("hidden"))
        );

        // error checking
        if (movies.size() == 0) {
            throw new ResultError(MoviesResults.NO_MOVIES_WITH_PERSON_ID_FOUND);
        }

        MovieSearchResponse response = new MovieSearchResponse()
                .setResult(MoviesResults.MOVIES_WITH_PERSON_ID_FOUND)
                .setMovies(movies);

        return ResponseEntity
                .status(response.getResult().status())
                .body(response);
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<MovieSearchIdResponse> movieMovieId(@AuthenticationPrincipal SignedJWT user,
                                                              @PathVariable Long movieId) {
        // get role of user
        String role = "";
        try {
            if (user.getJWTClaimsSet().getStringArrayClaim(JWTManager.CLAIM_ROLES).length != 0) {
                role = user.getJWTClaimsSet().getStringArrayClaim(JWTManager.CLAIM_ROLES)[0];
            }
        } catch (java.text.ParseException e) {
            e.getStackTrace();
        }

        // build initial sql string
        String sql = MOVIE_SEARCH;

        // hidden
        if (!role.equals("ADMIN") && !role.equals("EMPLOYEE")) {
            sql += " AND m.hidden != true ";
        }

        // error checking
        if (!repo.isMovieValidId(movieId, role)) {
            throw new ResultError(MoviesResults.NO_MOVIE_WITH_ID_FOUND);
        }

        // build initial response
        MovieSearchIdResponse response =
                repo.getTemplate().queryForObject(
                        sql,
                        new MapSqlParameterSource().addValue("movieId", movieId, Types.LONGVARCHAR),
                        this::methodInsteadOfLambda
                );

        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    private MovieSearchIdResponse methodInsteadOfLambda(ResultSet rs, Integer rowNum) throws SQLException {
        // build initial lists
        List<Genre> genres;
        List<SimplePerson> persons;

        try {
            // get json array strings
            String jsonArrayStringPersons = rs.getString("persons");
            String jsonArrayStringGenres = rs.getString("genres");

            // build object arrays
            SimplePerson[] personClassArray =
                    objectMapper.readValue(jsonArrayStringPersons, SimplePerson[].class);
            Genre[] genreClassArray =
                    objectMapper.readValue(jsonArrayStringGenres, Genre[].class);

            // This just helps convert from an Object Array to a List<>
            persons = Arrays.stream(personClassArray).collect(Collectors.toList());
            genres = Arrays.stream(genreClassArray).collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to map classes");
        }

        Movie movie =
                new Movie()
                        .setId(rs.getLong("id"))
                        .setTitle(rs.getString("title"))
                        .setYear(rs.getInt("year"))
                        .setDirector(repo.findDirectorName(rs.getInt("director_id")))
                        .setRating(rs.getDouble("rating"))
                        .setNumVotes(rs.getLong("num_votes"))
                        .setBudget(rs.getLong("budget"))
                        .setRevenue(rs.getLong("revenue"))
                        .setOverview(rs.getString("overview"))
                        .setBackdropPath(rs.getString("backdrop_path"))
                        .setPosterPath(rs.getString("poster_path"))
                        .setHidden(rs.getBoolean("hidden"));

        return new MovieSearchIdResponse()
                .setResult(MoviesResults.MOVIE_WITH_ID_FOUND)
                .setMovie(movie)
                .setPersons(persons)
                .setGenres(genres);
    }

    @GetMapping("/person/search")
    public ResponseEntity<PersonSearchResponse> moviePerson(PersonSearchRequest request) {
        // validate request parameters
        request.validate();

        // build sql, source, whereAdded
        StringBuilder         sql;
        MapSqlParameterSource source     = new MapSqlParameterSource();
        boolean               whereAdded = false;

        // movie title
        if (request.getMovieTitle() != null) {
            sql = new StringBuilder(PERSON_WITH_TITLE);
            sql.append(" WHERE ");
            whereAdded = true;

            sql.append(" m.title LIKE :movie_title ");
            String wildcardSearch = '%' + request.getMovieTitle() + '%';
            source.addValue("movie_title", wildcardSearch, Types.VARCHAR);
        } else {
            sql = new StringBuilder(PERSON_NO_TITLE);
        }

        // name
        if (request.getName() != null) {
            if (whereAdded) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
                whereAdded = true;
            }

            sql.append(" p.name LIKE :p_name ");
            String wildcardSearch = '%' + request.getName() + '%';
            source.addValue("p_name", wildcardSearch, Types.VARCHAR);
        }

        // birthday
        if (request.getBirthday() != null) {
            if (whereAdded) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
            }

            sql.append(" p.birthday = :p_birthday ");
            source.addValue("p_birthday", request.getBirthday(), Types.VARCHAR);
        }

        // order by
        switch (request.getOrderBy()) {
            case "name":
                sql.append(ORDER_BY_PERSON_NAME);
                break;
            case "popularity":
                sql.append(ORDER_BY_PERSON_POPULARITY);
                break;
            case "birthday":
                sql.append(ORDER_BY_PERSON_BIRTHDAY);
                break;
        }

        // direction
        switch (request.getDirection()) {
            case "desc":
                sql.append(PERSON_DESC);
                break;
            case "asc":
                sql.append(PERSON_ASC);
                break;
        }

        // limit/offset
        int offset = (request.getPage() - 1) * request.getLimit();
        sql.append(" LIMIT :limit OFFSET :offset ");
        source.addValue("limit", request.getLimit(), Types.INTEGER);
        source.addValue("offset", offset, Types.INTEGER);

        List<Person> persons = repo.getTemplate().query(
                sql.toString(),
                source,
                (rs, rowNum) ->
                        new Person()
                                .setId(rs.getLong("id"))
                                .setName(rs.getString("name"))
                                .setBirthday(rs.getDate("birthday"))
                                .setBiography(rs.getString("biography"))
                                .setBirthplace(rs.getString("birthplace"))
                                .setPopularity(rs.getFloat("popularity"))
                                .setProfilePath(rs.getString("profile_path"))
        );

        // error checking
        if (persons.size() == 0) {
            throw new ResultError(MoviesResults.NO_PERSONS_FOUND_WITHIN_SEARCH);
        }

        PersonSearchResponse response = new PersonSearchResponse()
                .setResult(MoviesResults.PERSONS_FOUND_WITHIN_SEARCH)
                .setPersons(persons);

        return ResponseEntity
                .status(response.getResult().status())
                .body(response);
    }

    @GetMapping("/person/{personId}")
    public ResponseEntity<PersonSearchIdResponse> moviePersonId(@PathVariable Long personId) {
        // build sql, source
        StringBuilder         sql;
        MapSqlParameterSource source     = new MapSqlParameterSource();

        sql = new StringBuilder(PERSON_ID);
        sql.append(" WHERE p.id = :person_id ");
        source.addValue("person_id", personId, Types.LONGVARCHAR);

        List<Person> person = repo.getTemplate().query(
                sql.toString(),
                source,
                (rs, rowNum) ->
                        new Person()
                                .setId(rs.getLong("id"))
                                .setName(rs.getString("name"))
                                .setBirthday(rs.getDate("birthday"))
                                .setBiography(rs.getString("biography"))
                                .setBirthplace(rs.getString("birthplace"))
                                .setPopularity(rs.getFloat("popularity"))
                                .setProfilePath(rs.getString("profile_path"))
        );

        // error checking
        if (person.size() == 0) {
            throw new ResultError(MoviesResults.NO_PERSON_WITH_ID_FOUND);
        }

        PersonSearchIdResponse response = new PersonSearchIdResponse()
                .setResult(MoviesResults.PERSON_WITH_ID_FOUND)
                .setPerson(person.get(0));

        return ResponseEntity
                .status(response.getResult().status())
                .body(response);
    }
}
