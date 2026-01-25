package com.appmsg.front.appmensajeriafront.service;

public class JavaBridge {

    public void login(String username, String password) {
        System.out.println("Usuario: " + username);
        System.out.println("Password: " + password);

        if ("admin".equals(username) && "1234".equals(password)) {
            System.out.println("Login correcto");
        } else {
            System.out.println("Login incorrecto");
        }
    }
}
