import Config from "backend/config.json";
import Axios from "axios";


/**
 * We use axios to create REST calls to our backend
 *
 * We have provided the login rest call for your
 * reference to build other rest calls with.
 *
 * This is an async function. Which means calling this function requires that
 * you "chain" it with a .then() function call.
 * <br>
 * What this means is when the function is called it will essentially do it "in
 * another thread" and when the action is done being executed it will do
 * whatever the logic in your ".then()" function you chained to it
 * @example
 * login(request)
 * .then(response => alert(JSON.stringify(response.data, null, 2)));
 */
export async function login(loginRequest) {
    const requestBody = {
        email: loginRequest.email,
        password: loginRequest.password
    };

    const options = {
        method: "POST", // Method type ("POST", "GET", "DELETE", ect)
        baseURL: Config.baseUrl, // Base URL (localhost:8081 for example)
        url: Config.idm.login, // Path of URL ("/login")
        data: requestBody // Data to send in Body (The RequestBody to send)
    }

    return Axios.request(options);
}

export async function registerUser(registerRequest) {
    const requestBody = {
        email: registerRequest.email,
        password: registerRequest.password
    };

    const options = {
        method: "POST", // Method type ("POST", "GET", "DELETE", ect)
        baseURL: Config.baseUrl, // Base URL (localhost:8081 for example)
        url: Config.idm.register, // Path of URL ("/register")
        data: requestBody // Data to send in Body (The RequestBody to send)
    };

    return Axios.request(options);
}

export async function searchMovies(queryParams, accessToken) {
    const options = {
        method: "GET",
        baseURL: Config.movieUrl,
        url: Config.idm.search,
        params: queryParams,
        headers: {
            Authorization: "Bearer " + accessToken
        }
    };

    return Axios.request(options);
}

export async function searchMovie(queryParams, accessToken, movieId) {
    const options = {
        method: "GET",
        baseURL: Config.movieUrl,
        url: Config.idm.getMovie + movieId,
        params: queryParams,
        headers: {
            Authorization: "Bearer " + accessToken
        }
    };

    return Axios.request(options);
}

export async function insertCart(insertRequest, accessToken) {
    const requestBody = {
        movieId: insertRequest.movieId,
        quantity: insertRequest.quantity
    };

    const options = {
        method: "POST", // Method type ("POST", "GET", "DELETE", ect)
        baseURL: Config.cartUrl,
        url: Config.idm.insertCart,
        data: requestBody,
        headers: {
            Authorization: "Bearer " + accessToken
        }
    };

    return Axios.request(options);
}

export async function retrieveCart(queryParams, accessToken) {
    const options = {
        method: "GET",
        baseURL: Config.cartUrl,
        url: Config.idm.retrieveCart,
        params: queryParams,
        headers: {
            Authorization: "Bearer " + accessToken
        }
    };

    return Axios.request(options);
}

export async function cartUpdate(cartRequest, accessToken) {
    const requestBody = {
        movieId: cartRequest.movieId,
        quantity: cartRequest.quantity
    };

    const options = {
        method: "POST", // Method type ("POST", "GET", "DELETE", ect)
        baseURL: Config.cartUrl,
        url: Config.idm.updateCart,
        data: requestBody,
        headers: {
            Authorization: "Bearer " + accessToken
        }
    };

    return Axios.request(options);
}

export async function cartDelete(deleteRequest, accessToken) {
    const requestBody = {
        movieId: deleteRequest.movieId
    }

    const options = {
        method: "DELETE", // Method type ("POST", "GET", "DELETE", ect)
        baseURL: Config.cartUrl,
        url: Config.idm.deleteCart + deleteRequest.movieId,
        data: requestBody,
        headers: {
            Authorization: "Bearer " + accessToken
        }
    };

    return Axios.request(options);
}