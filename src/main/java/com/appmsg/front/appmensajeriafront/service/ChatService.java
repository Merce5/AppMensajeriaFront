package com.appmsg.front.appmensajeriafront.service;

import com.appmsg.front.appmensajeriafront.config.ApiConfig;
import com.appmsg.front.appmensajeriafront.model.ChatCreateResponse;
import com.appmsg.front.appmensajeriafront.model.ChatListItemDto;
import com.appmsg.front.appmensajeriafront.model.ChatMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ChatService {

    private static final String CHATS_PATH = "/api/chats";
    private static final String CHAT_PATH = "/api/chat";
    private static final String MESSAGES_PATH = "/api/messages";
    private final Gson gson;

    public ChatService() {
        // Configurar Gson con un deserializador personalizado para ObjectIds
        this.gson = new com.google.gson.GsonBuilder()
                .registerTypeAdapter(String.class, new com.google.gson.JsonDeserializer<String>() {
                    @Override
                    public String deserialize(com.google.gson.JsonElement json,
                                            java.lang.reflect.Type typeOfT,
                                            com.google.gson.JsonDeserializationContext context)
                                            throws com.google.gson.JsonParseException {
                        if (json.isJsonPrimitive()) {
                            return json.getAsString();
                        } else if (json.isJsonObject()) {
                            // Es un ObjectId de MongoDB, convertir a string representativo
                            com.google.gson.JsonObject obj = json.getAsJsonObject();
                            if (obj.has("timestamp") && obj.has("counter")) {
                                // Formato simple: timestamp-counter
                                return obj.get("timestamp").getAsString() + "-" + obj.get("counter").getAsString();
                            }
                            return json.toString();
                        }
                        return null;
                    }
                })
                .create();
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

    /**
     * Obtiene mensajes de un chat con paginación.
     * @param chatId ID del chat
     * @return Lista de mensajes
     */
    public List<ChatMessage> getMessages(String chatId) throws Exception {
        StringBuilder urlStr = new StringBuilder(ApiConfig.BASE_API_URL + MESSAGES_PATH);
        urlStr.append("?chatId=").append(URLEncoder.encode(chatId, "UTF-8"));

        URL url = new URL(urlStr.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            String response = readResponse(conn.getInputStream());
            com.google.gson.JsonArray jsonArray = com.google.gson.JsonParser.parseString(response).getAsJsonArray();
            List<ChatMessage> messages = new ArrayList<>();

            for (com.google.gson.JsonElement element : jsonArray) {
                com.google.gson.JsonObject obj = element.getAsJsonObject();
                ChatMessage msg = new ChatMessage();

                msg.messageId = parseObjectIdOrString(obj.get("_messageId"));
                msg.chatId = parseObjectIdOrString(obj.get("_chatId"));
                msg.senderId = parseObjectIdOrString(obj.get("_senderId"));
                msg.message = obj.has("_message") ? obj.get("_message").getAsString() : null;
                msg.status = obj.has("_status") ? obj.get("_status").getAsString() : null;
                msg.timestamp = obj.has("_timestamp") ? obj.get("_timestamp").getAsString() : null;
                msg.type = obj.has("_type") ? obj.get("_type").getAsString() : null;

                if (obj.has("_multimedia") && obj.get("_multimedia").isJsonArray()) {
                    msg.multimedia = new ArrayList<>();
                    for (com.google.gson.JsonElement multimediaItem : obj.get("_multimedia").getAsJsonArray()) {
                        msg.multimedia.add(multimediaItem.getAsString());
                    }
                }

                messages.add(msg);
            }

            return messages;
        } else {
            String error = readResponse(conn.getErrorStream());
            throw new Exception("Error " + responseCode + ": " + error);
        }
    }

    /**
     * Parsea un ObjectId de MongoDB o un String simple
     */
    private String parseObjectIdOrString(com.google.gson.JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        } else if (element.isJsonObject()) {
            // Es un ObjectId, convertir a string
            com.google.gson.JsonObject obj = element.getAsJsonObject();
            if (obj.has("timestamp") && obj.has("counter")) {
                return obj.get("timestamp").getAsString() + "-" + obj.get("counter").getAsString();
            }
            return element.toString();
        }
        return null;
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
