package com.example.uts_pbo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    
    // Create a new user in the database
    public static boolean createUser(User user) {
        String sql = "INSERT INTO users (email, first_name, last_name, username, password, salt, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getFirstName());
            stmt.setString(3, user.getLastName());
            stmt.setString(4, user.getUsername());
            stmt.setString(5, user.getPassword());
            stmt.setString(6, user.getSalt());
            stmt.setString(7, user.getRole());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Log account creation
                AuthLogger.logAccountCreation(user);
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Get user by username or email
    public static User getUserByUsernameOrEmail(String usernameOrEmail) {
        String sql = "SELECT * FROM users WHERE username = ? OR email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, usernameOrEmail);
            stmt.setString(2, usernameOrEmail);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User(
                    rs.getInt("id"),
                    rs.getString("email"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("salt"),
                    rs.getString("role")
                );
                return user;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Authenticate user
    public static User authenticateUser(String usernameOrEmail, String password) {
        // First, get the user to access the salt
        User user = getUserByUsernameOrEmail(usernameOrEmail);
        
        if (user != null) {
            // Verify the password using the stored salt
            if (user.verifyPassword(password)) {
                // Log successful login
                AuthLogger.logLogin(user);
                return user;
            } else {
                // Log failed login attempt with correct username but wrong password
                AuthLogger.logFailedPasswordAttempt(user);
            }
        } else {
            // Log failed login attempt with non-existent username/email
            AuthLogger.logFailedLogin(usernameOrEmail);
        }
        
        return null;
    }
    
    // Update user details
    public static boolean updateUser(User user) {
        // Get the original user data to check for role changes
        User originalUser = getUserByUsernameOrEmail(user.getUsername());
        String originalRole = originalUser != null ? originalUser.getRole() : null;
        
        String sql = "UPDATE users SET email = ?, first_name = ?, last_name = ?, username = ?, password = ?, salt = ?, role = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getFirstName());
            stmt.setString(3, user.getLastName());
            stmt.setString(4, user.getUsername());
            stmt.setString(5, user.getPassword());
            stmt.setString(6, user.getSalt());
            stmt.setString(7, user.getRole());
            stmt.setInt(8, user.getId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Check if password was changed (simple check - if salt is different)
                if (originalUser != null && !originalUser.getSalt().equals(user.getSalt())) {
                    AuthLogger.logPasswordChange(user);
                }
                
                // Check if role was changed
                if (originalUser != null && !originalUser.getRole().equals(user.getRole())) {
                    AuthLogger.logRoleChange(user, originalRole, user.getRole());
                }
                
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Update user role
    public static boolean updateUserRole(int userId, String newRole) {
        // Get the original user to log role change
        User user = null;
        String oldRole = null;
        
        // First get the current user data
        String selectSql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                user = new User(
                    rs.getInt("id"),
                    rs.getString("email"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("salt"),
                    rs.getString("role")
                );
                oldRole = user.getRole();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Now update the role
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newRole);
            stmt.setInt(2, userId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0 && user != null) {
                // Update the user object with the new role for logging
                user.setRole(newRole);
                // Log role change
                AuthLogger.logRoleChange(user, oldRole, newRole);
                return true;
            }
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Get all users
    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                User user = new User(
                    rs.getInt("id"),
                    rs.getString("email"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("salt"),
                    rs.getString("role")
                );
                users.add(user);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return users;
    }
    
    // Get users by role
    public static List<User> getUsersByRole(String role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, role);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                User user = new User(
                    rs.getInt("id"),
                    rs.getString("email"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("salt"),
                    rs.getString("role")
                );
                users.add(user);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return users;
    }
    
    // Delete user
    public static boolean deleteUser(int userId) {
        // Get user data for logging before deletion
        User user = null;
        String selectSql = "SELECT * FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                user = new User(
                    rs.getInt("id"),
                    rs.getString("email"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("salt"),
                    rs.getString("role")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Now delete the user
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0 && user != null) {
                // Log account deletion
                AuthLogger.logAccountDeletion(user);
                return true;
            }
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Check if email exists
    public static boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Check if username exists
    public static boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Create admin user (utility method)
    public static boolean createAdminUser(String email, String username, String password, String firstName, String lastName) {
        // Create a User object with the admin role
        User adminUser = new User(firstName, lastName, email, username, password, "admin");
        return createUser(adminUser);
    }
    
    // Log user logout - call this when user logs out
    public static void logUserLogout(User user) {
        if (user != null) {
            AuthLogger.logLogout(user);
        }
    }
}