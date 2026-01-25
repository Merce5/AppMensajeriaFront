package com.appmsg.front.appmensajeriafront.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.util.Stack;

public class Navigator {

    private static StackPane contentPane;
    private static final Stack<Parent> history = new Stack<>();

    // pantalla por defecto para volver si el historial está vacío
    private static String homeFxml = null;

    public static void setContentPane(StackPane pane) {
        contentPane = pane;
    }

    // define cuál es la "home"
    public static void setHome(String fxmlAbsolutePath) {
        homeFxml = fxmlAbsolutePath;
    }

    public static void load(String fxmlAbsolutePath) {
        if (contentPane == null) {
            throw new IllegalStateException("Navigator contentPane no inicializado");
        }

        try {
            Parent view = FXMLLoader.load(Navigator.class.getResource(fxmlAbsolutePath));

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

        // si hay historial, volvemos
        if (!history.isEmpty()) {
            Parent previous = history.pop();
            contentPane.getChildren().setAll(previous);
            return;
        }

        // si no hay historial, volvemos a home si existe
        if (homeFxml != null) {
            load(homeFxml);
        }
    }
}
