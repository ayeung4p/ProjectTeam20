package library.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import library.book.Book;
import library.book.Borrow;
import library.user.Notification;

public class BorrowDurationPopupController {

    @FXML private TextField durationMinField;
    @FXML private TextField durationSecField;
    @FXML private Label durationInvalidLabel;

    private Book selectedBook;
    private static final int MAX_MINUTES = 20160; // 14 days
    private String currentUsername;

    public void setSelectedBook(Book book) {
        this.selectedBook = book;
    }

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
    }

    @FXML
    private void initialize() {
        // Set up input validation
        setupInputValidation();
    }

    private void setupInputValidation() {
        // Only allow numbers in minute field
        durationMinField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                durationMinField.setText(newValue.replaceAll("[^\\d]", ""));
                showInvalidLabel();
            } else {
                validateInput();
            }
        });

        // Only allow numbers in second field and limit to 59
        durationSecField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                durationSecField.setText(newValue.replaceAll("[^\\d]", ""));
                showInvalidLabel();
            } else if (!newValue.isEmpty()) {
                try {
                    int seconds = Integer.parseInt(newValue);
                    if (seconds > 59) {
                        durationSecField.setText("59");
                        showInvalidLabel();
                    } else {
                        validateInput();
                    }
                } catch (NumberFormatException e) {
                    showInvalidLabel();
                }
            } else {
                validateInput();
            }
        });
    }

    private boolean validateInput() {
        boolean isValid = true;
        String errorMessage = "";

        try {
            String minText = durationMinField.getText().trim();
            String secText = durationSecField.getText().trim();
            // Check if minutes field is empty or invalid
            if (durationMinField.getText().isEmpty() && durationSecField.getText().isEmpty()) {
                isValid = false;
                errorMessage = "Please enter duration";
            } else {
                int minutes = durationMinField.getText().isEmpty() ? 0 : Integer.parseInt(minText);
                int seconds = durationSecField.getText().isEmpty() ? 0 : Integer.parseInt(secText);

                // Check if total duration exceeds maximum (14 days)
                if (minutes > MAX_MINUTES) {
                    isValid = false;
                    errorMessage = "Duration exceeds 14 days maximum";
                } else if (minutes == MAX_MINUTES && seconds > 0) {
                    isValid = false;
                    errorMessage = "Duration exceeds 14 days maximum";
                }

                // Check if duration is zero or negative
                if (minutes == 0 && seconds == 0) {
                    isValid = false;
                    errorMessage = "Duration cannot be zero";
                } else if (minutes < 0) {
                    isValid = false;
                    errorMessage = "Duration cannot be negative";
                }
            }
        } catch (NumberFormatException e) {
            isValid = false;
            errorMessage = "Invalid number format";
        }

        if (!isValid) {
            showInvalidLabel(errorMessage);
        } else {
            hideInvalidLabel();
        }

        return isValid;
    }

    private void showInvalidLabel() {
        showInvalidLabel("Invalid input, please enter again");
    }

    private void showInvalidLabel(String message) {
        durationInvalidLabel.setText(message);
        durationInvalidLabel.setVisible(true);
    }

    private void hideInvalidLabel() {
        durationInvalidLabel.setVisible(false);
    }

    @FXML
    private void handleConfirm() {
        if (!validateInput()) {
            return;
        }

        try {
            int minutes = durationMinField.getText().isEmpty() ? 0 : Integer.parseInt(durationMinField.getText());
            int seconds = durationSecField.getText().isEmpty() ? 0 : Integer.parseInt(durationSecField.getText());

            // Call the borrowBook method using the passed username
            boolean success = Borrow.borrowBook(selectedBook, currentUsername, minutes, seconds);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Book '" + selectedBook.getBookTitle() + "' borrowed successfully for " +
                                minutes + " minutes and " + seconds + " seconds.");
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Borrow Failed",
                        "Failed to borrow book. The book may not be available or you may have already borrowed it.");
            }

        } catch (NumberFormatException e) {
            showInvalidLabel("Invalid number format");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "An unexpected error occurred while borrowing the book.");
        }
    }

    @FXML
    private void handleCancel() {
        // Return to previous interface (Student Dashboard) by closing the popup
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) durationMinField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}