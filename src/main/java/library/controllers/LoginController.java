package library.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.util.Duration;
import library.Main;
import library.user.Role;
import library.user.User;

import java.io.IOException;
import java.util.Objects;

public class LoginController {
    @FXML private Label        headerLabel;
    @FXML private TextField    usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private ToggleButton viewPasswordBtn;
    @FXML private Label toastLabel;

    private String selectedRole;

    public void setRole(String role) {
        this.selectedRole = role;
        String role_name = switch (role.toLowerCase()) {
            case "student", "staff" -> "Student/Staff";
            case "author" -> "Author";
            case "librarian" -> "Librarian";
            default -> "";
        };
        headerLabel.setText(role_name + " Login");
    }

    public String getHeaderLabelText() {
        return headerLabel != null ? headerLabel.getText() : null;
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent home = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/Home.fxml")));
        Main.getPrimaryStage().setScene(new Scene(home, 640, 480));
    }

    /**
     * Log into the system and handle cases when the input is invalid.
     * If the user login successfully, this method will pass the
     * <code>User</code> object to the controller of the corresponding
     * dashboard.
     * @param event
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.isVisible() ? passwordField.getText() : passwordTextField.getText();

        // check if the user exists
        if (!User.usernameExists(username)) {
            showSimpleToast("Incorrect username or password.");
        } else {
            User currUser = User.selectUserByUsername(username);

            // check password
            if (currUser.checkPassword(password)) {
                String fxml;

                // check user status
                if(currUser.getStatus().toLowerCase().equals("deactivated")){
                    showSimpleToast("Your account has been deactivated. Please contact the librarian to reactivate it.");
                    return;
                }

                Role selected = Role.fromString(selectedRole);

                // check role
                if(!currUser.getRole().toLowerCase().equals(selected.toString().toLowerCase())){
                    showSimpleToast("You don't have access rights to this dashboard.");
                    return;
                }

                try {
                    FXMLLoader loader = new FXMLLoader();
                    Parent dash;

                    // display the correct dashboard according to the role
                    // pass the User object to the corresponding controller
                    switch (selectedRole.toLowerCase()) {
                        case "student":
                            fxml = "/fxml/StudentDashboard.fxml";
                            loader.setLocation(getClass().getResource(fxml));
                            dash = loader.load();

                            StudentDashboardController stuCtrl = loader.getController();
                            stuCtrl.setUser(currUser);
                            break;
                        case "author":
                            fxml = "/fxml/AuthorDashboard.fxml";
                            loader.setLocation(getClass().getResource(fxml));
                            dash = loader.load();

                            AuthorDashboardController auCtrl = loader.getController();
                            auCtrl.setUser(currUser);
                            break;
                        case "librarian":
                            fxml = "/fxml/LibrarianDashboard.fxml";
                            loader.setLocation(getClass().getResource(fxml));
                            dash = loader.load();

                            LibrarianDashboardController liCtrl = loader.getController();
                            liCtrl.setUser(currUser);
                            break;
                        default:
                            fxml = "/fxml/Home.fxml";
                            dash = FXMLLoader.load(getClass().getResource(fxml));
                            break;
                    }

                    Main.getPrimaryStage().setScene(new Scene(dash, 1000, 700));
                } catch (IOException e) {
                    e.printStackTrace();
                    showSimpleToast("Error loading dashboard.");
                }
            } else {
                showSimpleToast("Incorrect username or password.");
            }
        }
    }

    /**
     * Click the toggle button <code>viewPasswordBtn</code> to view the password.
     * @param event
     */
    @FXML
    private void handleViewPassword(ActionEvent event){
        if(viewPasswordBtn.isSelected()){
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordField.setVisible(false);
            viewPasswordBtn.setText("Hide");
        }
        else{
            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true);
            passwordTextField.setVisible(false);
            viewPasswordBtn.setText("Show");
        }
    }

    /**
     * Display message with a toast label.
     * @param message
     */
    private void showSimpleToast(String message) {
        toastLabel.setText(message);
        toastLabel.setVisible(true);

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                Duration.seconds(3)
        );
        pause.setOnFinished(e -> {
            toastLabel.setVisible(false);
        });
        pause.play();
    }

    /** New: navigate to the standalone Register screen */
    @FXML
    private void handleGoToRegister(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Register.fxml"));
        Parent root = loader.load();
        RegisterController ctrl = loader.getController();
        ctrl.setRole(selectedRole);
        Main.getPrimaryStage().setScene(new Scene(root, 640, 480));
    }

    private String capitalize(String s) {
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }
}
