package com.appmsg.front.appmensajeriafront.ui.login;

import com.appmsg.front.appmensajeriafront.webview.WebViewManager;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;

import java.util.HashMap;
import java.util.Map;

public class LoginController {
    @FXML
    private WebView webView;

    private WebViewManager webViewManager;

    @FXML
    public void initialize() {
        webViewManager = new WebViewManager(webView);
    }

    public void loadLogin() {
        Map<String, String> params = new HashMap<>();
        webViewManager.initialize("login.html", params);
    }

    public void tryToLogin() {
        // TODO: Logica de login
    }
}