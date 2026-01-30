package com.appmsg.front.appmensajeriafront;

import com.appmsg.front.appmensajeriafront.session.Session;
import com.appmsg.front.appmensajeriafront.util.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        Session.setUserId("693ecc27684c0747c057fa31"); // Hardcoded user ID esto es lo que teneis que setear a la hora de iniciar sesion

        FXMLLoader loader = new FXMLLoader(
                HelloApplication.class.getResource("main-view.fxml")
        );

        Scene scene = new Scene(loader.load(), 900, 600);

        ThemeManager.apply(scene, ThemeManager.Theme.LIGHT); // o DARK por defecto

        stage.setTitle("App Mensajería");
        stage.setScene(scene);
        stage.show();
    }
}
