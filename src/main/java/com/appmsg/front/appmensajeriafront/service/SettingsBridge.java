package com.appmsg.front.appmensajeriafront.service;

import com.appmsg.front.appmensajeriafront.model.UserSettingsDto;
import com.appmsg.front.appmensajeriafront.session.Session;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class SettingsBridge {

    private final WebEngine webEngine;
    private final Supplier<Window> windowSupplier;
    private final SettingsService service = new SettingsService();
    private final Gson gson = new Gson();

    public SettingsBridge(WebEngine webEngine, Supplier<Window> windowSupplier) {
        this.webEngine = webEngine;
        this.windowSupplier = windowSupplier;
    }

    // ======== LOAD ========
    public void loadSettings() {
        String userId = Session.getUserId();
        if (userId == null) {
            callJs("onSettingsError", "No hay sesión");
            return;
        }

        CompletableFuture
                .supplyAsync(() -> {
                    try { return service.getSettings(userId); }
                    catch (Exception e) { throw new RuntimeException(e); }
                })
                .thenAccept(dto -> Platform.runLater(() ->
                        callJsObj("onSettingsLoaded", gson.toJson(dto))
                ))
                .exceptionally(ex -> {
                    Platform.runLater(() -> callJs("onSettingsError", ex.getMessage()));
                    return null;
                });
    }

    // ======== SAVE ========
    public void saveSettings(String json) {
        UserSettingsDto dto = gson.fromJson(json, UserSettingsDto.class);
        dto.userId = Session.getUserId();

        CompletableFuture
                .supplyAsync(() -> {
                    try { return service.saveSettings(dto); }
                    catch (Exception e) { throw new RuntimeException(e); }
                })
                .thenAccept(saved -> Platform.runLater(() ->
                        callJsObj("onSettingsSaved", gson.toJson(saved))
                ))
                .exceptionally(ex -> {
                    Platform.runLater(() -> callJs("onSettingsError", ex.getMessage()));
                    return null;
                });
    }

    // ======== FILECHOOSER ========
    public void chooseWallpaper() {
        Window w = windowSupplier.get();
        if (w == null) return;

        FileChooser fc = new FileChooser();
        fc.setTitle("Elegir fondo");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png","*.jpg","*.jpeg")
        );

        File f = fc.showOpenDialog(w);
        if (f != null) {
            callJsObj("onWallpaperChosen",
                    gson.toJson(Map.of("uri", f.toURI().toString())));
        }
    }

    // ======== JS CALLBACKS ========
    private void callJs(String fn, String msg) {
        String s = escape(msg);
        webEngine.executeScript(
                "if(typeof " + fn + "==='function'){ " + fn + "('" + s + "'); }"
        );
    }

    private void callJsObj(String fn, String json) {
        String s = escape(json);
        webEngine.executeScript(
                "if(typeof " + fn + "==='function'){ " + fn + "(JSON.parse('" + s + "')); }"
        );
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\\","\\\\").replace("'","\\'");
    }
}
