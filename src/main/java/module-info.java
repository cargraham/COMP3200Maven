module com.emailclient.emailclientv3 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires javafx.graphics;
    requires java.datatransfer;
    requires javafx.web;
    requires com.microsoft.graph;
    requires java.desktop;
    requires org.jsoup;
    requires com.microsoft.graph.core;
    requires okhttp3;
    requires com.azure.identity;

    opens com.emailclient.emailclientv3.Controller to javafx.fxml;
    exports com.emailclient.emailclientv3.Controller to javafx.fxml;
    exports com.emailclient.emailclientv3.View;
    exports com.emailclient.emailclientv3.Model;
}