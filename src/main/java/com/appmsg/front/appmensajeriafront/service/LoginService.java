package com.appmsg.front.appmensajeriafront.service;

import com.appmsg.front.appmensajeriafront.model.ResponseBase;
import com.appmsg.front.appmensajeriafront.model.auth.LoginRS;
import com.appmsg.front.appmensajeriafront.model.UserDto;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.appmsg.front.appmensajeriafront.config.ApiConfig;

public class LoginService {

    private HttpClient httpClient;

    private static final String BASE_PATH = "/api";

    private static final Gson gson = new Gson();

    public LoginService() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        httpClient = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .build();
    }

    public LoginRS login(UserDto user) throws IOException, InterruptedException {
        var request = gson.toJson(user);
        var httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.BASE_API_URL + BASE_PATH + "/login"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(request))
            .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        return gson.fromJson(response.body(), LoginRS.class);
    }

    public LoginRS register(UserDto user) throws IOException, InterruptedException {
        var request = gson.toJson(user);
        var httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.BASE_API_URL + BASE_PATH + "/register"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(request))
            .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            var loginRs = new LoginRS();
            loginRs.setError(response.body());
            return loginRs;
        }

        return gson.fromJson(response.body(), LoginRS.class);
    }

    public ResponseBase verifyRegister(String code) throws IOException, InterruptedException {
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BASE_API_URL + BASE_PATH + "/register?verificationCode=" + code))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed : HTTP error code : " + response.statusCode());
        }
        return gson.fromJson(response.body(), ResponseBase.class);
    }
}
