package com.appmsg.front.appmensajeriafront.service;

import com.appmsg.front.appmensajeriafront.session.Session;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;

import java.util.Map;

public class JavaBridge {

    private final WebEngine webEngine;
    private final PageLoader pageLoader;
    private final Map<String, String> initParams;

    public JavaBridge(WebEngine webEngine, Map<String, String> initParams, PageLoader pageLoader) {
        this.webEngine = webEngine;
        this.initParams = initParams;
        this.pageLoader = pageLoader;
    }

    // ======== LOGIN ========
    public void tryToLogin(String username, String password) {

        boolean ok = username != null && !username.isBlank()
                && password != null && !password.isBlank();

        if (ok) {
            Session.setUserId("123"); // mock (luego backend real)
        }

        Platform.runLater(() -> {
            String json = "{ \"ok\": " + ok + ", \"userId\": \"123\" }";
            webEngine.executeScript(
                    "if(typeof onLoginResult==='function'){ onLoginResult(" + json + "); }"
            );
        });
    }

    // ======== NAV ========
    public void navigate(String page) {
        Platform.runLater(() -> pageLoader.load(page));
    }

    // ======== LOG ========
    public void log(String msg) {
        System.out.println("[JS] " + msg);
    }
}
