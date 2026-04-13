package library.controllers;


import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.Event;
import javafx.scene.text.Text;
import javafx.util.Callback;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import library.Main;
import library.book.Book;
import library.book.Borrow;
import library.user.Notification;
import library.user.User;
import javafx.stage.Modality;

import static library.book.Book.getParticularBook;
import static library.book.Book.viewBook;
import static library.book.Borrow.getBorrowByUser;

import javafx.scene.control.PasswordField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.geometry.Insets;

// JavaFX Core
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;

// Collections
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

// Exceptions
import java.io.IOException;



public class StudentDashboardController {

    // Available Books Tab components
    @FXML private TableView<Book> availableBooksTable;
    @FXML private TableColumn<Book, String> tab1_TitleCol;
    @FXML private TableColumn<Book, String> tab1_AuthorCol;
    @FXML private TableColumn<Book, String> tab1_PublishedOnCol;
    @FXML private TableColumn<Book, String> tab1_AbstractCol;

    @FXML private Label tab1_TitleLabel;
    @FXML private Label tab1_AuthorLabel;
    @FXML private Label tab1_PublishedOnLabel;
    @FXML private Label tab1_AbstractLabel;

    @FXML private Button borrowBookButton;

    // My Borrowed Books Tab components
    @FXML private TableView<Borrow> myBorrowedBooksTable;
    @FXML private TableColumn<Borrow, String> tab2_TitleCol;
    @FXML private TableColumn<Borrow, String> tab2_AuthorCol;
    @FXML private TableColumn<Borrow, String> tab2_BorrowedOnCol;
    @FXML private TableColumn<Borrow, String> tab2_TimeLeftCol;
    @FXML private TableColumn<Borrow, Void> tab2_ActionsCol;
    @FXML private Button readBookButton;

    // My Profile Tab components
    @FXML private Text usernameText;
    @FXML private TextField fullNameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField passwordTextField;
    @FXML private TextField confirmPasswordTextField;
    @FXML private ToggleButton viewPasswordBtn;
    @FXML private ToggleButton viewConfirmPasswordBtn;
    @FXML private Label toastLabel;

    // Inform Board Tab components
    @FXML private ListView<Notification> notificationList;
    @FXML private Button clearAllButton;
    @FXML private Label notificationLabel;


    // Controller
    private User currUser;
    private final ObservableList<Borrow> myBorrowedBooks = FXCollections.observableArrayList();
    private final ObservableList<Book> availableBooks = FXCollections.observableArrayList();
    private final ObservableList<Notification> notifications = FXCollections.observableArrayList();

    //Initialization
    /**
     * Called automatically after FXML is loaded.
     * Initialize All Tabs.
     */
    @FXML
    private void initialize() {
        ensureDataFiles();
        // Available Books Table
        setupTableColumns();
        setupTableSelectionListener();
        // My Borrowed Books Table
        setupMyBorrowedBooksTable();
        // Inform Board
        setupInformBoardTab();

    }

    /** Ensure the existence of data directories. */
    private void ensureDataFiles() {
        try {
            Book.ensureStorageStructure();
            Borrow.ensureDataDirectory();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Data Error",
                    "Failed to initialize data files: " + e.getMessage());
        }
    }

    /**
     * Get user object and store as attribute.
     * Initialize datas after storing user attribute.
     * @param user
     */
    public void setUser(User user){
        this.currUser = user;
        initializeData();
        handleProfileTab();
    }

    /** Load All Datas after storing user attribute. */
    private void initializeData() {
        if (currUser != null) {
            loadBooksData();
            loadMyBorrowedBooksData();
            loadNotifications();
        }
    }

    /** Initialize the Update Profile Tab after storing user attribute. */
    private void handleProfileTab() {
        if (currUser != null) {
            usernameText.setText(currUser.getUsername());
            fullNameField.setText(currUser.getFullName());
        }
    }


    // Available Books Tab
    /** Initialize Available Books Tab: Setting up Table Columns */
    private void setupTableColumns() {
        // Set up cell value factories using SimpleStringProperty
        tab1_TitleCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getBookTitle()));

        tab1_AuthorCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAuthorUsername()));

        tab1_PublishedOnCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPublishedDate()));

        tab1_AbstractCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getBookAbstract()));

        // Set up cell factory to display text
        Callback<TableColumn<Book, String>, TableCell<Book, String>> cellFactory =
                column -> new TableCell<Book, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item);
                            setWrapText(true);
                        }
                    }
                };

        // Apply cell factory to all columns
        tab1_TitleCol.setCellFactory(cellFactory);
        tab1_AuthorCol.setCellFactory(cellFactory);
        tab1_PublishedOnCol.setCellFactory(cellFactory);
        tab1_AbstractCol.setCellFactory(cellFactory);

        // Connect table to data
        availableBooksTable.setItems(availableBooks);
    }

    /** Load Data into Available Books Tab. */
    private void loadBooksData() {
        try {
            List<Book> approvedBooks = Book.getApprovedBook();
            List<Borrow> userBorrows = Borrow.getBorrowByUser(currUser.getUsername());

            // Extract books that the user is currently borrowing
            List<Book> currentlyBorrowedBooks = new ArrayList<>();
            for (Borrow borrow : userBorrows) {
                currentlyBorrowedBooks.add(borrow.getBorrowBook());
            }

            List<Book> displayedBooks = new ArrayList<>();
            for (Book approvedBook : approvedBooks) {
                boolean isCurrentlyBorrowed = false;
                for (Book borrowedBook : currentlyBorrowedBooks) {
                    if (loadBooksData_checking(approvedBook, borrowedBook)) {
                        isCurrentlyBorrowed = true;
                        break;
                    }
                }

                if (!isCurrentlyBorrowed) {
                    displayedBooks.add(approvedBook);
                }
            }

            availableBooks.setAll(displayedBooks);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Data Error", "Failed to load approved books.");
        }
    }

    /**
     * Helper function:
     * Comparing two Book objects.
     * @param approvedBook
     * @param borrowedBook
     * @return if the two Book objects are equal
     */
    public boolean loadBooksData_checking(Book approvedBook, Book borrowedBook){
        return approvedBook.getBookTitle().equals(borrowedBook.getBookTitle()) && approvedBook.getAuthorUsername().equals(borrowedBook.getAuthorUsername());
    }

    /** Set up Table Selection Listener in Available Books Tab. */
    private void setupTableSelectionListener() {
        // Update details when a book is selected
        availableBooksTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        updateBookDetails(newValue);
                        borrowBookButton.setDisable(false);
                    } else {
                        clearBookDetails();
                        borrowBookButton.setDisable(true);
                    }
                }
        );

        // Initially disable borrow button until a book is selected
        borrowBookButton.setDisable(true);
    }

    /** Update Book Details in Description Box in Available Books Tab. */
    private void updateBookDetails(Book book) {
        tab1_TitleLabel.setText(book.getBookTitle());
        tab1_AuthorLabel.setText(book.getAuthorUsername());
        tab1_PublishedOnLabel.setText(book.getPublishedDate());
        tab1_AbstractLabel.setText(book.getBookAbstract());

        // Enable text wrapping for abstract label
        tab1_AbstractLabel.setWrapText(true);
        tab1_AbstractLabel.setMaxWidth(200);
    }

    /** Clear Book Details in Description Box in Available Books Tab. */
    private void clearBookDetails() {
        tab1_TitleLabel.setText("");
        tab1_AuthorLabel.setText("");
        tab1_PublishedOnLabel.setText("");
        tab1_AbstractLabel.setText("");
    }

    // Available Books Tab (Borrowing Button)
    /**
     * This method handles the BORROW action.
     * This method is called when the <code>borrow</code> button is clicked.
     */
    @FXML
    private void handleBorrowSelectedBook(ActionEvent event) {
        Book selectedBook = availableBooksTable.getSelectionModel().getSelectedItem();
        if (selectedBook != null) {
            showBorrowPopup(selectedBook);

        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a book to borrow.");
        }
    }

    /**
     * This method shows Borrow Duration Popup Window.
     * Borrower can fill in the borrowing duration time.
     */
    private void showBorrowPopup(Book book) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BorrowDurationPopup.fxml"));
            Parent popup = loader.load();

            BorrowDurationPopupController controller = loader.getController();
            controller.setSelectedBook(book);
            controller.setCurrentUsername(currUser.getUsername()); // Pass username from current stage

            Stage stage = new Stage();
            stage.setTitle("Borrow Book");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(popup));
            stage.showAndWait();

            // Refresh dashboard!
            loadBooksData();
            loadMyBorrowedBooksData();
            loadNotifications();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot open borrow window.");
        }
    }


    // My Borrowed Books
    /** Initialize My Borrowed Books Tab: Setting up Table Columns */
    private void setupMyBorrowedBooksTable(){
        // Set up cell value factories
        tab2_TitleCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getBorrowBook().getBookTitle()));

        tab2_AuthorCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getBorrowBook().getAuthorUsername()));

        tab2_BorrowedOnCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatDateTime(cellData.getValue().getBorrowedDateTime())));

        tab2_TimeLeftCol.setCellFactory(column -> new TableCell<Borrow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableView() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Borrow borrow = getTableView().getItems().get(getIndex());
                    String timeLeftText = calculateTimeLeft(borrow);
                    setText(timeLeftText);

                    if ("EXPIRED".equals(timeLeftText)) {
                        loadMyBorrowedBooksData();
                        setStyle("-fx-text-fill: red;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        setupBorrowedTableCellFactories();

        // Set up Actions column with Return button
        tab2_ActionsCol.setCellFactory(param -> new TableCell<Borrow, Void>() {
            private final Button returnButton = new Button("Return");

            {
                returnButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                returnButton.setOnAction(event -> {
                    Borrow borrow = getTableView().getItems().get(getIndex());
                    handleReturnBook(borrow);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(returnButton);
                }
            }
        });

        setupBorrowedTableSelection();
        startBorrowedBooksTimer();
    }

    /** Initialize My Borrowed Books Tab: Setting up Table Cell Factories */
    private void setupBorrowedTableCellFactories() {
        // Cell factory for text columns
        Callback<TableColumn<Borrow, String>, TableCell<Borrow, String>> cellFactory =
                column -> new TableCell<Borrow, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item);
                            setWrapText(true);
                        }
                    }
                };
        // Apply cell factory to all text columns
        tab2_TitleCol.setCellFactory(cellFactory);
        tab2_AuthorCol.setCellFactory(cellFactory);
        tab2_BorrowedOnCol.setCellFactory(cellFactory);
//        tab2_TimeLeftCol.setCellFactory(cellFactory);
    }

    /** Initialize My Borrowed Books Tab: Manages table row selection and read button state */
    private void setupBorrowedTableSelection() {
        myBorrowedBooksTable.setItems(myBorrowedBooks);

        myBorrowedBooksTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    readBookButton.setDisable(newValue == null);
                }
        );

        readBookButton.setDisable(true);
    }

    /** Load Data into My Borrowed Books Tab. */
    private void loadMyBorrowedBooksData() {
        try {
            List<Borrow> userBorrows = getBorrowByUser(currUser.getUsername());

            // Auto-return expired books
            boolean autoReturnedAny = false;
            List<Borrow> nonExpiredBorrows = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            for (Borrow borrow : userBorrows) {
                LocalDateTime expiryTime = borrow.getBorrowedDateTime().plus(borrow.getDuration());

                if (now.isAfter(expiryTime)) {
                    // Auto-return expired book
                    boolean success = Borrow.returnBook(borrow.getBorrowBook(), borrow.getBorrowerUsername());
                    if (success) {
                        // Create notification for expired book
                        Notification.save(borrow.getBorrowerUsername(), Notification.NotificationType.EXPIRED, borrow.getBorrowBook().getBookTitle());
                        autoReturnedAny = true;
                    }
                } else {
                    nonExpiredBorrows.add(borrow);
                }
            }

            myBorrowedBooks.setAll(nonExpiredBorrows);
            myBorrowedBooksTable.setItems(myBorrowedBooks);
            myBorrowedBooksTable.refresh();

            // Show notification if any books were auto-returned
            if (autoReturnedAny) {
                showAlert(Alert.AlertType.INFORMATION, "Auto-Return",
                        "Some expired books have been automatically returned.");
                loadNotifications();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Data Error", "Failed to load borrowed books.");
        }
    }

    // My Borrowed Books Tab (Buttons)
    /**
     * This method handles the RETURN action.
     * This method is called when the <code>return</code> button is clicked.
     */
    private void handleReturnBook(Borrow borrow) {
        if (borrow == null) return;

        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Return Book");
        confirmAlert.setHeaderText("Confirm Return");
        confirmAlert.setContentText("Are you sure you want to return '" +
                borrow.getBorrowBook().getBookTitle() + "'?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = Borrow.returnBook(borrow.getBorrowBook(), currUser.getUsername());

            if (success) {
                Notification.save(borrow.getBorrowerUsername(), Notification.NotificationType.RETURNED, borrow.getBorrowBook().getBookTitle());
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Book returned successfully!");
                // Refresh dashboard!
                loadBooksData();
                loadMyBorrowedBooksData();
                loadNotifications();
            } else {
                showAlert(Alert.AlertType.ERROR, "Return Failed",
                        "Failed to return book. Please try again.");
            }
        }
    }

    /**
     * This method will display the book content in a view window.
     * This method is called when the <code>view</code> button is clicked.
     */
    @FXML
    private void handleReadBook() {
        try{
            Borrow selectedBorrow = myBorrowedBooksTable.getSelectionModel().getSelectedItem();
            if (selectedBorrow != null) {
                Book book = selectedBorrow.getBorrowBook();
//                showAlert(Alert.AlertType.INFORMATION, "Read Book",
//                        "Opening book: " + book.getBookTitle() + "\n" +
//                                "Content directory: " + book.getContentDirectory());

                String dir = selectedBorrow.getBorrowBook().getContentDirectory();
                String bookContent = viewBook(dir);
//            showReadBookPopup(bookContent, selectedBorrow.getBorrowBook().getBookTitle());
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StudentReadBookPopup.fxml"));
                Parent windowRoot = loader.load();
                StudentReadBookPopup winCtrl = loader.getController();// Go to ViewWindow Controller

                winCtrl.setBookContent(bookContent);
                winCtrl.setBorrowInfo(selectedBorrow.getBorrowedDateTime(), selectedBorrow.getDuration());

                // Show in a new Stage (popup window)
                Stage stage = new Stage();
                stage.setTitle("Book Viewer");
                stage.setScene(new Scene(windowRoot, 700, 500));
                stage.show();


            } else {
                showAlert(Alert.AlertType.WARNING, "No Selection",
                        "Please select a book to read.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot open book reader.");
        }
        loadNotifications();
    }

    // My Borrowed Books Tab (Helper methods for formatting)
    /**
     * Formats a LocalDateTime object into a readable string pattern.
     * @param dateTime
     * @return the formatted time or "N/A" for null values
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }

    /**
     * Calculates the remaining time until a borrowed book expires.
     * @param borrow
     * @return formatted time or "EXPIRED" if overdue
     */
    private String calculateTimeLeft(Borrow borrow) {
        if (borrow == null || borrow.getBorrowedDateTime() == null || borrow.getDuration() == null) {
            return "N/A";
        }

        LocalDateTime borrowedTime = borrow.getBorrowedDateTime();
        Duration duration = borrow.getDuration();
        LocalDateTime expiryTime = borrowedTime.plus(duration);
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(expiryTime)) {
//            Platform.runLater(this::loadMyBorrowedBooksData);
            return "EXPIRED";
        }

        Duration timeLeft = Duration.between(now, expiryTime);
        long days = timeLeft.toDays();
        long hours = timeLeft.toHours() % 24;
        long minutes = timeLeft.toMinutes() % 60;
        long seconds = timeLeft.getSeconds() % 60;

        if (days > 0) {
            return String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format("0d %02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("0d 00:%02d:%02d", minutes, seconds);
        }
    }

    /**
     * Starts a timer that refreshes the borrowed books table every second
     * to update time remaining displays.
     */
    private void startBorrowedBooksTimer() {
        Timeline timeline = new Timeline(
                new KeyFrame(javafx.util.Duration.seconds(1), event -> {
                    myBorrowedBooksTable.refresh();
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }


    // Update Account
    /**
     * This method handles Profile Update.
     * This method is called when <code>update</code> button is clicked.
     **/
    @FXML
    private void handleProfileUpdate(ActionEvent event) throws IOException{
        String password = passwordField.isVisible() ?
                passwordField.getText() :
                passwordTextField.getText();
        String confirmPassword = confirmPasswordField.isVisible() ?
                confirmPasswordField.getText() :
                confirmPasswordTextField.getText();
        String fullName = fullNameField.getText().trim();


        if(fullName.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
            showAlert(Alert.AlertType.WARNING, "Empty Input", "Please enter new information to update your profile.");
        }
        else if(!User.checkFullnameLength(fullName)){
            showAlert(Alert.AlertType.ERROR, "Invalid Full Name", "Full name must be between 4 and 50 characters long.");
        }
        else if(password.length() < 8){
            showAlert(Alert.AlertType.ERROR, "Invalid Password", "Password must be at least 8 characters long.");
        }
        else if(!User.validatePassword(password)){
            showAlert(Alert.AlertType.ERROR, "Invalid Password",
                    "Password must contain at least one uppercase letter, one lowercase letter, and one number.");
        }
        else if(!password.equals(confirmPassword)){
            showAlert(Alert.AlertType.ERROR, "Password Mismatch", "New password and confirmation password do not match.");
        }
        else{
//            currUser.setPassword(password);
//            currUser.setFullName(fullName);
//            try{
//                boolean update = currUser.updateUser();
//
//                if(update){
//                    showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully!");
//                }
//                else{
//                    showAlert(Alert.AlertType.ERROR, "Update Failed", "Failed to update profile in database.");
//                }
//            }
//            catch(Exception e){
//                e.printStackTrace();
//                showAlert(Alert.AlertType.ERROR, "Update Error", "An error occurred while updating your profile: " + e.getMessage());
//            }
            // Update user information
            boolean updated = false;

            // Update full name if changed
            if (!fullName.isEmpty() && !fullName.equals(currUser.getFullName())) {
                currUser.setFullName(fullName);
                updated = true;
            }

            // Update password if changed and valid
            if (!password.isEmpty() && !currUser.checkPassword(password)) {
                currUser.setPassword(password);
                updated = true;
            }

            if (updated) {
                try{
                    boolean success = currUser.updateUser();
                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully!");
                        clearProfileFields();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Update Failed", "Failed to update profile in database.");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Update Error", "An error occurred while updating your profile: " + e.getMessage());
                }
            } else {
                showAlert(Alert.AlertType.INFORMATION, "No Changes", "No changes were made to your profile.");
            }
        }
    }

    /** Clear the Input Fields for other updates. */
    private void clearProfileFields() {
        passwordField.clear();
        confirmPasswordField.clear();
        passwordTextField.clear();
        confirmPasswordTextField.clear();

        // Reset toggle buttons
        if (!passwordField.isVisible()) {
            if (viewPasswordBtn.isSelected()) {
                viewPasswordBtn.setSelected(false); // Set to "viewing" state first
            }
            handleViewPassword(); // Toggle back to hide
        }
        if (!confirmPasswordField.isVisible()) {
            if (viewConfirmPasswordBtn.isSelected()) {
                viewConfirmPasswordBtn.setSelected(false); // Set to "viewing" state first
            }
            handleViewConfirmPassword(); // Toggle back to hide
        }
    }

    /**
     * Click the toggle button <code>viewPasswordBtn</code> to
     * view the password.
     */
    @FXML
    private void handleViewPassword(){
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
     */
    @FXML
    private void handleViewConfirmPassword(){
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


    // Inform Board
    /** Initialize Inform Board Tab */
    private void setupInformBoardTab() {
        setupNotificationList();
    }

    /** Initialize Inform Board Tab: Setting up Notification List */
    private void setupNotificationList() {
        notificationList.setItems(notifications);
        notificationList.setCellFactory(param -> new ListCell<Notification>() {
            private final HBox container = new HBox(10);
            private final Label messageLabel = new Label();
            private final Button clearButton = new Button("Delete");

            {
                container.setAlignment(Pos.CENTER_LEFT);
                container.setPadding(new Insets(5));
                container.setPrefWidth(780);
                messageLabel.setWrapText(true);
                messageLabel.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(messageLabel, Priority.ALWAYS);

                clearButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 11px;");
                clearButton.setMinWidth(60);

                clearButton.setOnAction(event -> {
                    Notification notification = getItem();
                    if (notification != null) {
                        handleClearNotification(notification);
                    }
                });

                container.getChildren().addAll(messageLabel, clearButton);
            }

            @Override
            protected void updateItem(Notification notification, boolean empty) {
                super.updateItem(notification, empty);
                if (empty || notification == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    messageLabel.setText(notification.getMessage());
                    setGraphic(container);
                }
            }
        });
    }

    /** Load Data into Inform Board. */
    private void loadNotifications() {
        if (currUser == null) {
            return;
        }
        try {
            List<Notification> userNotifications = Notification.selectByUser(currUser.getUsername());
            notifications.setAll(userNotifications);
            if(userNotifications.isEmpty()){
                notificationLabel.setText("No notifications available");
            } else {
                notificationLabel.setText("Notification list:");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load notifications.");
        }
    }

    /**
     * The method clears all the notifications.
     * The method is called when <code>ClearAll</code> button is clicked.
     */
    @FXML
    private void handleClearAllNotifications(){
        if (notifications.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Notifications", "There are no notifications to clear.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Clear All Notifications");
        confirmAlert.setHeaderText("Confirm Clear All");
        confirmAlert.setContentText("Are you sure you want to clear all " + notifications.size() + " notifications?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Delete each notification from database
            boolean allDeleted = true;
            List<Notification> notificationsToRemove = new ArrayList<>(notifications);

            for (Notification notification : notificationsToRemove) {
                boolean success = Notification.deleteNotification(notification);
                if (!success) {
                    allDeleted = false;
//                    System.err.println("Failed to delete notification: " + notification.getMessage());
                }
            }

            // Clear from UI regardless of individual success/failure
            notifications.clear();
            notificationLabel.setText("No notifications available");

            if (allDeleted) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "All notifications have been cleared.");
            } else {
                showAlert(Alert.AlertType.WARNING, "Partial Success",
                        "Most notifications were cleared, but some may not have been deleted properly.");
            }
        }
        refreshNotifications();
    }

    /**
     * The method clears the corresponding notification.
     * The method is called when <code>Delete</code> button is clicked.
     */
    private void handleClearNotification(Notification notification) {
        if (notification != null) {
            // Delete from database first
            boolean success = Notification.deleteNotification(notification);

            if (success) {
                // Remove from UI
                notifications.remove(notification);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete notification from database.");
            }
        }
        refreshNotifications();
    }

    /** Refresh the Inform Board. */
    private void refreshNotifications() {
        loadNotifications();
    }


    // Important function showAlert
    /** Show Alert to the User. */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    /** Refresh the dashboard when switching tabs. */
    @FXML
    private void handleTabSelection() {
        if(currUser != null) {
            loadBooksData();
            loadMyBorrowedBooksData();
            refreshNotifications();
        }
    }

    /** Log out back to the Home screen. */
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Home.fxml"));
            Stage st = Main.getPrimaryStage();
            st.setScene(new Scene(root, 640, 480));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}