package com.appmsg.front.appmensajeriafront.webview;

import com.appmsg.front.appmensajeriafront.service.LoginService;
import com.appmsg.front.appmensajeriafront.model.InviteResponse;
import com.appmsg.front.appmensajeriafront.model.UploadResponse;
import com.appmsg.front.appmensajeriafront.model.UserDto;
import com.appmsg.front.appmensajeriafront.model.UserProfile;
import com.appmsg.front.appmensajeriafront.service.ChatWebSocketClient;
import com.appmsg.front.appmensajeriafront.service.FileUploadService;
import com.appmsg.front.appmensajeriafront.service.InviteService;
import com.appmsg.front.appmensajeriafront.service.ProfileService;
import com.appmsg.front.appmensajeriafront.session.Session;
import com.appmsg.front.appmensajeriafront.ui.chat.ChatController;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class JavaBridge {

    private final WebEngine webEngine;
    private final Gson gson;
    private final Map<String, String> initParams;

    private final FileUploadService uploadService;
    private final InviteService inviteService;
    private final ProfileService profileService;
    private final ChatController chatController;

    private ChatWebSocketClient wsClient;
    private final PageLoader pageLoader;
    private LoginService gateway;

    // constructor
    public JavaBridge(WebViewManager webViewManager, Map<String, String> initParams, PageLoader pageLoader) {
        this.webEngine = webViewManager.getWebEngine();
        this.initParams = initParams;
        this.pageLoader = pageLoader;
        this.gson = new Gson();

        this.uploadService = new FileUploadService();
        this.inviteService = new InviteService();
        this.profileService = new ProfileService();
        this.chatController = new ChatController(webViewManager);
        this.gateway = new LoginService();
    }


    // ===== Session / Init =====

    public String getUserId() {
        return Session.getUserId();
    }

    public String getInitParams() {
        try {
            return gson.toJson(initParams);
        } catch (Exception e) {
            return "{}";
        }
    }

    // ===== Auth =====

    public void tryToLogin(String username, String password) throws IOException, InterruptedException {
        var user = new UserDto(username, password);
        var loginResult = gateway.login(user);
        if (loginResult.getUserId() == null) {
            // todo
            return;
        }
        Session.setUserId(loginResult.getUserId());
        chatController.loadIndex();
    }

    // ===== Chat (WS) =====

    public void connectToChat(String chatId) {
        String userId = Session.getUserId();

        if (userId == null || userId.isBlank() || chatId == null || chatId.isBlank()) {
            callJsFunction("onConnectionStatusChanged", "false");
            return;
        }

        wsClient = new ChatWebSocketClient();

        wsClient.setOnMessage(messageJson ->
                Platform.runLater(() -> callJsFunction("onMessageReceived", messageJson))
        );

        wsClient.setOnTyping(typingJson ->
                Platform.runLater(() -> callJsFunction("onTypingReceived", typingJson))
        );

        wsClient.setOnConnected(() ->
                Platform.runLater(() -> callJsFunction("onConnectionStatusChanged", "true"))
        );

        wsClient.setOnDisconnected(() ->
                Platform.runLater(() -> callJsFunction("onConnectionStatusChanged", "false"))
        );

        wsClient.connect(chatId, userId);
    }

    public void disconnectChat() {
        if (wsClient != null) wsClient.disconnect();
    }

    // Firma real: sendMessage(String, List<String> multimedia)
    public void sendMessage(String text, String multimediaJson) {
        if (wsClient == null) return;

        List<String> multimedia = new ArrayList<>();
        if (multimediaJson != null && !multimediaJson.isBlank()) {
            try {
                multimedia = gson.fromJson(multimediaJson, List.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        wsClient.sendMessage(text, multimedia);
    }

    public void sendTyping(boolean isTyping) {
        if (wsClient != null) wsClient.sendTyping(isTyping);
    }

    // ===== Upload =====

    // Antes llamabas upload(File). Aquí se adapta a uploadFiles(List<File>)
    public void uploadFile(String filePath, String callbackFunction) {
        CompletableFuture.runAsync(() -> {
            try {
                File file = new File(filePath);
                UploadResponse response = uploadService.uploadFiles(Arrays.asList(file));
                String responseJson = gson.toJson(response);
                Platform.runLater(() -> callJsFunction(callbackFunction, responseJson));
            } catch (Exception e) {
                e.printStackTrace();
                String errorJson = "{\"success\":false,\"error\":\"" + escape(e.getMessage()) + "\"}";
                Platform.runLater(() -> callJsFunction(callbackFunction, errorJson));
            }
        });
    }

    // ===== Invite =====

    public void getInviteInfo(String inviteCode, String callbackFunction) {
        CompletableFuture.runAsync(() -> {
            try {
                JsonObject info = inviteService.getInviteInfo(inviteCode);
                String json = gson.toJson(info);
                Platform.runLater(() -> callJsFunction(callbackFunction, json));
            } catch (Exception e) {
                e.printStackTrace();
                String errorJson = "{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}";
                Platform.runLater(() -> callJsFunction(callbackFunction, errorJson));
            }
        });
    }

    public void joinByCode(String inviteCode, String callbackFunction) {
        CompletableFuture.runAsync(() -> {
            try {
                String userId = Session.getUserId();
                InviteResponse response = inviteService.joinByCode(inviteCode, userId);
                String json = gson.toJson(response);
                Platform.runLater(() -> callJsFunction(callbackFunction, json));
            } catch (Exception e) {
                e.printStackTrace();
                String errorJson = "{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}";
                Platform.runLater(() -> callJsFunction(callbackFunction, errorJson));
            }
        });
    }

    // Compat: si algún JS antiguo llamaba sendInvite(email,...), lo tratamos como "join por código"
    public void sendInvite(String inviteCode, String callbackFunction) {
        joinByCode(inviteCode, callbackFunction);
    }

    // ===== Profile =====

    public void getProfile(String callbackFunction) {
        CompletableFuture.runAsync(() -> {
            try {
                UserProfile profile = profileService.getProfile(Session.getUserId());
                String json = gson.toJson(profile);
                Platform.runLater(() -> callJsFunction(callbackFunction, json));
            } catch (Exception e) {
                e.printStackTrace();
                String errorJson = "{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}";
                Platform.runLater(() -> callJsFunction(callbackFunction, errorJson));
            }
        });
    }

    public void getProfileByUsername(String username, String callbackFunction) {
        CompletableFuture.runAsync(() -> {
            try {
                UserProfile profile = profileService.getProfileByUsername(username);
                String json = gson.toJson(profile);
                Platform.runLater(() -> callJsFunction(callbackFunction, json));
            } catch (Exception e) {
                e.printStackTrace();
                String errorJson = "{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}";
                Platform.runLater(() -> callJsFunction(callbackFunction, errorJson));
            }
        });
    }

    // No existe updateProfile en ProfileService (por ahora). Mantengo método para no romper JS.
    public void updateProfile(String profileJson, String callbackFunction) {
        String errorJson = "{\"success\":false,\"message\":\"updateProfile no implementado en backend\"}";
        Platform.runLater(() -> callJsFunction(callbackFunction, errorJson));
    }

    // ===== Navigation (SPA) =====

    public void navigate(String page) {
        if (page == null || page.isBlank()) return;

        // Si parece un documento, cargamos HTML completo (sirve para login -> index)
        if (page.endsWith(".html")) {
            Platform.runLater(() -> pageLoader.load(page));
            return;
        }

        // Si no, asumimos "ruta SPA" dentro de index
        navigateTo(page);
    }

    public void navigateTo(String spaPageName) {
        Platform.runLater(() ->
                webEngine.executeScript(
                        "if(typeof loadPage === 'function') { loadPage('" + escape(spaPageName) + "'); }"
                )
        );
    }

    public void goBack() {
        Platform.runLater(() ->
                webEngine.executeScript("if(typeof goBack === 'function') { goBack(); }")
        );
    }

    // ===== Log =====

    public void log(String message) {
        System.out.println("[JS] " + message);
    }

    // ===== Util =====

    private void callJsFunction(String functionName, String jsonParam) {
        String escapedJson = escape(jsonParam);
        String script = "if(typeof " + functionName + " === 'function') { " +
                functionName + "('" + escapedJson + "'); }";
        webEngine.executeScript(script);
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'");
    }

    public void cleanup() {
        disconnectChat();
    }
}
