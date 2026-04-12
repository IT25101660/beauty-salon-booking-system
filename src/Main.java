public class Main {
    public static void main(String[] args) {
        ReviewController controller = new ReviewController();

        Review review1 = new VerifiedReview(1, "Nimal", "Haircut", 5, "Excellent service");
        Review review2 = new PublicReview(2, "Kamal", "Facial", 4, "Very good experience");

        controller.addReview(review1);
        controller.addReview(review2);

        System.out.println("\nAll Reviews:");
        controller.viewAllReviews();

        System.out.println("\nUpdating Review 1:");
        controller.updateReview(1, 3, "Good service");

        System.out.println("\nAll Reviews After Update:");
        controller.viewAllReviews();

        System.out.println("\nDeleting Review 2:");
        controller.deleteReview(2);

        System.out.println("\nAll Reviews After Delete:");
        controller.viewAllReviews();
    }
}
