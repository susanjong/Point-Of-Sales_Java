package com.example.uts_pbo;

public class User {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password; // This will store the hashed password
    private String salt; // New field to store salt
    private String role;

    // Constructor for new users (without ID) - plain password version
    public User(String firstName, String lastName, String email, String username, String plainPassword) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.salt = PasswordHasher.generateSalt();
        this.password = PasswordHasher.hashPassword(plainPassword, this.salt);
        this.role = "user"; // Default role
    }

    // Constructor for new users with specific role - plain password version
    public User(String firstName, String lastName, String email, String username, String plainPassword, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.salt = PasswordHasher.generateSalt();
        this.password = PasswordHasher.hashPassword(plainPassword, this.salt);
        this.role = role;
    }

    // Constructor for existing users from database (with ID and already hashed password)
    public User(int id, String email, String firstName, String lastName, String username, 
                String hashedPassword, String salt, String role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = hashedPassword;
        this.salt = salt;
        this.role = role;
    }

    public void setId(int id) {
        this.id = id;
    }
    // Getter methods
    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
    
    public String getSalt() {
        return salt;
    }
    
    public String getRole() {
        return role;
    }
    
    // Setter method for role
    public void setRole(String role) {
        this.role = role;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    

    // Method to verify a password against this user's stored hash
    public boolean verifyPassword(String inputPassword) {
        return PasswordHasher.verifyPassword(inputPassword, this.password, this.salt);
    }

    // Validation methods
    public boolean isValid() {
        return !firstName.isEmpty() && !lastName.isEmpty() && !email.isEmpty() && !username.isEmpty() && !password.isEmpty();
    }
    
    // Role check methods
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(this.role);
    }
    
    public boolean isUser() {
        return "user".equalsIgnoreCase(this.role);
    }

    // Add this method to the User class
public static boolean isValidPassword(String password) {
    // Check if password is between 6-20 characters
    if (password.length() < 6 || password.length() > 20) {
        return false;
    }
    
    // Check for at least one digit
    boolean hasDigit = false;
    // Check for at least one uppercase letter
    boolean hasUppercase = false;
    // Check for at least one lowercase letter
    boolean hasLowercase = false;
    
    for (char c : password.toCharArray()) {
        if (Character.isDigit(c)) {
            hasDigit = true;
        } else if (Character.isUpperCase(c)) {
            hasUppercase = true;
        } else if (Character.isLowerCase(c)) {
            hasLowercase = true;
        }
    }
    
    // Password must contain at least one digit, one uppercase, and one lowercase letter
    return hasDigit && hasUppercase && hasLowercase;
}
}