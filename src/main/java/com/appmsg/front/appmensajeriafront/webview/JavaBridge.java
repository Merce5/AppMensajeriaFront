package com.appmsg.front.appmensajeriafront.webview;
import com.appmsg.front.appmensajeriafront.config.ApiConfig;
import com.appmsg.front.appmensajeriafront.model.*;
import com.appmsg.front.appmensajeriafront.model.auth.LoginRS;
import com.appmsg.front.appmensajeriafront.service.*;
import com.appmsg.front.appmensajeriafront.session.Session;
import com.appmsg.front.appmensajeriafront.ui.chat.ChatController;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class JavaBridge {

    private final WebViewManager webViewManager;
    private final WebEngine webEngine;
    private final Gson gson;
    private final Map<String, String> initParams;

    private final FileUploadService uploadService;
    private final InviteService inviteService;
    private final ProfileService profileService;
    private final ChatController chatController;
    private final ChatService chatService;

    private ChatWebSocketClient wsClient;
    private final PageLoader pageLoader;
    private final LoginService loginService;
    private final SettingsService settingsService;

    // constructor
    public JavaBridge(WebViewManager webViewManager, Map<String, String> initParams, PageLoader pageLoader) {
        this.webViewManager = webViewManager;
        this.webEngine = webViewManager.getWebEngine();
        this.initParams = initParams;
        this.pageLoader = pageLoader;
        this.gson = new Gson();
        this.uploadService = new FileUploadService();
        this.inviteService = new InviteService();
        this.profileService = new ProfileService();
        this.chatController = new ChatController(webViewManager);
        this.loginService = new LoginService();
        this.chatService = new ChatService();
        this.settingsService = new SettingsService();
    }

    // ===== Settings (expuestos a JS) =====
    public String getWallpapersJson() {
        return gson.toJson(com.appmsg.front.appmensajeriafront.service.WallpaperProvider.getWallpaperUrls());
    }

    public void loadSettings() {
        String userId = Session.getUserId();
        if (userId == null) {
            Platform.runLater(() -> callJsFunction("onSettingsError", "No hay sesión"));
            return;
        }
        CompletableFuture.supplyAsync(() -> {
                    try { return settingsService.getSettings(userId); }
                    catch (Exception e) { throw new RuntimeException(e); }
                })
                .thenAccept(dto -> Platform.runLater(() ->
                        callJsFunctionObj("onSettingsLoaded", gson.toJson(dto))
                ))
                .exceptionally(ex -> {
                    Platform.runLater(() -> callJsFunction("onSettingsError", ex.getMessage()));
                    return null;
                });
    }

    public void saveSettings(String json) {
        com.appmsg.front.appmensajeriafront.model.UserSettingsDto dto = gson.fromJson(json, com.appmsg.front.appmensajeriafront.model.UserSettingsDto.class);
        dto.userId = Session.getUserId();
        CompletableFuture.supplyAsync(() -> {
                    try { return settingsService.saveSettings(dto); }
                    catch (Exception e) { throw new RuntimeException(e); }
                })
                .thenAccept(saved -> Platform.runLater(() ->
                        callJsFunctionObj("onSettingsSaved", gson.toJson(saved))
                ))
                .exceptionally(ex -> {
                    Platform.runLater(() -> callJsFunction("onSettingsError", ex.getMessage()));
                    return null;
                });
    }

    public void chooseWallpaper() {
        Window w = webViewManager.getWebView().getScene() != null
                ? webViewManager.getWebView().getScene().getWindow()
                : null;
        if (w == null) return;
        FileChooser fc = new FileChooser();
        fc.setTitle("Elegir fondo");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png","*.jpg","*.jpeg")
        );
        File f = fc.showOpenDialog(w);
        if (f != null) {
            Platform.runLater(() -> callJsFunctionObj("onWallpaperChosen",
                    gson.toJson(Map.of("uri", f.toURI().toString()))));
        }
    }

    // Utilidad para pasar objetos JSON a JS (parseados)
    private void callJsFunctionObj(String fn, String json) {
        String s = escape(json);
        webEngine.executeScript(
                "if(typeof " + fn + "==='function'){ " + fn + "(JSON.parse('" + s + "')); }"
        );
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

    public String getChatId() {
        return Session.getChatId();
    }

    public void setChatId(String chatId) {
        Session.setChatId(chatId);
    }

    /**
     * Devuelve la URL base del backend para resolver rutas de archivos.
     */
    public String getBaseUrl() {
        String baseUrl = ApiConfig.BASE_API_URL;

        // Si la url tiene APPMensajeriaUEM_war_exploded la quitamos
        if (baseUrl.contains("APPMensajeriaUEM_war_exploded")) {
            baseUrl = baseUrl.replace("APPMensajeriaUEM_war_exploded", "");
        }

        return baseUrl;
    }

    // ===== Auth =====

    public void tryToLogin(String username, String password) throws IOException, InterruptedException {
        var user = new UserDto(username, password);
        LoginRS loginResult = loginService.login(user);
        if (loginResult.getUserId() == null || loginResult.getError() != null) {
            Platform.runLater(() -> callJsFunction("onErrorLoginResult", gson.toJson(loginResult)));
        } else {
            Session.setUserId(loginResult.getUserId());
//          chatController.loadIndex();
            navigate("main.html");
//          navigate("home.html");
//        }
        }

    }

    public void register(String username, String password) throws IOException, InterruptedException {
        var user = new UserDto(username, password);
        var response = loginService.register(user);
        if (response.getStatus().equals("error")) {
            Platform.runLater(() -> callJsFunction("onErrorLoginResult", gson.toJson(response)));
        } else {
            chatController.loadVerification();
        }
    }

    public void verifyRegister(String verificationCode) throws IOException, InterruptedException {
        var response = loginService.verifyRegister(verificationCode);
        if (response.getStatus().equals("error")) {
            Platform.runLater(() -> callJsFunction("onErrorLoginResult", gson.toJson(response)));
        } else {
            Session.setUserId(response.getMessage());
            chatController.loadIndex();
        }
    }

    // ===== Chat (WS) =====

    public void getChats() {
        CompletableFuture.runAsync(() -> {
            try {
                List<ChatListItemDto> chats = chatService.getChats(Session.getUserId());
                String json = gson.toJson(chats);
                Platform.runLater(() -> callJsFunction("onChatsReceived", json));
            } catch (Exception e) {
                e.printStackTrace();
                // En caso de error, podrías enviar una lista vacía o un objeto de error
                Platform.runLater(() -> callJsFunction("onChatsReceived", "[]"));
            }
        });
    }

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

    // ===== File Chooser =====

    /**
     * Abre el FileChooser nativo de JavaFX.
     * Este metodo se llama desde el FX Application Thread (via WebView bridge),
     * por lo que puede mostrar el dialogo directamente.
     *
     * @param filterType "images", "videos", "documents", o "all"
     * @return JSON array de paths absolutos, o null si se cancela
     */
    public String openFileChooser(String filterType) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar archivos");

            String filter = (filterType != null) ? filterType : "all";
            switch (filter) {
                case "images":
                    fileChooser.getExtensionFilters().add(
                            new FileChooser.ExtensionFilter("Imagenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
                    );
                    break;
                case "videos":
                    fileChooser.getExtensionFilters().add(
                            new FileChooser.ExtensionFilter("Videos", "*.mp4", "*.webm", "*.avi", "*.mov")
                    );
                    break;
                case "documents":
                    fileChooser.getExtensionFilters().add(
                            new FileChooser.ExtensionFilter("Documentos", "*.pdf", "*.doc", "*.docx", "*.xls", "*.xlsx", "*.txt")
                    );
                    break;
                default:
                    fileChooser.getExtensionFilters().addAll(
                            new FileChooser.ExtensionFilter("Todos los archivos", "*.*"),
                            new FileChooser.ExtensionFilter("Imagenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"),
                            new FileChooser.ExtensionFilter("Videos", "*.mp4", "*.webm"),
                            new FileChooser.ExtensionFilter("Documentos", "*.pdf", "*.doc", "*.docx", "*.txt")
                    );
            }

            // Obtener la ventana del WebView para el dialogo modal
            Window window = webViewManager.getWebView().getScene() != null
                    ? webViewManager.getWebView().getScene().getWindow()
                    : null;

            List<File> files = fileChooser.showOpenMultipleDialog(window);

            if (files == null || files.isEmpty()) {
                return null;
            }

            List<String> paths = new ArrayList<>();
            for (File file : files) {
                paths.add(file.getAbsolutePath());
            }
            return gson.toJson(paths);

        } catch (Exception e) {
            System.err.println("Error abriendo FileChooser: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ===== Upload =====

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

    /**
     * Sube multiples archivos al servidor (llamado desde bridge.js).
     * @param filesJson JSON array de paths absolutos
     * @param callbackFunction nombre de la funcion JS a invocar con el resultado
     */
    public void uploadFiles(String filesJson, String callbackFunction) {
        CompletableFuture.runAsync(() -> {
            try {
                List<String> paths = gson.fromJson(filesJson, List.class);
                List<File> files = new ArrayList<>();
                for (String path : paths) {
                    files.add(new File(path));
                }

                UploadResponse response = uploadService.uploadFiles(files);
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

        // Si no, asumimos "ruta SPA" dentro de index y cargamos el html
        Platform.runLater(() -> pageLoader.load(page + ".html"));
    }

    // ===== Open file externally =====

    /**
     * Abre una URL en el navegador del sistema o descarga un archivo.
     */
    public void openExternal(String url) {
        if (url == null || url.isBlank()) return;

        // Resolver URL relativa
        String fullUrl = url;
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            String urlBase = ApiConfig.BASE_API_URL;

            // Si la url tiene APPMensajeriaUEM_war_exploded la quitamos
            if (urlBase.contains("APPMensajeriaUEM_war_exploded")) {
                urlBase = urlBase.replace("APPMensajeriaUEM_war_exploded", "");
            }

            fullUrl = urlBase + (url.startsWith("/") ? url : "/" + url);
        }

        final String targetUrl = fullUrl;
        CompletableFuture.runAsync(() -> {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(targetUrl));
            } catch (Exception e) {
                System.err.println("Error abriendo URL externa: " + e.getMessage());
                e.printStackTrace();
            }
        });
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
