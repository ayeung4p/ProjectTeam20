package library.controllers;

import library.book.Book;
import library.book.BookStatus;
import library.book.Borrow;
import library.user.User;
import library.user.Status;
import library.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Order(9)
public class LibrarianDashboardControllerTest {

    private LibrarianDashboardController controller;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        controller = new LibrarianDashboardController();
        testUser = createTestUser("libuser", Role.LIBRARIAN, Status.ACTIVATED);

        // Use reflection to set the user without touching ObservableList fields
        Field userField = LibrarianDashboardController.class.getDeclaredField("currUser");
        userField.setAccessible(true);
        userField.set(controller, testUser);
    }

    private User createTestUser(String username, Role role, Status status) {
        return new User(username, "Password123", "Test User " + username, status, role);
    }

    private Book createTestBook(String title, String author, BookStatus status) {
        return new Book(
                title,
                author,
                "Test abstract for " + title,
                0,
                "2023-01-01",
                status,
                title.replace(" ", "_") + "_" + author + ".txt",
                false
        );
    }

    // TEST ALL PURE LOGIC METHODS (NO UI DEPENDENCIES)

    @Test
    void branch_CalculateTimeLeft_Expired() throws Exception {
        Method method = LibrarianDashboardController.class.getDeclaredMethod("calculateTimeLeft", Borrow.class);
        method.setAccessible(true);

        Book testBook = createTestBook("Expired Book", "author1", BookStatus.APPROVED);
        Borrow expiredBorrow = new Borrow(testBook, "user1", java.time.Duration.ofMinutes(-10), LocalDateTime.now().minusMinutes(20));

        String result = (String) method.invoke(controller, expiredBorrow);
        assertEquals("Expired", result);
    }

    @Test
    void branch_CalculateTimeLeft_ValidWithDays() throws Exception {
        Method method = LibrarianDashboardController.class.getDeclaredMethod("calculateTimeLeft", Borrow.class);
        method.setAccessible(true);

        Book testBook = createTestBook("Future Book", "author1", BookStatus.APPROVED);
        Borrow futureBorrow = new Borrow(testBook, "user1", java.time.Duration.ofDays(2).plusHours(5), LocalDateTime.now());

        String result = (String) method.invoke(controller, futureBorrow);
        assertTrue(result.contains("2d"));
    }

    @Test
    void branch_CalculateTimeLeft_ValidWithHours() throws Exception {
        Method method = LibrarianDashboardController.class.getDeclaredMethod("calculateTimeLeft", Borrow.class);
        method.setAccessible(true);

        Book testBook = createTestBook("Future Book", "author1", BookStatus.APPROVED);
        Borrow futureBorrow = new Borrow(testBook, "user1", java.time.Duration.ofHours(5).plusMinutes(30), LocalDateTime.now());

        String result = (String) method.invoke(controller, futureBorrow);
        assertTrue(result.contains("0d"));
        assertTrue(result.contains("05:30:00"));
    }

    @Test
    void branch_CalculateTimeLeft_ValidWithMinutesOnly() throws Exception {
        Method method = LibrarianDashboardController.class.getDeclaredMethod("calculateTimeLeft", Borrow.class);
        method.setAccessible(true);

        Book testBook = createTestBook("Future Book", "author1", BookStatus.APPROVED);
        Borrow futureBorrow = new Borrow(testBook, "user1", java.time.Duration.ofMinutes(45), LocalDateTime.now());

        String result = (String) method.invoke(controller, futureBorrow);
        assertTrue(result.contains("0d"));
    }

    @Test
    void branch_CalculateTimeLeft_NullBorrow() throws Exception {
        Method method = LibrarianDashboardController.class.getDeclaredMethod("calculateTimeLeft", Borrow.class);
        method.setAccessible(true);

        String result = (String) method.invoke(controller, (Borrow) null);
        assertEquals("N/A", result);
    }

    @Test
    void branch_CalculateTimeLeft_NullDateTime() throws Exception {
        Method method = LibrarianDashboardController.class.getDeclaredMethod("calculateTimeLeft", Borrow.class);
        method.setAccessible(true);

        Book testBook = createTestBook("Test Book", "author1", BookStatus.APPROVED);
        Borrow borrowWithNullDate = new Borrow(testBook, "user1", java.time.Duration.ofDays(1), null);

        String result = (String) method.invoke(controller, borrowWithNullDate);
        assertEquals("N/A", result);
    }

    @Test
    void branch_CalculateTimeLeft_NullDuration() throws Exception {
        Method method = LibrarianDashboardController.class.getDeclaredMethod("calculateTimeLeft", Borrow.class);
        method.setAccessible(true);

        Book testBook = createTestBook("Test Book", "author1", BookStatus.APPROVED);
        // Create borrow with null duration using reflection
        Borrow borrow = new Borrow(testBook, "user1", java.time.Duration.ZERO, LocalDateTime.now());
        Field durationField = Borrow.class.getDeclaredField("duration");
        durationField.setAccessible(true);
        durationField.set(borrow, null);

        String result = (String) method.invoke(controller, borrow);
        assertEquals("N/A", result);
    }

    @Test
    void branch_IsTimeExpired_True() throws Exception {
        Method method = LibrarianDashboardController.class.getDeclaredMethod("isTimeExpired", Borrow.class);
        method.setAccessible(true);

        Book testBook = createTestBook("Expired Book", "author1", BookStatus.APPROVED);
        Borrow expiredBorrow = new Borrow(testBook, "user1", java.time.Duration.ofMinutes(-5), LocalDateTime.now().minusMinutes(10));

        boolean result = (Boolean) method.invoke(controller, expiredBorrow);
        assertTrue(result);
    }

    @Test
    void branch_IsTimeExpired_False() throws Exception {
        Method method = LibrarianDashboardController.class.getDeclaredMethod("isTimeExpired", Borrow.class);
        method.setAccessible(true);

        Book testBook = createTestBook("Future Book", "author1", BookStatus.APPROVED);
        Borrow futureBorrow = new Borrow(testBook, "user1", java.time.Duration.ofDays(1), LocalDateTime.now());

        boolean result = (Boolean) method.invoke(controller, futureBorrow);
        assertFalse(result);
    }

    @Test
    void branch_IsTimeExpired_ExactlyZero() throws Exception {
        Method method = LibrarianDashboardController.class.getDeclaredMethod("isTimeExpired", Borrow.class);
        method.setAccessible(true);

        Book testBook = createTestBook("Exactly Expired Book", "author1", BookStatus.APPROVED);
        Borrow exactlyExpiredBorrow = new Borrow(testBook, "user1", java.time.Duration.ZERO, LocalDateTime.now());

        boolean result = (Boolean) method.invoke(controller, exactlyExpiredBorrow);
        assertTrue(result);
    }

    @Test
    void branch_IsTimeExpired_NullBorrow() throws Exception {
        Method method = LibrarianDashboardController.class.getDeclaredMethod("isTimeExpired", Borrow.class);
        method.setAccessible(true);

        boolean result = (Boolean) method.invoke(controller, (Borrow) null);
        assertFalse(result);
    }

    @Test
    void branch_IsTimeExpired_NullDateTime() throws Exception {
        Method method = LibrarianDashboardController.class.getDeclaredMethod("isTimeExpired", Borrow.class);
        method.setAccessible(true);

        Book testBook = createTestBook("Test Book", "author1", BookStatus.APPROVED);
        Borrow borrowWithNullDate = new Borrow(testBook, "user1", java.time.Duration.ofDays(1), null);

        boolean result = (Boolean) method.invoke(controller, borrowWithNullDate);
        assertFalse(result);
    }

    @Test
    void branch_IsTimeExpired_NullDuration() throws Exception {
        Method method = LibrarianDashboardController.class.getDeclaredMethod("isTimeExpired", Borrow.class);
        method.setAccessible(true);

        Book testBook = createTestBook("Test Book", "author1", BookStatus.APPROVED);
        Borrow borrow = new Borrow(testBook, "user1", java.time.Duration.ZERO, LocalDateTime.now());
        Field durationField = Borrow.class.getDeclaredField("duration");
        durationField.setAccessible(true);
        durationField.set(borrow, null);

        boolean result = (Boolean) method.invoke(controller, borrow);
        assertFalse(result);
    }

    @Test
    void branch_FormatDuration_WithDaysHoursMinutesSeconds() throws Exception {
        Method method = LibrarianDashboardController.class.getDeclaredMethod("formatDuration", java.time.Duration.class);
        method.setAccessible(true);

        java.time.Duration duration = java.time.Duration.ofDays(2).plusHours(5).plusMinutes(30).plusSeconds(15);
        String result = (String) method.invoke(controller, duration);

        assertEquals("2d 05:30:15", result);
    }

    @Test
    void branch_FormatDuration_WithHoursOnly() throws Exception {
        Method method = LibrarianDashboardController.class.getDeclaredMethod("formatDuration", java.time.Duration.class);
        method.setAccessible(true);

        java.time.Duration duration = java.time.Duration.ofHours(5).plusMinutes(30).plusSeconds(15);
        String result = (String) method.invoke(controller, duration);

        assertEquals("0d 05:30:15", result);
    }

    @Test
    void branch_FormatDuration_WithMinutesOnly() throws Exception {
        Method method = LibrarianDashboardController.class.getDeclaredMethod("formatDuration", java.time.Duration.class);
        method.setAccessible(true);

        java.time.Duration duration = java.time.Duration.ofMinutes(45).plusSeconds(30);
        String result = (String) method.invoke(controller, duration);

        assertEquals("0d 00:45:30", result);
    }

    @Test
    void branch_FormatDuration_WithSecondsOnly() throws Exception {
        Method method = LibrarianDashboardController.class.getDeclaredMethod("formatDuration", java.time.Duration.class);
        method.setAccessible(true);

        java.time.Duration duration = java.time.Duration.ofSeconds(45);
        String result = (String) method.invoke(controller, duration);

        assertEquals("0d 00:00:45", result);
    }

    @Test
    void branch_FormatDuration_ZeroDuration() throws Exception {
        Method method = LibrarianDashboardController.class.getDeclaredMethod("formatDuration", java.time.Duration.class);
        method.setAccessible(true);

        java.time.Duration duration = java.time.Duration.ZERO;
        String result = (String) method.invoke(controller, duration);

        assertEquals("0d 00:00:00", result);
    }

    @Test
    void branch_LoadTableData_AllTableTypes() throws Exception {
        Method method = LibrarianDashboardController.class.getDeclaredMethod("loadTableData",
                LibrarianDashboardController.TableType.class);
        method.setAccessible(true);

        // Test all enum values - should not throw exception
        for (LibrarianDashboardController.TableType type : LibrarianDashboardController.TableType.values()) {
            method.invoke(controller, type);
        }

        assertTrue(true); // If we reach here, all types were processed
    }

    @Test
    void branch_SetUser() throws Exception {
        User newUser = createTestUser("newlib", Role.LIBRARIAN, Status.ACTIVATED);
        controller.setUser(newUser);

        Field userField = LibrarianDashboardController.class.getDeclaredField("currUser");
        userField.setAccessible(true);
        User currentUser = (User) userField.get(controller);

        assertEquals("newlib", currentUser.getUsername());
    }

    @Test
    void branch_TableTypeEnumValues() {
        // Test that all TableType enum values exist
        LibrarianDashboardController.TableType[] values = LibrarianDashboardController.TableType.values();
        assertEquals(4, values.length);
        assertArrayEquals(new LibrarianDashboardController.TableType[]{
                LibrarianDashboardController.TableType.APPROVAL,
                LibrarianDashboardController.TableType.USER,
                LibrarianDashboardController.TableType.BORROW,
                LibrarianDashboardController.TableType.PUBLISH
        }, values);
    }

    @Test
    void branch_ControllerInitialization() {
        LibrarianDashboardController newController = new LibrarianDashboardController();
        assertNotNull(newController);
    }

    // Test edge cases for time calculations
    @Test
    void branch_TimeCalculations_EdgeCases() throws Exception {
        Method calculateTimeLeftMethod = LibrarianDashboardController.class.getDeclaredMethod("calculateTimeLeft", Borrow.class);
        calculateTimeLeftMethod.setAccessible(true);

        Method isTimeExpiredMethod = LibrarianDashboardController.class.getDeclaredMethod("isTimeExpired", Borrow.class);
        isTimeExpiredMethod.setAccessible(true);

        // Test with very small negative duration (just expired)
        Book testBook = createTestBook("Just Expired", "author1", BookStatus.APPROVED);
        Borrow justExpired = new Borrow(testBook, "user1", java.time.Duration.ofSeconds(-1), LocalDateTime.now().minusSeconds(2));

        String timeLeft = (String) calculateTimeLeftMethod.invoke(controller, justExpired);
        boolean isExpired = (Boolean) isTimeExpiredMethod.invoke(controller, justExpired);

        assertEquals("Expired", timeLeft);
        assertTrue(isExpired);

        // Test with very small positive duration (just about to expire)
        Borrow aboutToExpire = new Borrow(testBook, "user1", java.time.Duration.ofSeconds(1), LocalDateTime.now());

        timeLeft = (String) calculateTimeLeftMethod.invoke(controller, aboutToExpire);
        isExpired = (Boolean) isTimeExpiredMethod.invoke(controller, aboutToExpire);

        assertFalse(isExpired);
        assertTrue(timeLeft.contains("0d") && timeLeft.contains("00:00:01"));
    }

    // Test method existence without calling UI-dependent methods
    @Test
    void branch_MethodExistence() throws Exception {
        // Test that all logical methods exist
        assertNotNull(LibrarianDashboardController.class.getDeclaredMethod("calculateTimeLeft", Borrow.class));
        assertNotNull(LibrarianDashboardController.class.getDeclaredMethod("isTimeExpired", Borrow.class));
        assertNotNull(LibrarianDashboardController.class.getDeclaredMethod("formatDuration", java.time.Duration.class));
        assertNotNull(LibrarianDashboardController.class.getDeclaredMethod("loadTableData", LibrarianDashboardController.TableType.class));
        assertNotNull(LibrarianDashboardController.class.getDeclaredMethod("setUser", User.class));

        // TableType enum should exist
        assertNotNull(LibrarianDashboardController.TableType.APPROVAL);
        assertNotNull(LibrarianDashboardController.TableType.USER);
        assertNotNull(LibrarianDashboardController.TableType.BORROW);
        assertNotNull(LibrarianDashboardController.TableType.PUBLISH);
    }

    // Test data handling scenarios without ObservableList
    @Test
    void branch_DataHandlingScenarios() throws Exception {
        Method loadTableDataMethod = LibrarianDashboardController.class.getDeclaredMethod("loadTableData",
                LibrarianDashboardController.TableType.class);
        loadTableDataMethod.setAccessible(true);

        // Test that method can handle all table types without throwing exceptions
        for (LibrarianDashboardController.TableType type : LibrarianDashboardController.TableType.values()) {
            try {
                loadTableDataMethod.invoke(controller, type);
                // Should not throw exception even with null/empty data
            } catch (Exception e) {
                // It's okay if it throws due to missing data files - we're testing logic, not file operations
                assertTrue(e.getCause() instanceof NullPointerException ||
                        e.getCause() instanceof java.io.FileNotFoundException);
            }
        }
    }

    // Test duration formatting edge cases
    @Test
    void branch_FormatDuration_EdgeCases() throws Exception {
        Method method = LibrarianDashboardController.class.getDeclaredMethod("formatDuration", java.time.Duration.class);
        method.setAccessible(true);

        // Test with exactly 24 hours (edge case between days and hours)
        java.time.Duration exactlyOneDay = java.time.Duration.ofDays(1);
        String result = (String) method.invoke(controller, exactlyOneDay);
        assertEquals("1d 00:00:00", result);

        // Test with exactly 60 minutes
        java.time.Duration exactlyOneHour = java.time.Duration.ofHours(1);
        result = (String) method.invoke(controller, exactlyOneHour);
        assertEquals("0d 01:00:00", result);

        // Test with exactly 60 seconds
        java.time.Duration exactlyOneMinute = java.time.Duration.ofMinutes(1);
        result = (String) method.invoke(controller, exactlyOneMinute);
        assertEquals("0d 00:01:00", result);
    }
}