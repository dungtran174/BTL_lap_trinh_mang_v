module com.ltm.countgame {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires java.sql;
    requires javafx.media;

//    opens client.controller to javafx.fxml;

    // exports client
    exports client.controller;
    exports client.helper;
    exports client.network;
    exports client.view;

    // exports server
    exports server.controller;
    exports server.dao;
    exports server.helper;
    exports server.network;
    exports server.view;

    // exports shared
    exports shared.dto;
    exports shared.model;

}