package com.appmsg.front.appmensajeriafront.service;

import com.appmsg.front.appmensajeriafront.config.ApiConfig;
import com.appmsg.front.appmensajeriafront.model.ChatCreateResponse;
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
    private static final String CHAT_PATH = "/api/chat";
    private final Gson gson;

    public ChatService() {
        this.gson = new Gson();
    }

    /**
     * Crea un nuevo chat de grupo.
     * @param chatName Nombre del chat
     * @param creatorId ID del usuario que crea el chat
     * @param maxParticipants Número máximo de participantes
     * @return ChatCreateResponse con chatId y success
     */
    public ChatCreateResponse createChat(String chatName, String creatorId, int maxParticipants) throws Exception {
        String urlStr = ApiConfig.BASE_API_URL + CHAT_PATH;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");

        // Crear body
        com.google.gson.JsonObject body = new com.google.gson.JsonObject();
        body.addProperty("action", "createChat");
        body.addProperty("chatName", chatName);
        body.addProperty("creatorId", creatorId);
        body.addProperty("maxParticipants", maxParticipants);

        // Campos requeridos por el backend
        com.google.gson.JsonArray userList = new com.google.gson.JsonArray();
        userList.add(creatorId); // El creador es el primer miembro
        body.add("userList", userList);
        body.addProperty("chatImage", ""); // Imagen vacía por defecto

        // Enviar request
        try (java.io.OutputStream os = conn.getOutputStream()) {
            os.write(gson.toJson(body).getBytes("UTF-8"));
        }

        int responseCode = conn.getResponseCode();
        String response = readResponse(
                responseCode >= 200 && responseCode < 300
                        ? conn.getInputStream()
                        : conn.getErrorStream()
        );

        ChatCreateResponse chatResponse = gson.fromJson(response, ChatCreateResponse.class);

        if (responseCode >= 200 && responseCode < 300) {
            return chatResponse;
        } else {
            System.out.println(chatResponse);
            if (chatResponse != null && chatResponse.message != null) {
                throw new Exception(chatResponse.message);
            }
            throw new Exception("Error al crear el chat");
        }
    }

    public List<ChatListItemDto> getChats(String userId) throws Exception {
        String urlStr = ApiConfig.BASE_API_URL + CHATS_PATH + "?userId=" + URLEncoder.encode(userId, "UTF-8");
        URL url = new URL(urlStr);

        System.out.println(urlStr);

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
