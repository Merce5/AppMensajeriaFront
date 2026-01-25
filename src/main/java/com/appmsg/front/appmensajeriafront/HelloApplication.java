package com.appmsg.front.appmensajeriafront;

import com.appmsg.front.appmensajeriafront.service.JavaBridge;
import com.appmsg.front.appmensajeriafront.util.ThemeManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        webEngine.load(getClass().getResource("login.html").toExternalForm());

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("app", new JavaBridge());
            }
        });


        Scene scene = new Scene(webView, 800, 600);

        ThemeManager.apply(scene, ThemeManager.Theme.LIGHT);
        stage.setTitle("App Mensajería");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
