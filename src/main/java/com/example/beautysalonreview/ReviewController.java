package com.example.beautysalonreview;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles CRUD operations for reviews, with file-based persistence.
 * Reviews are stored in a plain text CSV file (reviews.txt).
 */
public class ReviewController {
    private List<Review> reviews;
    private final String FILE_NAME = "reviews.txt";

    // Initializes the controller and loads existing reviews from file
    public ReviewController() {
        reviews = new ArrayList<>();
        loadReviewsFromFile();
    }

    // Adds a review to the in-memory list and appends it to the file
    public void addReview(Review review) {
        reviews.add(review);
        saveReviewToFile(review);
        System.out.println("Review added successfully.");
    }

    // Appends a single review as a comma-separated line to the file
    private void saveReviewToFile(Review review) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(review.getReviewId() + "," +
                    review.getCustomerName() + "," +
                    review.getServiceName() + "," +
                    review.getRating() + "," +
                    review.getComment());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    // Reads all reviews from the file into the in-memory list
    public void loadReviewsFromFile() {
        reviews.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");

                if (data.length == 5) {
                    int id = Integer.parseInt(data[0]);
                    String customer = data[1];
                    String service = data[2];
                    int rating = Integer.parseInt(data[3]);
                    String comment = data[4];

                    Review review = new Review(id, customer, service, rating, comment);
                    reviews.add(review);
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    // Prints all reviews to the console
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

    // Updates the rating and comment of an existing review, then rewrites the file
    public void updateReview(int reviewId, int newRating, String newComment) {
        for (Review review : reviews) {
            if (review.getReviewId() == reviewId) {
                review.setRating(newRating);
                review.setComment(newComment);
                rewriteFile();
                System.out.println("Review updated successfully.");
                return;
            }
        }

        System.out.println("Review not found.");
    }

    // Removes a review by ID from the list and rewrites the file
    public void deleteReview(int reviewId) {
        for (int i = 0; i < reviews.size(); i++) {
            if (reviews.get(i).getReviewId() == reviewId) {
                reviews.remove(i);
                rewriteFile();
                System.out.println("Review deleted successfully.");
                return;
            }
        }

        System.out.println("Review not found.");
    }

    // Overwrites the file with the current in-memory review list
    private void rewriteFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Review review : reviews) {
                writer.write(review.getReviewId() + "," +
                        review.getCustomerName() + "," +
                        review.getServiceName() + "," +
                        review.getRating() + "," +
                        review.getComment());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error rewriting file: " + e.getMessage());
        }
    }

    public List<Review> getAllReviews() {
        return reviews;
    }

    // Returns the review with the given ID, or null if not found
    public Review getReviewById(int reviewId) {
        for (Review review : reviews) {
            if (review.getReviewId() == reviewId) {
                return review;
            }
        }
        return null;
    }
}