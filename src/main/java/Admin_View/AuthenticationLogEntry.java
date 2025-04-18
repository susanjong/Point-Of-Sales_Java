package Admin_View;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuthenticationLogEntry {
    private int id;
    private LocalDateTime timestamp;
    private int userId;
    private String username;
    private String role;
    private String email;
    private String activity;
    
    // For new log entries
    public AuthenticationLogEntry(int userId, String username, String role, String email, String activity) {
        this.timestamp = LocalDateTime.now();
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.email = email;
        this.activity = activity;
    }
    
    // For loading from database
    public AuthenticationLogEntry(int id, LocalDateTime timestamp, int userId, String username, 
                                  String role, String email, String activity) {
        this.id = id;
        this.timestamp = timestamp;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.email = email;
        this.activity = activity;
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }
    
    public int getUserId() {
        return userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getRole() {
        return role;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getActivity() {
        return activity;
    }
}