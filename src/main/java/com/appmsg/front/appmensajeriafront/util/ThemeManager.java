package com.appmsg.front.appmensajeriafront.util;

import javafx.scene.Scene;

public class ThemeManager {

    public enum Theme { LIGHT, DARK }

    public static void apply(Scene scene, Theme theme) {
        scene.getStylesheets().clear();

        String base = ThemeManager.class.getResource(
                "/com/appmsg/front/appmensajeriafront/styles/base.css"
        ).toExternalForm();

        String themeCss = ThemeManager.class.getResource(
                theme == Theme.DARK
                        ? "/com/appmsg/front/appmensajeriafront/styles/dark.css"
                        : "/com/appmsg/front/appmensajeriafront/styles/light.css"
        ).toExternalForm();

        scene.getStylesheets().addAll(base, themeCss);
    }

}
