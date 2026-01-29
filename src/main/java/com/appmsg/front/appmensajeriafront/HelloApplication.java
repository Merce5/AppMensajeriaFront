package com.appmsg.front.appmensajeriafront;

import com.appmsg.front.appmensajeriafront.service.WebViewManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {
        WebView webView = new WebView();
        WebViewManager manager = new WebViewManager(webView);
        manager.initialize("login.html", null);

        BorderPane root = new BorderPane(webView);
        Scene scene = new Scene(root, 1200, 800);

        stage.setScene(scene);
        stage.setTitle("AppMensajería");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
