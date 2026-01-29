package com.appmsg.front.appmensajeriafront.service;

import com.google.gson.Gson;
import javafx.scene.web.WebEngine;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Bridge entre Java y JavaScript.
 * Todos los métodos públicos son accesibles desde JavaScript.
 */
public class JavaBridge {

    private final WebEngine webEngine;
    private final Gson gson;
    private final Map<String, String> initParams;


    public JavaBridge(WebEngine webEngine, Map<String, String> initParams) {
        this.webEngine = webEngine;
        this.initParams = initParams;
        this.gson = new Gson();
    }

    public void tryToLogin(String username, String password) {
        CompletableFuture.runAsync(() -> {
            System.out.println(String.format("%s %s", username, password));
        });
    }


    // ==================== LOG ====================

    /**
     * Log desde JavaScript (útil para debugging).
     */
    public void log(String message) {
        System.out.println("[JS] " + message);
    }

    private void callJsFunction(String functionName, String jsonParam) {
        String escapedJson = jsonParam.replace("\\", "\\\\").replace("'", "\\'");
        String script = "if(typeof " + functionName + " === 'function') { " + functionName + "('" + escapedJson + "'); }";
        webEngine.executeScript(script);
    }
}