package Admin_View;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class UserManagementController implements Initializable {

    // Navigation buttons
    @FXML private Button profileBtn;
    @FXML private Button cashierBtn;
    @FXML private Button productsBtn;
    @FXML private Button usersBtn;
    @FXML private Button adminLogBtn;
    
    // Table view and columns
    @FXML private TableView<User> UserTable;
    @FXML private TableColumn<User, Integer> UserIDColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> UsernameColumn;
    @FXML private TableColumn<User, String> EmailColumn;
    @FXML private TableColumn<User, String> PasswordColumn;
    @FXML private TableColumn<User, String> RoleColumn;
    
    // Form fields
    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private TextField UsernameField;
    @FXML private TextField EmailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    
    // Action buttons
    @FXML private Button saveUserBtn;
    @FXML private Button deleteUserBtn;
    
    // Observable list to hold user data
    private ObservableList<User> userList = FXCollections.observableArrayList();
    
    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/simplemart";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";
    
    // SQL queries
    private static final String SQL_SELECT_ALL_USERS = "SELECT * FROM users";
    private static final String SQL_INSERT_USER = "INSERT INTO users (name, username, email, password, role) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE_USER = "UPDATE users SET name = ?, username = ?, email = ?, password = ?, role = ? WHERE user_id = ?";
    private static final String SQL_DELETE_USER = "DELETE FROM users WHERE user_id = ?";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize table columns
        UserIDColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        UsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        EmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        PasswordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));
        RoleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        
        // Load data from database
        loadUsersFromDatabase();
        
        // Set table data
        UserTable.setItems(userList);
        
        // Add listener for table row selection
        UserTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    displayUserDetails(newSelection);
                }
            }
        );
        
        // Initialize combo box if not already populated
        if (roleComboBox.getItems().isEmpty()) {
            roleComboBox.setItems(FXCollections.observableArrayList("Admin", "Cashier", "Staff"));
        }
        
        // Set auto-generated ID field as disabled
        codeField.setDisable(true);
    }
    
    /**
     * Establishes a connection to the database
     */
    private Connection getConnection() throws SQLException {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }
    
    /**
     * Loads users from the database into the observable list
     */
    private void loadUsersFromDatabase() {
        userList.clear();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_SELECT_ALL_USERS)) {
            
            while (rs.next()) {
                int id = rs.getInt("user_id");
                String name = rs.getString("name");
                String username = rs.getString("username");
                String email = rs.getString("email");
                String password = rs.getString("password");
                String role = rs.getString("role");
                
                userList.add(new User(id, name, username, email, password, role));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Error loading users from database", e.getMessage());
        }
    }
    
    /**
     * Inserts a new user into the database
     */
    private boolean insertUser(String name, String username, String email, String password, String role) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, username);
            pstmt.setString(3, email);
            pstmt.setString(4, password);
            pstmt.setString(5, role);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                return false;
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    userList.add(new User(id, name, username, email, password, role));
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Error inserting user", e.getMessage());
            return false;
        }
    }
    
    /**
     * Updates an existing user in the database
     */
    private boolean updateUser(int id, String name, String username, String email, String password, String role) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE_USER)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, username);
            pstmt.setString(3, email);
            pstmt.setString(4, password);
            pstmt.setString(5, role);
            pstmt.setInt(6, id);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // Update succeeded, refresh the list
                loadUsersFromDatabase();
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Error updating user", e.getMessage());
            return false;
        }
    }
    
    /**
     * Deletes a user from the database
     */
    private boolean deleteUser(int id) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_DELETE_USER)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // Update succeeded, refresh the list
                loadUsersFromDatabase();
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Error deleting user", e.getMessage());
            return false;
        }
    }
    
    /**
     * Handles navigation button clicks
     */
    @FXML
    void handleNavigation(ActionEvent event) {
        Object source = event.getSource();
        
        try {
            String fxmlFile = "";
            
            if (source == profileBtn) {
                fxmlFile = "Profile.fxml";
            } else if (source == cashierBtn) {
                fxmlFile = "Cashier.fxml";
            } else if (source == productsBtn) {
                fxmlFile = "ProductManagement.fxml";
            } else if (source == adminLogBtn) {
                fxmlFile = "AuthenticationLog.fxml";
            } else if (source == usersBtn) {
                return;
            }
            
            if (!fxmlFile.isEmpty()) {
                URL url = getClass().getResource(fxmlFile);
                
                if (url == null) {
                    // Try alternative path format if the first attempt fails
                    String altPath = fxmlFile.replace("/com/example/uts_pbo/", "/");
                    url = getClass().getResource(altPath);
                    
                    if (url == null) {
                        // Try one more alternative - without leading slash
                        String noSlashPath = fxmlFile.substring(1);
                        url = getClass().getClassLoader().getResource(noSlashPath);
                        
                        if (url == null) {
                            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                                "Could not find FXML file: " + fxmlFile + 
                                "\nPlease check if the file exists in the resources folder.");
                            return;
                        }
                    }
                }
                
                Parent root = FXMLLoader.load(url);
                Stage stage = (Stage) ((Button) source).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            }
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                    "Could not navigate to the requested page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveUser() {
        String name = nameField.getText();
        String username = UsernameField.getText();
        String email = EmailField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();
        
        // Validate inputs
        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || 
            password.isEmpty() || role == null) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input", 
                    "Please fill in all fields", null);
            return;
        }
        
        boolean success;
        
        // Check if we're updating or creating
        if (!codeField.getText().isEmpty()) {
            // Updating existing user
            try {
                int userId = Integer.parseInt(codeField.getText());
                success = updateUser(userId, name, username, email, password, role);
                
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                            "User updated successfully", null);
                    clearForm();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", 
                            "Failed to update user", null);
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Invalid user ID format", null);
            }
        } else {
            // Creating new user
            success = insertUser(name, username, email, password, role);
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                        "User created successfully", null);
                clearForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Failed to create user", null);
            }
        }
    }
    
    /**
     * Handles delete user button click
     */
    @FXML
    private void handleDeleteUser() {
        User selectedUser = UserTable.getSelectionModel().getSelectedItem();
        
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", 
                    "Please select a user to delete", null);
            return;
        }
        
        // Confirm deletion
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Delete User");
        confirmDialog.setContentText("Are you sure you want to delete user: " + 
                selectedUser.getName() + " (" + selectedUser.getUsername() + ")?");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Delete from database
            boolean success = deleteUser(selectedUser.getId());
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                        "User deleted successfully", null);
                clearForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Failed to delete user", null);
            }
        }
    }
    
    /**
     * Display selected user details in form
     */
    private void displayUserDetails(User user) {
        codeField.setText(String.valueOf(user.getId()));
        nameField.setText(user.getName());
        UsernameField.setText(user.getUsername());
        EmailField.setText(user.getEmail());
        passwordField.setText(user.getPassword());
        roleComboBox.setValue(user.getRole());
    }
    
    /**
     * Clear the form fields
     */
    private void clearForm() {
        codeField.clear();
        nameField.clear();
        UsernameField.clear();
        EmailField.clear();
        passwordField.clear();
        roleComboBox.setValue(null);
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class User {
        private final int id;
        private String name;
        private String username;
        private String email;
        private String password;
        private String role;
        
        public User(int id, String name, String username, String email, String password, String role) {
            this.id = id;
            this.name = name;
            this.username = username;
            this.email = email;
            this.password = password;
            this.role = role;
        }
        
        // Getters and setters
        public int getId() { return id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}