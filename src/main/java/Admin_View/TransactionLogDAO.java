package Admin_View;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionLogDAO {
    
    public static List<TransactionLogEntry> getAllLogs() {
        List<TransactionEntry> transactions = TransactionDAO.getAllTransactions();
        List<TransactionLogEntry> logs = new ArrayList<>();
        
        int id = 1;
        for (TransactionEntry transaction : transactions) {
            String status = "Completed"; // Default status
            
            logs.add(TransactionLogEntry.fromTransactionEntry(id++, transaction, status));
        }
        
        return logs;
    }
    
    public static List<TransactionLogEntry> getLogsByTransactionType(String transactionType) {
        return getAllLogs().stream()
                .filter(log -> log.getTransactionType().equals(transactionType))
                .collect(Collectors.toList());
    }
    
    public static List<TransactionLogEntry> getLogsByStatus(String status) {
        return getAllLogs().stream()
                .filter(log -> log.getStatus().equals(status))
                .collect(Collectors.toList());
    }
    
    public static List<TransactionLogEntry> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<TransactionEntry> transactions = TransactionDAO.getTransactionsByDateRange(startDate, endDate);
        List<TransactionLogEntry> logs = new ArrayList<>();
        
        int id = 1;
        for (TransactionEntry transaction : transactions) {
            String status = "Completed"; // Default status
            
            logs.add(TransactionLogEntry.fromTransactionEntry(id++, transaction, status));
        }
        
        return logs;
    }
    
}