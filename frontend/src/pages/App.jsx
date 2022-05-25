import React, {useEffect, useState} from "react";
import { loadStripe } from "@stripe/stripe-js";
import { Elements } from "@stripe/react-stripe-js";
import CheckoutForm from "./CheckoutForm";
import {useUser} from "../hook/User";
import {useNavigate} from "react-router-dom";

const stripePromise = loadStripe("pk_test_51KwYPfERWirZqgkdBtsKTBwDwRFWNFKwagaCSLrICtPZtmFoqOVRZ03eFvDk7aM8r8s5YmB2u5owHjCwY1yPY3LT00sG8VFGSi");

const App = () => {
  const {
      accessToken, setAccessToken,
      refreshToken, setRefreshToken
  } = useUser();

  const navigate = useNavigate();
  const [clientSecret, setClientSecret] = useState("");

  const appearance = {
      theme: 'stripe',
  };

  const options = {
      clientSecret,
      appearance,
  };

  useEffect(() => {
    if (accessToken == null) {
      navigate("/");
    }
  });

    useEffect(() => {
        // Create PaymentIntent as soon as the page loads
        fetch("http://localhost:8083/order/payment", {
          method: "GET",
          headers: { "Content-Type": "application/json", Authorization: "Bearer " + accessToken}
        })
          .then((res) => res.json())
          .then((data) => setClientSecret(data.clientSecret));
      }, []);

  return (
    <div className="App">
      {clientSecret && (
        <Elements options={options} stripe={stripePromise}>
          <CheckoutForm />
        </Elements>
      )}
    </div>
  );
}

export default App;