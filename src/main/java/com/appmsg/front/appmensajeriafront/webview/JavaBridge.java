package com.appmsg.front.appmensajeriafront.webview;

import com.appmsg.front.appmensajeriafront.model.InviteResponse;
import com.appmsg.front.appmensajeriafront.model.UploadResponse;
import com.appmsg.front.appmensajeriafront.model.UserProfile;
import com.appmsg.front.appmensajeriafront.service.ChatWebSocketClient;
import com.appmsg.front.appmensajeriafront.service.FileUploadService;
import com.appmsg.front.appmensajeriafront.service.InviteService;
import com.appmsg.front.appmensajeriafront.service.ProfileService;
import com.appmsg.front.appmensajeriafront.session.Session;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Bridge entre Java y JavaScript.
 * Todos los métodos públicos son accesibles desde JavaScript.
 */
public class JavaBridge {

    private final WebEngine webEngine;
    private final Gson gson;
    private final Map<String, String> initParams;

    private ChatWebSocketClient wsClient;
    private final FileUploadService uploadService;
    private final ProfileService profileService;
    private final InviteService inviteService;

    public JavaBridge(WebEngine webEngine, Map<String, String> initParams) {
        this.webEngine = webEngine;
        this.initParams = initParams;
        this.gson = new Gson();
        this.uploadService = new FileUploadService();
        this.profileService = new ProfileService();
        this.inviteService = new InviteService();
    }

    // ==================== DATOS DE SESIÓN ====================

    /**
     * Obtiene el userId de la sesión actual.
     */
    public String getUserId() {
        return Session.getUserId();
    }

    /**
     * Obtiene el chatId actual.
     */
    public String getChatId() {
        return Session.getChatId();
    }

    /**
     * Obtiene los parámetros de inicialización como JSON.
     */
    public String getInitParams() {
        return gson.toJson(initParams);
    }

    // ==================== CHAT / WEBSOCKET ====================

    /**
     * Conecta al WebSocket del chat.
     */
    public void connectToChat(String chatId) {
        String userId = Session.getUserId();
        if (userId == null || chatId == null) {
            callJsFunction("onConnectionStatusChanged", "false");
            return;
        }

        wsClient = new ChatWebSocketClient();

        // Configurar callbacks
        wsClient.setOnMessage(messageJson -> {
            Platform.runLater(() -> callJsFunction("onMessageReceived", messageJson));
        });

        wsClient.setOnTyping(typingJson -> {
            Platform.runLater(() -> callJsFunction("onTypingReceived", typingJson));
        });

        wsClient.setOnConnected(() -> {
            Platform.runLater(() -> callJsFunction("onConnectionStatusChanged", "true"));
        });

        wsClient.setOnDisconnected(() -> {
            Platform.runLater(() -> callJsFunction("onConnectionStatusChanged", "false"));
        });

        wsClient.connect(chatId, userId);
    }

    /**
     * Envía un mensaje de texto con multimedia opcional.
     */
    public void sendMessage(String text, String multimediaJson) {
        if (wsClient != null) {
            List<String> multimedia = new ArrayList<>();
            if (multimediaJson != null && !multimediaJson.isEmpty()) {
                try {
                    multimedia = gson.fromJson(multimediaJson, List.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            wsClient.sendMessage(text, multimedia);
        }
    }

    /**
     * Envía indicador de escritura.
     */
    public void sendTypingIndicator(boolean isTyping) {
        if (wsClient != null) {
            wsClient.sendTyping(isTyping);
        }
    }

    /**
     * Desconecta del chat.
     */
    public void disconnectChat() {
        if (wsClient != null) {
            wsClient.disconnect();
            wsClient = null;
        }
    }

    // ==================== FILE CHOOSER ====================

    /**
     * Abre el FileChooser nativo y retorna los paths seleccionados.
     * @param filterType "images", "videos", "documents", o "all"
     * @return JSON array de paths o null si se cancela
     */
    public String openFileChooser(String filterType) {
        AtomicReference<List<File>> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Seleccionar archivos");

                // Configurar filtros según tipo
                switch (filterType != null ? filterType : "all") {
                    case "images":
                        fileChooser.getExtensionFilters().add(
                                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
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
                                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"),
                                new FileChooser.ExtensionFilter("Videos", "*.mp4", "*.webm"),
                                new FileChooser.ExtensionFilter("Documentos", "*.pdf", "*.doc", "*.docx", "*.txt")
                        );
                }

                List<File> files = fileChooser.showOpenMultipleDialog(
                        webEngine.getLoadWorker().getState() == javafx.concurrent.Worker.State.SUCCEEDED
                                ? null
                                : null
                );
                result.set(files);
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

        List<File> files = result.get();
        if (files == null || files.isEmpty()) {
            return null;
        }

        List<String> paths = new ArrayList<>();
        for (File file : files) {
            paths.add(file.getAbsolutePath());
        }
        return gson.toJson(paths);
    }

    // ==================== UPLOAD ====================

    /**
     * Sube archivos al servidor (asíncrono con callback).
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
                String errorJson = "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}";
                Platform.runLater(() -> callJsFunction(callbackFunction, errorJson));
            }
        });
    }

    // ==================== PROFILE ====================

    /**
     * Carga el perfil de un usuario (asíncrono con callback).
     */
    public void loadProfile(String userId, String callbackFunction) {
        CompletableFuture.runAsync(() -> {
            try {
                UserProfile profile = profileService.getProfile(userId);
                String responseJson = gson.toJson(profile);

                Platform.runLater(() -> callJsFunction(callbackFunction, responseJson));
            } catch (Exception e) {
                e.printStackTrace();
                String errorJson = "{\"error\":\"" + e.getMessage() + "\"}";
                Platform.runLater(() -> callJsFunction(callbackFunction, errorJson));
            }
        });
    }

    // ==================== INVITE ====================

    /**
     * Une al usuario a un chat mediante código de invitación (asíncrono con callback).
     */
    public void joinByInvite(String inviteCode, String callbackFunction) {
        CompletableFuture.runAsync(() -> {
            try {
                String userId = Session.getUserId();
                InviteResponse response = inviteService.joinByCode(inviteCode, userId);
                String responseJson = gson.toJson(response);

                Platform.runLater(() -> callJsFunction(callbackFunction, responseJson));
            } catch (Exception e) {
                e.printStackTrace();
                String errorJson = "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}";
                Platform.runLater(() -> callJsFunction(callbackFunction, errorJson));
            }
        });
    }

    // ==================== NAVEGACIÓN ====================

    /**
     * Navega a otra página del WebView.
     */
    public void navigateTo(String page) {
        Platform.runLater(() -> {
            webEngine.executeScript("if(typeof loadPage === 'function') { loadPage('" + page + "'); }");
        });
    }

    /**
     * Vuelve a la página anterior (controlado por JS).
     */
    public void goBack() {
        Platform.runLater(() -> {
            webEngine.executeScript("if(typeof goBack === 'function') { goBack(); }");
        });
    }

    // ==================== LOG ====================

    /**
     * Log desde JavaScript (útil para debugging).
     */
    public void log(String message) {
        System.out.println("[JS] " + message);
    }

    // ==================== UTILIDADES INTERNAS ====================

    /**
     * Llama a una función JavaScript con un parámetro JSON.
     */
    private void callJsFunction(String functionName, String jsonParam) {
        String escapedJson = jsonParam.replace("\\", "\\\\").replace("'", "\\'");
        String script = "if(typeof " + functionName + " === 'function') { " + functionName + "('" + escapedJson + "'); }";
        webEngine.executeScript(script);
    }

    /**
     * Limpia recursos.
     */
    public void cleanup() {
        disconnectChat();
    }
}
