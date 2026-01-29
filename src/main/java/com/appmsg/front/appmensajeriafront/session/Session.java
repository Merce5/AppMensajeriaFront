package com.appmsg.front.appmensajeriafront.session;

public class Session {

    private static String userId;

    public static void setUserId(String id) {
        userId = id;
    }

    public static String getUserId() {
        return userId;
    }

    public static void clear() {
        userId = null;
    }
}
