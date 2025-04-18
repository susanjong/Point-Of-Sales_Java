package Admin_View;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BundleProduct extends Product {
    private List<Product> bundleItems = new ArrayList<>();
    
    public BundleProduct() {
        super();
    }
    
    public BundleProduct(String code, String name, double price, int quantity, 
                       String category, String imagePath) {
        super(code, name, price, quantity, null, category, imagePath);
    }
    
    public BundleProduct(String code, String name, double price, int quantity, 
                       String category, String imagePath, List<Product> bundleItems) {
        super(code, name, price, quantity, null, category, imagePath);
        this.bundleItems = new ArrayList<>(bundleItems);
    }
    
    public List<Product> getBundleItems() {
        return bundleItems;
    }
    
    public void setBundleItems(List<Product> bundleItems) {
        this.bundleItems = new ArrayList<>(bundleItems);
    }
    
    public void addProductToBundle(Product product) {
        if (!bundleItems.contains(product)) {
            bundleItems.add(product);
        }
    }
    
    public void removeProductFromBundle(Product product) {
        bundleItems.remove(product);
    }
    
    /**
     * Calculate the price difference between buying the products as a bundle versus individually
     * @return the discount amount (difference between individual item total and bundle price)
     */
    public double getDiscountedPrice() {
        double totalIndividualPrice = 0;
        
        for (Product product : bundleItems) {
            totalIndividualPrice += product.getPrice();
        }
        
        // The discount is the difference between sum of individual prices and the bundle price
        return totalIndividualPrice - getPrice();
    }
    
    /**
     * Get the total value of products if purchased individually
     * @return sum of the prices of all products in the bundle
     */
    public double getTotalIndividualPrice() {
        double totalIndividualPrice = 0;
        
        for (Product product : bundleItems) {
            totalIndividualPrice += product.getPrice();
        }
        
        return totalIndividualPrice;
    }
}
