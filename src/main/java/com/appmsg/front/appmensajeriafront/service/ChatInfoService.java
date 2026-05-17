package com.appmsg.front.appmensajeriafront.service;

import com.appmsg.front.appmensajeriafront.config.ApiConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ChatInfoService {

    private static final String CHAT_PATH = "/api/chat";
    private static final String MESSAGES_PATH = "/api/messages";
    private final Gson gson = new Gson();

    public String getChatInfo(String chatId) throws Exception {
        String urlStr = ApiConfig.BASE_API_URL + CHAT_PATH
                + "?chatId=" + URLEncoder.encode(chatId, "UTF-8")
                + "&members=true";
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        int code = conn.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
        return readStream(is);
    }

    public String updateChatInfo(String jsonBody) throws Exception {
        String urlStr = ApiConfig.BASE_API_URL + CHAT_PATH;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");

        try (java.io.OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes("UTF-8"));
        }

        int code = conn.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
        return readStream(is);
    }

    public String addMember(String chatId, String username) throws Exception {
        String urlStr = ApiConfig.BASE_API_URL + CHAT_PATH;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");

        JsonObject body = new JsonObject();
        body.addProperty("action", "addMember");
        body.addProperty("chatId", chatId);
        body.addProperty("username", username);

        try (java.io.OutputStream os = conn.getOutputStream()) {
            os.write(gson.toJson(body).getBytes("UTF-8"));
        }

        int code = conn.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
        return readStream(is);
    }

    public String getChatMedia(String chatId) throws Exception {
        String urlStr = ApiConfig.BASE_API_URL + MESSAGES_PATH
                + "?chatId=" + URLEncoder.encode(chatId, "UTF-8")
                + "&onlyMedia=true";
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        int code = conn.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
        return readStream(is);
    }

    private String readStream(InputStream is) throws Exception {
        if (is == null) return "{}";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }
}
