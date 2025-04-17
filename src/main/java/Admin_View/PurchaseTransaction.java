package Admin_View;

import java.util.ArrayList;
import java.util.List;

public class PurchaseTransaction extends Transaction implements Payable {
    private List<CashierController.CartItem> items;
    private double amountPaid;
    
    public PurchaseTransaction() {
        super();
        this.items = new ArrayList<>();
        this.amountPaid = 0.0;
    }
    
    public void addItem(CashierController.CartItem item) {
        items.add(item);
    }
    
    public void setItems(List<CashierController.CartItem> items) {
        this.items = new ArrayList<>(items);
    }
    
    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }
    
    public double getAmountPaid() {
        return amountPaid;
    }
    
    public List<CashierController.CartItem> getItems() {
        return new ArrayList<>(items);
    }
    
    public double getChange() {
        return amountPaid - calculateTotal();
    }
    
    @Override
    public double calculateTotal() {
        double total = 0.0;
        for (CashierController.CartItem item : items) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }
    
    @Override
    public void processTransaction() {
        System.out.println("Transaksi dengan ID " + getTransactionId() + " telah diproses");
    }
    
    @Override
    public String serializeTransaction() {
        StringBuilder sb = new StringBuilder();
        sb.append("PurchaseTransaction{");
        sb.append("id=").append(getTransactionId());
        sb.append(", date=").append(getDate());
        sb.append(", items=[");
        
        for (int i = 0; i < items.size(); i++) {
            CashierController.CartItem item = items.get(i);
            sb.append(item.getName()).append("(").append(item.getQuantity()).append(")");
            if (i < items.size() - 1) {
                sb.append(", ");
            }
        }
        
        sb.append("], total=").append(calculateTotal());
        sb.append(", amountPaid=").append(amountPaid);
        sb.append(", change=").append(getChange());
        sb.append("}");
        
        return sb.toString();
    }
}