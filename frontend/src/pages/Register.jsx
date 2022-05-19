import React from "react";
import styled from "styled-components";
import {useForm} from "react-hook-form";
import {registerUser} from "backend/idm";
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

const Register = () => {
    const {register, getValues, handleSubmit} = useForm();
    const navigate = useNavigate();

    const submitRegister = () => {
        const email = getValues("email");
        const password = getValues("password");

        const payLoad = {
            email: email,
            password: password.split('')
        }

        registerUser(payLoad)
            .then(response => {
                alert(JSON.stringify(response.data, null, 2));
                navigate("/login");
            })
            .catch(error => alert(JSON.stringify(error.response.data, null, 2)))
    }

    return (
        <div id="container">
            <div id="header">
                <h1>REGISTRATION</h1>
            </div>

            <StyledDiv>
                <span className="subtitle">USERNAME:</span>
                <StyledInput {...register("email")} type={"email"} autoComplete="off"/>
                <span className="subtitle">PASSWORD:</span>
                <StyledInput {...register("password")} type={"password"} autoComplete="off"/>
                <StyledButton onClick={handleSubmit(submitRegister)}>REGISTER</StyledButton>
            </StyledDiv>
        </div>
    );
}

export default Register;