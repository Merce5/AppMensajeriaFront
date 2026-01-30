package com.appmsg.front.appmensajeriafront.webview;

import com.appmsg.front.appmensajeriafront.service.SettingsBridge;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Window;
import netscape.javascript.JSObject;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class WebViewManager implements PageLoader {

    private final WebView webView;
    private final WebEngine webEngine;

    private JavaBridge bridge;
    private SettingsBridge settingsBridge;
    private Map<String, String> initParams = new HashMap<>();

    public WebViewManager(WebView webView) {
        this.webView = webView;
        this.webEngine = webView.getEngine();
        setupWebEngine();
    }

    @Override
    public void load(String page) {
        loadPage(page);
    }

    private void setupWebEngine() {
        webEngine.setJavaScriptEnabled(true);
        webEngine.setCreatePopupHandler(param -> null);

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                injectBridges();
            }
        });

        webEngine.setOnError(event -> System.err.println("WebView Error: " + event.getMessage()));
    }

    public void initialize(String htmlPage, Map<String, String> params) {
        this.initParams = (params != null) ? params : new HashMap<>();

        this.bridge = new JavaBridge(webEngine, initParams, this);

        this.settingsBridge = new SettingsBridge(webEngine, this::getWindow);

        loadPage(htmlPage);
    }

    public void loadPage(String htmlPage) {
        String resourcePath = "/com/appmsg/front/appmensajeriafront/webview/" + htmlPage;
        URL url = getClass().getResource(resourcePath);
        if (url != null) {
            webEngine.load(url.toExternalForm());
        } else {
            System.err.println("No se encontró el recurso: " + resourcePath);
        }
    }

    private void injectBridges() {
        if (bridge == null) {
            bridge = new JavaBridge(webEngine, initParams, this);
        }
        if (settingsBridge == null) {
            settingsBridge = new SettingsBridge(webEngine, this::getWindow);
        }

        JSObject window = (JSObject) webEngine.executeScript("window");
        window.setMember("javaBridge", bridge);
        window.setMember("settingsBridge", settingsBridge);

        webEngine.executeScript("if(typeof onBridgeReady === 'function') { onBridgeReady(); }");
    }

    private Window getWindow() {
        return webView.getScene() != null ? webView.getScene().getWindow() : null;
    }

    public void cleanup() {
        if (bridge != null) bridge.cleanup();
    }

    public WebView getWebView() { return webView; }
    public WebEngine getWebEngine() { return webEngine; }
    public JavaBridge getBridge() { return bridge; }
}
