// src/main/java/library/controllers/LibrarianDashboardController.java
package library.controllers;

import com.lowagie.text.Table;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import library.Main;
import library.book.Book;
import library.book.Borrow;
import library.user.Notification;
import library.user.User;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.PauseTransition;

public class LibrarianDashboardController {
    private User currUser;

    @FXML private Tab approvalTab, userTab, borrowTab, publishTab;

    /* control for approval tab */
    @FXML private TableView<Book> approvalTable;
    @FXML private TableColumn<Book, String> appTitleCol;
    @FXML private TableColumn<Book, String> appAuthorCol;
    @FXML private TableColumn<Book, String> appAbstractCol;
    @FXML private TableColumn<Book, Void> appActionsCol;
    @FXML private TextArea abstractField;

    /* control for users tab */
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, String> roleCol;
    @FXML private TableColumn<User, String> nameCol;
    @FXML private TableColumn<User, String> activeCol;
    @FXML private TableColumn<User, Void> actionsCol;

    /* control for update profile tab */
    @FXML private Text usernameText;
    @FXML private TextField fullNameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField passwordTextField;
    @FXML private TextField confirmPasswordTextField;
    @FXML private ToggleButton viewPasswordBtn;
    @FXML private ToggleButton viewConfirmPasswordBtn;
    @FXML private Label toastLabel;

    /* control for borrowed book tab */
    @FXML private TableView<Borrow> borrowTable;
    @FXML private TableColumn<Borrow, String> borTitleCol;
    @FXML private TableColumn<Borrow, String> borAuthorCol;
    @FXML private TableColumn<Borrow, String> borrowerCol;
    @FXML private TableColumn<Borrow, String> borrowedOnCol;
    @FXML private TableColumn<Borrow, String> timeLeftCol;

    /* control for publish book tab */
    @FXML private TableView<Book> publishTable;
    @FXML private TableColumn<Book, String> pubTitleCol;
    @FXML private TableColumn<Book, String> pubAuthorCol;
    @FXML private TableColumn<Book, String> publishedOnCol;
    @FXML private TableColumn<Book, String> timesBorrowedCol;
    @FXML private TableColumn<Book, Void> pubActionsCol;


    enum TableType {APPROVAL, USER, BORROW, PUBLISH};

    private ObservableList<Book> approvalData = FXCollections.observableArrayList();
    private ObservableList<User> userData = FXCollections.observableArrayList();
    private ObservableList<Borrow> borrowData = FXCollections.observableArrayList();
    private ObservableList<Book> booksData = FXCollections.observableArrayList();

    /** Called automatically after FXML is loaded. */
    @FXML
    private void initialize() {
        setupApprovals();
        loadTableData(TableType.APPROVAL);
    }

    /**
     * Initialize Pending Approval Tab.
     * @param event
     */
    @FXML
    private void handleApprovalTab(Event event){
        setupApprovals();
        loadTableData(TableType.APPROVAL);
    }

    /**
     * Initialize User tab.
     * @param event
     */
    @FXML
    private void handleUserTab(Event event){
        setupUsers();
        loadTableData(TableType.USER);
    }

    /**
     * Initialize the Update Profile Tab.
     * @param event
     */
    @FXML
    private void handleProfileTab(Event event) {
        if (currUser != null) {
            usernameText.setText(currUser.getUsername());
            fullNameField.setText(currUser.getFullName());
        }
    }

    /**
     * Initialize the Borrowed Books Tab.
     * @param event
     */
    @FXML
    private void handleBorrowTab(Event event){
        setupBorrow();
        loadTableData(TableType.BORROW);
    }

    /**
     * Initialize the Publish Book Tab.
     * @param event
     */
    @FXML
    private void handlePublishTab(Event event){
        setupPublish();
        loadTableData(TableType.PUBLISH);
    }

    /**
     * Get user object and store as attribute.
     * @param user
     */
    public void setUser(User user){
        this.currUser = user;
    }

    /**
     * Load required data according to the table type
     * and store in the controller.
     * @param type type of table
     */
    private void loadTableData(TableType type){
        switch(type){
            case APPROVAL:
                // load pending books
                approvalData.clear();
                List<Book> pendings = Book.getPendingBook();
                for(Book book : pendings){
                    approvalData.add(book);
                }
                break;
            case USER:
                // load all users
                userData.clear();
                List<User> users = User.getAllUsers();
                for(User user : users){
                    userData.add(user);
                }
                break;
            case BORROW:
                // load all borrow records
                borrowData.clear();
                List<Borrow> borrows = Borrow.getAllBorrows();
                for(Borrow borrow : borrows){
                    borrowData.add(borrow);
                }
                break;
            case PUBLISH:
                // load all published books
                booksData.clear();
                List<Book> books = Book.getApprovedBook();
                for(Book book : books){
                    booksData.add(book);
                }
                break;
        }
    }

    /** Handle Pending Approval Tab Data Loading and Display **/
    private void setupApprovals(){
        // set the value for each column
        appTitleCol.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        appAuthorCol.setCellValueFactory(cellData -> {
            Book book = cellData.getValue();
            User author = User.selectUserByUsername(book.getAuthorUsername());
            return new SimpleStringProperty(author != null ? author.getFullName() : "N/A");
        });
        appAbstractCol.setCellValueFactory(new PropertyValueFactory<>("bookAbstract"));
        appActionsCol.setCellFactory(param -> new ApprovalActionsCell());

        // set width
        approvalTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double tableWidth = newWidth.doubleValue();
            appTitleCol.setPrefWidth(tableWidth * 0.2);
            appAuthorCol.setPrefWidth(tableWidth * 0.2);
            appAbstractCol.setPrefWidth(tableWidth * 0.3);
            appActionsCol.setPrefWidth(tableWidth * 0.3);
        });

        approvalTable.setItems(approvalData);
    }

    /**
     * Table cell for the Actions column of table in the Pending Approval Tab
     */
    private class ApprovalActionsCell extends TableCell<Book, Void> {
        private final HBox buttonContainer = new HBox(4);
        private final Button viewAbstractBtn = new Button("View");
        private final Button approveBtn = new Button("Approve");
        private final Button rejectBtn = new Button("Reject");

        /**
         * Initialize the cell, create and add buttons to the cell.
         */
        public ApprovalActionsCell() {
            // add event listeners
            viewAbstractBtn.setOnAction(event -> handleViewAbstractAction());
            approveBtn.setOnAction(event -> handleApproveAction());
            rejectBtn.setOnAction(event -> handleRejectAction());

            // set width of each buttons
            viewAbstractBtn.setPrefWidth(75);
            approveBtn.setPrefWidth(95);
            rejectBtn.setPrefWidth(80);

            buttonContainer.getChildren().addAll(viewAbstractBtn, approveBtn, rejectBtn);
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setGraphic(null);
            } else {
                setGraphic(buttonContainer);
            }
        }

        /**
         * This method gets and displays the book title and abstract
         * in the description box. This method was called when the
         * <code>view</code> button is clicked.
         */
        private void handleViewAbstractAction(){
            Book book = getTableView().getItems().get(getIndex());
            String bookAbstract = book.getBookAbstract();
            String title = book.getTitle();
            abstractField.setText("Title: " + title + "\n\nAbstract: " + bookAbstract);
        }

        /**
         * This method updates the book status to <code>APPROVED</code>
         * and send notification to the author.
         * This method is called when the
         * <code>approve</code> button is clicked.
         */
        private void handleApproveAction() {
            // update book status and the whole table
            Book book = getTableView().getItems().get(getIndex());
            Book.updateBookStatus(book, "approved");
            loadTableData(TableType.APPROVAL);
            getTableView().getItems().remove(book);

            abstractField.setText("Please select a book to view details.");

            // send notification
            Notification notice = new Notification(book.getAuthorUsername(), Notification.NotificationType.APPROVED, book.getBookTitle());
            notice.save();
        }

        /**
         * This method update the book status to <code>REJECTED</code>
         * and send notification to the author.
         * A confirmation box will show when the librarian decides to
         * <b>reject</b> the pending approval.
         * <br>
         * This method is called when the <code>reject</code> button
         * is clicked.
         */
        private void handleRejectAction() {
            Book book = getTableView().getItems().get(getIndex());

            // show confirmation dialog
            Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationDialog.setTitle("Confirmation");
            confirmationDialog.setHeaderText("Confirmation");
            confirmationDialog.setContentText("Are you sure you want to reject the book \"" + book.getTitle() + "\"?");

            DialogPane dialogPane = confirmationDialog.getDialogPane();
            dialogPane.setMinWidth(400);
            dialogPane.setMinHeight(200);

            ButtonType noButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType yesButton = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
            confirmationDialog.getButtonTypes().setAll(yesButton, noButton);

            Optional<ButtonType> result = confirmationDialog.showAndWait();

            // update the status of the book if the librarian confirm the rejection
            if (result.isPresent() && result.get() == yesButton) {
                Book.updateBookStatus(book, "rejected");
                loadTableData(TableType.APPROVAL);
                getTableView().getItems().remove(book);
                abstractField.setText("Please select a book to view details.");

                // send notification
                Notification notice = new Notification(book.getAuthorUsername(), Notification.NotificationType.REJECTED, book.getBookTitle());
                notice.save();
            }
        }
    }

    /** Handle Users Tab Data Loading and Display **/
    private void setupUsers(){
        // set the value of each column
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("roleDisplay"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        activeCol.setCellValueFactory(new PropertyValueFactory<>("statusDisplay"));
        actionsCol.setCellFactory(param -> new UsersActionsCell());

        // set width
        usersTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double tableWidth = newWidth.doubleValue();
            usernameCol.setPrefWidth(tableWidth * 0.25);
            roleCol.setPrefWidth(tableWidth * 0.13);
            nameCol.setPrefWidth(tableWidth * 0.32);
            activeCol.setPrefWidth(tableWidth * 0.1);
            actionsCol.setPrefWidth(tableWidth * 0.2);
        });

        usersTable.setItems(userData);
    }

    /**
     * Table cell for the Action column of the table in the Users Tab
     */
    private class UsersActionsCell extends TableCell<User, Void> {
        private final HBox buttonContainer = new HBox(5);
        private final Button activateBtn = new Button("Activate");
        private final Button deactivateBtn = new Button("Deactivate");

        /**
         * Initialize the cell, create and add buttons to the cell.
         */
        public UsersActionsCell() {
            // add event listener to each buttons
            activateBtn.setOnAction(event -> handleActivateAction());
            deactivateBtn.setOnAction(event -> handleDeactivateAction());

            // set width
            activateBtn.setPrefWidth(80);
            deactivateBtn.setPrefWidth(80);
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setGraphic(null);
            } else {
                User user = getTableView().getItems().get(getIndex());

                buttonContainer.getChildren().clear();

                // do not display button for the current user
                if (currUser.getUsername().equals(user.getUsername())) {
                    setGraphic(null);
                } else if ("true".equals(user.getStatusDisplay())) {
                    // display deactivate button if the user is active
                    buttonContainer.getChildren().add(deactivateBtn);
                    setGraphic(buttonContainer);
                } else {
                    // display activate button if the user is not active
                    buttonContainer.getChildren().add(activateBtn);
                    setGraphic(buttonContainer);
                }
            }
        }

        /**
         * This method updates the user status to <code>Activated</code>.
         * This method is called when the <code>activate</code> button
         * is clicked.
         */
        private void handleActivateAction(){
            // update user status to activated and the whole table
            User user = getTableView().getItems().get(getIndex());
            user.setStatus("activated");
            user.updateUser();
            getTableView().refresh();
        }

        /**
         * This method updates the user status to <code>DEACTIVATED</code>.
         * This method is called when the <code>deactivate</code>
         * button is clicked.
         */
        private void handleDeactivateAction() {
            // update user status to deactivated and the whole table
            User user = getTableView().getItems().get(getIndex());
            user.setStatus("deactivated");
            user.updateUser();
            getTableView().refresh();
        }
    }

    /** Handle Update Profile tab **/
    @FXML
    private void handleProfileUpdate(ActionEvent event) throws IOException{
        String password = passwordField.isVisible() ? passwordField.getText() : passwordTextField.getText();
        String confirmPassword = confirmPasswordField.isVisible() ? confirmPasswordField.getText() : confirmPasswordTextField.getText();
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
            if(updated) {
                try {
                    boolean update = currUser.updateUser();

                    if (update) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully!");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Update Failed", "Failed to update profile in database.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Update Error", "An error occurred while updating your profile: " + e.getMessage());
                }
            } else{
                showAlert(Alert.AlertType.INFORMATION, "No Changes", "No changes were made to your profile.");
            }

            passwordField.setText("");
            passwordTextField.setText("");
            confirmPasswordField.setText("");
            confirmPasswordTextField.setText("");
            // Reset toggle buttons
            if (!passwordField.isVisible()) {
                if (viewPasswordBtn.isSelected()) {
                    viewPasswordBtn.setSelected(false); // Set to "viewing" state first
                }
                handleViewPassword(null); // Toggle back to hide
            }
            if (!confirmPasswordField.isVisible()) {
                if (viewConfirmPasswordBtn.isSelected()) {
                    viewConfirmPasswordBtn.setSelected(false); // Set to "viewing" state first
                }
                handleViewConfirmPassword(null); // Toggle back to hide
            }
        }
    }

    /**
     * Show alert message.
     * @param type
     * @param title
     * @param message
     */
    private void showAlert(Alert.AlertType type, String title, String message){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText("");
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

    /**
     * Show the message in a toast label.
     * @param message
     */
    private void showSimpleToast(String message) {
        toastLabel.setText(message);
        toastLabel.setVisible(true);

        PauseTransition pause = new PauseTransition(
                javafx.util.Duration.seconds(3)
        );
        pause.setOnFinished(e -> {
            toastLabel.setVisible(false);
        });
        pause.play();
    }

    /** Handle Borrowed Books Tab Data Loading and Display **/
    private void setupBorrow(){
        // set the value of each column
        borTitleCol.setCellValueFactory(cellData -> {
            Borrow borrow = cellData.getValue();
            Book book = borrow.getBorrowBook();
            return new SimpleStringProperty(book != null ? book.getTitle() : "N/A");
        });

        borAuthorCol.setCellValueFactory(cellData -> {
            Borrow borrow = cellData.getValue();
            Book book = borrow.getBorrowBook();
            User author = User.selectUserByUsername(book.getAuthorUsername());
            return new SimpleStringProperty(author != null ? author.getFullName() : "N/A");
        });

        borrowerCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getBorrowerUsername()));

        borrowedOnCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getBorrowedDateTime() != null ?
                                cellData.getValue().getBorrowedDateTime().format(
                                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                ) : "N/A"
                ));

        timeLeftCol.setCellFactory(column -> new TableCell<Borrow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableView() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    // show the time left
                    Borrow borrow = getTableView().getItems().get(getIndex());
                    String timeLeftText = calculateTimeLeft(borrow);
                    setText(timeLeftText);

                    // return expired book and remove it from the table
                    if ("Expired".equals(timeLeftText) || isTimeExpired(borrow)) {
                        String username = borrow.getBorrowerUsername();
                        Borrow.returnBook(borrow.getBorrowBook(), username);

                        // send notification to the user
                        if(username != null){
                            Notification notice = new Notification(username, Notification.NotificationType.EXPIRED, borrow.getBorrowBook().getBookTitle());
                            notice.save();
                        }

                        loadTableData(TableType.BORROW);
                        setupBorrow();
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        // set width
        borrowTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double tableWidth = newWidth.doubleValue();
            borTitleCol.setPrefWidth(tableWidth * 0.3);
            borAuthorCol.setPrefWidth(tableWidth * 0.15);
            borrowerCol.setPrefWidth(tableWidth * 0.15);
            borrowedOnCol.setPrefWidth(tableWidth * 0.2);
            timeLeftCol.setPrefWidth(tableWidth * 0.2);
        });

        borrowTable.setItems(borrowData);
        startCountdownTimer();
    }

    /**
     * Calculate if the borrowed book expired.
     * @param borrow the borrow record
     * @return if the borrowed book expired
     */
    private boolean isTimeExpired(Borrow borrow) {
        if (borrow == null || borrow.getBorrowedDateTime() == null || borrow.getDuration() == null) {
            return false;
        }

        LocalDateTime borrowedDateTime = borrow.getBorrowedDateTime();
        java.time.Duration duration = borrow.getDuration();
        LocalDateTime returnDateTime = borrowedDateTime.plus(duration);
        LocalDateTime now = LocalDateTime.now();

        java.time.Duration timeLeft = java.time.Duration.between(now, returnDateTime);

        return timeLeft.isZero() || timeLeft.isNegative();
    }

    /**
     * calculate the time difference between the borrowed DateTime
     * and the duration of this borrow action.
     * @param borrow the borrow record
     * @return the time left for the borrowed book
     */
    private String calculateTimeLeft(Borrow borrow) {
        if (borrow == null || borrow.getBorrowedDateTime() == null || borrow.getDuration() == null) {
            return "N/A";
        }

        LocalDateTime borrowedDateTime = borrow.getBorrowedDateTime();
        java.time.Duration duration = borrow.getDuration();

        LocalDateTime returnDateTime = borrowedDateTime.plus(duration);
        LocalDateTime now = LocalDateTime.now();

        java.time.Duration timeLeft = java.time.Duration.between(now, returnDateTime);

        if (timeLeft.isZero() || timeLeft.isNegative()) {
            return "Expired";
        }

        return formatDuration(timeLeft);
    }

    /**
     * Return the string in the format:<br>
     * "dd hh:mm:ss"
     * @param duration
     * @return the formated time string
     */
    private String formatDuration(java.time.Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        if (days > 0) {
            return String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format("0d %02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("0d 00:%02d:%02d", minutes, seconds);
        }
    }

    /**
     * This method set up the timer in the borrow table.
     */
    private void startCountdownTimer() {
        Timeline timeline = new Timeline(
                new KeyFrame(javafx.util.Duration.seconds(1), event -> {
                    borrowTable.refresh();
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /** Handle Publish Book Tab Data Loading and Display **/
    private void setupPublish(){
        // set the value of each column
        pubTitleCol.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        pubAuthorCol.setCellValueFactory(cellData -> {
            Book book = cellData.getValue();
            User author = User.selectUserByUsername(book.getAuthorUsername());
            return new SimpleStringProperty(author != null ? author.getFullName() : "N/A");
        });
        publishedOnCol.setCellValueFactory(new PropertyValueFactory<>("publishedDate"));
        timesBorrowedCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getTimesBorrowed())));
        pubActionsCol.setCellFactory(param -> new PublishActionsCell());

        // set width
        publishTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double tableWidth = newWidth.doubleValue();
            pubTitleCol.setPrefWidth(tableWidth * 0.3);
            pubAuthorCol.setPrefWidth(tableWidth * 0.17);
            publishedOnCol.setPrefWidth(tableWidth * 0.2);
            timesBorrowedCol.setPrefWidth(tableWidth * 0.13);
            pubActionsCol.setPrefWidth(tableWidth * 0.2);
        });

        publishTable.setItems(booksData);
    }

    /**
     * Table cell for the Action column of table in the Publish Book Tab
     */
    private class PublishActionsCell extends TableCell<Book, Void> {
        private final HBox buttonContainer = new HBox(5);
        private final Button viewBtn = new Button("View");
        private final Button deleteBtn = new Button("Delete");

        /**
         * Initialize the cell, create and add buttons to the cell.
         */
        public PublishActionsCell() {
            viewBtn.setOnAction(event -> handleViewAction());
            deleteBtn.setOnAction(event -> handleDeleteAction());

            viewBtn.setPrefWidth(70);
            deleteBtn.setPrefWidth(70);

            buttonContainer.getChildren().addAll(viewBtn, deleteBtn);
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setGraphic(null);
            } else {
                setGraphic(buttonContainer);
            }
        }

        /**
         * This method will display the book content in a view window.
         * This method is called when the <code>view</code> button is clicked.
         */
        private void handleViewAction(){
            try{
                // get book content
                Book book = getTableView().getItems().get(getIndex());
                String dir = book.getContentDirectory();
                String bookContent = book.viewBook(dir);

                // load view book fxml
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ViewWindow.fxml"));
                Parent windowRoot = loader.load();
                ViewWindowController winCtrl = loader.getController(); // Go to ViewWindow Controller

                winCtrl.setBookContent(bookContent);

                // Show in a new Stage (popup window)
                Stage stage = new Stage();
                stage.setTitle("Book Viewer");
                stage.setScene(new Scene(windowRoot, 700, 500));
                stage.show();
            } catch(IOException e){
                e.printStackTrace();
            }
        }

        /**
         * This method will delete the book from the database, regardless
         * how many people borrow the book. A confirmation box will show
         * before the execution of the delete action.
         * <br>
         * This method is called when the <code>delete</code>
         * button is clicked.
         */
        private void handleDeleteAction() {
            Book book = getTableView().getItems().get(getIndex());

            // set up confirmation dialog
            Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationDialog.setTitle("Confirmation");
            confirmationDialog.setHeaderText("Confirmation");
            confirmationDialog.setContentText("Delete \"" + book.getTitle() + "\"? All borrowing privileges for this book will be revoked.");

            DialogPane dialogPane = confirmationDialog.getDialogPane();
            dialogPane.setMinWidth(350);
            dialogPane.setMinHeight(175);
            dialogPane.setPrefWidth(350);
            dialogPane.setPrefHeight(175);

            ButtonType noButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType yesButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            confirmationDialog.getButtonTypes().setAll(yesButton, noButton);

            Optional<ButtonType> result = confirmationDialog.showAndWait();

            if (result.isPresent() && result.get() == yesButton) {
                // delete the borrow records of the book
                List<Borrow> borrows = Borrow.getAllBorrows();
                for(Borrow borrow: borrows){
                    Book borrowedBook = borrow.getBorrowBook();
                    if(borrowedBook.getBookTitle().equals(book.getBookTitle()) &&
                    borrowedBook.getAuthorUsername().equals(book.getAuthorUsername())){
                        // send notification to the borrowers
                        Notification borrowerNotice = new Notification(borrow.getBorrowerUsername(), Notification.NotificationType.DELETED, book.getBookTitle());
                        borrowerNotice.save();
                    }
                }

                // send notification to the author
                Notification authorNotice = new Notification(book.getAuthorUsername(), Notification.NotificationType.DELETED, book.getBookTitle());
                authorNotice.save();

                // delete the book and update the table
                book.deleteBook(book);
                getTableView().getItems().remove(book);
            }
        }
    }

    /** Log out back to the Home screen. **/
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