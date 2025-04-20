package Admin_View;

import java.util.Date;

public abstract class Transaction {
    protected Date date;
    protected int transactionId;
    
    public Transaction() {
        this.date = new Date();
    }
    
    public Transaction(Date date, int transactionId) {
        this.date = date;
        this.transactionId = transactionId;
    }
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }
    
    public int getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }
}