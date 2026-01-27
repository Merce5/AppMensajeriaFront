package com.appmsg.front.appmensajeriafront.service;

import com.appmsg.front.appmensajeriafront.model.UserProfile;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Servicio para obtener perfiles de usuario.
 */
public class ProfileService {

    private static final String PROFILE_URL = "http://localhost:8080/APPMensajeriaUEM/api/profile";

    private final Gson gson;

    public ProfileService() {
        this.gson = new Gson();
    }

    /**
     * Obtiene el perfil de un usuario por ID.
     */
    public UserProfile getProfile(String userId) throws Exception {
        String urlStr = PROFILE_URL + "?userId=" + URLEncoder.encode(userId, "UTF-8");
        URL url = new URL(urlStr);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            String response = readResponse(conn.getInputStream());
            return gson.fromJson(response, UserProfile.class);
        } else if (responseCode == 404) {
            throw new Exception("Usuario no encontrado");
        } else {
            String error = readResponse(conn.getErrorStream());
            throw new Exception("Error " + responseCode + ": " + error);
        }
    }

    /**
     * Obtiene el perfil de un usuario por username.
     */
    public UserProfile getProfileByUsername(String username) throws Exception {
        String urlStr = PROFILE_URL + "?username=" + URLEncoder.encode(username, "UTF-8");
        URL url = new URL(urlStr);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            String response = readResponse(conn.getInputStream());
            return gson.fromJson(response, UserProfile.class);
        } else {
            throw new Exception("Usuario no encontrado");
        }
    }

    /**
     * Lee un InputStream completamente.
     */
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
