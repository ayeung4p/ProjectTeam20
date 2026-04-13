package library.user;

public class UserDoesNotExistException extends RuntimeException {
    public UserDoesNotExistException(String username) {
        super("User " + username + " does not exist!");
    }
}
