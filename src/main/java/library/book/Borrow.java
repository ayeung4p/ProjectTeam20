package library.book;

import library.user.Notification;
import library.user.User;


import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static library.book.Book.getParticularBook;
import static library.book.Book.incrementNoOfTimeBorrowed;
import static library.user.Role.STUDENT;
import static library.user.User.selectUserByUsername;
import static library.user.User.usernameExists;


// database: "borrows.txt"
/*
* format: (separated by \t)
<String>bookTitle    <String>authorUsername    <String>borrowerUsername    <Duration>duration    <LocalDateTime>borrowedDateTime
* example:
Java Programming    johnDoe    alice123    P2D    2023-10-01T10:15:30
 */

public class Borrow {
    // Attributes:
    private Book book;
    private final String borrowerUsername;
    private Duration duration;
    private LocalDateTime borrowedDateTime;

    // Constructors:
    /**
     * Create a Borrow object when a user borrows a book
     * (with a pre-created duration object)
     * @param book
     * @param borrowerUsername
     * @param duration
     */
    public Borrow(Book book, String borrowerUsername, Duration duration){
        this.book = book;
        this.borrowerUsername = borrowerUsername;
        this.duration = duration;
        this.borrowedDateTime = LocalDateTime.now();
    }

    /**
     * Create a Borrow object when a user borrows a book
     * (with separate minutes and seconds)
     * @param book
     * @param borrowerUsername
     * @param minutes
     * @param seconds
     */
    public Borrow(Book book, String borrowerUsername, int minutes, int seconds){
        this(book, borrowerUsername, Duration.ofMinutes(minutes).plusSeconds(seconds));
    }

    /**
     * Constructor for loading existing borrow records
     * @param book
     * @param borrowerUsername
     * @param duration
     * @param borrowedDateTime
     */
    public Borrow(Book book, String borrowerUsername, Duration duration, LocalDateTime borrowedDateTime){
        this.book = book;
        this.borrowerUsername = borrowerUsername;
        this.duration = duration;
        this.borrowedDateTime = borrowedDateTime;
    }



    // Methods:
    private static final List<Borrow> currentBorrows = new ArrayList<>();
    private static final String BORROWS_FILE_PATH = "data" + File.separator + "borrows.txt";


    /**
     * Ensure data directory exists for Borrow class
     */
    public static void ensureDataDirectory() {
        File dataDir = new File("data");
        if (!dataDir.exists()) dataDir.mkdir();

        File borrowsFile = new File(BORROWS_FILE_PATH);
        try {
            if (!borrowsFile.exists()) {
                borrowsFile.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("Could not create " + BORROWS_FILE_PATH + ": " + e.getMessage());
        }
    }

    /**
     * helper function: load all borrowing records in the database.
     */
    private static void loadAllBorrows(){
        ensureDataDirectory();
        currentBorrows.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(BORROWS_FILE_PATH))){
            String line;
            while((line = br.readLine()) != null){
                String[] parts = line.split("\t");
                if ( parts.length < 5) continue;        // Skip invalid lines

                String bookTitle = parts[0];
                String authorUsername = parts[1];
                String borrowerUsername = parts[2];
                Duration duration = Duration.parse(parts[3]);
                LocalDateTime borrowedDateTime = LocalDateTime.parse(parts[4]);

                Book book = getParticularBook(bookTitle, authorUsername);
                if(!usernameExists(borrowerUsername) || book == null){continue;}     // Skip this entry if the user or book is not found

                Borrow borrow = new Borrow(book, borrowerUsername, duration, borrowedDateTime);
                currentBorrows.add(borrow);
            }
        } catch (IOException e){
            System.err.println("Fail to load all borrowing records: " + e);
        }
    }

    /**
     * helper function: save all borrowing records into the file.
     */
    private static void saveBorrowsToFile() {
        ensureDataDirectory();
        try (PrintWriter writer = new PrintWriter(new FileWriter(BORROWS_FILE_PATH))) {
            for (Borrow borrow: currentBorrows) {
                writer.println(borrow.getBorrowBook().getTitle() + "\t" +
                        borrow.getBorrowBook().getAuthorUsername() + "\t" +
                        borrow.getBorrowerUsername() + "\t" +
                        borrow.getDuration() + "\t" +
                        borrow.getBorrowedDateTime());
            }
        } catch (IOException e) {
            System.err.println("Error saving borrow records: " + e.getMessage());
        }
    }

    /**
     * Return all current borrowing records in the database.
     * @return a list of Borrow objects
     */
    public static List<Borrow> getAllBorrows(){
        loadAllBorrows();
        return new ArrayList<>(currentBorrows);
    }

    /**
     * Return borrowing records of a particular user.
     * @param username
     * @return a list of Borrow objects associated with the user
     */
    public static List<Borrow> getBorrowByUser(String username){
        // Expired then return
        returnExpiredBook();

        // Check existence of User
        if (!usernameExists(username)) {
            return new ArrayList<>(); // Return an empty list if the username is null
        }

        // Filter currentBorrows for entries that match the user's username
        loadAllBorrows();
        List<Borrow> userBorrows = new ArrayList<>();
        for (Borrow borrow: currentBorrows) {
            if (borrow.borrowerUsername.equals(username)) {
                userBorrows.add(borrow);
            }
        }

        return userBorrows;
    }

    /**
     * Borrow a book by a user.
     * @param book
     * @param username
     * @param duration
     * @return true if successful, false otherwise
     */
    public static boolean borrowBook(Book book, String username, Duration duration) {
        loadAllBorrows();

        if(!usernameExists(username)){return false;}


        // Check if valid borrowing
        if (!(selectUserByUsername(username).getRole().equals("Student")) || book == null || book.getBookStatus() != BookStatus.APPROVED) {
            return false;
        }
        for (Borrow borrow: currentBorrows) {
            if (borrow.getBorrowBook().getBookTitle().equals(book.getBookTitle()) && borrow.getBorrowerUsername().equals(username)) {
                System.out.println("error2");
                return false; }

        }

        // Update book details
        incrementNoOfTimeBorrowed(book);
        if (!book.getBorrowed()) { book.setIsBorrowed(book, true); }

        // Update borrow records
        Borrow newBorrow = new Borrow(book, username, duration);
        currentBorrows.add(newBorrow);
        saveBorrowsToFile();

        return true;
    }
    /**
     * Same: Borrow a book by a user.
     * @param book
     * @param username
     * @param minutes
     * @param seconds
     * @return true if successful, false otherwise
     */
    public static boolean borrowBook(Book book, String username, int minutes, int seconds) {
        return borrowBook(book, username, Duration.ofMinutes(minutes).plusSeconds(seconds));
    }


    /**
     * Return a book by a user.
     * @param book
     * @param username
     * @return true if successful, false otherwise
     */
    public static boolean returnBook(Book book, String username){
        loadAllBorrows();

        // Check validation
        Borrow borrowToRemove = null;
        for (Borrow borrow: currentBorrows) {
            if (borrow.getBorrowBook().getTitle().equals(book.getTitle()) && borrow.getBorrowerUsername().equals(username)) {
                borrowToRemove = borrow;
                break;
            }
        }
        if(borrowToRemove == null){ return false; }

        // Update Book details & Borrow records
        currentBorrows.remove(borrowToRemove);

        // Update Book detail
        boolean stillBorrowed = false;
        for (Borrow borrow: currentBorrows) {
            if (getParticularBook(borrow.getBorrowBook().getBookTitle(), borrow.getBorrowBook().getAuthorUsername()).equals(getParticularBook(book.getBookTitle(),book.getAuthorUsername()))) {
                stillBorrowed = true;
                break;
            }
        }
        if(!stillBorrowed) { book.setIsBorrowed(book, false); }
        saveBorrowsToFile();

        return true;
    }


    /**
     * auto return expired books
     */
    private static void returnExpiredBook(){
        List<Borrow> allBorrows = getAllBorrows();

        // Auto-return expired books
        LocalDateTime now = LocalDateTime.now();
        for (Borrow borrow : allBorrows) {
            LocalDateTime expiryTime = borrow.getBorrowedDateTime().plus(borrow.getDuration());

            if (now.isAfter(expiryTime)) {
                // Auto-return expired book
                boolean success = returnBook(borrow.getBorrowBook(), borrow.getBorrowerUsername());
                if (success) {
                    // Create notification for expired book
                    Notification.save(borrow.getBorrowerUsername(), Notification.NotificationType.EXPIRED, borrow.getBorrowBook().getBookTitle());
                }
            }
        }

    }



    /**
     * Return the number of current borrowers for the book
     * @param book
     * @return integer
     */
    public static int countBorrowed(Book book){
        // Expired then return
        returnExpiredBook();

        loadAllBorrows();

        int count = 0;
        for (Borrow borrow: currentBorrows) {
            if (borrow.getBorrowBook().getTitle().equals(book.getTitle()) && borrow.getBorrowBook().getAuthorUsername().equals(book.getAuthorUsername())) {
                count++;
            }
        }
        return count;
    }
    /**
     * Same: Return the number of current borrowers for the book
     * @param bookTitle
     * @param authorUsername
     * @return integer
     */
    public static int countBorrowed(String bookTitle, String authorUsername) {
        // Expired then return
        returnExpiredBook();

        Book book = getParticularBook(bookTitle, authorUsername);
        if (book == null) {
            return 0;
        }
        return countBorrowed(book);
    }

    // Getters:
    /*
    getBorrowBook() getDuration(), getBorrowerUsername(), getBorrowedDateTime()
     */
    public Book getBorrowBook(){ return this.book; }
    public String getBorrowerUsername(){ return this.borrowerUsername; }
    public Duration getDuration(){ return this.duration; }
    public LocalDateTime getBorrowedDateTime(){ return this.borrowedDateTime;}



}
