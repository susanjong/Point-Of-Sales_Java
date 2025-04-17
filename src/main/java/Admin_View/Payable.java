package Admin_View;

public interface Payable {
    double calculateTotal();
    void processTransaction();
    String serializeTransaction();
}
