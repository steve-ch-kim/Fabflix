import React from "react";
import {NavLink} from "react-router-dom";
import styled from "styled-components";

const StyledNav = styled.nav`
  display: flex;
  justify-content: flex-end;

  width: calc(100vw - 10px);
  height: 50px;
  padding: 5px;
  
  background-color: #181818;
  box-shadow: 0 2px 4px 0 red;
`;

const StyledNavLink = styled(NavLink)`
  font-size: 18px;
  color: #989898;
  text-decoration: none;
  float: right;
  margin-left: 50px;
`;

const NameLink = styled(NavLink)`
  font-size: 20px;
  font-style: italic;
  color: #38d39f;
  text-decoration: none;
  float: left;
`;

/**
 * To be able to navigate around the website we have these NavLink's (Notice
 * that they are "styled" NavLink's that are now named StyledNavLink)
 * <br>
 * Whenever you add a NavLink here make sure to add a corresponding Route in
 * the Content Component
 * <br>
 * You can add as many Link as you would like here to allow for better navigation
 * <br>
 * Below we have two Links:
 * <li>Home - A link that will change the url of the page to "/"
 * <li>Login - A link that will change the url of the page to "/login"
 */
const NavBar = () => {
    if (localStorage.getItem("access_token")) {
        return (
            <StyledNav className="navbar">
                <div id="navbar-outside">
                    <NameLink to="/">
                        FabFlix
                    </NameLink>
                    <StyledNavLink to="/logout" className="styledNavLink" activeClassName="active">
                        Log Out
                    </StyledNavLink>
                    <StyledNavLink to="/cart" className="styledNavLink" activeClassName="active">
                        Cart
                    </StyledNavLink>
                    <StyledNavLink to="/orders" className="styledNavLink" activeClassName="active">
                        Orders
                    </StyledNavLink>
                </div>
            </StyledNav>
        );
    } else {
        return (
            <StyledNav className="navbar">
                <div id="navbar-outside">
                    <NameLink to="/">
                        FabFlix
                    </NameLink>
                    <StyledNavLink to="/login" className="styledNavLink" activeClassName="active">
                        Log In
                    </StyledNavLink>
                    <StyledNavLink to="/register" className="styledNavLink" activeClassName="active">
                        Register
                    </StyledNavLink>
                </div>
            </StyledNav>
        );
    }
}

export default NavBar;
