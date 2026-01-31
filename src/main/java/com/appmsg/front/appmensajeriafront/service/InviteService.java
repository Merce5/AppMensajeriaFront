package com.appmsg.front.appmensajeriafront.service;

import com.appmsg.front.appmensajeriafront.model.InviteResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import com.appmsg.front.appmensajeriafront.config.ApiConfig;

/**
 * Servicio para manejar invitaciones a chats.
 */
public class InviteService {

    private static final String INVITE_PATH = "/api/invite";

    private final Gson gson;

    public InviteService() {
        this.gson = new Gson();
    }

    /**
     * Une al usuario a un chat mediante código de invitación.
     */
    public InviteResponse joinByCode(String inviteCode, String userId) throws Exception {
        String urlStr = ApiConfig.BASE_API_URL + INVITE_PATH;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");

        // Crear body
        JsonObject body = new JsonObject();
        body.addProperty("action", "join");
        body.addProperty("inviteCode", inviteCode);
        body.addProperty("userId", userId);

        // Enviar request
        try (OutputStream os = conn.getOutputStream()) {
            os.write(gson.toJson(body).getBytes("UTF-8"));
        }

        // Leer respuesta
        int responseCode = conn.getResponseCode();
        String response = readResponse(
                responseCode >= 200 && responseCode < 300
                        ? conn.getInputStream()
                        : conn.getErrorStream()
        );

        InviteResponse inviteResponse = gson.fromJson(response, InviteResponse.class);

        if (responseCode >= 200 && responseCode < 300) {
            return inviteResponse;
        } else {
            if (inviteResponse != null && inviteResponse.message != null) {
                throw new Exception(inviteResponse.message);
            }
            throw new Exception("Error al unirse al chat");
        }
    }

    /**
     * Obtiene información de un enlace de invitación.
     */
    public JsonObject getInviteInfo(String inviteCode) throws Exception {
        String urlStr = ApiConfig.BASE_API_URL + INVITE_PATH;
        URL url = new URL(urlStr + "?code=" + inviteCode);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            String response = readResponse(conn.getInputStream());
            return gson.fromJson(response, JsonObject.class);
        } else {
            throw new Exception("Enlace de invitación no válido");
        }
    }

    /**
     * Lee un InputStream completamente.
     */
    private String readResponse(InputStream is) throws Exception {
        if (is == null) return "{}";

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
