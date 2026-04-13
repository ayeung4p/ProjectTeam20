package library.user;

public enum Role {
    STUDENT("Student"),
    AUTHOR("Author"),
    LIBRARIAN("Librarian");

    private final String displayName;

    Role(String role){
        this.displayName = role;
    }

    /**
     * Return the display name of the type.
     * @return displayName of the <code>Role</code>
     */
    public String getDisplayName(){
        return this.displayName;
    }

    /**
     * Convert String to valid enum value.
     * @param roleStr
     * @return <code>Role</code> object
     */
    public static Role fromString(String roleStr) {
        return switch(roleStr.toLowerCase()) {
            case "student", "staff", "student/staff" -> STUDENT;
            case "author" -> AUTHOR;
            case "librarian" -> LIBRARIAN;
            default -> throw new IllegalArgumentException("Invalid Role: " + roleStr);
        };
    }
}
