package User_dashboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminInitializer {
    
    // Default admin credentials
    private static final String DEFAULT_ADMIN_EMAIL = "admin@simplemart.com";
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin123";
    
    /**
     * Initializes the default admin user if no admin exists in the system
     */
    public static void initialize() {
        // Check if any admin exists
        if (!adminExists()) {
            // Create default admin
            UserDAO.createAdminUser(
                DEFAULT_ADMIN_EMAIL,
                DEFAULT_ADMIN_USERNAME,
                DEFAULT_ADMIN_PASSWORD,
                "Admin",
                "User"
            );
            System.out.println("Default admin account created!");
        }
    }
    
    /**
     * Checks if any user with admin role exists in the database
     */
    private static boolean adminExists() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'admin'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
}