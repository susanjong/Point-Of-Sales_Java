package Admin_View;
import com.example.uts_pbo.User;
import com.example.uts_pbo.UserSession;
/**
 * Utility class for logging transaction activities
 */
public class TransactionLogger {
    
    // Static method to log a transaction activity
    public static boolean logTransaction(int transactionId, double amount, int itemCount, 
                                        String paymentMethod, String transactionType, String status) {
        
        User currentUser = UserSession.getCurrentUser();
        int userId = 0;
        String username = "guest";
        String role = "guest";
        
        if (currentUser != null) {
            userId = currentUser.getId();
            username = currentUser.getUsername();
            role = currentUser.getRole();
        }
        
        TransactionLogEntry entry = new TransactionLogEntry(
            transactionId, userId, username, role, amount, itemCount, paymentMethod, transactionType, status
        );
        
        return TransactionLogDAO.recordLog(entry);
    }
    
    // Convenience method for logging completed sales
    public static boolean logSale(int transactionId, double amount, int itemCount, String paymentMethod) {
        return logTransaction(transactionId, amount, itemCount, paymentMethod, "Sale", "Completed");
    }
    
    // Convenience method for logging canceled sales
    public static boolean logCanceledSale(int transactionId, double amount, int itemCount, String paymentMethod) {
        return logTransaction(transactionId, amount, itemCount, paymentMethod, "Sale", "Canceled");
    }
    
    // Convenience method for logging returns
    public static boolean logReturn(int transactionId, double amount, int itemCount, String paymentMethod) {
        return logTransaction(transactionId, amount, itemCount, paymentMethod, "Return", "Completed");
    }
    
    // Ensure the transaction log table exists in the database
    public static void ensureLogTableExists() {
        TransactionLogDAO.createTableIfNotExists();
    }
    
    // Initialize the logging system
    public static void initialize() {
        ensureLogTableExists();
    }
    
    // Convenience method for logging canceled returns
    public static boolean logCanceledReturn(int transactionId, double amount, int itemCount, String paymentMethod) {
        return logTransaction(transactionId, amount, itemCount, paymentMethod, "Return", "Canceled");
    }
}