import React, {useEffect, useState} from "react";
import {orderList} from "../backend/idm";
import {useUser} from "../hook/User";
import {useNavigate} from "react-router-dom";

const Orders = () => {
    const {
        accessToken, setAccessToken,
        refreshToken, setRefreshToken
    } = useUser();

    const [orders, setOrders] = useState([]);
    const navigate = useNavigate();

    const getOrders = (accessToken) => {
        orderList({}, accessToken)
            .then(response => setOrders(response.data));
    }

    const backHome = () => {
        navigate("/");
        window.location.reload();
    }

    useEffect(() => getOrders(accessToken), []);

    return (
        <div className="cart-container">
            <div className="cart-header">Orders</div>
            <div className="cart-details">
                <h4 className="movie-details">Date</h4>
                <h4 className="quantity-details">Amount</h4>
            </div>
            <hr className="cart-line"/>

            <div>
                {orders["sales"]?.map(order =>
                    <div>
                        <h4 className="date-detail">{order.orderDate}</h4>
                        <h4 className="amount-detail"><span>${order.total}</span></h4>
                    </div>
                )}
            </div>
            <div id="checkout-items">
                <button onClick={() => backHome()} id="pageButton">Home</button>
            </div>
        </div>
    );
};

export default Orders;