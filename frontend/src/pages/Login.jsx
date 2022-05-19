import React from "react";
import styled from "styled-components";
import {useUser} from "hook/User";
import {useForm} from "react-hook-form";
import {login} from "backend/idm";
import {useNavigate} from 'react-router-dom';

const StyledDiv = styled.div`
  display: flex;
  flex-direction: column;
`

const StyledInput = styled.input`
  border: none;
  border-bottom: solid rgb(143, 143, 143) 1px;

  margin-bottom: 30px;

  background: none;
  color: rgba(255, 255, 255, 0.555);

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
/**
 * useUser():
 * <br>
 * This is a hook we will use to keep track of our accessToken and
 * refreshToken given to use when the user calls "login".
 * <br>
 * For now, it is not being used, but we recommend setting the two tokens
 * here to the tokens you get when the user completes the login call (once
 * you are in the .then() function after calling login)
 * <br>
 * These have logic inside them to make sure the accessToken and
 * refreshToken are saved into the local storage of the web browser
 * allowing you to keep values alive even when the user leaves the website
 * <br>
 * <br>
 * useForm()
 * <br>
 * This is a library that helps us with gathering input values from our
 * users.
 * <br>
 * Whenever we make a html component that takes a value (<input>, <select>,
 * ect) we call this function in this way:
 * <pre>
 *     {...register("email")}
 * </pre>
 * Notice that we have "{}" with a function call that has "..." before it.
 * This is just a way to take all the stuff that is returned by register
 * and <i>distribute</i> it as attributes for our components. Do not worry
 * too much about the specifics of it, if you would like you can read up
 * more about it on "react-hook-form"'s documentation:
 * <br>
 * <a href="https://react-hook-form.com/">React Hook Form</a>.
 * <br>
 * Their documentation is very detailed and goes into all of these functions
 * with great examples. But to keep things simple: Whenever we have a html with
 * input we will use that function with the name associated with that input,
 * and when we want to get the value in that input we call:
 * <pre>
 * getValue("email")
 * </pre>
 * <br>
 * To Execute some function when the user asks we use:
 * <pre>
 *     handleSubmit(ourFunctionToExecute)
 * </pre>
 * This wraps our function and does some "pre-checks" before (This is useful if
 * you want to do some input validation, more of that in their documentation)
 */
const Login = () => {
    const {
        accessToken, setAccessToken,
        refreshToken, setRefreshToken
    } = useUser();

    const {register, getValues, handleSubmit} = useForm();
    const navigate = useNavigate();

    const submitLogin = () => {
        const email = getValues("email");
        const password = getValues("password");

        const payLoad = {
            email: email,
            password: password.split('')
        }

        login(payLoad)
            .then(response => {
                alert(JSON.stringify(response.data, null, 2));
                setAccessToken(response.data.accessToken);
                navigate("/");
                window.location.reload();
            })
            .catch(error => alert(JSON.stringify(error.response.data, null, 2)));
    }

    return (
        <div id="container">
            <div id="header">
                <h1>LOG IN</h1>
            </div>

            <StyledDiv>
                <span className="subtitle">USERNAME:</span>
                <StyledInput {...register("email")} type={"email"} autoComplete="off"/>
                <span className="subtitle">PASSWORD:</span>
                <StyledInput {...register("password")} type={"password"} autoComplete="off"/>
                <StyledButton onClick={handleSubmit(submitLogin)}>LOGIN</StyledButton>
            </StyledDiv>
        </div>
    );
}

export default Login;
