package library.user;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Notification {
    private User receiver;
    private String receiverUsername;
    private String message;

    public enum NotificationType {
        APPROVED, REJECTED, RETURNED, DELETED, EXPIRED
    }

    private static final String DATA_DIR = "data";
    private static final String DATA_FILE = DATA_DIR + "/notifications.txt";
    private static final List<Notification> noticeCache = new ArrayList<>();
    private static Boolean cacheLoaded = false;

    /**
     * Create a <code>Notification</code> object
     * @param receiver
     * @param message
     */
    public Notification(User receiver, String message){
        this.receiver = receiver;
        this.receiverUsername = receiver.getUsername();
        this.message = message;
    }

    /**
     * Create a <code>Notification</code> object with user, notification type and book title
     * @param receiver
     * @param type
     * @param bookTitle
     */
    public Notification(User receiver, NotificationType type, String bookTitle) {
        if (receiver == null) {
            throw new IllegalArgumentException("Receiver cannot be null");
        }

        if (bookTitle == null || bookTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("Book title cannot be null or empty");
        }

        if (type == null) {
            throw new IllegalArgumentException("Notification type cannot be null");
        }

        this.receiver = receiver;
        this.receiverUsername = receiver.getUsername();
        this.message = generateMessage(type, bookTitle.trim());
    }

    /**
     * Create a <code>Notification</code> object with user, notification type and book title
     * @param receiver
     * @param type
     * @param bookTitle
     */
    public Notification(String receiver, NotificationType type, String bookTitle) {
        this(validateAndGetUser(receiver), type, bookTitle);
    }

    /**
     * Check if the <code>username</code> is <b>valid</b> and the user
     * <b>exists</b> before creating a Notification object.
     * @param username
     * @return <code>User</code> object if the user exists
     */
    private static User validateAndGetUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Receiver username cannot be null or empty");
        }
        User user = User.selectUserByUsername(username.trim());
        if (user == null) {
            throw new UserDoesNotExistException(username);
        }
        return user;
    }

    /**
     * Generate the required message according to the message type and book title
     * @param type type of message
     * @param bookTitle title of the required book
     * @return message
     */
    private String generateMessage(NotificationType type, String bookTitle) {
        return switch (type) {
            case APPROVED -> String.format("Your book \"%s\" has been Approved!", bookTitle);
            case REJECTED -> String.format("The book \"%s\" has been Rejected!", bookTitle);
            case RETURNED -> String.format("The book \"%s\" you borrowed has been Returned!", bookTitle);
            case DELETED -> String.format("The book \"%s\" has been Deleted by librarian!", bookTitle);
            case EXPIRED -> String.format("The book \"%s\" you borrowed has been Expired!", bookTitle);
        };
    }

    /**
     * Create a <code>Notification</code> object with a String
     * @param line String with format username + "\t" + message
     */
    private Notification(String line){
        String[] notification = line.trim().split("\t");
        this.receiverUsername = notification[0];
        this.receiver = User.selectUserByUsername(this.receiverUsername);

        if (this.receiver == null) {
            throw new UserDoesNotExistException(this.receiverUsername);
        }

        this.message = notification[1];
    }

    /* getters and setters functions */
    public User getReceiver() {
        if (receiver == null && receiverUsername != null) {
            receiver = User.selectUserByUsername(receiverUsername);
            if (receiver == null) {
                throw new UserDoesNotExistException(receiverUsername);
            }
        }
        return receiver;
    }
    public void setReceiver(User receiver) {
        this.receiver = receiver;
        this.receiverUsername = receiver != null ? receiver.getUsername() : null;
    }
    public String getReceiverUsername(){ return this.receiverUsername; }
    public void setReceiverUsername(String username){
        this.receiverUsername = username;
        this.receiver = User.selectUserByUsername(username);
    }
    public String getMessage(){ return this.message; }
    public void setMessage(String message){ this.message = message; }

    /**
     * Initialize the text file in <code>DATA_FILE</code> for
     * storing user information.
     * <br>
     * The method will create a file if the file specifies in
     * <code>DATA_FILE</code> <b>does not exist</b>.
     */
    private static void initializeFile(){
        try{
            // create file if the data file does not exist
            Path path = Paths.get(DATA_FILE);
            if(!Files.exists(path)){
                Files.createFile(path);
            }
        } catch(IOException e){
            System.err.println("Fail to create user data file: " + e);
        }
    }

    /**
     * Load all users in the database to <code>List&#60;Notification&#62; noticeCache</code>
     */
    private static void loadAllNotifications(){
        initializeFile();
        noticeCache.clear();

        try{
            // load all notifications
            Path path = Paths.get(DATA_FILE);
            List<String> notifications = Files.readAllLines(path);

            // create Notification object list
            for(String notice : notifications){
                if(!notice.trim().isEmpty()){
                    try{
                        Notification newNotice = new Notification(notice.trim());
                        noticeCache.add(newNotice);
                    }
                    catch(Exception e) {
                        System.err.println("Fail to extract notification: " + e);
                    }
                }
            }
            cacheLoaded = true;
        } catch(IOException e){
            System.err.println("Fail to load all notifications: " + e);
        }
    }

    /**
     * Store all users in <code>List&#60;Notification&#62; noticeCache</code> to the database.
     * <br>
     * The param <code>notifications</code> <b>must not be null</b>.
     * @param notifications list of notifications that need to be stored in the database, cannot be null
     */
    private static void saveAllNotification(List<Notification> notifications){
        if(notifications == null){
            System.err.println("Notifications list cannot be null!");
            return;
        }

        initializeFile();

        try{
            // convert Notification objects to string and save
            Path path = Paths.get(DATA_FILE);
            List<String> notificationsList = notifications.stream()
                    .map(notice->notice.toString())
                    .collect(Collectors.toList());
            cacheLoaded = false;
            Files.write(path, notificationsList);
        } catch(IOException e){
            System.err.println("Fail to save all notifications: " + e);
        }
    }

    /**
     * Store the notification to the database. The receiver and message of
     * this notification must not exist in the database before calling this method.
     * <br>
     * The method only save the notification which <b>has not been stored before</b>.
     * @return <code>true</code> if the notification has been successfully saved to the database.
     */
    public Boolean save(){
        loadAllNotifications();

        if(this.message.length() == 0){
            throw new IllegalArgumentException("Invalid message!");
        }

        noticeCache.add(this);
        saveAllNotification(noticeCache);

        return true;
    }

    /**
     * Create Notification object and store it in the database.
     * The receiver and message of this notification must not
     * exist in the database before calling this method.
     * <br>
     * The method only save the notification which <b>has not been stored before</b>.
     * @param receiver
     * @param type
     * @param bookTitle
     * @return <code>true</code> if the required notification has been saved successfully
     */
    public static Boolean save(String receiver, NotificationType type, String bookTitle){
        Notification notification = new Notification(receiver, type, bookTitle);
        return notification.save();
    }

    /**
     * Returns the notification to the required receiver in the database.
     * @param receiver
     * @return the list of notifications (in chronological order)
     */
    public static List<Notification> findByUser(String receiver){
        loadAllNotifications();
        return noticeCache.stream()
                .filter(notice->notice.getReceiverUsername().equals(receiver))
                .collect(Collectors.toList()).reversed();
    }

    /**
     * Returns the required notification in the database.
     * This method will return null if the notification <b>does not exist in the database</b>.
     * @param n1 the target notification
     * @return notification or null
     */
    private Notification selectByNotification(Notification n1){
        return noticeCache.stream()
                .filter(n2 -> n2.equals(n1))
                .findFirst()
                .orElse(null);
    }

    /**
     * Return the notification received by the receiver.
     * @param receiver
     * @return the list of Notifications
     */
    public static List<Notification> selectByUser(String receiver){
        return findByUser(receiver);
    }

    /**
     * Removed the required notification in the database.
     * @param notification
     * @return if the notification has been deleted successfully
     */
    public static boolean deleteNotification(Notification notification){
        if(notification == null){
            return false;
        }

        loadAllNotifications();

        Boolean delete = noticeCache.remove(notification);

        if(delete){
            saveAllNotification(noticeCache);
            return true;
        }

        return false;
    }

    @Override
    public String toString(){
        return String.join("\t", receiverUsername, message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Notification notification = (Notification) o;

        return Objects.equals(receiverUsername, notification.receiverUsername) &&
                Objects.equals(message, notification.message);
    }

}
