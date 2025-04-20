package Admin_View;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BundleProduct extends Product {
    private final List<BundleItem> bundleItems = new ArrayList<>();
    private final StringProperty bundleName = new SimpleStringProperty();
    
    public BundleProduct() {
        super();
    }
    
    public BundleProduct(String code, String bundleName, double price, int quantity, String category, String imagePath) {
        super(code, bundleName, price, quantity, null, category, imagePath);
        this.bundleName.set(bundleName);
    }
    
    public String getBundleName() {
        return bundleName.get();
    }
    
    public StringProperty bundleNameProperty() {
        return bundleName;
    }
    
    public void setBundleName(String bundleName) {
        this.bundleName.set(bundleName);
        setName(bundleName);
    }
    
    public List<BundleItem> getBundleItems() {
        return bundleItems;
    }
    
    public void addBundleItem(Product product, int quantity) {
        bundleItems.add(new BundleItem(product, quantity));
    }
    
    public void removeBundleItem(Product product) {
        bundleItems.removeIf(item -> item.getProduct().getCode().equals(product.getCode()));
    }
    
    // Calculate total price of individual products
    public double getTotalIndividualPrice() {
        double total = 0;
        for (BundleItem item : bundleItems) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        return total;
    }
    
    // Calculate discount amount
    public double getDiscountedPrice() {
        double individualTotal = getTotalIndividualPrice();
        double bundlePrice = getPrice();
        return individualTotal - bundlePrice;
    }
    
    // Inner class to represent bundle items
    public class BundleItem {
        private final Product product;
        private final IntegerProperty quantity = new SimpleIntegerProperty();
        
        public BundleItem(Product product, int quantity) {
            this.product = product;
            this.quantity.set(quantity);
        }
        
        public Product getProduct() {
            return product;
        }
        
        public int getQuantity() {
            return quantity.get();
        }
        
        public IntegerProperty quantityProperty() {
            return quantity;
        }
        
        public void setQuantity(int quantity) {
            this.quantity.set(quantity);
        }
    }
}

