import React, {useEffect} from "react";
import {useNavigate, useParams} from 'react-router-dom';
import {insertCart, searchMovie} from "backend/idm";
import {useUser} from "../hook/User";

const GetMovie = () => {
    const {
        accessToken, setAccessToken,
        refreshToken, setRefreshToken
    } = useUser();

    const [quantity, setQuantity] = React.useState(1);
    const [movie, setMovie] = React.useState({});
    const navigate = useNavigate();
    const {movieId} = useParams();

    const getMovie = () => {
        searchMovie({}, accessToken, movieId)
            .then(response => setMovie(response.data))
            .catch(error => alert(JSON.stringify(error.response.data, null, 2)));
    }

    const addCart = (movieId, quantity) => {
        const payload = {
            movieId: movieId,
            quantity: quantity
        }

        insertCart(payload, accessToken)
            .catch(error => alert(JSON.stringify(error.response.data, null, 2)));

        navigate("/cart");
    }

    useEffect(() => getMovie(), []);

    return (
        <div>
            <div className="cart-details">
                { movie["movie"] && <img src={"https://image.tmdb.org/t/p/w500" + movie["movie"].posterPath} alt="" id="detail-img"/>}
                { movie["movie"] && <h4 className="movie-detail"><span>Title:</span> {movie["movie"].title}</h4>}
                { movie["movie"] && <h4 className="movie-detail"><span>Director:</span> {movie["movie"].director}</h4>}
                { movie["movie"] && <h4 className="movie-detail"><span>Rating:</span> {movie["movie"].rating}/10</h4>}
                { movie["movie"] && <h4 className="movie-detail"><span>Revenue:</span> ${movie["movie"].revenue}</h4>}
                { movie["movie"] && <h4 className="movie-detail"><span>Budget:</span> ${movie["movie"].budget}</h4>}
                { movie["movie"] && <h4 className="movie-detail"><span>Overview:</span> {movie["movie"].overview}</h4>}

                {movie["genres"]?.map(genre =>
                    <h4 className="movie-detail"><span>Genre:</span> {genre.name}</h4>
                )}

                <div id="cartButtons">
                    <button onClick={() => setQuantity(quantity - 1)} id="pageButton">Remove</button>
                    <button onClick={() => setQuantity(quantity + 1)} id="pageButton">{quantity}</button>
                    { movie["movie"] && <button onClick={() => addCart(movie["movie"].id, quantity)} id="cartButton">Add To Cart</button>}
                </div>
            </div>
        </div>
    );
}

export default GetMovie;