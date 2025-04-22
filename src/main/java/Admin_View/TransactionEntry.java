package Admin_View;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TransactionEntry {
    private int transactionId;
    private LocalDateTime date;
    private String username;
    private String products;
    private double totalPrice;
    private int totalItem;
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TransactionEntry(int transactionId, LocalDateTime date, String username, String products, double totalPrice, int totalItem) {
        this.transactionId = transactionId;
        this.date = date;
        this.username = username;
        this.products = products;
        this.totalPrice = totalPrice;
        this.totalItem = totalItem;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getFormattedDate() {
        return date.format(formatter);
    }

    public String getUsername() {
        return username;
    }

    public String getProducts() {
        return products;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getFormattedTotalPrice() {
        return String.format("%,.2f", totalPrice);
    }

    public int getTotalItem() {
        return totalItem;
    }
}