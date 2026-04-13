package library.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import library.Main;
import library.book.Book;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;

import library.book.Book;
import library.book.BookStatus;
import library.user.User;
import library.user.Notification;
import library.book.Borrow;

import static library.book.Book.*;
import static library.user.Notification.findByUser;
import static library.user.User.validatePassword;


public class AuthorDashboardController {
    private User currUser;
    // Attributes:
    @FXML
    private TabPane tabPane;
    // Tab 1 -- My Books
    @FXML private TableView<Book> myBooksTable;
    @FXML private TableColumn<Book, String> titleCol;
    @FXML private TableColumn<Book, String> statusCol;
    @FXML private TableColumn<Book, String> dateCol;
    @FXML private TableColumn<Book, Integer> readersCol;
    @FXML private TableColumn<Book, String> abstractCol;
    @FXML private TextArea descriptionBox;
    // View Button Part
    @FXML private Button myBooksView;
    @FXML private TextArea bookContentArea;
    @FXML private Button authorViewZoomIn;
    @FXML private Button authorViewZoomOut;
    @FXML private Button authorViewOK;
    private double fontSize = 16.0;
    // Modify Button Part
    @FXML private Button myBooksModify;
    // Delete button Part
    @FXML private Button myBooksDelete;

    // Tab 2 -- Publish New Book
    @FXML private TextField newBookTitle;
    @FXML private TextField newBookFilename;
    @FXML private TextArea newBookAbstract;
    @FXML private Button newBookChooseDir;
    @FXML private Button newBookPublish;
    private File selectedBookFile;
    // Tab 3 -- Stats Review
    @FXML private Tab StatusViewTab;
    @FXML private PieChart statusGraph;
    @FXML private BarChart<String, Number> top5Graph;
    @FXML private Button statusRefresh;
    @FXML private Label bookCount;
    private Boolean firstTime = true;
    // Tab 4 -- Manage Profile
    @FXML private Tab MyProfileTab;
    @FXML private Label profileUsername;
    @FXML private TextField profileNewFullname;

    @FXML private PasswordField profileNewPassword;
    @FXML private TextField profileNewPasswordTextField; // Initially invisible
    @FXML private ToggleButton profileNewPWHS;

    @FXML private PasswordField profileConfirmationPassword;
    @FXML private TextField profileConfirmationPasswordTextField; // Initially invisible
    @FXML private ToggleButton profileConfirmationPWHS;

    @FXML private Button profileUpdate;

    // Tab 5 -- Inform Board
    @FXML private Tab InformBoardTab;
    @FXML private ListView<Notification> informBoardListView;
    @FXML private Button informBoardClearAll;
    @FXML private Label notificationLabel;




    /** Called by FXMLLoader after all @FXML fields are injected. */
    @FXML
    private void initialize() {

        // Setting Up Tab 1 -- My Books
        ensureStorageStructure();
        setupTableColumns();
        setUpListeners();
        //initDescriptionBox();

        // Setting up Tab 5 -- Inform Board
        setUpInformBoard();
        // Setting up tab 3(View Stats), 4(Manage Profile), 5(Inform board) -- When the tab is opened
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == StatusViewTab && firstTime == true) {
                handleStatusRefresh();
                firstTime = false;
            }
            if (newTab == MyProfileTab) {
                setProfileUsername(); // Only called when the profile tab is selected
                setProfileFullname(); // Only called when the profile tab is selected
            }
            if (newTab == InformBoardTab) {
                loadNotifications();
            }

        });

        profileNewPasswordTextField.setVisible(false);
        profileNewPassword.setVisible(true);
        profileConfirmationPasswordTextField.setVisible(false);
        profileConfirmationPassword.setVisible(true);

    }
    /**
     * Helper function called by initialize().
     * Set up table size and table column title.
     */
    private void setupTableColumns() {
        titleCol.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        abstractCol.setCellValueFactory(new PropertyValueFactory<>("bookAbstract"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("publishedDate"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        readersCol.setCellValueFactory(cellData -> {
            Book book = cellData.getValue();
            Integer count = Borrow.countBorrowed(book);
            return new SimpleIntegerProperty(count).asObject();
        });

        myBooksTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double tableWidth = newWidth.doubleValue();
            titleCol.setPrefWidth(tableWidth * 0.2);
            abstractCol.setPrefWidth(tableWidth * 0.5);
            dateCol.setPrefWidth(tableWidth * 0.125);
            statusCol.setPrefWidth(tableWidth * 0.1);
            readersCol.setPrefWidth(tableWidth * 0.075);
        });
    }

    /**
     * Helper function called by initialize().
     * Set up interactive clickable row.
     * When a row is selected, extract title and abstract and set up the description box
     */
    private void setUpListeners() {
        myBooksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Simulate label spacing using String.format and wrap content manually if needed
                String titleLine = String.format("%-9s%s", "Title:", newSelection.getBookTitle());
                String abstractLine = String.format("%-9s%s", "Abstract:", newSelection.getBookAbstract());
                descriptionBox.setWrapText(true); // Ensures text wraps
                descriptionBox.setText(titleLine + "\n\n" + abstractLine);
            } else {
                descriptionBox.setWrapText(true);
                descriptionBox.setText("Please choose a row for detail description.");
            }
        });
    }


    /**
     * Helper function called by login controller
     * Set up the current user and retrieve all the books by this author user
     * @param user the current user using the system
     */
    protected void setUser(User user){
        this.currUser = user;
        loadUserBooks();
    }

    /**
     * Helper function called by setUser()
     * Retrieve all the pending / approved book by this user
     */
    private void loadUserBooks() {
        if (currUser == null) return;
        List<Book> books = getBookByAuthor(currUser.getUsername());
        ObservableList<Book> data = FXCollections.observableArrayList(books);
        myBooksTable.setItems(data);

    }

    /**
     * Helper function called by initialize()
     * Set up notification in inform board row by row with delete button dynamically
     * Access notification txt file and retrieve notification by this user
     */
    private void setUpInformBoard() {
        informBoardListView.setCellFactory(lv -> new ListCell<Notification>() {
            public final Button deleteButton = new Button("Delete");

            {
                deleteButton.setOnAction(event -> {
                    Notification notification = getItem();
                    if (notification != null) {
                        Notification.deleteNotification(notification); // DB
                        getListView().getItems().remove(notification); // UI

                        // Check if the entire ListView is empty after deletion
                        if (getListView().getItems().isEmpty()) {
                            handleEmptyNotificationBoard();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Notification item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    // Label for left-aligned message
                    Label messageLabel = new Label(item.getMessage());
                    messageLabel.setMaxWidth(Double.MAX_VALUE); // Allow expansion
                    messageLabel.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 0 15 0 8;");

                    // Delete button for right-aligned action
                    Button deleteButton = new Button("Delete");
                    deleteButton.setOnAction(event -> {
                        Notification notification = getItem();
                        if (notification != null) {
                            Notification.deleteNotification(notification);
                            getListView().getItems().remove(notification);

                            // Check if the entire ListView is empty after deletion
                            if (getListView().getItems().isEmpty()) {
                                handleEmptyNotificationBoard();
                            }
                        }
                    });

                    // HBox layout: spacing + alignment
                    HBox row = new HBox(messageLabel, deleteButton);
                    row.setSpacing(20);                          // Increase spacing between label and button
                    row.setFillHeight(true);
                    row.setPrefHeight(40);                       // Increase row height for more spacing
                    HBox.setHgrow(messageLabel, Priority.ALWAYS);// Message expands to fill space

                    // Align button to right using region expansion
                    // (already done by HBox.setHgrow)

                    setGraphic(row);
                }
            }
        });
    }
    /**
     * Helper function called by setUpInformBoard() and inform board related methods
     * Simply set the label in inform board.
     */
    private void handleEmptyNotificationBoard() {
        notificationLabel.setText("There is currently no notification");
    }

    /**
     * Helper function called throughout the whole controller
     * Make a pop-up window alert according to input message and title.
     * @param title
     * @param message
     */
    private void showSimpleAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait(); // Only closes when user clicks OK
        });
    }

    /**
     * Helper function called in inform board related methods
     * Make a pop-up window information according to input message and title.
     * @param title
     * @param message
     */
    private void showSimpleInformation(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait(); // Only closes when user clicks OK
        });
    }

    // The following function are related to buttons
    // Tab 1 -- ViewButton Handling
    /**
     * Called when View Button is selected in My Books Tab
     * Retrieve the book from user selected row and pass the controller to ViewWindowController
     * for further view features
     * @param event
     */
    @FXML
    private void handleMyBooksView(ActionEvent event) {
        try {

            Book selectedBook = myBooksTable.getSelectionModel().getSelectedItem();
            if (selectedBook == null) { // Warning the user to select a book first
                showSimpleAlert("Selection Error", "Please select a book first.");
                return;
            }
            String dir = selectedBook.getContentDirectory();
            String bookContent = viewBook(dir);
            System.out.println("bookContent: " + bookContent); // Debug output
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ViewWindow.fxml"));
            Parent windowRoot = loader.load();
            ViewWindowController winCtrl = loader.getController(); // Go to ViewWindow Controller

            winCtrl.setBookContent(bookContent);

            // Show in a new Stage (popup window)
            Stage stage = new Stage();
            stage.setTitle("Book Viewer");
            stage.setScene(new Scene(windowRoot, 700, 500));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    // Tab 1 -- EditButton Handling
    /**
     * Called when Edit Button is selected in My Books Tab
     * Retrieve the book from user selected row and pass the controller to EditWindowController
     * for further edit features
     * @param event
     */
    @FXML private void handleMyBooksModify(ActionEvent event) {
        try {
            Book selectedBook = myBooksTable.getSelectionModel().getSelectedItem();
            if (selectedBook == null) {
                // Show alert
                showSimpleAlert("Selection Error", "Please select a book to edit.");
            }
            else if (selectedBook.getStatus() == BookStatus.APPROVED && selectedBook.getBorrowed() == true) {
                // Show alert
                showSimpleAlert("Selection Error", "Approved Book that is currently being borrowed cannot be edit.");
            }
            else {
                // Load the Edit Window (not ViewWindow) FXML:
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditWindow.fxml")); // Or your edit window FXML path
                Parent windowRoot = loader.load();
                EditWindowController editCtrl = loader.getController();

                // Pass current book to the popup controller
                editCtrl.setBook(selectedBook);

                editCtrl.setBookEditListener(editedBook -> {
                    loadUserBooks();
                });

                Stage stage = new Stage();
                stage.setTitle("Edit Book");
                stage.setScene(new Scene(windowRoot));
                stage.show();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Tab 1 -- Delete Button Handling
    /**
     * Called when Delete Button is selected in My Books Tab
     * Retrieve the book from user selected row and provide a double confirming window
     * Delete the book from the system if user confirm deletion
     * @param event
     */
    @FXML private void handleMyBooksDelete(ActionEvent event) {
        try {
            Book selectedBook = myBooksTable.getSelectionModel().getSelectedItem();
            if (selectedBook == null) {
                // Show alert
                showSimpleAlert("Selection Error", "Please Choose A Book to Delete.");
            }
            else if (selectedBook.getStatus() == BookStatus.APPROVED && selectedBook.getBorrowed() == true) {
                // Show alert
                showSimpleAlert("Invalid Selection", "Approved Book that is currently being borrowed cannot deleted.");
            }
            else {
                // Show confirmation window
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Confirmation");
                alert.setHeaderText("Are you sure you want to delete this book?");
                alert.setContentText("This action cannot be undone.");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK){
                    // proceed with delete
                    deleteBook(selectedBook);
                    loadUserBooks();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Tab 2 -- Publish Book Handling
    /**
     * Called when Choose Text File Button is selected in Publish Book Tab
     * Open the default OS file manager and save the directory of the file
     * Only allow users to choose .txt file
     * @param event
     */
    @FXML private void handleNewBookChooseDir(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Book Text File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        Stage stage = (Stage) newBookChooseDir.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedBookFile = file; // Save the selected file for later use
            newBookFilename.setText(file.getName()); // Only show the filename in the TextField
        }
    }

    /**
     * Called when Publish Button is selected in My Books Tab
     * Retrieve the input data and publish the book if all the fields are vaild
     * @param event
     */
    @FXML private void handleNewBookPublish(ActionEvent event) {
        String title = newBookTitle.getText().trim();
        String bookAbstract = newBookAbstract.getText().trim();
        // selectedBookFile holds your hidden full directory
        if (title.isEmpty() || bookAbstract.isEmpty() || selectedBookFile == null) {
            // Show alert
            showSimpleAlert("Selection Error", "Title, Selected File or Summary should not be empty");
        }
        else {

            Boolean publishStatus = publishBook(title, bookAbstract, currUser.getUsername(), selectedBookFile.getPath());
            // Show Hint
            if (!publishStatus) {
                showSimpleAlert("Book Publish Error", "Identical Book Exist");
            } else {
                showSimpleAlert("Hint", "Publication Successful, Waiting Approval");

                newBookTitle.clear();       // clears title input
                newBookFilename.clear();        // clears file name display
                newBookAbstract.clear();    // clears abstract input
                selectedBookFile = null;      // forgets the last selected file (for safety)
                loadUserBooks();
            }
        }
    }

    // Tab 3 -- Stats Review
    /**
     * Called when refresh button is clicked on Status View Tab
     * Re-calculate the statistic of two graph and show on screen
     */
    @FXML
    private void handleStatusRefresh() {
        // Get new stats
        int[] StatusNumber = getStatusNumber(currUser.getUsername());
        List<Book> newTop5Books = getBestFiveBooks(currUser.getUsername());

        updatePieChart(StatusNumber[0], StatusNumber[1], StatusNumber[2]);
        updateBarChart(newTop5Books);
    }

    /**
     * Helper function called in handleStatusRefresh
     * Retrieve the current statistic for pie chart and update pie chart
     * @param pending
     * @param approved
     * @param rejected
     */
    private void updatePieChart(int pending, int approved, int rejected) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Pending", pending),
                new PieChart.Data("Approved", approved),
                new PieChart.Data("Rejected", rejected)
        );

        statusGraph.setData(pieData);

        for (PieChart.Data data : statusGraph.getData()) {
            data.nameProperty().set(data.getName() + " (" + (int)data.getPieValue() + ")");
        }
    }
    /**
     * Helper function called in handleStatusRefresh
     * Retrieve the current statistic for bar chart and update bar chart
     * @param books List of best five book in descending order
     */
    private void updateBarChart(List<Book> books) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Book b : books.subList(0, Math.min(5, books.size()))) {
            series.getData().add(new XYChart.Data<>(b.getTitle(), b.getTimesBorrowed()));
        }
        top5Graph.setCategoryGap(15); // default is 25, decrease for less space between groups
        top5Graph.setBarGap(3);
        top5Graph.getData().setAll(series);

        StringBuilder sb = new StringBuilder();
        for (Book b : books) {
            sb.append(b.getTitle()).append(" (").append(b.getTimesBorrowed()).append(")\n");
        }
        bookCount.setText(sb.toString());

    }

    // Tab 4 -- Profile Update
    /**
     * Helper function called in initialize()
     * Set the username in Profile Update Tab
     */
    @FXML
    private void setProfileUsername() {
        if (currUser == null) return;
        profileUsername.setText(currUser.getUsername());
    }
    /**
     * Helper function called in initialize()
     * Set the fullname in Profile Update Tab
     */
    @FXML
    private void setProfileFullname() {
        if (currUser == null) return;
        profileNewFullname.setText(currUser.getFullName());
    }

    /**
     * Called when user clicked on Hide/Show button in NewPassword row
     * Toggle the Textfield from password to text or vice versa
     */
    @FXML private void handleNewPWHS() {
        if (profileNewPWHS.isSelected()) {
            profileNewPasswordTextField.setText(profileNewPassword.getText());
            profileNewPasswordTextField.setVisible(true);
            profileNewPassword.setVisible(false);
            profileNewPWHS.setText("Hide");
        } else {
            profileNewPassword.setText(profileNewPasswordTextField.getText());
            profileNewPassword.setVisible(true);
            profileNewPasswordTextField.setVisible(false);
            profileNewPWHS.setText("Show");
        }
    }
    /**
     * Called when user clicked on Hide/Show button in ConfirmPassword row
     * Toggle the Textfield from password to text or vice versa
     */
    @FXML private void handleConfirmationPWHS() {
        if (profileConfirmationPWHS.isSelected()) {
            profileConfirmationPasswordTextField.setText(profileConfirmationPassword.getText());
            profileConfirmationPasswordTextField.setVisible(true);
            profileConfirmationPassword.setVisible(false);
            profileConfirmationPWHS.setText("Hide");
        } else {
            profileConfirmationPassword.setText(profileConfirmationPasswordTextField.getText());
            profileConfirmationPassword.setVisible(true);
            profileConfirmationPasswordTextField.setVisible(false);
            profileConfirmationPWHS.setText("Show");
        }
    }

    /**
     * Called when user clicked on Update button in Update Profile Tab
     * Update the user aata if all the fields are vaild
     * Show according alert if not
     * @param event
     */
    @FXML private void handleProfileUpdate(ActionEvent event) {
        String fullName = profileNewFullname.getText().trim();
        // Extract password and confirmation password from visible field
        String password;
        if (profileNewPassword.isVisible()) {
            password = profileNewPassword.getText().trim();
        } else {
            password = profileNewPasswordTextField.getText().trim();
        }

        String confirmPassword;
        if (profileConfirmationPassword.isVisible()) {
            confirmPassword = profileConfirmationPassword.getText();
        } else {
            confirmPassword = profileConfirmationPasswordTextField.getText();
        }
        if(fullName.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
            showSimpleAlert("Empty Input", "Please enter new information to update your profile.");
        }
        else if(!User.checkFullnameLength(fullName)){
            showSimpleAlert("Invalid Full Name", "Full name must be 4 to 50 characters long.");
        }
        else if (password.length() < 8) {
            showSimpleAlert("Invalid Password", "Password must be at least 8 characters long.");
        }
        else if (!validatePassword(password)) {
            showSimpleAlert("Invalid Password", "Password must contain at least one uppercase letter, one lowercase letter, and one number.");
        }
        else if (!password.equals(confirmPassword)) {
            showSimpleAlert("Password Mismatch", "New password and confirmation password do not match.");
        }


        else {
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
                        showSimpleAlert("Success", "Profile updated successfully!");
                        // Clear Text Field
                        profileNewPassword.setText("");
                        profileNewPasswordTextField.setText("");
                        profileConfirmationPassword.setText("");
                        profileConfirmationPasswordTextField.setText("");
                        // Reset toggle buttons
                        if (!profileNewPassword.isVisible()) {
                            if (profileNewPWHS.isSelected()) {
                                profileNewPWHS.setSelected(false); // Set to "viewing" state first
                            }
                            handleNewPWHS(); // Toggle back to hide
                        }
                        if (!profileConfirmationPassword.isVisible()) {
                            if (profileConfirmationPWHS.isSelected()) {
                                profileConfirmationPWHS.setSelected(false); // Set to "viewing" state first
                            }
                            handleConfirmationPWHS(); // Toggle back to hide
                        }
                    } else {
                        showSimpleAlert("Update Failed", "Failed to update profile in database.");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    showSimpleAlert("Update Error", "An error occurred while updating your profile: " + e.getMessage());
                }
            } else {
                showSimpleAlert("No Changes", "No changes were made to your profile.");
            }
        }
    }

    // Tab 5 -- Notification Board
    /**
     * Called when user clicked on Clear All button in Inform Board Tab
     * Delete all the existing notification shown on the board
     */
    @FXML
    private void handleClearAll() {
        ObservableList<Notification> notices = informBoardListView.getItems();
        if (notices.isEmpty()) {
            showSimpleInformation( "No Notifications", "There are no notifications to clear.");
            return;
        }
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Clear All Notifications");
        confirmAlert.setHeaderText("Confirm Clear All");
        confirmAlert.setContentText("Are you sure you want to clear all " + notices.size() + " notifications?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK){

            List<Notification> toDelete = new ArrayList<>(notices);
            for (Notification notice : toDelete) {
                Notification.deleteNotification(notice); // remove from DB
            }
            notices.clear(); // clear all in UI
            handleEmptyNotificationBoard();

        }


    }
    /**
     * Helper function called in initialize()
     * Load all the notification owned by this user, and show them on the inform board
     */
    public void loadNotifications() {
        ObservableList<Notification> notifications =
                FXCollections.observableArrayList(Notification.selectByUser(currUser.getUsername()));
        informBoardListView.setItems(notifications);
        if (notifications.isEmpty()) {
            notificationLabel.setText("There is currently no notification");
        }
        else {
            notificationLabel.setText("Notification List:");
        }
    }


    /** Log out back to the Home screen. */
    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Home.fxml"));
            Stage st = Main.getPrimaryStage();
            st.setScene(new Scene(root, 640, 480));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
