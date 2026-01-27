package com.appmsg.front.appmensajeriafront.model;

/**
 * DTO para respuesta de upload de archivos.
 */
public class UploadResponse {
    public boolean success;
    public int count;
    public String files;  // JSON array string: "[\"url1\", \"url2\"]"
    public String error;

    public UploadResponse() {}
}
