package com.appmsg.front.appmensajeriafront.ui.main;

import com.appmsg.front.appmensajeriafront.ui.chat.ChatController;
import com.appmsg.front.appmensajeriafront.util.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML private StackPane contentPane;

    private ChatController currentChatController;

    @FXML
    public void initialize() {
        Navigator.setContentPane(contentPane);

        // Cargar WebView con index.html por defecto
        loadWebViewHome();
    }

    /**
     * Carga el WebView con la pagina index.html por defecto.
     */
    private void loadWebViewHome() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/appmsg/front/appmensajeriafront/chat-view.fxml")
            );
            Parent view = loader.load();

            currentChatController = loader.getController();
            // Cargar index.html sin pagina especifica (mostrara el home/lista de chats)
            currentChatController.loadIndex();

            contentPane.getChildren().setAll(view);

        } catch (Exception e) {
            System.err.println("Error cargando WebView: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onOpenSettings() {
        // Tu pantalla de ajustes
        Navigator.load("/com/appmsg/front/appmensajeriafront/settings-view.fxml");
    }

    /**
     * Abre un chat con el chatId especificado.
     * Este metodo carga la vista de chat con WebView.
     */
    public void openChat(String chatId) {
        try {
            // Limpiar chat anterior si existe
            if (currentChatController != null) {
                currentChatController.cleanup();
            }

            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/appmsg/front/appmensajeriafront/chat-view.fxml")
            );
            Parent view = loader.load();

            // Obtener controller y configurar chatId
            currentChatController = loader.getController();
            currentChatController.initializeChat(chatId);

            // Guardar vista actual en historial
            if (!contentPane.getChildren().isEmpty()) {
                // Navigator maneja el historial internamente
            }

            contentPane.getChildren().setAll(view);

        } catch (Exception e) {
            System.err.println("Error abriendo chat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Abre la vista de invitacion para unirse a un chat.
     */
    public void openInvite() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/appmsg/front/appmensajeriafront/chat-view.fxml")
            );
            Parent view = loader.load();

            ChatController controller = loader.getController();
            controller.openInvite();

            contentPane.getChildren().setAll(view);

        } catch (Exception e) {
            System.err.println("Error abriendo invitacion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onNewChat(ActionEvent actionEvent) {
        // Aqui se podria abrir un dialogo para crear nuevo chat
        // o navegar a una vista de seleccion de contactos
    }

    /**
     * Obtiene el ChatController actual (si hay un chat abierto).
     */
    public ChatController getCurrentChatController() {
        return currentChatController;
    }
}
