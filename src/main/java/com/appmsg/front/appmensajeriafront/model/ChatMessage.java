package com.appmsg.front.appmensajeriafront.model;

import java.util.List;

/**
 * DTO para mensajes de chat.
 * Sin @SerializedName para que al serializar use nombres sin guión bajo.
 */
public class ChatMessage {
    public String type;           // "message" | "typing" | "status"
    public String messageId;
    public String chatId;
    public String senderId;
    public String message;
    public List<String> multimedia;
    public String status;         // "sent" | "delivered" | "read"
    public String timestamp;      // String porque la API devuelve formato "May 1, 2026, 12:16:28 PM"
    public Boolean isTyping;      // Para mensajes tipo "typing"
    public String username;       // Nombre del remitente (opcional)

    public ChatMessage() {}
}
