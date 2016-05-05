package in.codehex.shopit.model;

/**
 * Created by Bobby on 16-11-2015
 */
public class ProductItem {
    private int productId;
    private String price;
    private String productName;

    public ProductItem(int productId, String price, String productName) {
        this.productId = productId;
        this.price = price;
        this.productName = productName;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
