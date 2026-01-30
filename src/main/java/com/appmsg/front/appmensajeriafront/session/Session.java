package com.appmsg.front.appmensajeriafront.session;

public class Session {
    private static String userId; // lo setea Merce al hacer login
    private static String chatId; // chat actual abierto

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String userId) {
        Session.userId = userId;
    }

    public static String getChatId() {
        return chatId;
    }

    public static void setChatId(String chatId) {
        Session.chatId = chatId;
    }
}