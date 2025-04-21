package com.example.uts_pbo;
import Admin_View.AuthenticationLogDAO;
import Admin_View.AuthenticationLogEntry;
public class AuthLogger {
    
    // Log successful login
    public static void logLogin(User user) {
        AuthenticationLogEntry entry = new AuthenticationLogEntry(
            user.getId(),
            user.getUsername(),
            user.getRole(),
            user.getEmail(),
            "Login"
        );
        AuthenticationLogDAO.recordLog(entry);
    }
    
    // Log failed login attempt
    public static void logFailedLogin(String usernameOrEmail) {
        // Try to get user info if the username/email exists
        User user = UserDAO.getUserByUsernameOrEmail(usernameOrEmail);
        
        if (user != null) {
            AuthenticationLogEntry entry = new AuthenticationLogEntry(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getEmail(),
                "Failed Login"
            );
            AuthenticationLogDAO.recordLog(entry);
        } else {
            // Handle case where username doesn't exist
            AuthenticationLogEntry entry = new AuthenticationLogEntry(
                -1, // Use -1 to indicate unknown user
                usernameOrEmail,
                "unknown",
                "unknown@example.com",
                "Failed Login"
            );
            AuthenticationLogDAO.recordLog(entry);
        }
    }
    
    // Log account creation
    public static void logAccountCreation(User user) {
        AuthenticationLogEntry entry = new AuthenticationLogEntry(
            user.getId(),
            user.getUsername(),
            user.getRole(),
            user.getEmail(),
            "Account Creation"
        );
        AuthenticationLogDAO.recordLog(entry);
    }
    
    // Log account deletion
    public static void logAccountDeletion(User user) {
        AuthenticationLogEntry entry = new AuthenticationLogEntry(
            user.getId(),
            user.getUsername(),
            user.getRole(),
            user.getEmail(),
            "Account Deletion"
        );
        AuthenticationLogDAO.recordLog(entry);
    }
    
    // Log role change
    public static void logRoleChange(User user, String oldRole, String newRole) {
        AuthenticationLogEntry entry = new AuthenticationLogEntry(
            user.getId(),
            user.getUsername(),
            newRole, // Log the new role
            user.getEmail(),
            "Role Change"
        );
        AuthenticationLogDAO.recordLog(entry);
    }
    
    // Log password change
    public static void logPasswordChange(User user) {
        AuthenticationLogEntry entry = new AuthenticationLogEntry(
            user.getId(),
            user.getUsername(),
            user.getRole(),
            user.getEmail(),
            "Password Change"
        );
        AuthenticationLogDAO.recordLog(entry);
    }
    
    // Log failed password attempts
    public static void logFailedPasswordAttempt(User user) {
        AuthenticationLogEntry entry = new AuthenticationLogEntry(
            user.getId(),
            user.getUsername(),
            user.getRole(),
            user.getEmail(),
            "Failed Password Attempt"
        );
        AuthenticationLogDAO.recordLog(entry);
    }

    // Add this method to AuthLogger.java
    public static void logLogout(User user) {
        AuthenticationLogEntry entry = new AuthenticationLogEntry(
            user.getId(),
            user.getUsername(),
            user.getRole(),
            user.getEmail(),
            "Logout"
        );
        AuthenticationLogDAO.recordLog(entry);
    }
}