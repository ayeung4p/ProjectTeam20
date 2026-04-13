package library.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import java.time.LocalDateTime;


public class StudentReadBookPopup {
    @FXML private TextArea studentReadBookPopup_BookContentArea;
    @FXML private Button studentReadBookPopup_OKButton;
    @FXML private Button studentReadBookPopup_ZoomInButton;
    @FXML private Button studentReadBookPopup_ZoomOutButton;
    @FXML private Label zoomLabel;

    private double currentFontSize = 16.0;
    private static final double MIN_FONT_SIZE = 8.0;
    private static final double MAX_FONT_SIZE = 36.0;
    private static final double FONT_STEP = 2.0;

    private Timeline expirationTimer;
    private LocalDateTime borrowedTime;
    private java.time.Duration borrowDuration;

    public void setBookContent(String bookContent) {
        studentReadBookPopup_BookContentArea.setText(bookContent);
        studentReadBookPopup_BookContentArea.setFont(Font.font("Arial", currentFontSize));
        updateButtonStates();
    }

    public void setBorrowInfo(LocalDateTime borrowedTime, java.time.Duration borrowDuration) {
        this.borrowedTime = borrowedTime;
        this.borrowDuration = borrowDuration;
        startExpirationCheckTimer();
    }

    @FXML
    private void initialize() {
        updateButtonStates();
    }

    private void startExpirationCheckTimer() {
        expirationTimer = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    System.out.println("checking");
                    checkAndHandleExpiration();
                })
        );
        expirationTimer.setCycleCount(Timeline.INDEFINITE);
        expirationTimer.play();
    }
    private void checkAndHandleExpiration() {
        if (isBookExpired()) {
            System.out.println("yes expired");
            if (expirationTimer != null) {
                expirationTimer.stop();
            }
            javafx.application.Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.WARNING);
                alert.setTitle("Book Expired");
                alert.setHeaderText(null);
                alert.setContentText("This book has expired and is no longer available for reading.");

                // Set event handler to close window after alert is dismissed
                alert.setOnHidden(event -> {
                    closeWindow();
                });

                alert.show();
            });
        }
        System.out.println("not yet expired");
    }
    private boolean isBookExpired() {
        if (borrowedTime == null || borrowDuration == null) {
            return true;
        }

        LocalDateTime expiryTime = borrowedTime.plus(borrowDuration);
//        LocalDateTime earlyExpiryTime = expiryTime.minusSeconds(2); // Mark expired 2 seconds early
        return LocalDateTime.now().isAfter(expiryTime);
    }

    @FXML
    private void handleStudentReadBookPopupOKButton() {
        closeWindow();
    }

    @FXML
    private void handleStudentReadBookPopupZoomInButton() {
        if (currentFontSize < MAX_FONT_SIZE) {
            currentFontSize += FONT_STEP;
            updateFontSize();
        }
    }
    @FXML
    private void handleStudentReadBookPopupZoomOutButton() {
        if (currentFontSize > MIN_FONT_SIZE) {
            currentFontSize -= FONT_STEP;
            updateFontSize();
        }
    }

    private void updateFontSize() {
        studentReadBookPopup_BookContentArea.setFont(Font.font("Arial", currentFontSize));
        int percentage = (int) ((currentFontSize / 16.0) * 100);
        zoomLabel.setText(String.format("%d%%", percentage));
        updateButtonStates();
    }

    private void updateButtonStates() {
        studentReadBookPopup_ZoomInButton.setDisable(currentFontSize >= MAX_FONT_SIZE);
        studentReadBookPopup_ZoomOutButton.setDisable(currentFontSize <= MIN_FONT_SIZE);
    }

    private void closeWindow() {
        if (expirationTimer != null) {
            expirationTimer.stop();
        }

        if (studentReadBookPopup_BookContentArea == null ||
                studentReadBookPopup_BookContentArea.getScene() == null) {
            return;
        }

        Stage stage = (Stage) studentReadBookPopup_BookContentArea.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
}


