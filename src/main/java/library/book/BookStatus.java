package library.book;

/**
 * Enum BookStatus
 */
public enum BookStatus {
    PENDING("Pending"),
    DELETED("Deleted"),
    APPROVED("Approved"),
    REJECTED("Rejected");

    private final String statusName;

    BookStatus(String status) {
        this.statusName = status;
    }
    /**
     * Getters: Return BookStatus
     * @return BookStatus Object
     */
    public String getBookStatus() {
        return statusName;
    }

    /**
     * Help Function call whenever need to turn a String to BookStatus
     * @return BookStatus Object
     */
    public static BookStatus fromString(String status) {
        for (BookStatus bs : BookStatus.values()) {
            if (bs.name().equalsIgnoreCase(status) || bs.getBookStatus().equalsIgnoreCase(status)) {
                return bs;
            }
        }
        throw new IllegalArgumentException("Invalid BookStatus: " + status);
    }
}
