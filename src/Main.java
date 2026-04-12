public class Main {
    public static void main(String[] args) {
        ReviewController controller = new ReviewController();

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