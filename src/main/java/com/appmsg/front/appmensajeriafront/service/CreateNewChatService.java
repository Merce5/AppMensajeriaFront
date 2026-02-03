package com.appmsg.front.appmensajeriafront.service;

import com.appmsg.front.appmensajeriafront.config.ApiConfig;
import com.appmsg.front.appmensajeriafront.model.ChatListItemDto;
import com.appmsg.front.appmensajeriafront.model.UserSettingsDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

public class CreateNewChatService {

    private final Gson gson;
    private static final String CHATS_PATH = "/api/chat";

    public CreateNewChatService() {
        this.gson = new Gson();
    }

    public void createNewChat(String userId, String chatName) throws Exception {

        String urlStr = ApiConfig.BASE_API_URL + CHATS_PATH + "?userId=" + URLEncoder.encode(userId, "UTF-8");
        URL url = new URL(urlStr);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        String json = gson.toJson(chatName);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes("UTF-8"));
        }

        int code = conn.getResponseCode();

        String body = readResponse(code >= 200 && code < 400 ? conn.getInputStream() : conn.getErrorStream());

 //       if (code == 200) return gson.fromJson(body, newchat.id);
        throw new RuntimeException("POST chat error " + code + " body=" + body);
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
