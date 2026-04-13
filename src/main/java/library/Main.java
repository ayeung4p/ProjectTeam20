package library;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    /** The primary JavaFX Stage, made available statically. */
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        // Show the Home (role‐select) first, not the Login screen directly:
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/Home.fxml")));
        stage.setTitle("Library Management System");
        stage.setScene(new Scene(root, 640, 480));
        stage.show();
    }

    /** @return the primary Stage for file-choosers, new windows, etc. */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
