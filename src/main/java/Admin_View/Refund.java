package Admin_View;

import javafx.beans.property.*;

public class Refund {
    private final StringProperty refundCode;
    private final StringProperty productName;
    private final StringProperty productCode;
    private final DoubleProperty price;
    private final IntegerProperty quantity;
    
    public Refund(String refundCode, String productName, String productCode, double price, int quantity) {
        this.refundCode = new SimpleStringProperty(refundCode);
        this.productName = new SimpleStringProperty(productName);
        this.productCode = new SimpleStringProperty(productCode);
        this.price = new SimpleDoubleProperty(price);
        this.quantity = new SimpleIntegerProperty(quantity);
    }
    
    // Getters
    public String getRefundCode() {
        return refundCode.get();
    }
    
    public String getProductName() {
        return productName.get();
    }
    
    public String getProductCode() {
        return productCode.get();
    }
    
    public double getPrice() {
        return price.get();
    }
    
    public int getQuantity() {
        return quantity.get();
    }
    
    // Setters
    public void setRefundCode(String refundCode) {
        this.refundCode.set(refundCode);
    }
    
    public void setProductName(String productName) {
        this.productName.set(productName);
    }
    
    public void setProductCode(String productCode) {
        this.productCode.set(productCode);
    }
    
    public void setPrice(double price) {
        this.price.set(price);
    }
    
    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }
    
    // Property getters
    public StringProperty refundCodeProperty() {
        return refundCode;
    }
    
    public StringProperty productNameProperty() {
        return productName;
    }
    
    public StringProperty productCodeProperty() {
        return productCode;
    }
    
    public DoubleProperty priceProperty() {
        return price;
    }
    
    public IntegerProperty quantityProperty() {
        return quantity;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Refund refund = (Refund) obj;
        return getRefundCode().equals(refund.getRefundCode());
    }
    
    @Override
    public int hashCode() {
        return refundCode.hashCode();
    }
}