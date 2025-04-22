package Admin_View;

public class NonPerishableProduct extends Product {
    
    public NonPerishableProduct() {
        super();
    }
    
    public NonPerishableProduct(String code, String name, double price, int quantity, String category) {
        super(code, name, price, quantity, null, category);
    }
}