package com.appmsg.front.appmensajeriafront.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * Cliente WebSocket para chat en tiempo real.
 */
public class ChatWebSocketClient implements WebSocket.Listener {

    private static final String WS_BASE_URL = "ws://localhost:8080/APPMensajeriaUEM/chat";

    private final Gson gson;
    private final HttpClient httpClient;
    private WebSocket webSocket;
    private StringBuilder messageBuffer;

    // Callbacks
    private Consumer<String> onMessageCallback;
    private Consumer<String> onTypingCallback;
    private Runnable onConnectedCallback;
    private Runnable onDisconnectedCallback;

    public ChatWebSocketClient() {
        this.gson = new Gson();
        this.httpClient = HttpClient.newHttpClient();
        this.messageBuffer = new StringBuilder();
    }

    /**
     * Conecta al WebSocket del chat.
     */
    public void connect(String chatId, String userId) {
        String wsUrl = WS_BASE_URL + "/" + chatId + "/" + userId;

        try {
            httpClient.newWebSocketBuilder()
                    .buildAsync(URI.create(wsUrl), this)
                    .thenAccept(ws -> {
                        this.webSocket = ws;
                        System.out.println("WebSocket conectado: " + wsUrl);
                    })
                    .exceptionally(ex -> {
                        System.err.println("Error conectando WebSocket: " + ex.getMessage());
                        if (onDisconnectedCallback != null) {
                            onDisconnectedCallback.run();
                        }
                        return null;
                    });
        } catch (Exception e) {
            System.err.println("Error creando WebSocket: " + e.getMessage());
        }
    }

    /**
     * Desconecta del WebSocket.
     */
    public void disconnect() {
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Closing");
            webSocket = null;
        }
    }

    /**
     * Envía un mensaje de chat.
     */
    public void sendMessage(String text, List<String> multimedia) {
        if (webSocket == null) return;

        JsonObject msg = new JsonObject();
        msg.addProperty("type", "message");
        msg.addProperty("message", text);
        msg.add("multimedia", gson.toJsonTree(multimedia));

        webSocket.sendText(gson.toJson(msg), true);
    }

    /**
     * Envía indicador de escritura.
     */
    public void sendTyping(boolean isTyping) {
        if (webSocket == null) return;

        JsonObject msg = new JsonObject();
        msg.addProperty("type", "typing");
        msg.addProperty("isTyping", isTyping);

        webSocket.sendText(gson.toJson(msg), true);
    }

    // ==================== CALLBACKS SETTERS ====================

    public void setOnMessage(Consumer<String> callback) {
        this.onMessageCallback = callback;
    }

    public void setOnTyping(Consumer<String> callback) {
        this.onTypingCallback = callback;
    }

    public void setOnConnected(Runnable callback) {
        this.onConnectedCallback = callback;
    }

    public void setOnDisconnected(Runnable callback) {
        this.onDisconnectedCallback = callback;
    }

    // ==================== WEBSOCKET LISTENER ====================

    @Override
    public void onOpen(WebSocket webSocket) {
        System.out.println("WebSocket abierto");
        if (onConnectedCallback != null) {
            onConnectedCallback.run();
        }
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        messageBuffer.append(data);

        if (last) {
            String message = messageBuffer.toString();
            messageBuffer = new StringBuilder();
            handleMessage(message);
        }

        webSocket.request(1);
        return null;
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        System.out.println("WebSocket cerrado: " + statusCode + " - " + reason);
        if (onDisconnectedCallback != null) {
            onDisconnectedCallback.run();
        }
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.err.println("WebSocket error: " + error.getMessage());
        if (onDisconnectedCallback != null) {
            onDisconnectedCallback.run();
        }
    }

    // ==================== MANEJO DE MENSAJES ====================

    private void handleMessage(String message) {
        try {
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String type = json.has("type") ? json.get("type").getAsString() : "message";

            switch (type) {
                case "typing":
                    if (onTypingCallback != null) {
                        onTypingCallback.accept(message);
                    }
                    break;
                case "message":
                case "user_connected":
                case "user_disconnected":
                case "status":
                default:
                    if (onMessageCallback != null) {
                        onMessageCallback.accept(message);
                    }
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error parseando mensaje: " + e.getMessage());
            // Pasar mensaje crudo al callback
            if (onMessageCallback != null) {
                onMessageCallback.accept(message);
            }
        }
    }

    /**
     * Verifica si está conectado.
     */
    public boolean isConnected() {
        return webSocket != null;
    }
}
