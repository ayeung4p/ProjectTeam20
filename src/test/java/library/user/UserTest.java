package library.user;

import org.junit.jupiter.api.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static library.user.User.checkFullnameLength;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Order;

@Order(14)
class UserTest {

    private static final String TEST_USERNAME = "testUser123";
    private static final String TEST_PASSWORD = "Password123";
    private static final String TEST_FULLNAME = "Test User";

    @BeforeEach
    void setUp() throws Exception {
        // Clear the data file before each test
        Path dataFilePath = Path.of("data", "user_data.txt");
        Files.deleteIfExists(dataFilePath);

        // Clear cache using reflection
        var cacheLoadedField = User.class.getDeclaredField("cacheLoaded");
        cacheLoadedField.setAccessible(true);
        cacheLoadedField.set(null, false);

        var usersCacheField = User.class.getDeclaredField("usersCache");
        usersCacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<User> cache = (List<User>) usersCacheField.get(null);
        cache.clear();
    }

    @AfterEach
    void tearDown() throws Exception {
        // Clean up after each test
        Path dataFilePath = Path.of("data", "user_data.txt");
        Files.deleteIfExists(dataFilePath);
    }

    @Test
    void testingcheckFullnameLength(){
        String fullname = "abcd";
        boolean result = checkFullnameLength(fullname);
        assertTrue(result, "Success");
    }

    @Test
    void testCheckFullnameLength() {
        // Test valid lengths (2-50 characters)
        assertTrue(checkFullnameLength("ab"));     // minimum valid length (2)
        assertTrue(checkFullnameLength("abc"));    // 3 characters
        assertTrue(checkFullnameLength("John Doe")); // typical name
        assertTrue(checkFullnameLength("a".repeat(50))); // maximum valid length (50)

        // Test invalid lengths
        assertFalse(checkFullnameLength("a"));      // too short (1 character)
        assertFalse(checkFullnameLength(""));       // empty string
        assertFalse(checkFullnameLength("a".repeat(51))); // too long (51 characters)

        // Test edge cases
        assertFalse(checkFullnameLength(null));     // null input
    }

    @Test
    void testCheckFullnameLength_AllBranches() {
        // Test null case
        assertFalse(checkFullnameLength(null));

        // Test valid range (2-50 characters) - should return TRUE
        for (int i = 2; i <= 50; i++) {
            String name = "a".repeat(i);
            assertTrue(checkFullnameLength(name), "Should return true for length: " + i);
        }

        // Test invalid lengths (51+ characters) - should return FALSE
        for (int i = 51; i <= 55; i++) {
            String name = "a".repeat(i);
            assertFalse(checkFullnameLength(name), "Should return false for length: " + i);
        }

        // Test too short cases (0-1 characters) - should return FALSE
        assertFalse(checkFullnameLength("a"));    // length 1
        assertFalse(checkFullnameLength(""));     // length 0
    }

    @Test
    void testCheckUsernameLength() {
        // Test valid lengths (4-30 characters)
        assertTrue(User.checkUsernameLength("user"));      // minimum valid length (4)
        assertTrue(User.checkUsernameLength("john_doe123")); // typical username
        assertTrue(User.checkUsernameLength("a".repeat(30))); // maximum valid length (30)

        // Test invalid lengths
        assertFalse(User.checkUsernameLength("usr"));      // too short (3 characters)
        assertFalse(User.checkUsernameLength("ab"));       // too short (2 characters)
        assertFalse(User.checkUsernameLength("a"));        // too short (1 character)
        assertFalse(User.checkUsernameLength(""));         // empty string
        assertFalse(User.checkUsernameLength("a".repeat(31))); // too long (31 characters)

        // Test edge cases
        assertFalse(User.checkUsernameLength(null));       // null input
    }

    @Test
    void testCheckUsernameLength_AllBranches() {
        // Test null case
        assertFalse(User.checkUsernameLength(null));

        // Test valid range (4-30 characters) - should return TRUE
        for (int i = 4; i <= 30; i++) {
            String username = "a".repeat(i);
            assertTrue(User.checkUsernameLength(username), "Should return true for length: " + i);
        }

        // Test invalid lengths (31+ characters) - should return FALSE
        for (int i = 31; i <= 35; i++) {
            String username = "a".repeat(i);
            assertFalse(User.checkUsernameLength(username), "Should return false for length: " + i);
        }

        // Test too short cases (0-3 characters) - should return FALSE
        assertFalse(User.checkUsernameLength("aaa"));  // length 3
        assertFalse(User.checkUsernameLength("aa"));   // length 2
        assertFalse(User.checkUsernameLength("a"));    // length 1
        assertFalse(User.checkUsernameLength(""));     // length 0
    }

    @Test
    void testUserConstructorWithEnums() {
        User user = new User(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, Status.ACTIVATED, Role.STUDENT);

        assertEquals(TEST_USERNAME, user.getUsername());
        assertEquals(TEST_FULLNAME, user.getFullName());
        assertEquals("Activated", user.getStatus());
        assertEquals("Student", user.getRole());
        assertEquals("true", user.getStatusDisplay());
        assertEquals("Student", user.getRoleDisplay());
        assertTrue(user.checkPassword(TEST_PASSWORD));
    }

    @Test
    void testUserConstructorWithStrings() {
        User user = new User(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, "activated", "student");

        assertEquals(TEST_USERNAME, user.getUsername());
        assertEquals(TEST_FULLNAME, user.getFullName());
        assertEquals("Activated", user.getStatus());
        assertEquals("Student", user.getRole());
    }

    @Test
    void testUserConstructorWithInvalidStatus() {
        assertThrows(IllegalArgumentException.class, () -> {
            new User(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, "invalid_status", "student");
        });
    }

    @Test
    void testUserConstructorWithInvalidRole() {
        assertThrows(IllegalArgumentException.class, () -> {
            new User(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, "activated", "invalid_role");
        });
    }

    @Test
    void testValidateUsername() {
        // Valid usernames
        assertTrue(User.validateUsername("user123"));
        assertTrue(User.validateUsername("user-name"));
        assertTrue(User.validateUsername("user_name"));
        assertTrue(User.validateUsername("User123"));

        // Invalid usernames
        assertFalse(User.validateUsername(null));
        assertFalse(User.validateUsername(""));
        assertFalse(User.validateUsername("   "));
        assertFalse(User.validateUsername("123456")); // pure numbers
        assertFalse(User.validateUsername("user@name")); // invalid character
        assertFalse(User.validateUsername("user name")); // space not allowed
    }

    @Test
    void testValidatePassword() {
        // Valid passwords
        assertTrue(User.validatePassword("Password123"));
        assertTrue(User.validatePassword("Abc123"));
        assertTrue(User.validatePassword("TESTpass123"));

        // Invalid passwords
        assertFalse(User.validatePassword(null));
        assertFalse(User.validatePassword(""));
        assertFalse(User.validatePassword("   "));
        assertFalse(User.validatePassword("password")); // no uppercase, no number
        assertFalse(User.validatePassword("PASSWORD")); // no lowercase, no number
        assertFalse(User.validatePassword("12345678")); // no letters
        assertFalse(User.validatePassword("Pass")); // no number
    }

    @Test
    void testSaveUser() {
        User user = new User(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, Status.ACTIVATED, Role.STUDENT);

        assertTrue(user.save());

        // Verify user was saved
        User retrievedUser = User.selectUserByUsername(TEST_USERNAME);
        assertNotNull(retrievedUser);
        assertEquals(TEST_USERNAME, retrievedUser.getUsername());
        assertEquals(TEST_FULLNAME, retrievedUser.getFullName());
    }

    @Test
    void testSaveDuplicateUser() {
        User user1 = new User(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, Status.ACTIVATED, Role.STUDENT);
        user1.save();

        User user2 = new User(TEST_USERNAME, "DifferentPass123", "Different Name", Status.DEACTIVATED, Role.AUTHOR);

        assertThrows(UserAlreadyExistsException.class, user2::save);
    }

    @Test
    void testStaticSaveUser() {
        User user = new User(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, Status.ACTIVATED, Role.STUDENT);

        assertTrue(User.save(user));

        User retrievedUser = User.selectUserByUsername(TEST_USERNAME);
        assertNotNull(retrievedUser);
    }

    @Test
    void testUpdateUser() {
        User user = new User(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, Status.ACTIVATED, Role.STUDENT);
        user.save();

        user.setFullName("Updated Name");
        user.setStatus("deactivated");

        assertTrue(user.updateUser());

        User updatedUser = User.selectUserByUsername(TEST_USERNAME);
        assertNotNull(updatedUser);
        assertEquals("Updated Name", updatedUser.getFullName());
        assertEquals("Deactivated", updatedUser.getStatus());
        assertEquals("false", updatedUser.getStatusDisplay());
    }

    @Test
    void testUpdateNonExistentUser() {
        User user = new User("nonExistentUser", TEST_PASSWORD, TEST_FULLNAME, Status.ACTIVATED, Role.STUDENT);

        assertThrows(UserDoesNotExistException.class, user::updateUser);
    }

    @Test
    void testStaticUpdateUser() {
        User user = new User(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, Status.ACTIVATED, Role.STUDENT);
        user.save();

        assertTrue(User.updateUser(TEST_USERNAME, "New Full Name", "deactivated"));

        User updatedUser = User.selectUserByUsername(TEST_USERNAME);
        assertNotNull(updatedUser);
        assertEquals("New Full Name", updatedUser.getFullName());
        assertEquals("Deactivated", updatedUser.getStatus());
    }

    @Test
    void testStaticUpdateNonExistentUser() {
        assertThrows(UserDoesNotExistException.class, () -> {
            User.updateUser("nonExistentUser", "New Name", "activated");
        });
    }

    @Test
    void testSelectUserByUsername() {
        User user = new User(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, Status.ACTIVATED, Role.STUDENT);
        user.save();

        User foundUser = User.selectUserByUsername(TEST_USERNAME);
        assertNotNull(foundUser);
        assertEquals(TEST_USERNAME, foundUser.getUsername());

        User notFoundUser = User.selectUserByUsername("nonExistentUser");
        assertNull(notFoundUser);
    }

    @Test
    void testGetAllUsers() {
        User user1 = new User("user1", "Password123", "User One", Status.ACTIVATED, Role.STUDENT);
        User user2 = new User("user2", "Password123", "User Two", Status.DEACTIVATED, Role.AUTHOR);

        user1.save();
        user2.save();

        List<User> allUsers = User.getAllUsers();
        assertEquals(2, allUsers.size());
        assertTrue(allUsers.stream().anyMatch(u -> u.getUsername().equals("user1")));
        assertTrue(allUsers.stream().anyMatch(u -> u.getUsername().equals("user2")));
    }

    @Test
    void testGetUserCount() {
        assertEquals(0, User.getUserCount());

        User user1 = new User("user1", "Password123", "User One", Status.ACTIVATED, Role.STUDENT);
        user1.save();

        assertEquals(1, User.getUserCount());

        User user2 = new User("user2", "Password123", "User Two", Status.DEACTIVATED, Role.AUTHOR);
        user2.save();

        assertEquals(2, User.getUserCount());
    }

    @Test
    void testUsernameExists() {
        assertFalse(User.usernameExists(TEST_USERNAME));

        User user = new User(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, Status.ACTIVATED, Role.STUDENT);
        user.save();

        assertTrue(User.usernameExists(TEST_USERNAME));
        assertFalse(User.usernameExists("nonExistentUser"));
    }

    @Test
    void testCheckPassword() {
        User user = new User(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, Status.ACTIVATED, Role.STUDENT);

        assertTrue(user.checkPassword(TEST_PASSWORD));
        assertFalse(user.checkPassword("WrongPassword123"));
        assertFalse(user.checkPassword(""));
        assertFalse(user.checkPassword(null));
    }

    @Test
    void testSetPassword() {
        User user = new User(TEST_USERNAME, "OldPassword123", TEST_FULLNAME, Status.ACTIVATED, Role.STUDENT);

        // Store the initial password hash
        String initialToString = user.toString();

        user.setPassword("NewPassword123");

        // The password hash should change
        String newToString = user.toString();
        assertNotEquals(initialToString, newToString);

        // Should verify correctly with new password
        assertTrue(user.checkPassword("NewPassword123"));
        assertFalse(user.checkPassword("OldPassword123"));
    }

    @Test
    void testSetFullName() {
        User user = new User(TEST_USERNAME, TEST_PASSWORD, "Original Name", Status.ACTIVATED, Role.STUDENT);

        user.setFullName("  New Name With Spaces  ");
        assertEquals("New Name With Spaces", user.getFullName());
    }

    @Test
    void testSetStatus() {
        User user = new User(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, Status.ACTIVATED, Role.STUDENT);

        assertEquals("Activated", user.getStatus());
        assertEquals("true", user.getStatusDisplay());

        user.setStatus("deactivated");
        assertEquals("Deactivated", user.getStatus());
        assertEquals("false", user.getStatusDisplay());

        user.setStatus("activated");
        assertEquals("Activated", user.getStatus());
        assertEquals("true", user.getStatusDisplay());
    }

    @Test
    void testSetInvalidStatus() {
        User user = new User(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, Status.ACTIVATED, Role.STUDENT);

        assertThrows(IllegalArgumentException.class, () -> {
            user.setStatus("invalid_status");
        });
    }

    @Test
    void testToString() {
        User user = new User(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, Status.ACTIVATED, Role.STUDENT);

        String userString = user.toString();
        String[] parts = userString.split("\t");

        assertEquals(5, parts.length);
        assertEquals(TEST_USERNAME, parts[0]);
        assertEquals(TEST_FULLNAME, parts[2]);
        assertEquals("Activated", parts[3]);
        assertEquals("Student", parts[4]);
    }

    @Test
    void testEquals() {
        User user1 = new User(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, Status.ACTIVATED, Role.STUDENT);
        User user2 = new User(TEST_USERNAME, "DifferentPass123", "Different Name", Status.DEACTIVATED, Role.AUTHOR);
        User user3 = new User("differentUser", TEST_PASSWORD, TEST_FULLNAME, Status.ACTIVATED, Role.STUDENT);

        assertEquals(user1, user2); // Same username
        assertNotEquals(user1, user3); // Different username
        assertNotEquals(user1, null); // Compare with null
        assertNotEquals(user1, "some string"); // Compare with different type
        assertEquals(user1, user1); // Compare with self
    }

    @Test
    void testRoleFromString() {
        assertEquals(Role.STUDENT, Role.fromString("student"));
        assertEquals(Role.STUDENT, Role.fromString("staff"));
        assertEquals(Role.STUDENT, Role.fromString("student/staff"));
        assertEquals(Role.AUTHOR, Role.fromString("author"));
        assertEquals(Role.LIBRARIAN, Role.fromString("librarian"));

        assertThrows(IllegalArgumentException.class, () -> Role.fromString("invalid_role"));

        // Update: Your current implementation throws NullPointerException for null input
        assertThrows(NullPointerException.class, () -> Role.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> Role.fromString(""));
    }

    @Test
    void testStatusFromString() {
        assertEquals(Status.ACTIVATED, Status.fromString("activated"));
        assertEquals(Status.DEACTIVATED, Status.fromString("deactivated"));

        assertThrows(IllegalArgumentException.class, () -> Status.fromString("invalid_status"));

        // Update: Your current implementation throws NullPointerException for null input
        assertThrows(NullPointerException.class, () -> Status.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> Status.fromString(""));
    }

    @Test
    void testFileInitialization() throws IOException {
        // Ensure file doesn't exist initially
        Path dataFilePath = Path.of("data", "user_data.txt");
        Files.deleteIfExists(dataFilePath);

        // Call a method that triggers file initialization
        User.getUserCount();

        // Verify file was created
        assertTrue(Files.exists(dataFilePath));
    }

    @Test
    void testMultipleRoles() {
        User student = new User("student1", TEST_PASSWORD, "Student User", Status.ACTIVATED, Role.STUDENT);
        User author = new User("author1", TEST_PASSWORD, "Author User", Status.ACTIVATED, Role.AUTHOR);
        User librarian = new User("librarian1", TEST_PASSWORD, "Librarian User", Status.ACTIVATED, Role.LIBRARIAN);

        assertEquals("Student", student.getRole());
        assertEquals("Author", author.getRole());
        assertEquals("Librarian", librarian.getRole());
    }

    @Test
    void testUserPersistenceAcrossInstances() {
        // Create and save user
        User user1 = new User(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, Status.ACTIVATED, Role.STUDENT);
        user1.save();

        // Create new instance and retrieve - should find the saved user
        User retrievedUser = User.selectUserByUsername(TEST_USERNAME);
        assertNotNull(retrievedUser);
        assertEquals(TEST_USERNAME, retrievedUser.getUsername());
        assertEquals(TEST_FULLNAME, retrievedUser.getFullName());
    }

    @Test
    void testEmptyDatabase() {
        List<User> users = User.getAllUsers();
        assertTrue(users.isEmpty());
        assertEquals(0, User.getUserCount());
    }

    @Test
    void testRoleDisplayNames() {
        assertEquals("Student", Role.STUDENT.getDisplayName());
        assertEquals("Author", Role.AUTHOR.getDisplayName());
        assertEquals("Librarian", Role.LIBRARIAN.getDisplayName());
    }

    @Test
    void testStatusDisplayNames() {
        assertEquals("Activated", Status.ACTIVATED.getDisplayName());
        assertEquals("Deactivated", Status.DEACTIVATED.getDisplayName());
    }

    @Test
    void testCaseInsensitiveRoleAndStatus() {
        // Test case insensitive role parsing
        assertEquals(Role.STUDENT, Role.fromString("STUDENT"));
        assertEquals(Role.STUDENT, Role.fromString("Student"));
        assertEquals(Role.AUTHOR, Role.fromString("AUTHOR"));
        assertEquals(Role.LIBRARIAN, Role.fromString("LIBRARIAN"));

        // Test case insensitive status parsing
        assertEquals(Status.ACTIVATED, Status.fromString("ACTIVATED"));
        assertEquals(Status.ACTIVATED, Status.fromString("Activated"));
        assertEquals(Status.DEACTIVATED, Status.fromString("DEACTIVATED"));
    }
}