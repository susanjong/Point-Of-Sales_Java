package Admin_View;

import javafx.beans.property.*;

public class Refund {
    private final StringProperty refundCode;
    private final StringProperty productName;
    private final StringProperty productCode;
    private final DoubleProperty price;
    private final IntegerProperty quantity;
    
    public Refund(String refundCode, String productName, String productCode, Double price, Integer quantity) {
        this.refundCode = new SimpleStringProperty(refundCode);
        this.productName = new SimpleStringProperty(productName);
        this.productCode = new SimpleStringProperty(productCode);
        this.price = new SimpleDoubleProperty(price);
        this.quantity = new SimpleIntegerProperty(quantity);
    }
    
    // Getters for properties
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
    
    // Getters for values
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
    public void setRefundCode(String value) {
        refundCode.set(value);
    }
    
    public void setProductName(String value) {
        productName.set(value);
    }
    
    public void setProductCode(String value) {
        productCode.set(value);
    }
    
    public void setPrice(double value) {
        price.set(value);
    }
    
    public void setQuantity(int value) {
        quantity.set(value);
    }
    
    // Equals method for checking if a refund is already in a list
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Refund refund = (Refund) o;
        
        if (!getRefundCode().equals(refund.getRefundCode())) return false;
        return getProductCode().equals(refund.getProductCode());
    }
    
    @Override
    public int hashCode() {
        int result = getRefundCode().hashCode();
        result = 31 * result + getProductCode().hashCode();
        return result;
    }
}