package com.appmsg.front.appmensajeriafront.util;

import com.appmsg.front.appmensajeriafront.ui.main.MainController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.util.Stack;

public class Navigator {

    private static StackPane contentPane;
    private static final Stack<Parent> history = new Stack<>();
    private static MainController mainControllerInstance;

    // ✅ pantalla por defecto para volver si el historial está vacío
    private static String homeFxml = null;

    public static void setContentPane(StackPane pane) {
        contentPane = pane;
    }

    public static void setMainController(MainController controller) {
        mainControllerInstance = controller;
    }

    // ✅ define cuál es la "home" (solo una vez, al arrancar)
    public static void setHome(String fxmlAbsolutePath) {
        homeFxml = fxmlAbsolutePath;
    }

    public static void load(String fxmlAbsolutePath) {
        if (contentPane == null) {
            throw new IllegalStateException("Navigator contentPane no inicializado");
        }

        try {
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource(fxmlAbsolutePath));
            Parent view = loader.load();

            // guardamos la vista actual antes de cambiar
            if (!contentPane.getChildren().isEmpty() && contentPane.getChildren().get(0) instanceof Parent) {
                history.push((Parent) contentPane.getChildren().get(0));
            }

            contentPane.getChildren().setAll(view);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo cargar FXML: " + fxmlAbsolutePath, e);
        }
    }

    public static void back() {
        if (contentPane == null) return;

        // ✅ si hay historial, volvemos
        if (!history.isEmpty()) {
            Parent previous = history.pop();
            contentPane.getChildren().setAll(previous);

            // Si hemos vuelto a la vista principal (historial vacio), refrescar
            if (history.isEmpty() && mainControllerInstance != null) {
                mainControllerInstance.refresh();
            }
            return;
        }

        // ✅ si no hay historial, volvemos a home si existe
        if (homeFxml != null) {
            load(homeFxml);
        }
    }
}
