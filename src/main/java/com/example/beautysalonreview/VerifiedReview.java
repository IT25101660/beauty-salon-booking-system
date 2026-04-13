package com.example.beautysalonreview;

/**
 * A review from a verified customer, displayed with a verification header.
 */
public class VerifiedReview extends Review {

    public VerifiedReview() {
        super();
    }

    public VerifiedReview(int reviewId, String customerName, String serviceName, int rating, String comment) {
        super(reviewId, customerName, serviceName, rating, comment);
    }

    // Adds a "Verified Review" header before printing base review details
    @Override
    public void displayReview() {
        System.out.println("=== Verified Review ===");
        super.displayReview();
    }
}