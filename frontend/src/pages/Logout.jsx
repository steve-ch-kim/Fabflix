import React from "react";
import styled from "styled-components";
import {useNavigate} from 'react-router-dom';

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

const CancelButton = styled.button`
  cursor: pointer;

  border: none;
  border-radius: 8px;

  box-shadow: 2px 2px 7px #38d39f70;

  background: #ff4c4c;
  color: rgba(255, 255, 255, 0.8);

  width: 100%;
  margin-top: 10px;
  padding: 20px;
`

const Logout = () => {
    const navigate = useNavigate();

    const handleLogout = () => {
        window.localStorage.clear();
        navigate("/login");
        window.location.reload();
    };

    const cancel = () => {
        navigate("/");
    }

    return (
        <div id="container">
            <div id="header">
                <h1>LOG OUT</h1>
            </div>

            <StyledButton type="button" onClick={handleLogout}>
                Confirm
            </StyledButton>
            <CancelButton type="button" onClick={cancel}>
                Cancel
            </CancelButton>
        </div>
    );
};

export default Logout;