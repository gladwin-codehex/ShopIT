package in.codehex.shopit.model;

/**
 * Created by Bobby on 16-11-2015
 */
public class ReviewItem {

    private double rating;
    private String feedback;

    public ReviewItem(double rating, String feedback) {
        this.rating = rating;
        this.feedback = feedback;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
