package deliveryservice;

public class Order {

    private String email;
    private int[] products;
    private String status = "CREATED";


    public Order(String email, int[] products) {
        this.email = email;
        this.products = products;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int[] getProducts() {
        return products;
    }

    public void setProducts(int[] products) {
        this.products = products;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
