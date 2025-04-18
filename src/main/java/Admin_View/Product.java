package Admin_View;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.ArrayList;
import java.util.List;

public class Product {
    private static ObservableList<Product> allProducts = FXCollections.observableArrayList();

    private final StringProperty code = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final DoubleProperty price = new SimpleDoubleProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final StringProperty expirationDate = new SimpleStringProperty();
    private final StringProperty category = new SimpleStringProperty();
    private String imagePath;
    
    public Product() {
    }
    
    public Product(String code, String name, double price, int quantity, String expirationDate, String category, String imagePath) {
        this.code.set(code);
        this.name.set(name);
        this.price.set(price);
        this.quantity.set(quantity);
        this.expirationDate.set(expirationDate);
        this.category.set(category);
        this.imagePath = imagePath;
    }
    
    public static ObservableList<Product> getAllProducts() {
        return allProducts;
    }
    
    public static void addProduct(Product product) {
        allProducts.add(product);
    }
    
    public static void removeProduct(Product product) {
        allProducts.remove(product);
    }
    
    public String getCode() {
        return code.get();
    }
    
    public StringProperty codeProperty() {
        return code;
    }
    
    public void setCode(String code) {
        this.code.set(code);
    }
    
    public String getName() {
        return name.get();
    }
    
    public StringProperty nameProperty() {
        return name;
    }
    
    public void setName(String name) {
        this.name.set(name);
    }
    
    public double getPrice() {
        return price.get();
    }
    
    public DoubleProperty priceProperty() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price.set(price);
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
    
    public String getExpirationDate() {
        return expirationDate.get();
    }
    
    public StringProperty expirationDateProperty() {
        return expirationDate;
    }
    
    public void setExpirationDate(String expirationDate) {
        this.expirationDate.set(expirationDate);
    }
    
    public String getCategory() {
        return category.get();
    }
    
    public StringProperty categoryProperty() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category.set(category);
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}