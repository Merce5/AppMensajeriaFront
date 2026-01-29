package com.appmsg.front.appmensajeriafront.model;

import java.util.List;

/**
 * DTO para mensajes de chat.
 */
public class ChatMessage {
    public String type;           // "message" | "typing" | "status"
    public String messageId;
    public String chatId;
    public String senderId;
    public String message;
    public List<String> multimedia;
    public String status;         // "sent" | "delivered" | "read"
    public long timestamp;
    public Boolean isTyping;      // Para mensajes tipo "typing"
    public String username;       // Nombre del remitente (opcional)

    public ChatMessage() {}
}
