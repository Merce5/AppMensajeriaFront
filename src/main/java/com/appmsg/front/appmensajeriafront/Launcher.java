package com.appmsg.front.appmensajeriafront;

/**
 * Launcher class para ejecutar la aplicacion desde un fat JAR.
 * JavaFX requiere que el main class no extienda Application
 * cuando se ejecuta desde un JAR empaquetado con shade.
 */
public class Launcher {
    public static void main(String[] args) {
        AppMensajeriaFront.main(args);
    }
}
