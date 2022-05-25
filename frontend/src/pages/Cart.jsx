import React, {useEffect, useState} from "react";
import {useUser} from "../hook/User";
import {cartDelete, cartUpdate, orderPayment, retrieveCart} from "../backend/idm";
import {useNavigate} from "react-router-dom";
import App from "pages/App";

const Cart = () => {
    const {
        accessToken, setAccessToken,
        refreshToken, setRefreshToken
    } = useUser();

    // cart
    const [cart, setCart] = React.useState([]);
    const navigate = useNavigate();

    const viewCart = () => {
        retrieveCart({}, accessToken)
            .then(response => setCart(response.data))
            .catch(error => alert(JSON.stringify(error.response.data, null, 2)));
    }

    const backHome = () => {
        navigate("/");
        window.location.reload();
    }

    const updateCart = (event, movieId) => {
        const payload = event.target.value > 0 ? {
            movieId: movieId,
            quantity: event.target.value
        } : {
            movieId: movieId
        }

        event.target.value > 0
            ? cartUpdate(payload, accessToken)
                .catch(error => alert(JSON.stringify(error.response.data, null, 2)))
            : cartDelete(payload, accessToken)
                .catch(error => alert(JSON.stringify(error.response.data, null, 2)));

        window.location.reload();
    }

    useEffect(() => viewCart(), []);

    return (
        <div className="cart-container">
            <div className="cart-header">Shopping Cart</div>
            <div className="cart-details">
                <h4 className="movie-details">Movie</h4>
                <h4 className="quantity-details">Quantity</h4>
                <h4 className="price-details">Price</h4>
            </div>
            <hr className="cart-line"/>

            <div id="cart-items">
                {cart["items"]?.map(movie =>
                     <div>
                         <img src={"https://image.tmdb.org/t/p/w185" + movie.posterPath} alt="" className="img-item"/>
                         <select onChange={event => updateCart(event, movie.movieId)} className="quantity-item" value={movie.quantity}>
                            <option value={0}>0</option>
                             <option value={1}>1</option>
                            <option value={2}>2</option>
                            <option value={3}>3</option>
                            <option value={4}>4</option>
                             <option value={5}>5</option>
                            <option value={6}>6</option>
                            <option value={7}>7</option>
                            <option value={8}>8</option>
                             <option value={9}>9</option>
                            <option value={10}>10</option>
                        </select>
                         <h4 className="price-item">${Math.round((movie.unitPrice * movie.quantity) * 100)/100}</h4>
                     </div>
                )}
                <div id="checkout-items">
                    { cart["total"] && <h2 id="cart-total">Total: ${cart["total"]}</h2>}
                    <button onClick={() => window.location.href="/checkout"} id="cartButton">Checkout</button>
                    <button onClick={() => backHome()} id="pageButton">Home</button>
                </div>
            </div>
        </div>
    );
};

export default Cart;