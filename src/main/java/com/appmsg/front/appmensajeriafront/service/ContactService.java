package com.appmsg.front.appmensajeriafront.service;

import com.appmsg.front.appmensajeriafront.config.ApiConfig;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class ContactService {
    private HttpClient httpClient;

    public ContactService() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        httpClient = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .build();
    }

    public String getContacts(String userId)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BASE_API_URL + "/api/contact?userId=" + userId))
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    public String addContact(String userId, String identifier)
            throws IOException, InterruptedException {

        Gson gson = new Gson();

        String json = gson.toJson(
                Map.of(
                        "userId", userId,
                        "identifier", identifier
                )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BASE_API_URL + "/api/contact"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    public void removeContact(
            String userId,
            String contactId
    ) throws IOException, InterruptedException {

        Gson gson = new Gson();

        String json = gson.toJson(
                Map.of(
                        "userId", userId,
                        "identifier", contactId
                )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BASE_API_URL + "/api/contact"))
                .header("Content-Type", "application/json")
                .method("DELETE", HttpRequest.BodyPublishers.ofString(json))
                .build();

        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
