package com.appmsg.front.appmensajeriafront.ui.settings;

import com.appmsg.front.appmensajeriafront.model.UserSettingsDto;
import com.appmsg.front.appmensajeriafront.service.SettingsService;
import com.appmsg.front.appmensajeriafront.session.Session;
import com.appmsg.front.appmensajeriafront.util.Navigator;
import com.appmsg.front.appmensajeriafront.util.ThemeManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class SettingsController {

    @FXML private CheckBox darkModeCheck;
    @FXML private ComboBox<String> wallpaperCombo;
    @FXML private TextField displayNameField;
    @FXML private TextField statusField;
    @FXML private Label statusLabel;

    private final SettingsService service = new SettingsService();

    // Guardamos el último wallpaper elegido
    private String wallpaperPath;

    @FXML
    public void initialize() {
        // Opciones simples para probar
        wallpaperCombo.getItems().addAll(
                "/com/appmsg/front/appmensajeriafront/wallpapers/wp1.jpg",
                "/com/appmsg/front/appmensajeriafront/wallpapers/wp2.jpg",
                "/com/appmsg/front/appmensajeriafront/wallpapers/wp3.jpg"
        );

        // Cambios en caliente del tema + guardar
        darkModeCheck.selectedProperty().addListener((obs, oldV, newV) -> {
            Scene scene = darkModeCheck.getScene();
            if (scene != null) {
                ThemeManager.apply(scene, newV ? ThemeManager.Theme.DARK : ThemeManager.Theme.LIGHT);
            }
        });

        wallpaperCombo.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) wallpaperPath = newV;
        });

        loadFromBackend();
    }

    private void loadFromBackend() {
        String userId = Session.getUserId(); // TODO: esto lo debe setear Merce al login
        if (userId == null || userId.isBlank()) {
            statusLabel.setText("No hay userId en sesión.");
            return;
        }

        statusLabel.setText("Cargando ajustes...");

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return service.getSettings(userId);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .thenAccept(settings -> Platform.runLater(() -> {
                    if (settings == null) {
                        statusLabel.setText("No había ajustes guardados aún. Puedes crear los tuyos.");
                        return;
                    }

                    darkModeCheck.setSelected(settings.darkMode);
                    wallpaperPath = settings.wallpaperPath;
                    if (wallpaperPath != null) wallpaperCombo.setValue(wallpaperPath);

                    displayNameField.setText(settings.displayName != null ? settings.displayName : "");
                    statusField.setText(settings.status != null ? settings.status : "");

                    statusLabel.setText("Ajustes cargados.");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Error cargando ajustes: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    public void onChooseWallpaper(ActionEvent e) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Elige un fondo");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fc.showOpenDialog(darkModeCheck.getScene().getWindow());
        if (file != null) {
            // Por ahora guardamos ruta local
            wallpaperPath = file.toURI().toString();
            wallpaperCombo.setValue(wallpaperPath);
            statusLabel.setText("Fondo seleccionado.");
        }
    }

    @FXML
    public void onSave(ActionEvent e) {
        String userId = Session.getUserId(); // TODO: viene del login
        if (userId == null || userId.isBlank()) {
            statusLabel.setText("No hay userId en sesión.");
            return;
        }

        UserSettingsDto dto = new UserSettingsDto();
        dto.userId = userId;
        dto.darkMode = darkModeCheck.isSelected();
        dto.wallpaperPath = wallpaperPath;
        dto.displayName = displayNameField.getText();
        dto.status = statusField.getText();

        statusLabel.setText("Guardando...");

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return service.saveSettings(dto);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .thenAccept(saved -> Platform.runLater(() -> statusLabel.setText("Guardado.")))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Error guardando: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    public void onBack() {
        Navigator.back();
    }

}
