package com.appmsg.front.appmensajeriafront.service;

import com.appmsg.front.appmensajeriafront.config.ApiConfig;
import com.appmsg.front.appmensajeriafront.model.ChatListItemDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

public class ChatService {

    private static final String CHATS_PATH = "/api/chats";
    private final Gson gson;

    public ChatService() {
        this.gson = new Gson();
    }

    public List<ChatListItemDto> getChats(String userId) throws Exception {
        String urlStr = ApiConfig.BASE_API_URL + CHATS_PATH + "?userId=" + URLEncoder.encode(userId, "UTF-8");
        URL url = new URL(urlStr);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            String response = readResponse(conn.getInputStream());
            Type chatListType = new TypeToken<List<ChatListItemDto>>(){}.getType();
            return gson.fromJson(response, chatListType);
        } else {
            String error = readResponse(conn.getErrorStream());
            throw new Exception("Error " + responseCode + ": " + error);
        }
    }

    private String readResponse(InputStream is) throws Exception {
        if (is == null) return "";

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}
