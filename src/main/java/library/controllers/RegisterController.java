package library.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.util.Duration;
import library.Main;
import library.user.User;

import java.io.IOException;

public class RegisterController {
    @FXML private Label headerLabel;
    @FXML private Label toastLabel;
    @FXML private TextField usernameField;
    @FXML private TextField fullNameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField passwordTextField;
    @FXML private TextField confirmPasswordTextField;
    @FXML private ToggleButton viewPasswordBtn;
    @FXML private ToggleButton viewConfirmPasswordBtn;

    private String selectedRole;

    public void setRole(String role) {
        this.selectedRole = role;
        headerLabel.setText(capitalize(role) + " Register");
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent home = FXMLLoader.load(getClass().getResource("/fxml/Home.fxml"));
        Main.getPrimaryStage().setScene(new Scene(home, 640, 480));
    }

    /**
     * Register new user and handle cases when the input is invalid.
     * @param event
     * @throws IOException
     */
    @FXML
    private void handleRegister(ActionEvent event) throws IOException{
        String username = usernameField.getText().trim();
        String password = passwordField.isVisible() ? passwordField.getText() : passwordTextField.getText();
        String confirmPassword = confirmPasswordField.isVisible() ? confirmPasswordField.getText() : confirmPasswordTextField.getText();
        String fullName = fullNameField.getText().trim();

        if(username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || fullName.isEmpty()){
            showSimpleToast("Please enter information.");
        }
        else if(!User.checkUsernameLength(username)){
            showSimpleToast("Username must be 4 to 30 characters long.");
        }
        else if(!User.validateUsername(username)){
            showSimpleToast("Invalid username.");
        }
        else if(User.usernameExists(username)){
            showSimpleToast("Username already exists! Please try again.");
        }
        else if(password.length() < 8){
            showSimpleToast("Password must be at least 8 characters long.");
        }
        else if(!User.validatePassword(password)){
            showSimpleToast("Invalid password.");
        }
        else if(!password.equals(confirmPassword)){
            showSimpleToast("New password and confirmation password do not match.");
        }
        else if(!User.checkFullnameLength(fullName)){
            showSimpleToast("Full name must be 2 to 50 characters long.");
        }
        else{
            // save the new user
            User newUser = new User(username, password, fullName, "activated", selectedRole);
            try{
                Boolean save = newUser.save();

                if(save){
                    showSimpleToast("Registration success! You can return to login page.");
                }
                else{
                    showSimpleToast("Registration fails! Please try again.");
                }
            }
            catch(Exception e){
                e.printStackTrace();
                showSimpleToast("An error occurred while updating your profile: " + e.getMessage());
            }
        }
    }


    /**
     * Click to show all the requirement for password.
     * @param event
     */
    @FXML
    private void handleUsernamePasswordRequirement(ActionEvent event){
        String requirements =
                "Username Requirements:\n" +
                    "• English letters or combination of letters and numbers\n" +
                    "• Cannot be pure numbers\n" +
                    "• 5 to 29 characters long\n" +
                    "• No special characters like &#@*\n\n" +
                "Password Requirements:\n" +
                    "• Combination of capital letters, small letters and numbers\n" +
                    "• At least 8 characters long";
        showAlert(Alert.AlertType.INFORMATION, "Username and Password Requirements", requirements);
    }

    // Important function: showAlert
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
     * Click the toggle button <code>viewConfirmPasswordBtn</code> to
     * view the confirmed password.
     * @param event
     */
    @FXML
    private void handleViewConfirmPassword(ActionEvent event){
        if(viewConfirmPasswordBtn.isSelected()){
            confirmPasswordTextField.setText(confirmPasswordField.getText());
            confirmPasswordTextField.setVisible(true);
            confirmPasswordField.setVisible(false);
            viewConfirmPasswordBtn.setText("Hide");
        }
        else{
            confirmPasswordField.setText(confirmPasswordTextField.getText());
            confirmPasswordField.setVisible(true);
            confirmPasswordTextField.setVisible(false);
            viewConfirmPasswordBtn.setText("Show");
        }
    }

    @FXML
    private void handleGoToLogin(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent root = loader.load();
        LoginController ctrl = loader.getController();
        ctrl.setRole(selectedRole);
        Main.getPrimaryStage().setScene(new Scene(root, 640, 480));
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

    private String capitalize(String s) {
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }
}
