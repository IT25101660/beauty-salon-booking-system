import java.util.ArrayList;
import java.util.List;

public class ReviewController {
    private List<Review> reviews;

    public ReviewController() {
        reviews = new ArrayList<>();
    }

    public void addReview(Review review) {
        reviews.add(review);
        System.out.println("Review added successfully.");
    }

    public void viewAllReviews() {
        if (reviews.isEmpty()) {
            System.out.println("No reviews found.");
            return;
        }

        for (Review review : reviews) {
            review.displayReview();
            System.out.println("--------------------");
        }
    }

    public void updateReview(int reviewId, int newRating, String newComment) {
        for (Review review : reviews) {
            if (review.getReviewId() == reviewId) {
                review.setRating(newRating);
                review.setComment(newComment);
                System.out.println("Review updated successfully.");
                return;
            }
        }

        System.out.println("Review not found.");
    }

    public void deleteReview(int reviewId) {
        for (int i = 0; i < reviews.size(); i++) {
            if (reviews.get(i).getReviewId() == reviewId) {
                reviews.remove(i);
                System.out.println("Review deleted successfully.");
                return;
            }
        }

        System.out.println("Review not found.");
    }
}