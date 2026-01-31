package com.appmsg.front.appmensajeriafront.ui.chat;

import com.appmsg.front.appmensajeriafront.session.Session;
import com.appmsg.front.appmensajeriafront.webview.WebViewManager;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller para la vista de chat con WebView.
 */
public class ChatController {

    @FXML
    private WebView webView;

    private WebViewManager webViewManager;

    public ChatController(WebViewManager manager) {
        this.webViewManager = manager;
        this.webView = manager.getWebView();
    }

    public ChatController() {
        // CDI
    }

    @FXML
    public void initialize() {
        webViewManager = new WebViewManager(webView);
    }

    /**
     * Carga index.html como pagina principal.
     */
    public void loadIndex() {
        Map<String, String> params = new HashMap<>();
        params.put("userId", Session.getUserId());
        webViewManager.initialize("main.html", params);
    }

    /**
     * Inicializa el chat con un chatId específico.
     */
    public void initializeChat(String chatId) {
        Session.setChatId(chatId);

        Map<String, String> params = new HashMap<>();
        params.put("chatId", chatId);
        params.put("userId", Session.getUserId());

        webViewManager.initialize("chat.html", params);
    }

    /**
     * Abre la vista de perfil de un usuario.
     */
    public void openProfile(String userId) {
        Map<String, String> params = new HashMap<>();
        params.put("profileUserId", userId);

        webViewManager.initialize("profile.html", params);
    }

    /**
     * Abre la vista de invitación.
     */
    public void openInvite() {
        Map<String, String> params = new HashMap<>();

        webViewManager.initialize("invite.html", params);
    }

    /**
     * Limpia recursos al cerrar.
     */
    public void cleanup() {
        if (webViewManager != null) {
            webViewManager.cleanup();
        }
    }

    /**
     * Obtiene el WebViewManager.
     */
    public WebViewManager getWebViewManager() {
        return webViewManager;
    }
}
