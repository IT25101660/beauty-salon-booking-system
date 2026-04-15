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
    private static final String V2_PREFIX = "v2|";

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
            writer.write(serializeReview(review));
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    private static String escapeField(String raw) {
        if (raw == null) return "";
        return raw
                .replace("\\", "\\\\")
                .replace("|", "\\|")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private static String unescapeField(String raw) {
        if (raw == null) return "";
        StringBuilder out = new StringBuilder();
        boolean escaping = false;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (escaping) {
                if (c == 'n') out.append('\n');
                else if (c == 'r') out.append('\r');
                else out.append(c);
                escaping = false;
            } else if (c == '\\') {
                escaping = true;
            } else {
                out.append(c);
            }
        }
        if (escaping) out.append('\\');
        return out.toString();
    }

    private static List<String> splitV2Fields(String payload) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escaping = false;
        for (int i = 0; i < payload.length(); i++) {
            char c = payload.charAt(i);
            if (escaping) {
                current.append('\\').append(c);
                escaping = false;
                continue;
            }
            if (c == '\\') {
                escaping = true;
                continue;
            }
            if (c == '|') {
                fields.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        fields.add(current.toString());
        return fields;
    }

    private static String serializeReview(Review review) {
        // v2 format:
        // v2|id|customer|service|stylist|rating|comment|type|ownerToken
        return V2_PREFIX +
                review.getReviewId() + "|" +
                escapeField(review.getCustomerName()) + "|" +
                escapeField(review.getServiceName()) + "|" +
                escapeField(review.getStylistName()) + "|" +
                review.getRating() + "|" +
                escapeField(review.getComment()) + "|" +
                escapeField(review.getReviewType()) + "|" +
                escapeField(review.getOwnerToken());
    }

    private static Review createByType(
            String type,
            int id,
            String customer,
            String service,
            String stylist,
            int rating,
            String comment,
            String ownerToken
    ) {
        if (type != null && type.equalsIgnoreCase("Verified")) {
            return new VerifiedReview(id, customer, service, stylist, rating, comment, ownerToken);
        }
        if (type != null && type.equalsIgnoreCase("Public")) {
            return new PublicReview(id, customer, service, stylist, rating, comment, ownerToken);
        }
        // Fallback: default to public review
        return new PublicReview(id, customer, service, stylist, rating, comment, ownerToken);
    }

    // Reads all reviews from the file into the in-memory list
    public void loadReviewsFromFile() {
        reviews.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                if (line.startsWith(V2_PREFIX)) {
                    String payload = line.substring(V2_PREFIX.length());
                    List<String> fields = splitV2Fields(payload);
                    if (fields.size() >= 7) {
                        int id = Integer.parseInt(fields.get(0));
                        String customer = unescapeField(fields.get(1));
                        String service = unescapeField(fields.get(2));
                        String stylist = unescapeField(fields.get(3));
                        int rating = Integer.parseInt(fields.get(4));
                        String comment = unescapeField(fields.get(5));
                        String type = unescapeField(fields.get(6));
                        String ownerToken = fields.size() >= 8 ? unescapeField(fields.get(7)) : "";
                        reviews.add(createByType(type, id, customer, service, stylist, rating, comment, ownerToken));
                    }
                    continue;
                }

                // Legacy CSV fallback: id,customer,service,rating,comment
                String[] data = line.split(",", 5);
                if (data.length == 5) {
                    int id = Integer.parseInt(data[0]);
                    String customer = data[1];
                    String service = data[2];
                    int rating = Integer.parseInt(data[3]);
                    String comment = data[4];

                    reviews.add(new PublicReview(id, customer, service, "", rating, comment, ""));
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
                writer.write(serializeReview(review));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error rewriting file: " + e.getMessage());
        }
    }

    public List<Review> getAllReviews() {
        return reviews;
    }

    public List<Review> getFilteredReviews(String service, String stylist) {
        String s1 = service == null ? "" : service.trim().toLowerCase();
        String s2 = stylist == null ? "" : stylist.trim().toLowerCase();

        if (s1.isEmpty() && s2.isEmpty()) {
            return reviews;
        }

        List<Review> out = new ArrayList<>();
        for (Review r : reviews) {
            boolean ok = true;
            if (!s1.isEmpty()) {
                ok = r.getServiceName() != null && r.getServiceName().toLowerCase().contains(s1);
            }
            if (ok && !s2.isEmpty()) {
                ok = r.getStylistName() != null && r.getStylistName().toLowerCase().contains(s2);
            }
            if (ok) out.add(r);
        }
        return out;
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