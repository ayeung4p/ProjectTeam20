package library.user;

import java.util.ArrayList;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import org.mindrot.jbcrypt.BCrypt;

/**
 * This class consists methods to operate on User objects and
 * static methods to create, read, update and delete the users in the database.
 */
public class User {
    private String username;
    private String password;
    private String fullName;
    private Status status;
    private String statusDisplay;
    private Role role;
    private String roleDisplay;

    private static final String DATA_DIR = "data";
    private static final String DATA_FILE = DATA_DIR + File.separator +"user_data.txt";
    private static final List<User> usersCache = new ArrayList<>();
    private static boolean cacheLoaded = false;

    /**
     * Create a <code>User</code> object
     * @param username
     * @param password
     * @param fullName
     * @param status
     * @param role
     */
    public User(String username, String password, String fullName, Status status, Role role){
        this.username = username;
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
        this.fullName = fullName;
        setStatus(status.getDisplayName());
        this.role = role;
        this.roleDisplay = role.getDisplayName();
    }

    /**
     * Create a <code>User</code> object from strings
     * @param username
     * @param password
     * @param fullName
     * @param status
     * @param role
     */
    public User(String username, String password, String fullName, String status, String role){
        this(username, password, fullName,
                Status.fromString(status.toLowerCase()),
                Role.fromString(role.toLowerCase())
        );
    }

    /**
     * Create a <code>User</code> object from a string
     * <br>
     * This method can only be accessed within the class.
     * @param userInfo a String with format "username\t password\t fullName\t status\t role"
     */
    private User(String userInfo){
        String[] user = userInfo.trim().split("\t");
        this.username = user[0];
        this.password = user[1];
        this.fullName = user[2];
        setStatus(user[3].toLowerCase());
        this.role = Role.fromString(user[4].toLowerCase());
        this.roleDisplay = this.role.getDisplayName();
    }

    /* Getter and Setter functions */
    public String getUsername() {return username;}
    public void setPassword(String password) { this.password = BCrypt.hashpw(password, BCrypt.gensalt()); }
    public String getFullName() {return fullName;}
    public void setFullName(String fullName){this.fullName = fullName.trim();}
    public String getStatus(){return status.getDisplayName();}
    public String getStatusDisplay(){return this.statusDisplay;}
    public void setStatus(String status){
        this.status = Status.fromString(status);
        this.statusDisplay = switch (this.status){
            case ACTIVATED -> "true";
            case DEACTIVATED -> "false";
        };
    }
    public String getRole() {return role.getDisplayName();}
    public String getRoleDisplay(){return this.roleDisplay;}

    /**
     * Check if the <code>username</code> is valid. A <b>valid</b> <code>username</code>
     * should include Upper case letter, Lower case letter, number, '-' or '_'.
     * It should consist of <b>at least an English letter</b>
     * and should not be composed by pure number.
     * @param username
     * @return if the username is valid
     */
    public static Boolean validateUsername(String username){
        if(username == null || username.trim().isEmpty()){
            return false;
        }

        String trimmedUsername = username.trim();

        if (!trimmedUsername.matches("^[a-zA-Z0-9_-]+$") ||
                trimmedUsername.matches("^[0-9]+$") ||
                !trimmedUsername.matches(".*[a-zA-Z].*")) {
            return false;
        }

        return true;
    }

    /**
     * Check if the <code>password</code> is valid. A <b>valid</b> <code>password</code> should be a
     * <b>combination of Upper case letter, Lower case letter and Number</b>.
     * @param password
     * @return if the password is valid
     */
    public static Boolean validatePassword(String password){
        if(password == null || password.trim().isEmpty()){
            return false;
        }

        if (!password.matches(".*[a-z].*") ||
                !password.matches(".*[A-Z].*") ||
                !password.matches(".*[0-9].*")) {
            return false;
        }

        return true;
    }

    /**
     * Check the length of the username
     * @param username
     * @return if the length of the username is acceptable
     */
    public static Boolean checkUsernameLength(String username){
        if(username == null) return false;
        int len = username.length();

        if(len <= 30 && len >= 4) return true;
        else return false;
    }

    /**
     * Check the length of the full name
     * @param fullname
     * @return if the length of the fullname is acceptable
     */
    public static Boolean checkFullnameLength(String fullname) {
        if (fullname == null) return false;
        int len = fullname.length();

        if (len <= 50 && len >= 2) return true;
        else return false;
    }

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
            Path dir = path.getParent();

            if(!Files.exists(dir)){
                Files.createDirectories(dir);
            }
            if(!Files.exists(path)){
                Files.createFile(path);
            }
        } catch(IOException e){
            System.err.println("Fail to create user data file: " + e);
        }
    }

    /**
     * Load all users in the database to <code>List&#60;User&#62; usersCache</code>
     */
    private static void loadAllUsers(){
        initializeFile();
        usersCache.clear();

        try{
            // load all users from the data file, convert to User object and store in a list
            Path path = Paths.get(DATA_FILE);
            List<String> users = Files.readAllLines(path);
            for(String user : users){
                if(!user.trim().isEmpty()){
                    try{
                        User newUser = new User(user.trim());
                        usersCache.add(newUser);
                    }
                    catch(Exception e) {
                        System.err.println("Fail to extract user: " + e);
                    }
                }
            }
            cacheLoaded = true;
        } catch(IOException e){
            System.err.println("Fail to load all users: " + e);
        }
    }

    /**
     * Store all users in <code>List&#60;User&#62; usersCache</code> to the database.
     * <br>
     * The param <code>users</code> <b>must not be null</b>.
     * @param users the list of users that need to be stored in the database, cannot be null
     */
    private static void saveAllUsers(List<User> users){
        if(users == null){
            System.err.println("Users list cannot be null!");
            return;
        }

        initializeFile();

        try{
            // convert the User objects in list and save
            Path path = Paths.get(DATA_FILE);
            List<String> usersList = users.stream()
                    .map(user->user.toString())
                    .collect(Collectors.toList());
            cacheLoaded = false;
            Files.write(path, usersList);
        } catch(IOException e){
            System.err.println("Fail to save all users: " + e);
        }
    }

    /**
     * Return all users in the database.
     * @return user list
     */
    public static List<User> getAllUsers(){
        loadAllUsers();
        return new ArrayList<>(usersCache);
    }

    /**
     * Store the information of this user to the database. The username of
     * this user must not exist in the database before calling this method.
     * <br>
     * The method only save the information of the user which <b>has not been stored before</b>.
     * @return <code>true</code> if the user information has been successfully saved to the database.
     */
    public Boolean save(){
        loadAllUsers();

        List<User> existingUsers = usersCache.stream()
                        .filter(user -> user.getUsername().equals(this.username))
                        .collect(Collectors.toList());

        if(!existingUsers.isEmpty()){
            throw new UserAlreadyExistsException(this.username);
        }

        usersCache.add(this);

        saveAllUsers(usersCache);

        return true;
    }

    /**
     * Store the information of the required user to the database. The username of
     * the user must not exist in the database before calling this method.
     * <br>
     * The method only save the information of the user which <b>has not been stored before</b>.
     * @param user
     * @return <code>true</code> if the user information has been successfully saved to the database.
     */
    public static Boolean save(User user){
        return user.save();
    }

    /**
     * Update the user information in the database.
     * <br>
     * The user must <b>exist in the database</b> before calling this method.
     * @return if the user information has been successfully updated in the database
     */
    public Boolean updateUser(){
        loadAllUsers();

        User targetUser = findByUsername(this.username);

        if(targetUser == null){
            throw new UserDoesNotExistException(this.username);
        }
        else{
            usersCache.remove(targetUser);
            usersCache.add(this);
            saveAllUsers(usersCache);
        }

        return true;
    }

    /**
     * Update the user information in the database.
     * <br>
     * The user must <b>exist in the database</b> before calling this method.
     * @param username
     * @param fullName
     * @param status
     * @return <code>true</code> if the user information has been successfully updated in the database
     */
    public static Boolean updateUser(String username, String fullName, String status) {
        User existingUser = findByUsername(username);
        if (existingUser == null) {
            throw new UserDoesNotExistException(username);
        }

        existingUser.setFullName(fullName);
        existingUser.setStatus(status);
        return existingUser.updateUser();
    }

    /**
     * Returns the user with the same username as the argument in the database.
     * This method will return null if the user <b>does not exist in the database</b>.
     * @param username
     * @return the target user or null
     */
    private static User findByUsername(String username) {
        loadAllUsers();
        return usersCache.stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the user with the same username as the argument.
     * This method will return null if the user does not exist.
     * @param username
     * @return the target user or null
     */
    public static User selectUserByUsername(String username){
        return findByUsername(username);
    }

    /**
     * Returns the total number of user stored in the database.
     * @return total number of user
     */
    public static Integer getUserCount(){
        loadAllUsers();
        return usersCache.size();
    }

    /**
     * Check if the user already exists in the database.
     * @param username
     * @return whether the username exists
     */
    public static Boolean usernameExists(String username){
        return findByUsername(username) != null;
    }

    /**
     * Check if the password is the same as the password of this user.
     * @param password the password for comparison
     * @return if the password is the same as this user
     */
    public Boolean checkPassword(String password){
        return BCrypt.checkpw(password, this.password);
    }

    @Override
    public String toString(){
        return String.join("\t",
                            username, password, fullName,
                            status.getDisplayName(),
                            role.getDisplayName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        User user = (User) o;

        return Objects.equals(username, user.username);
    }
    /*
     * attributes: private
     * username
     * password
     * full name
     * status (activate, deactivate)
     * role (student/staff, author, librarian)
     * methods:
     * static getAllUsers() -- Librarian
     * static createUser()
     * updateUser()
     * updateUserStatus()
     * static selectUserByUsername() : User
     * checkPassword(string password)
     * getters: getUsername(), getFullName()
     * setters: setFullName(), setPassword()
     * */
}
