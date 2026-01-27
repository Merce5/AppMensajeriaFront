package com.appmsg.front.appmensajeriafront.model;

/**
 * DTO para perfil de usuario.
 */
public class UserProfile {
    public String userId;
    public String username;
    public String email;
    public String picture;
    public String status;       // "Online" | "Offline" | custom
    public String wallpaper;

    public UserProfile() {}
}
