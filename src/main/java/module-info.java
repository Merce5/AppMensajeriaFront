module com.appmsg.front.appmensajeriafront {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;

    requires com.google.gson;
    requires java.net.http; // <-- necesario si SettingsService usa HttpClient

    opens com.appmsg.front.appmensajeriafront to javafx.fxml;

    // ✅ ESTO ES LO QUE TE FALTABA (para que cargue settings-view.fxml)
    opens com.appmsg.front.appmensajeriafront.ui.settings to javafx.fxml;

    // ✅ recomendado para Gson (DTOs)
    opens com.appmsg.front.appmensajeriafront.model to com.google.gson;

    opens com.appmsg.front.appmensajeriafront.ui.main to javafx.fxml;


    exports com.appmsg.front.appmensajeriafront;
    exports com.appmsg.front.appmensajeriafront.ui.main;
}
