module com.hotel {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.hotel to javafx.fxml;
    opens com.hotel.controller to javafx.fxml;
    opens com.hotel.model to javafx.base;

    exports com.hotel;
    exports com.hotel.controller;
    exports com.hotel.model;
    exports com.hotel.service;
    exports com.hotel.dao;
    exports com.hotel.util;
}
