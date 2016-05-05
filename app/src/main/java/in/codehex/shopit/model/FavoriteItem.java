package in.codehex.shopit.model;

/**
 * Created by Bobby on 18-11-2015
 */
public class FavoriteItem {

    private String productName;

    public FavoriteItem(String productName) {
        this.productName = productName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
