package com.appmsg.front.appmensajeriafront.ui.login;

import com.appmsg.front.appmensajeriafront.clients.HttpGateway;
import com.appmsg.front.appmensajeriafront.model.UserDto;
import com.appmsg.front.appmensajeriafront.session.Session;
import com.appmsg.front.appmensajeriafront.webview.WebViewManager;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginController {
    @FXML
    private WebView webView;

    private WebViewManager webViewManager;

    private HttpGateway gateway;

    @FXML
    public void initialize() {
        webViewManager = new WebViewManager(webView);
        gateway = new HttpGateway();
    }

    public void loadLogin(String userId) {
        Map<String, String> params = new HashMap<>();
        params.put("userid", userId);
        webViewManager.initialize("index.html", params);
    }

    public void tryToLogin(String username, String password) throws IOException, InterruptedException {
        var user = new UserDto(username, password);
        var loginResult = gateway.login(user);
        if (loginResult.getUserId() == null) {
            // todo
            return;
        }
        Session.setUserId(loginResult.getUserId());
    }
}