package Admin_View;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NonPerishableProduct extends Product {
    
    public NonPerishableProduct() {
        super();
    }
    
    public NonPerishableProduct(String code, String name, double price, int quantity, String category, String imagePath) {
        super(code, name, price, quantity, null, category, imagePath);
    }
}