package Admin_View;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DigitalProduct extends Product {
    private URL url;
    private String vendorName;
    
    public DigitalProduct() {
        super();
    }
    
    public DigitalProduct(String code, String name, double price, int quantity, 
                        String category, String imagePath, URL url, String vendorName) {
        super(code, name, price, quantity, null, category, imagePath);
        this.url = url;
        this.vendorName = vendorName;
    }
    
    public URL getUrl() {
        return url;
    }
    
    public void setUrl(URL url) {
        this.url = url;
    }
    
    public String getVendorName() {
        return vendorName;
    }
    
    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }
}