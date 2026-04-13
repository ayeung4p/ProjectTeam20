module library {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires kotlin.stdlib;
    requires opencsv;
    requires jbcrypt;
    requires javafx.base;
    requires com.github.librepdf.openpdf;
    requires javafx.graphics;

    opens library to javafx.fxml;
    opens library.book to javafx.base;
    opens library.user to javafx.base, javafx.fxml;
    opens library.controllers to javafx.fxml;

    exports library;
    exports library.book;
    exports library.controllers;
}