import React, {useEffect} from "react";
import styled from "styled-components";
import {orderComplete, searchMovies} from "backend/idm";
import {useForm} from "react-hook-form";
import {useUser} from "hook/User";
import {useSearchParams} from "react-router-dom";

const StyledDiv = styled.div` 
    padding: 15px;
    margin-bottom: 18px;
    text-align: left;
    color: rgba(255, 255, 255, 0.8);
    border: solid;
    border-radius: 4px;
    border-color: rgba(255, 255, 255, 0.555);
    white-space: normal;
`

const StyledInput = styled.input` 
      border: none;
      border-bottom: solid rgb(143, 143, 143) 1px;
    
      margin-bottom: 30px;
    
      background: none;
      color: rgba(255, 255, 255, 0.8);
    
      height: 35px;
      width: 100%;
      outline: none; 
`

const StyledButton = styled.button`
  cursor: pointer;

  border: none;
  border-radius: 8px;

  box-shadow: 2px 2px 7px #38d39f70;

  background: #38d39f;
  color: rgba(255, 255, 255, 0.8);

  width: 100%;

  padding: 20px;
`

const Home = () => {
    const {
        accessToken, setAccessToken,
        refreshToken, setRefreshToken
    } = useUser();

    const [movies, setMovies] = React.useState([]);
    const {register, getValues, handleSubmit} = useForm();
    const [page, setPage] = React.useState(1);
    const [searchParams, setSearchParams] = useSearchParams();

    const completeOrder = () => {
        const paymentIntentId = searchParams.get("payment_intent");

        if (paymentIntentId !== null) {
            orderComplete({
                paymentIntentId: paymentIntentId
            }, accessToken)
                .then(response => alert(JSON.stringify(response.data, null, 2)))
                .catch(error => alert(JSON.stringify(error.response.data, null, 2)));
        }
    }

    const getMovies = () => {
        const title = getValues("title").trim();
        const year = getValues("year").trim();
        const director = getValues("director").trim();
        const genre = getValues("genre");

        const queryParams = {
            title: title !== "" ? title : undefined,
            year: year !== "" ? year : undefined,
            director: director !== "" ? director : undefined,
            genre: genre !== "" ? genre : undefined,
            direction: getValues("direction"),
            orderBy: getValues("orderBy"),
            limit: getValues("limit"),
            page: page
        };

        searchMovies(queryParams, accessToken)
            .then(response => setMovies(response.data))
            .catch(error => alert(JSON.stringify(error.response.data, null, 2)));
    };

    useEffect(() => completeOrder(), []);

    return (
        <div>
            <StyledInput {...register("title")} placeholder="TITLE..." autoComplete="off"/>
            <StyledInput {...register("year")} placeholder="YEAR..." autoComplete="off"/>
            <StyledInput {...register("director")} placeholder="DIRECTOR..." autoComplete="off"/>
            <span id="firstSelects">GENRE:</span>
            <select id="selectsButtons" {...register("genre")}>
                <option value={""}>None</option>
                <option value={"Adventure"}>Adventure</option>
                <option value={"Fantasy"}>Fantasy</option>
                <option value={"Animation"}>Animation</option>
                <option value={"Drama"}>Drama</option>
                <option value={"Horror"}>Horror</option>
                <option value={"Action"}>Action</option>
                <option value={"Comedy"}>Comedy</option>
                <option value={"History"}>History</option>
                <option value={"Western"}>Western</option>
                <option value={"Thriller"}>Thriller</option>
                <option value={"Crime"}>Crime</option>
                <option value={"Documentary"}>Documentary</option>
                <option value={"Science Fiction"}>v</option>
                <option value={"Mystery"}>Mystery</option>
                <option value={"Music"}>Music</option>
                <option value={"Romance"}>Romance</option>
                <option value={"Family"}>Family</option>
                <option value={"War"}>War</option>
                <option value={"TV Movie"}>TV Movie</option>
            </select>
            <span id="selects">DIRECTION:</span>
            <select id="selectsButtons" {...register("direction")}>
                <option value={"asc"}>ASC</option>
                <option value={"desc"}>DESC</option>
            </select>
            <span id="selects">ORDER BY:</span>
            <select id="selectsButtons" {...register("orderBy")}>
                <option value={"title"}>Title</option>
                <option value={"rating"}>Rating</option>
                <option value={"year"}>Year</option>
            </select>
            <span id="selects">PER PAGE:</span>
            <select id="selectsButtons" {...register("limit")}>
                <option value={10}>10</option>
                <option value={25}>25</option>
                <option value={50}>50</option>
                <option value={50}>100</option>
            </select>
            <button onClick={() => setPage(page + 1)} id="pageButton">{page}</button>
            <button onClick={() => setPage(page - 1)} id="pageButton">Back</button>
            <div id="searchButton">
                <StyledButton onClick={handleSubmit(getMovies)}>Search</StyledButton>
            </div>

            <div id="movies-home">
                {movies["movies"]?.map(movie =>
                    <StyledDiv>
                        <button onClick={() => window.location.href="/movie/"+movie.id} id="pageButton">View More</button>
                       <h2>{movie.title}</h2>
                       <img src={"https://image.tmdb.org/t/p/w185" + movie.posterPath} alt="" id="img"/>
                   </StyledDiv>
                )}
            </div>
        </div>
    );
}

export default Home;
