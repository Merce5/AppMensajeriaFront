package com.appmsg.front.appmensajeriafront.service;

import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class WebViewManager implements PageLoader {

    private final WebView webView;
    private final WebEngine webEngine;

    private JavaBridge javaBridge;
    private SettingsBridge settingsBridge;
    private Map<String,String> initParams = new HashMap<>();

    public WebViewManager(WebView webView) {
        this.webView = webView;
        this.webEngine = webView.getEngine();
        setup();
    }

    private void setup() {
        webEngine.setJavaScriptEnabled(true);

        webEngine.getLoadWorker().stateProperty().addListener((o,old,s)->{
            if (s == Worker.State.SUCCEEDED) inject();
        });
    }

    public void initialize(String page, Map<String,String> params) {
        initParams = params != null ? params : new HashMap<>();
        javaBridge = new JavaBridge(webEngine, initParams, this);
        settingsBridge = new SettingsBridge(webEngine, this::getWindow);
        load(page);
    }

    private void inject() {
        JSObject win = (JSObject) webEngine.executeScript("window");
        win.setMember("javaBridge", javaBridge);
        win.setMember("settingsBridge", settingsBridge);
        webEngine.executeScript("if(window.onBridgeReady) onBridgeReady();");
    }

    @Override
    public void load(String page) {
        URL url = getClass().getResource("/com/appmsg/front/appmensajeriafront/" + page);
        if (url != null) webEngine.load(url.toExternalForm());
    }

    public javafx.stage.Window getWindow() {
        return webView.getScene() != null ? webView.getScene().getWindow() : null;
    }
}
