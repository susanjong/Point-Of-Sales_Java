package Admin_View;

import com.example.uts_pbo.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    public static List<TransactionEntry> getAllTransactions() {
        List<TransactionEntry> list = new ArrayList<>();
        String sql = "SELECT transaction_id, date, username, products, total_price, total_item FROM transaction ORDER BY date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("transaction_id");
                LocalDateTime date = rs.getTimestamp("date").toLocalDateTime();
                String username = rs.getString("username");
                String products = rs.getString("products");
                double totalPrice = rs.getDouble("total_price");
                int totalItem = rs.getInt("total_item");

                list.add(new TransactionEntry(id, date, username, products, totalPrice, totalItem));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
