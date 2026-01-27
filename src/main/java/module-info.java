module com.appmsg.front.appmensajeriafront {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires jdk.jsobject; // Para JSObject (bridge Java-JS)

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;

    requires com.google.gson;
    requires java.net.http;

    opens com.appmsg.front.appmensajeriafront to javafx.fxml;
    opens com.appmsg.front.appmensajeriafront.ui.settings to javafx.fxml;
    opens com.appmsg.front.appmensajeriafront.ui.main to javafx.fxml;
    opens com.appmsg.front.appmensajeriafront.ui.chat to javafx.fxml;

    // Para Gson (DTOs)
    opens com.appmsg.front.appmensajeriafront.model to com.google.gson;

    // Para WebView bridge
    opens com.appmsg.front.appmensajeriafront.webview to javafx.fxml, javafx.web;
    exports com.appmsg.front.appmensajeriafront.webview;

    exports com.appmsg.front.appmensajeriafront;
    exports com.appmsg.front.appmensajeriafront.ui.main;
    exports com.appmsg.front.appmensajeriafront.ui.chat;
}
