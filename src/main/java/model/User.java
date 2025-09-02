package model;

/**
 * Represents a user (normal or admin)
 */
public class User {
    private int id;
    private String username;
    private String encryptedPassword;  // Shift cipher encrypted
    private String preferredName;      // Display name
    private boolean isAdmin;           // Admin flag

    public User() {}

    public User(int id, String username, String encryptedPassword, String preferredName, boolean isAdmin) {
        this.id = id;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.preferredName = preferredName;
        this.isAdmin = isAdmin;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEncryptedPassword() { return encryptedPassword; }
    public void setEncryptedPassword(String encryptedPassword) { this.encryptedPassword = encryptedPassword; }
    public String getPreferredName() { return preferredName; }
    public void setPreferredName(String preferredName) { this.preferredName = preferredName; }
    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }
}