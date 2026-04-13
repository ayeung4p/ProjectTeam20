package library.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

public class ViewWindowController {
    @FXML private TextArea bookContentArea;
    @FXML private Button authorViewOK;
    @FXML private Button authorViewZoomIn;
    @FXML private Button authorViewZoomOut;
    @FXML private Label zoomLabel;

    private double fontSize = 16.0;

    /**
     * Helper function called in handleMyBooksView()
     * Set the content to the pop-up window
     * Also set the default font and font size
     * @param bookContent
     */
    public void setBookContent(String bookContent) {
        bookContentArea.setText(bookContent);
        bookContentArea.setFont(Font.font("Arial", fontSize));
    }

    /**
     * Called when the user clicks OK button in the pop-up window
     * Simply close the window and pass back the controller to AuthorDashboardController
     * @param event
     */
    @FXML
    private void handleAuthorViewOK(ActionEvent event) {
        Stage stage = (Stage) authorViewOK.getScene().getWindow();
        stage.close();
    }
    /**
     * Called when the user clicks Zoom in button in the pop-up window
     * Increase the font size by 2, max = 36
     * @param event
     */
    @FXML
    private void handleAuthorViewZoomIn(ActionEvent event) {
        fontSize = Math.min(36.0, fontSize + 2.0);
        bookContentArea.setFont(Font.font("Arial", fontSize));
        bookContentArea.requestLayout();
        updateFont();
    }
    /**
     * Called when the user clicks Zoom out button in the pop-up window
     * Decrease the font size by 2, min = 8
     * @param event
     */
    @FXML
    private void handleAuthorViewZoomOut(ActionEvent event) {
        fontSize = Math.max(8.0, fontSize - 2.0);
        bookContentArea.setFont(Font.font("Arial", fontSize));
        bookContentArea.requestLayout();
        updateFont();
    }

    /**
     * Helper function called in Zoom in and out methods
     * Calculate the zoom in rate and set the label accordingly
     */
    private void updateFont() {
        bookContentArea.setFont(Font.font("Arial", fontSize));

        int percentage = (int) ((fontSize / 16.0) * 100);
        zoomLabel.setText(String.format("%d%%", percentage));

        bookContentArea.requestLayout();
    }
}