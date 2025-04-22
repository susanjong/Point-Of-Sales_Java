package Admin_View;

import java.net.URL;

public class DigitalProduct extends Product {
    private URL url;
    private String vendorName;
    
    public DigitalProduct() {
        super();
    }
    
    public DigitalProduct(String code, String name, double price, int quantity, 
                        String category, URL url, String vendorName) {
        super(code, name, price, quantity, null, category);
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