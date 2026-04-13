package library.user;

public enum Status {
    ACTIVATED("Activated"),
    DEACTIVATED("Deactivated");

    private final String displayName;

    Status(String status){
        this.displayName = status;
    }

    /**
     * Return the display name of the type.
     * @return displayName of the <code>Status</code>
     */
    public String getDisplayName(){
        return this.displayName;
    }

    /**
     * Convert String to valid enum value.
     * @param statusStr
     * @return <code>Status</code> object
     */
    public static Status fromString(String statusStr) {
        return switch(statusStr.toLowerCase()) {
            case "activated" -> ACTIVATED;
            case "deactivated" -> DEACTIVATED;
            default -> throw new IllegalArgumentException("Invalid status: " + statusStr);
        };
    }
}
