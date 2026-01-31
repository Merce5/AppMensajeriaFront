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

    public void loadLogin(String username) {
        Map<String, String> params = new HashMap<>();
        params.put("userid", username);
        webViewManager.initialize("index.html", params);
    }

    public void tryToLogin() {
        // TODO: Logica de login
    }
}