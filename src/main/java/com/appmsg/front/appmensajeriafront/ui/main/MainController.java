package com.appmsg.front.appmensajeriafront.ui.main;

import com.appmsg.front.appmensajeriafront.util.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML private StackPane contentPane;

    @FXML
    public void initialize() {
        Navigator.setContentPane(contentPane);

        // define una home (la que sea)
        Navigator.setHome("/com/appmsg/front/appmensajeriafront/hello-view.fxml");

        // carga la home al arrancar, así ya hay "pantalla anterior"
        Navigator.load("/com/appmsg/front/appmensajeriafront/hello-view.fxml");
    }

    @FXML
    public void onOpenSettings() {
        // Tu pantalla de ajustes
        Navigator.load("/com/appmsg/front/appmensajeriafront/settings-view.fxml");
    }

    public void onNewChat(ActionEvent actionEvent) {
        //Implementar lógica de nuevo chat
    }
}
