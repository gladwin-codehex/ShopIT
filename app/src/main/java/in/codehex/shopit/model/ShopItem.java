package in.codehex.shopit.model;

/**
 * Created by Bobby on 16-11-2015
 */
public class ShopItem {

    private int shopId;
    private String shopName;
    private double lat, lng;

    public ShopItem(int shopId, String shopName, double lat, double lng) {
        this.shopId = shopId;
        this.shopName = shopName;
        this.lat = lat;
        this.lng = lng;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public int getShopId() {
        return shopId;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
