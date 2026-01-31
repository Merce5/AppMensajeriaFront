package com.appmsg.front.appmensajeriafront.service;

import com.appmsg.front.appmensajeriafront.model.LoginRS;
import com.appmsg.front.appmensajeriafront.model.UserDto;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LoginService {

    private HttpClient httpClient;

    private final String BASE_URL = "http://localhost:8080/APPMensajeriaUEM_war_exploded/api";

    private static final Gson gson = new Gson();

    public LoginService() {
        httpClient = HttpClient.newHttpClient();
    }

    public LoginRS login(UserDto user) throws IOException, InterruptedException {
        var request = gson.toJson(user);
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(request))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        return gson.fromJson(response.body(), LoginRS.class);
    }

    public void register(UserDto user) throws IOException, InterruptedException {
        var request = gson.toJson(user);
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(request))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed : HTTP error code : " + response.statusCode());
        }
    }

    public void verifyRegister(String code) throws IOException, InterruptedException {
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/register?verificationCode=" + code))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed : HTTP error code : " + response.statusCode());
        }
    }
}
