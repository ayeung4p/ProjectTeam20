package library.book;

import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.nio.file.Files;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import com.opencsv.CSVReader;


/**
 * Book Class
 */
public class Book {
    // Attributes:
    private String bookTitle;
    private final String authorUsername;
    private String bookAbstract;
    private int noOfTimeBorrowed;
    private String publishedDate;
    private BookStatus status;
    private String contentDirectory;
    private Boolean isBorrowed;


    /**
     * Some important immutable keywords for directory manipulation
     */
    private static final String BOOK_DIR = "book";
    private static final String ALL_BOOKS_FILE = "book" + File.separator + "AllBooks.txt";
    private static final String BOOK_CONTENT_DIR = "book" + File.separator + "bookcontent";
    // Methods:


    /**
     * Call whenever the system starts! It ensure proper data structure of Books
     * It create a Book file if doesn't exist, and then create a BookContent file inside Book file if doesn't exist
     * Finally, create allBooks.txt inside Book file for saving data of all books
     */
    public static void ensureStorageStructure() {
        File bookDir = new File(BOOK_DIR);
        if (!bookDir.exists()) bookDir.mkdir();

        File bookContentDir = new File(BOOK_CONTENT_DIR);
        if (!bookContentDir.exists()) bookContentDir.mkdir();

        File allBooksFile = new File(ALL_BOOKS_FILE);
        try {
            if (!allBooksFile.exists()) allBooksFile.createNewFile();
        } catch (IOException e) {
            System.err.println("Could not create " + ALL_BOOKS_FILE + ": " + e.getMessage());
        }
    }
    //-------------------------------------------------------------------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Create a Book Object when Author publish a book
     * @param title
     * @param username
     * @param bookAbstract
     * @param date
     * @param bookStatus
     * @param contentDirectory
     */
    public Book(String title, String username ,String bookAbstract, String date, BookStatus bookStatus, String contentDirectory) {
        this.bookTitle = title;
        this.authorUsername = username;
        this.bookAbstract = bookAbstract;
        this.noOfTimeBorrowed = 0;
        this.publishedDate = date;
        this.status = bookStatus;
        this.contentDirectory = contentDirectory;
        this.isBorrowed = false;

    }

    /**
     * Create a Book Object when any user access/read a book
     * @param bookTitle
     * @param username
     * @param bookAbstract
     * @param date
     * @param bookStatus
     * @param contentDirectory
     * @param b
     */
    public Book(String bookTitle, String username, String bookAbstract, int noOfTimeBorrowed, String date, BookStatus bookStatus, String contentDirectory, boolean b) {
        this.bookTitle = bookTitle;
        this.authorUsername = username;
        this.bookAbstract = bookAbstract;
        this.noOfTimeBorrowed = noOfTimeBorrowed;
        this.publishedDate = date;
        this.status = bookStatus;
        this.contentDirectory = contentDirectory;
        this.isBorrowed = b;
    }


    //-------------------------------------------------------------------------------------------------------------------------------------
    // Finding Book Related Function (Read txt file)
    //-------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Use by other getBooks() methods only
     * Access to AllBooks.txt and save every Book Object to a List of Book
     * @return List<Book> containing every Book Object excluding those status with DELETED
     */

    static public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(ALL_BOOKS_FILE), '|')) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                try {
                    Book b = new Book(
                            line[0],                   // bookTitle
                            line[1],                   // username
                            line[2],                   // bookAbstract
                            Integer.parseInt(line[3]), // noOfTimeBorrowed
                            line[4],                   // publishedDate
                            BookStatus.fromString(line[5].toUpperCase()), // status
                            line[6],                   // contentDirectory
                            Boolean.parseBoolean(line[7]) // isBorrowed
                    );
                    if (b.getBookStatus() != BookStatus.DELETED) {
                        books.add(b);
                    }
                } catch (Exception lineEx) {
                    System.err.println("Skipping malformed line: " + java.util.Arrays.toString(line));
                    lineEx.printStackTrace();
                    // continue automatically implied
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Handle file not found or IO problems
        }
        return books;
    }

    /**
     * Use by Author
     * Get a subset of Books from all Books, the subset containing books published by a specific author
     * @param username Author's username, cannot be empty
     * @return List<Book> containing a books published by a specific author excluding those status with DELETED
     */
     public static List<Book> getBookByAuthor(String username) {
        List<Book> allBooks = getAllBooks();
        List<Book> authorBooks = new ArrayList<>();
        for (Book book : allBooks) {
            if (book.authorUsername.equals(username)) {
                if (book.status != BookStatus.DELETED){
                    authorBooks.add(book);
                }
            }
        }
        return authorBooks;
    }
    /**
     * Use by Librarian
     * Get a subset of Books from all Books, the subset containing books with status PENDING
     * @return List<Book> containing every Book Object with status PENDING
     */
    static public List<Book> getPendingBook() {
        List<Book> allBooks = getAllBooks();
        List<Book> pendingBooks = new ArrayList<>();
        for (Book book : allBooks) {
            if (book.status.equals(BookStatus.PENDING)) {
                pendingBooks.add(book);
            }
        }
        return pendingBooks;
    }
    // Urgent made function
    static public List<Book> getApprovedBook() {
        List<Book> allBooks = getAllBooks();
        List<Book> approvedBooks = new ArrayList<>();
        for (Book book : allBooks) {
            if (book.status.equals(BookStatus.APPROVED)) {
                approvedBooks.add(book);
            }
        }
        return approvedBooks;
    }
    
    /**
     * Use by any user
     * Helper Function for easier parameter inputing (To be used when implementing controllers)
     * Get a particular Book copy from all Books
     * @param bookTitle
     * @param authorUsername
     * @return Book , the COPY of the required book in the AllBooks.txt
     */
    static public Book getParticularBook(String bookTitle, String authorUsername) {
        List<Book> allBooks = getAllBooks();
        for (Book book : allBooks) {
            if (book.authorUsername.equals(authorUsername)) {
                if (book.bookTitle.equals(bookTitle)) {
                    return book;
                }
            }
        }
        return null; // Cannot find that book
    }

    
    //-------------------------------------------------------------------------------------------------------------------------------------
    // Publish Book Related Function (Write to txt file)
    //-------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Use by publishBook() and updateBookStatus() Onlu
     * Get the current date in specific format
     * @return LocalDate Object , current Date
     */
    static public String getCurrentDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.now().format(formatter);
    }

    /**
     * Use by publishBook() only
     * Generate a unique bookDir based on the book title and author's username
     * @return String , the unique directory
     */
    private static String makeUniqueContentDirectory(String title, String username) {
        return title + "_" + username + ".txt";
    }

    /**
     * Use by publishBook() only
     * Quote every String attributes with "". delimiter "|" appears in Book's Attributes and lead to unexpected behaviour
     * @param data
     * @return String , the quoted data
     */
    static private String quoteField(String data) {
        if (data == null) return "\"\"";
        return "\"" + data.replace("\"", "\"\"") + "\"";
    }

    /**
     * Use by Author
     * Publish a book with provided parameters. It will do 2 things:
     * 1. Update AllBooks.txt with a quoted, well-aligned syntax row
     * 2. Rename the file in selectedFilePath and put it into book/bookcontent/
     * @param title
     * @param bookAbstract
     * @param authorUsername
     * @param selectedFilePath provided dynamically when an author select from the file explorer
     * @return Boolean value indicating successful publication or not
     */
    // This function is being called when "publish" button is pressed, write a new line to the txt file status = pending
    static public Boolean publishBook(String title, String bookAbstract, String authorUsername, String selectedFilePath) {
        // 1. it will check whether the author have published the same book before
        List<Book> authorBooks = getBookByAuthor(authorUsername);
        for (Book book: authorBooks) {
            if (book.bookTitle.equals(title)&&(book.bookAbstract.equals(bookAbstract))) {
                return false;
            }
        }
        // 2. Prepare necessary parameters for writing/moving file
        String today = getCurrentDate();
        String uniqueContentDirectory = makeUniqueContentDirectory(title, authorUsername);
        File sourceFile = new File(selectedFilePath);                              // To be confirmed when implementing button in controller
        File destFile = new File(BOOK_CONTENT_DIR, uniqueContentDirectory);
        Book newBook = new Book(title, authorUsername, bookAbstract, today, BookStatus.PENDING, uniqueContentDirectory);

        // 3. Move the sourceFile from selectedFilePath to destFilePath
        try {
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            return false; // If copying abort publish
        }

        // 4. Write newBook to AllBooks.txt --- status = Pending
        try (FileWriter fw = new FileWriter(ALL_BOOKS_FILE, true)) {
            String record = String.join("|",
                    quoteField(newBook.bookTitle.trim()),
                    quoteField(newBook.authorUsername),
                    quoteField(newBook.bookAbstract.trim()),
                    String.valueOf(newBook.noOfTimeBorrowed),
                    quoteField(newBook.publishedDate),
                    newBook.status.name(),
                    quoteField(newBook.contentDirectory.trim()),
                    String.valueOf(newBook.isBorrowed)
            );
            fw.write(record + System.lineSeparator());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    return false;
    }

    /**
     * Helper Function
     * Overwrite the whole AllBooks.txt with the new List of Book (Update of AllBooks.txt)
     * @param allBooks
     */
    static private void updateBooks(List<Book> allBooks) {
        try (FileWriter fw = new FileWriter(ALL_BOOKS_FILE, false)) {
            for (Book book : allBooks) {
                String record = String.join("|",
                        quoteField(book.bookTitle),
                        quoteField(book.authorUsername),
                        quoteField(book.bookAbstract),
                        String.valueOf(book.noOfTimeBorrowed),
                        quoteField(book.publishedDate),
                        book.status.getBookStatus(),
                        quoteField(book.contentDirectory),
                        String.valueOf(book.isBorrowed)
                );
                fw.write(record + System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------------------
    // Managing Book Related Function
    //-------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Use by Student/Staff
     * Update AllBooks.txt, given a particular book, increase the number of time that book being borrowed
     * @param book The target book
     * Remarks: Can use getParticularBook() to return the Book Object
     */
    static public void incrementNoOfTimeBorrowed(Book book) {
        List<Book> allBooks = getAllBooks();
        for (Book bookit : allBooks) {
            if (bookit.bookTitle.equals(book.bookTitle)) {
                if (bookit.authorUsername.equals(book.authorUsername)) {
                    if (!bookit.status.equals(BookStatus.DELETED)) {
                        bookit.noOfTimeBorrowed++;
                    }

                }
            }
        }
        updateBooks(allBooks);
    }



    /**
     * Use by any user
     * Need to work with interface. Call when someone clicks "view" Book, this method provide one single String of the book
     * @param bookDir
     * @return String , the single String in the .txt file
     */
    public static String viewBook(String bookDir) {
        try {
            Path filePath = Paths.get(BOOK_CONTENT_DIR, bookDir);
            System.out.println("Reading file: " + filePath);
            if (!Files.exists(filePath)) {
                System.out.println("File does not exist: " + filePath);
            }
            return Files.readString(filePath);
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Use by Author
     * Need to work with interface. Call when Author clicks "edit" Book. Change the title and abstract
     * Status is changed to Pending (Exclude from books that can be borrowed)
     * @param book For searching the target book
     * @param newTitle
     * @param newAbstract
     * Remarks: Can use getParticularBook() to return the Book Object
     */
    static public void editBook(Book book, String newTitle, String newAbstract) {
        List<Book> allBooks = getAllBooks();
        for (Book bookit : allBooks) {
            if (bookit.bookTitle.equals(book.bookTitle)) {
                if (bookit.authorUsername.equals(book.authorUsername)) {
                    if (!bookit.status.equals(BookStatus.DELETED)) {
                        if (!bookit.bookTitle.equals(newTitle)) { // check if the title is changed, if so, change the name of bookDir in /bookcontent
                            String oldDir = bookit.contentDirectory;
                            String newDir = makeUniqueContentDirectory(newTitle, bookit.authorUsername);
                            File oldFile = new File(BOOK_CONTENT_DIR, oldDir);
                            File newFile = new File(BOOK_CONTENT_DIR, newDir);

                            try {
                                // Perform rename
                                Files.move(oldFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                bookit.contentDirectory = newDir; // Update stored filename
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        bookit.bookTitle = newTitle;
                        bookit.bookAbstract = newAbstract;
                        bookit.status = BookStatus.PENDING;
                    }
                }
            }
        }
        updateBooks(allBooks);
    }

    /**
     * Use by Author or Librarian
     * Need to work with interface. Call when Author or Librarian clicks "delete" Book
     * Status is changed to DELETED
     * Remarks: Can use getParticularBook() to return the Book Object
     */
     public static void deleteBook(Book book) {
        List<Book> allBooks = getAllBooks();
        for (Book bookit : allBooks) {
            if (bookit.bookTitle.equals(book.bookTitle)) {
                if (bookit.authorUsername.equals(book.authorUsername)) {
                    if (!bookit.status.equals(BookStatus.DELETED)) {
                        bookit.status = BookStatus.DELETED;
                    }
                }
            }
        }
        updateBooks(allBooks);
    }

    /**
     * Use by Librarian
     * Need to work with interface. Call whenLibrarian clicks "Approve/Rejected" Book
     * Status is changed to APPROVED/REJCTED and the published date is updated
     * Remarks: Can use getParticularBook() to return the Book Object
     */
    static public void updateBookStatus(Book book, String NewStatus) {   // Librarian can call this function to change any book's status(Approve/Rejected)
        List<Book> allBooks = getAllBooks();
        for (Book bookit : allBooks) {
            if (bookit.bookTitle.equals(book.bookTitle)) {
                if (bookit.authorUsername.equals(book.authorUsername)) {
                    if (!bookit.status.equals(BookStatus.DELETED)) {
                        bookit.status = BookStatus.fromString(NewStatus);
                        bookit.publishedDate = getCurrentDate();
                    }
                }
            }
        }
        updateBooks(allBooks);
    }

    /**
     * Use by Student/Staff
     * Helper function for modifying isBorrowed status of this book
     * Update the AllBooks.txt
     * @param book
     * @param newIsBorrowedOrNot The new Boolean value (True/False)
     */
    // The following two functions wait Borrow Class to be done first
    public void setIsBorrowed(Book book, Boolean newIsBorrowedOrNot) {

        List<Book> allBooks = getAllBooks();
        for (Book bookit : allBooks) {
            if (bookit.bookTitle.equals(book.bookTitle)) {
                if (bookit.authorUsername.equals(book.authorUsername)) {
                    if (!bookit.status.equals(BookStatus.DELETED)) {
                        bookit.isBorrowed = newIsBorrowedOrNot;
                    }
                }
            }
        }
        updateBooks(allBooks);

    }
    //-------------------------------------------------------------------------------------------------------------------------------------
    // Statistic Related Function
    //-------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Use by Author
     * Helper function for easier implementation of Pie Chart (Pending% vs Approved%)
     * Count the no. of pending/approved books
     * @param authorUsername
     * @return int[], first int = pending / second int = approved / third int = rejected
     */
    public static int[] getStatusNumber(String authorUsername) {
        List<Book> authorBooks = getBookByAuthor(authorUsername);
        if (!authorBooks.isEmpty()) {
            int pendingNumber = 0, approvedNumber = 0, rejectedNumber = 0;
            for (Book book : authorBooks) {
                if (book.status.equals(BookStatus.PENDING)) {
                    pendingNumber++;
                } else if (book.status.equals(BookStatus.APPROVED)) {
                    approvedNumber++;
                }
                else if (book.status.equals(BookStatus.REJECTED)) {
                    rejectedNumber++;
                }
            }
            return new int[]{pendingNumber, approvedNumber, rejectedNumber};
        }
        else {
            return new int[]{0, 0, 0};
        }
    }

    /**
     * Use by Author
     * Helper function for easier implementation of Bar Chart (Top 5 Best Books)
     * Count the top 5 borrow count books
     * @param authorUsername
     * @return int[], first int = pending / second int = approved
     */
    public static List<Book> getBestFiveBooks(String authorUsername) {
        List<Book> authorBooks = getBookByAuthor(authorUsername);
        if (!authorBooks.isEmpty()) {
            List<Book> vaildBooks = new ArrayList<>();
            for (Book book : authorBooks) {
                if (book.status.equals(BookStatus.APPROVED)) {
                    vaildBooks.add(book);
                }
            }
            vaildBooks.sort((a, b) -> Integer.compare(b.noOfTimeBorrowed, a.noOfTimeBorrowed));
            int size = vaildBooks.size();
            if (size > 5) {
                // Remove from the front (most popular), keep only last 5
                return vaildBooks.subList(0, 5);
            }
            else {
                return vaildBooks;
            }
        }
        else {
            return new ArrayList<>();
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------------------
    // Other Necessary Function
    //-------------------------------------------------------------------------------------------------------------------------------------


    // Getters:
    public String getTitle() {return this.bookTitle;}
    public String getAuthorUsername() {return this.authorUsername;}
    public String getBookAbstract() {return this.bookAbstract;}
    public int getTimesBorrowed() {return this.noOfTimeBorrowed;}
    public String getPublishedDate() {return this.publishedDate;}
    public String getContentDirectory() {return this.contentDirectory;}
    public Boolean getBorrowed() {return this.isBorrowed;}
    public BookStatus getBookStatus() {return this.status;}
    public BookStatus getStatus() {return this.status;} // this is for controller
    public String getBookTitle() {return this.bookTitle;}// this is for controller
    // Setters:
    public void setTitle(String newTitle) {this.bookTitle = newTitle;}
    public void setAbstract(String newAbstract) {this.bookAbstract = newAbstract;}
    public void setTimesBorrowed(int newTimesBorrowed) {this.noOfTimeBorrowed = newTimesBorrowed;}
    public void setStatus(BookStatus newStatus) { this.status = newStatus;}


    /*
     * attributes:
     * bookTitle
     * authorName(username) -- must check if it exists in Users
     * abstract
     * noOfTimeBorrowed -- total number of times that the book being borrowed, for Librarian, Author
     * publishDate
     * status(pending, approved, rejected)
     * content(file path, e.g. "xx.txt")
     * isBorrowed
     * --- done
     * methods:
     * static getAllBooks(): List<Book> -- Book.getAllBooks()
     * static getBooksByAuthor()
     * readBook() -- open text file
     * editAbstract() -- change status to pending, can call updateStatus()
     * editTitle()
     * deleteBook()
     * publishBook() -- Author
     * setIsBorrowed()
     * updateStatus() -- Librarian, if the status becomes "approved", update publish date
     * getters: getTitle(), getAbstract(), getTimesBorrowed()
     * setters: setTitle(), setAbstract(), setTimesBorrowed()
     * */
}
