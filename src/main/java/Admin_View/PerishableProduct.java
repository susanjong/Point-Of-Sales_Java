package Admin_View;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PerishableProduct extends Product {
    private LocalDate expiryDate;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    
    public PerishableProduct() {
        super();
    }
    
    public PerishableProduct(String code, String name, double price, int quantity, 
                           LocalDate expiryDate, String category) {
        super(code, name, price, quantity, expiryDate != null ? expiryDate.format(formatter) : null, 
              category);
        this.expiryDate = expiryDate;
    }
    
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        setExpirationDate(expiryDate != null ? expiryDate.format(formatter) : null);
    }
    
    // Parse string date to LocalDate
    public void setExpiryDateFromString(String dateString) {
        if (dateString != null && !dateString.isEmpty()) {
            this.expiryDate = LocalDate.parse(dateString, formatter);
            setExpirationDate(dateString);
        } else {
            this.expiryDate = null;
            setExpirationDate(null);
        }
    }
}
