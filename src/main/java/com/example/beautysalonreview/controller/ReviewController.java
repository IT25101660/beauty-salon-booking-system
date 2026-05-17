package com.example.beautysalonreview.controller;

import com.example.beautysalonreview.model.*;
import com.example.beautysalonreview.controller.*;
import com.example.beautysalonreview.repository.*;
import com.example.beautysalonreview.util.*;




import java.util.ArrayList;
import java.util.List;

/**
 * Handles CRUD operations by acting as a bridge to the ReviewFileManager.
 * Ensures the application relies on a single source of truth for file persistence.
 */
public class ReviewController {
    
    // We link this controller directly to our shiny new File Manager!
    private final ReviewFileManager fileManager = new ReviewFileManager();

    public ReviewController() {
        // No longer needs to maintain its own disconnected in-memory list
    }

    public void addReview(Review review) {
        List<Review> all = fileManager.readAllReviews();
        all.add(review);
        fileManager.writeAllReviews(all);
        System.out.println("Review added successfully via FileManager.");
    }

    public void updateReview(int reviewId, int newRating, String newComment) {
        List<Review> all = fileManager.readAllReviews();
        for (Review r : all) {
            if (r.getReviewId() == reviewId) {
                r.setRating(newRating);
                r.setComment(newComment);
                break;
            }
        }
        fileManager.writeAllReviews(all);
    }

    public void deleteReview(int reviewId) {
        List<Review> all = fileManager.readAllReviews();
        all.removeIf(r -> r.getReviewId() == reviewId);
        fileManager.writeAllReviews(all);
    }

    public List<Review> getAllReviews() { 
        return fileManager.readAllReviews(); 
    }

    public List<Review> getFilteredReviews(String service, String stylist) {
        // Gets fresh data directly from the text file every time!
        List<Review> all = fileManager.readAllReviews(); 
        
        String s1 = service == null ? "" : service.trim().toLowerCase();
        String s2 = stylist == null ? "" : stylist.trim().toLowerCase();
        
        if (s1.isEmpty() && s2.isEmpty()) return all;
        
        List<Review> out = new ArrayList<>();
        for (Review r : all) {
            boolean ok = true;
            if (!s1.isEmpty()) ok = r.getServiceName() != null && r.getServiceName().toLowerCase().contains(s1);
            if (ok && !s2.isEmpty()) ok = r.getStylistName() != null && r.getStylistName().toLowerCase().contains(s2);
            if (ok) out.add(r);
        }
        return out;
    }

    public Review getReviewById(int reviewId) {
        return fileManager.readAllReviews().stream()
                .filter(r -> r.getReviewId() == reviewId)
                .findFirst().orElse(null);
    }
    
    // Kept to prevent breaking existing method calls, 
    // though WebController now handles the toggle directly.
    public void toggleReviewVerification(int reviewId) {
         System.out.println("Toggle handled by WebController.");
    }
}
