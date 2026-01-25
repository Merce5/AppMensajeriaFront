package com.appmsg.front.appmensajeriafront.ui.main;

import com.appmsg.front.appmensajeriafront.util.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML private StackPane contentPane;

    @FXML
    public void onOpenSettings() {
        // Tu pantalla de ajustes
        Navigator.load("/settings-view.fxml");
    }

    public void onNewChat(ActionEvent actionEvent) {
        //Implementar lógica de nuevo chat
    }
}
