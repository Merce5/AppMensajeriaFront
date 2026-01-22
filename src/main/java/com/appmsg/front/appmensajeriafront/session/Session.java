package com.appmsg.front.appmensajeriafront.session;

public class Session {
    private static String userId; // lo setea Merce al hacer login

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String userId) {
        Session.userId = userId;
    }
}
