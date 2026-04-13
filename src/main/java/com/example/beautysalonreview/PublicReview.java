package com.example.beautysalonreview;

/**
 * A publicly visible review — displayed without any verification badge.
 */
public class PublicReview extends Review {

    public PublicReview() {
        super();
    }

    public PublicReview(int reviewId, String customerName, String serviceName, int rating, String comment) {
        super(reviewId, customerName, serviceName, rating, comment);
    }

    // Overrides display method to show type of review
    @Override
    public void displayReview() {
        System.out.println("=== Public Review ===");
        super.displayReview();
    }
}