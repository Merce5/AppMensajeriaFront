package com.appmsg.front.appmensajeriafront.service;

import com.appmsg.front.appmensajeriafront.model.UserSettingsDto;
import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class SettingsService {
    private final Gson gson = new Gson();

    // TODO: poner URL real del backend (host, puerto, contexto)
    private final String baseUrl = "http://localhost:8080/TU_APP/api/settings"; // <-- CAMBIAR

    public UserSettingsDto getSettings(String userId) throws Exception {
        URL url = new URL(baseUrl + "?userId=" + userId);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");

        int code = con.getResponseCode();
        String body = readAll(code >= 200 && code < 400 ? con.getInputStream() : con.getErrorStream());

        if (code == 200) return gson.fromJson(body, UserSettingsDto.class);
        if (code == 404) return null;
        throw new RuntimeException("GET settings error " + code + " body=" + body);
    }

    public UserSettingsDto saveSettings(UserSettingsDto dto) throws Exception {
        URL url = new URL(baseUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.setRequestProperty("Accept", "application/json");

        String json = gson.toJson(dto);
        try (OutputStream os = con.getOutputStream()) {
            os.write(json.getBytes("UTF-8"));
        }

        int code = con.getResponseCode();
        String body = readAll(code >= 200 && code < 400 ? con.getInputStream() : con.getErrorStream());

        if (code == 200) return gson.fromJson(body, UserSettingsDto.class);
        throw new RuntimeException("POST settings error " + code + " body=" + body);
    }

    private static String readAll(InputStream is) throws IOException {
        if (is == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }
}
