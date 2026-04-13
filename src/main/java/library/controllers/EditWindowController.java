package library.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import library.book.Book;

import java.util.function.Consumer;

import static library.book.Book.editBook;


public class EditWindowController {
    @FXML private TextField authorEditTitleField;
    @FXML private TextArea authorEditAbstractField;
    @FXML private Button authorEditCancel;
    @FXML private Button authorEditSave;

    private Book bookInEditBook;
    private Consumer<Book> editListener;


    // Methods:
    /**
     * Helper function called by handleMyBooksModify()
     * Simply set the editing book to the input book
     * @param book
     */
    public void setBook(Book book) {
        this.bookInEditBook = book;
    }

    /**
     * Helper function called by handleMyBooksModify()
     * Simply set the listener to the input listener for later callback use
     * @param listener
     */
    public void setBookEditListener(Consumer<Book> listener) {
        this.editListener = listener;
    }

    /**
     * Called when the user click Cancel in the pop-up window
     * Simply close the window
     */
    @FXML
    public void handleAuthorEditCancel() {
        Stage stage = (Stage) authorEditCancel.getScene().getWindow();
        stage.close();
    }

    /**
     * Called when the user click Save in the pop-up window
     * Update the book by the field filled in accordingly if fields are vaild
     * Callback to the myBooks table so that immediate changes appears
     */
    @FXML
    public void handleAuthorEditSave() {
        String newAbstractText = authorEditAbstractField.getText();
        String newTitleText = authorEditTitleField.getText();
        if (newAbstractText.isEmpty() || newTitleText.isEmpty()) { // Check if the modified information is empty
            // Show alert
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Input");
            alert.setHeaderText(null);
            alert.setContentText("Title or Summary should not be empty");
            alert.showAndWait();
        } else {
            // Perform Edit Book
            editBook(bookInEditBook, newTitleText, newAbstractText);
            // Trigger callback to parent controller, if set
            if (editListener != null) {
                editListener.accept(bookInEditBook);
            }
            // Close the window after saving
            Stage stage = (Stage) authorEditSave.getScene().getWindow();
            stage.close();
        }
    }

    }



