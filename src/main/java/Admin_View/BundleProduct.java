package Admin_View;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BundleProduct extends Product {
    
    // Additional properties needed by BundleProductsController
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty productCode = new SimpleStringProperty();
    private final IntegerProperty qty = new SimpleIntegerProperty();
    private LocalDate expDate;
    
    // List of products in this bundle
    private List<BundleItem> bundleItems;
    
    // Discount percentage for this bundle
    private DoubleProperty discountPercentage = new SimpleDoubleProperty(0);
    
    // Default constructor
    public BundleProduct() {
        super();
        this.bundleItems = new ArrayList<>();
        setCategory("Bundle");
        this.expDate = LocalDate.now().plusMonths(3); // Default expiry date
    }
    
    // Constructor matching the one used in BundleProductsController
    public BundleProduct(int id, String code, String name, String productCode, double price, int qty, 
                        LocalDate expDate, String category) {
        super(code, name, price, qty, expDate.toString(), category, "");
        this.id.set(id);
        this.productCode.set(productCode);
        this.qty.set(qty);
        this.expDate = expDate;
        this.bundleItems = new ArrayList<>();
    }

    public BundleProduct(String code, double price) {
        super();
        setCode(code); // Use the provided code from database
        setPrice(price);
        setCategory("Bundle");
        this.expDate = LocalDate.now().plusMonths(3); 
        this.bundleItems = new ArrayList<>();
    }
    
    // Constructor to match Product's constructor plus our additional fields
    public BundleProduct(String code, String name, double price, int quantity, 
                        String expirationDate, String category, String imagePath,
                        String productCode, int id) {
        super(code, name, price, quantity, expirationDate, category, imagePath);
        this.productCode.set(productCode);
        this.id.set(id);
        this.qty.set(quantity);
        try {
            this.expDate = LocalDate.parse(expirationDate);
        } catch (Exception e) {
            this.expDate = LocalDate.now().plusMonths(3);
        }
        this.bundleItems = new ArrayList<>();
    }
    
    // Constructor with discount percentage
    public BundleProduct(String code, String name, double price, int quantity, 
                        String expirationDate, String category, String imagePath,
                        String productCode, int id, double discountPercentage) {
        this(code, name, price, quantity, expirationDate, category, imagePath, productCode, id);
        this.discountPercentage.set(discountPercentage);
    }
    
    // Getters and setters for id
    public int getId() {
        return id.get();
    }
    
    public void setId(int id) {
        this.id.set(id);
    }
    
    public IntegerProperty idProperty() {
        return id;
    }
    
    // Getters and setters for productCode
    public String getProductCode() {
        return productCode.get();
    }
    
    public void setProductCode(String productCode) {
        this.productCode.set(productCode);
    }
    
    public StringProperty productCodeProperty() {
        return productCode;
    }
    
    // Getters and setters for qty
    public int getQty() {
        return qty.get();
    }
    
    public void setQty(int qty) {
        this.qty.set(qty);
        super.setQuantity(qty); // Update the parent quantity as well
    }
    
    public IntegerProperty qtyProperty() {
        return qty;
    }
    
    // Override setQuantity to update qty as well
    @Override
    public void setQuantity(int quantity) {
        super.setQuantity(quantity);
        this.qty.set(quantity);
    }
    
    // Getters and setters for expDate
    public LocalDate getExpDate() {
        return expDate;
    }
    
    public void setExpDate(LocalDate expDate) {
        this.expDate = expDate;
        super.setExpirationDate(expDate.toString());
    }
    
    // Override setExpirationDate to update expDate as well
    @Override
    public void setExpirationDate(String expirationDate) {
        super.setExpirationDate(expirationDate);
        try {
            this.expDate = LocalDate.parse(expirationDate);
        } catch (Exception e) {
            // Keep the current expDate if parsing fails
        }
    }
    
    // Remove a product from the bundle
    public boolean removeProduct(String productCode) {
        boolean removed = bundleItems.removeIf(item -> item.getProduct().getCode().equals(productCode));
        if (removed) {
            updateBundlePrice();
        }
        return removed;
    }
    
    // Get all products in the bundle
    public List<BundleItem> getBundleItems() {
        return new ArrayList<>(bundleItems);
    }
    
    // Clear all products from the bundle
    public void clearBundle() {
        bundleItems.clear();
        updateBundlePrice();
    }
    
    // Add to BundleProduct.java class
    private boolean preserveOriginalPrice = false;

    public void setPreserveOriginalPrice(boolean preserveOriginalPrice) {
        this.preserveOriginalPrice = preserveOriginalPrice;
    }

    public boolean isPreserveOriginalPrice() {
        return preserveOriginalPrice;
    }

    // Then modify the addProduct method
    public void addProduct(Product product, int quantity) {
        // Check if product already exists in bundle
        for (BundleItem item : bundleItems) {
            if (item.getProduct().getCode().equals(product.getCode())) {
                // Update quantity if product exists
                item.setQuantity(item.getQuantity() + quantity);
                if (!preserveOriginalPrice) {
                    updateBundlePrice();
                }
                return;
            }
        }
        
        // Add new product to bundle
        bundleItems.add(new BundleItem(product, quantity));
        if (!preserveOriginalPrice) {
            updateBundlePrice();
        }
    }
    
    private void updateBundlePrice() {
        if (preserveOriginalPrice) {
            return; // Skip price recalculation if preserveOriginalPrice is true
        }
        
        double totalPrice = 0;
        for (BundleItem item : bundleItems) {
            totalPrice += item.getProduct().getPrice() * item.getQuantity();
        }
        
        // Apply discount
        double discountedPrice = totalPrice * (1 - (discountPercentage.get() / 100));
        setPrice(discountedPrice);
    }

    public double calculateRegularPrice() {
        double totalRegularPrice = 0;
        for (BundleItem item : bundleItems) {
            totalRegularPrice += item.getProduct().getPrice() * item.getQuantity();
        }
        return totalRegularPrice;
    }
    // Add this method to BundleProduct.java
    public double getDiscountedPrice() {
        // Return the price from the database (which is already the discounted price)
        return getPrice();
    }

    // Add this method to calculate the savings amount
    public double getSavingsAmount() {
        double regularPrice = calculateRegularPrice();
        double discountedPrice = getDiscountedPrice();
        return regularPrice - discountedPrice;
    }

    // Add this method to calculate the savings percentage
    public double getSavingsPercentage() {
        double regularPrice = calculateRegularPrice();
        double savings = getSavingsAmount();
        if (regularPrice > 0) {
            return (savings / regularPrice) * 100;
        }
        return 0;
    }
    
    
    public static class BundleItem {
        private Product product;
        private int quantity;
        
        public BundleItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
        
        public Product getProduct() {
            return product;
        }
        
        public void setProduct(Product product) {
            this.product = product;
        }
        
        public int getQuantity() {
            return quantity;
        }
        
        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
        
        public double getTotalPrice() {
            return product.getPrice() * quantity;
        }
    }
}