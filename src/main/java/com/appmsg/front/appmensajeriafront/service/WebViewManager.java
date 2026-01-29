package com.appmsg.front.appmensajeriafront.service;

import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestiona el WebView y la comunicación con JavaScript.
 * Inyecta el JavaBridge cuando la página carga.
 */
public class WebViewManager {

    private final WebView webView;
    private final WebEngine webEngine;
    private JavaBridge bridge;
    private Map<String, String> initParams;

    public WebViewManager(WebView webView) {
        this.webView = webView;
        this.webEngine = webView.getEngine();
        this.initParams = new HashMap<>();
        setupWebEngine();
    }

    private void setupWebEngine() {
        webEngine.setJavaScriptEnabled(true);

        // Deshabilitar popups
        webEngine.setCreatePopupHandler(param -> null);

        // Inyectar bridge cuando la página carga
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                injectBridge();
            }
        });

        // Log de errores JavaScript
        webEngine.setOnError(event -> {
            System.err.println("WebView Error: " + event.getMessage());
        });
    }

    /**
     * Inicializa el WebView con una página HTML y parámetros.
     */
    public void initialize(String htmlPage, Map<String, String> params) {
        this.initParams = params != null ? params : new HashMap<>();
        this.bridge = new JavaBridge(webEngine, initParams);
        loadPage(htmlPage);
    }

    /**
     * Carga una página HTML desde los recursos.
     */
    public void loadPage(String htmlPage) {
        String resourcePath = "/com/appmsg/front/appmensajeriafront/" + htmlPage;
        URL url = getClass().getResource(resourcePath);
        if (url != null) {
            webEngine.load(url.toExternalForm());
        } else {
            System.err.println("No se encontró el recurso: " + resourcePath);
        }
    }

    /**
     * Inyecta el JavaBridge en el contexto JavaScript.
     */
    private void injectBridge() {
        if (bridge == null) {
            bridge = new JavaBridge(webEngine, initParams);
        }

        JSObject window = (JSObject) webEngine.executeScript("window");
        window.setMember("javaBridge", bridge);

        // Notificar a JS que el bridge está listo
        webEngine.executeScript("if(typeof onBridgeReady === 'function') { onBridgeReady(); }");
    }

    /**
     * Ejecuta JavaScript en el WebView.
     */
    public void executeScript(String script) {
        webEngine.executeScript(script);
    }

    /**
     * Obtiene el JavaBridge para configuración adicional.
     */
    public JavaBridge getBridge() {
        return bridge;
    }

    /**
     * Obtiene el WebView.
     */
    public WebView getWebView() {
        return webView;
    }

    /**
     * Obtiene el WebEngine.
     */
    public WebEngine getWebEngine() {
        return webEngine;
    }
}
