package library.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import library.Main;

public class HomeController {

    @FXML
    private void handleStudent(ActionEvent event) {
        navigateToLogin("student");
    }

    @FXML
    private void handleAuthor(ActionEvent event) {
        navigateToLogin("author");
    }

    @FXML
    private void handleLibrarian(ActionEvent event) {
        navigateToLogin("librarian");
    }

    private void navigateToLogin(String role) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            // Pass the role to LoginController
            LoginController ctrl = loader.getController();
            ctrl.setRole(role);

            Stage stage = Main.getPrimaryStage();
            stage.setScene(new Scene(root, 640, 480));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
