package com.appmsg.front.appmensajeriafront;

import com.appmsg.front.appmensajeriafront.service.JavaBridge;
import com.appmsg.front.appmensajeriafront.service.WebViewManager;
import com.appmsg.front.appmensajeriafront.util.ThemeManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.util.HashMap;
import java.util.Map;

public class HelloApplication extends Application {
    private WebViewManager webViewManager;

    @Override
    public void start(Stage primaryStage) {

        WebView webView = new WebView();

        webViewManager = new WebViewManager(webView);

        webViewManager.initialize("login.html", null);

        BorderPane root = new BorderPane();
        root.setCenter(webView);

        Scene scene = new Scene(root, 1200, 800);

        primaryStage.setTitle("App Mensajería");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
