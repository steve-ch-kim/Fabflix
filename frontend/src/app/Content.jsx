import React from "react";
import {Route, Routes} from "react-router-dom";

import Login from "pages/Login";
import Home from "pages/Home";
import Register from "pages/Register";
import Logout from "pages/Logout";
import Orders from "pages/Orders";
import Cart from "pages/Cart";
import GetMovie from "pages/MovieDetail";
import App from "pages/App";
import styled from "styled-components";

const StyledDiv = styled.div`
  display: flex;
  justify-content: center;

  width: 100vw;
  height: 100vh;
  padding: 25px;

  background-color: #121212;
  box-shadow: inset 0 3px 5px -3px #000000;
`

/**
 * This is the Component that will switch out what Component is being shown
 * depending on the "url" of the page
 * <br>
 * You'll notice that we have a <Routes> Component and inside it, we have
 * multiple <Route> components. Each <Route> maps a specific "url" to show a
 * specific Component.
 * <br>
 * Whenever you add a Route here make sure to add a corresponding NavLink in
 * the NavBar Component.
 * <br>
 * You can essentially think of this as a switch statement:
 * @example
 * switch (url) {
 *     case "/login":
 *         return <Login/>;
 *     case "/":
 *         return <Home/>;
 * }
 *
 */
const Content = () => {
    return (
        <StyledDiv>
            <Routes>
                <Route path="/register" element={<Register/>}/>
                <Route path="/login" element={<Login/>}/>
                <Route path="/" element={<Home/>}/>
                <Route path="/logout" element={<Logout/>}/>
                <Route path="/orders" element={<Orders/>}/>
                <Route path="/cart" element={<Cart/>}/>
                <Route path="/movie/:movieId" element={<GetMovie/>}/>
                <Route path="/checkout" element={<App/>}/>
            </Routes>
        </StyledDiv>
    );
}

export default Content;
