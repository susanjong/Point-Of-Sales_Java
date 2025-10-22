package Admin_View;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import User_dashboard.DatabaseConnection;

public class RefundDAO {
    public static List<RefundEntry> getAllRefunds() {
        List<RefundEntry> refunds = new ArrayList<>();
        String sql = "SELECT refund_id, timestamp, transaction_id, username, product_code, qty, total_refund FROM refund ORDER BY timestamp DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                int refundId = rs.getInt("refund_id");
                LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
                int transactionId = rs.getInt("transaction_id");
                String username = rs.getString("username");
                String productCode = rs.getString("product_code");
                int qty = rs.getInt("qty");
                double totalRefund = rs.getDouble("total_refund");
                
                RefundEntry refund = new RefundEntry(refundId, timestamp, transactionId, 
                                                  username, productCode, qty, totalRefund);
                refunds.add(refund);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return refunds;
    }
}