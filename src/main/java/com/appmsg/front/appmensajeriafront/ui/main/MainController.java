package com.appmsg.front.appmensajeriafront.ui.main;
import com.appmsg.front.appmensajeriafront.ui.login.LoginController;
import com.appmsg.front.appmensajeriafront.util.Navigator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML private StackPane contentPane;

    private LoginController loginController;

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
                    getClass().getResource("/com/appmsg/front/appmensajeriafront/login-view.fxml")
            );
            Parent view = loader.load();

            loginController = loader.getController();
            // Cargar index.html sin pagina especifica (mostrara el home/lista de chats)
            loginController.loadLogin();

            contentPane.getChildren().setAll(view);

        } catch (Exception e) {
            System.err.println("Error cargando WebView: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
