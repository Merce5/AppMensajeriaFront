package com.appmsg.front.appmensajeriafront.service;

import com.appmsg.front.appmensajeriafront.model.UploadResponse;
import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;

/**
 * Servicio para subir archivos al backend.
 */
public class FileUploadService {

    private static final String UPLOAD_URL = "http://localhost:8080/APPMensajeriaUEM/api/upload";
    private static final String BOUNDARY = "----WebKitFormBoundary" + System.currentTimeMillis();
    private static final String LINE_FEED = "\r\n";

    private final Gson gson;

    public FileUploadService() {
        this.gson = new Gson();
    }

    /**
     * Sube una lista de archivos al servidor.
     */
    public UploadResponse uploadFiles(List<File> files) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(UPLOAD_URL).openConnection();

        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
        conn.setRequestProperty("Accept", "application/json");

        try (OutputStream os = conn.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true)) {

            for (File file : files) {
                addFilePart(writer, os, "file", file);
            }

            // Finalizar multipart
            writer.append("--").append(BOUNDARY).append("--").append(LINE_FEED);
            writer.flush();
        }

        // Leer respuesta
        int responseCode = conn.getResponseCode();
        String response = readResponse(conn);

        if (responseCode >= 200 && responseCode < 300) {
            return gson.fromJson(response, UploadResponse.class);
        } else {
            UploadResponse error = new UploadResponse();
            error.success = false;
            error.error = "Error " + responseCode + ": " + response;
            return error;
        }
    }

    /**
     * Añade un archivo a la request multipart.
     */
    private void addFilePart(PrintWriter writer, OutputStream os, String fieldName, File file) throws IOException {
        String fileName = file.getName();
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        writer.append("--").append(BOUNDARY).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"").append(fieldName)
                .append("\"; filename=\"").append(fileName).append("\"").append(LINE_FEED);
        writer.append("Content-Type: ").append(contentType).append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        // Escribir contenido del archivo
        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }

        writer.append(LINE_FEED);
        writer.flush();
    }

    /**
     * Lee la respuesta del servidor.
     */
    private String readResponse(HttpURLConnection conn) throws IOException {
        InputStream is;
        try {
            is = conn.getInputStream();
        } catch (IOException e) {
            is = conn.getErrorStream();
        }

        if (is == null) {
            return "";
        }

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
