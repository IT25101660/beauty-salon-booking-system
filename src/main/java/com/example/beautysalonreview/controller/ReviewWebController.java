package com.example.beautysalonreview.controller;

import com.example.beautysalonreview.model.*;
import com.example.beautysalonreview.controller.*;
import com.example.beautysalonreview.repository.*;
import com.example.beautysalonreview.util.*;




import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Spring MVC controller that maps HTTP requests to review-related Thymeleaf views.
 */
@Controller
public class ReviewWebController {

    private final ReviewController reviewController = new ReviewController();
    private final ReviewFileManager reviewFileManager = new ReviewFileManager();
    // NEW: We need the ServiceFileManager to fetch the dynamic dropdown list!
    private final ServiceFileManager serviceFileManager = new ServiceFileManager();

    // ==========================================================
    // PUBLIC REVIEW ROUTING
    // ==========================================================
    
    @GetMapping("/public-review")
    public String showPublicReviewPage(HttpSession session, Model model) {
        String loggedInName = (String) session.getAttribute("loggedInCustomerName");
        
        if (loggedInName == null) {
            return "redirect:/customers?action=login";
        }
        
        model.addAttribute("customerName", loggedInName);

        // FIXED: Fetch services from the text file and pass them to the HTML dropdown
        try {
            model.addAttribute("services", serviceFileManager.readAllServices());
        } catch (IOException e) {
            System.err.println("Error loading services for the review form: " + e.getMessage());
        }

        return "public-review-form"; 
    }

    @PostMapping("/submitPublicReview")
    public String submitPublicReview(
            HttpSession session,
            @RequestParam String customerName,
            @RequestParam String serviceName,
            @RequestParam(required = false) String stylistName,
            @RequestParam int rating,
            @RequestParam String comment
    ) {
        // Staff cannot submit public reviews unless they are a manager
        if (session.getAttribute("staffRole") != null && !SecurityUtils.isManager(session)) {
            return "redirect:/admin?error=unauthorized";
        }

        int newId = 5001;
        try {
            newId = reviewFileManager.generateNextReviewId();
        } catch (IOException ignored) {}

        String ownerToken = UUID.randomUUID().toString();
        
        // 1. UPDATED: Newly submitted reviews default to unverified (false)
        Review review = new Review(newId, customerName, serviceName, stylistName, rating, comment, ownerToken, false);
        reviewController.addReview(review);

        return "redirect:/my-portal";
    }

    // ==========================================================
    // ADMIN / GENERAL ROUTING
    // ==========================================================

    @GetMapping("/reviews")
    public String showReviewsPage(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String stylist,
            @RequestParam(required = false) String error,
            Model model
    ) {
        model.addAttribute("reviews", reviewController.getFilteredReviews(service, stylist));
        model.addAttribute("service", service == null ? "" : service);
        model.addAttribute("stylist", stylist == null ? "" : stylist);
        model.addAttribute("error", error == null ? "" : error);
        return "review-list";
    }

    @GetMapping("/admin/reviews")
    public String showAdminReviews(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String stylist,
            HttpSession session,
            Model model
    ) {
        if (session.getAttribute("staffRole") == null) {
            return "redirect:/staff-login";
        }
        model.addAttribute("reviews", reviewController.getFilteredReviews(service, stylist));
        model.addAttribute("service", service == null ? "" : service);
        model.addAttribute("stylist", stylist == null ? "" : stylist);
        return "admin-review-control";
    }

    @GetMapping("/admin/reviews/toggle")
    public String toggleReviewStatus(@RequestParam Integer id, HttpSession session) {
        if (session.getAttribute("staffRole") == null) {
            return "redirect:/staff-login";
        }
        if (!SecurityUtils.isManager(session)) {
            return "redirect:/admin/reviews?error=unauthorized";
        }
        
        // 2. UPDATED: Flips the boolean and saves directly to the text file
        List<Review> allReviews = reviewFileManager.readAllReviews();
        for (Review r : allReviews) {
            if (r.getReviewId() == id) {
                r.setVerified(!r.isVerified()); // Flips true to false, or false to true
                reviewFileManager.updateReview(r); // Saves the change permanently
                break;
            }
        }
        
        return "redirect:/admin/reviews";
    }

    @GetMapping("/admin/deleteReview")
    public String adminDeleteReview(@RequestParam(required = false) Integer id, HttpSession session) {
        if (session.getAttribute("staffRole") == null) {
            return "redirect:/staff-login";
        }
        if (!SecurityUtils.isManager(session)) {
            return "redirect:/admin?error=unauthorized";
        }
        if (id != null) {
            reviewController.deleteReview(id);
        }
        return "redirect:/admin/reviews";
    }

    // ==========================================================
    // CUSTOMER PORTAL - SELF-EDIT LOGIC
    // ==========================================================

    @GetMapping("/editReview")
    public String showEditPage(@RequestParam Integer id, HttpSession session, Model model) {
        String loggedInCustomer = (String) session.getAttribute("loggedInCustomerName");

        if (loggedInCustomer == null) {
            return "redirect:/customers?action=login";
        }

        Review review = reviewController.getReviewById(id);

        if (review != null && review.getCustomerName().equalsIgnoreCase(loggedInCustomer)) {
            model.addAttribute("review", review);
            return "review-edit";
        }

        return "redirect:/my-portal?error=unauthorized";
    }

    @PostMapping("/updateReview")
    public String updateReview(
            @RequestParam Integer reviewId,
            @RequestParam Integer rating,
            @RequestParam String comment,
            HttpSession session
    ) {
        String loggedInCustomer = (String) session.getAttribute("loggedInCustomerName");

        if (loggedInCustomer == null) {
            return "redirect:/customers?action=login";
        }

        Review review = reviewController.getReviewById(reviewId);
        
        if (review != null && review.getCustomerName().equalsIgnoreCase(loggedInCustomer)) {
            reviewController.updateReview(reviewId, rating, comment);
            return "redirect:/my-portal?status=updated";
        }

        return "redirect:/my-portal?error=failed";
    }

    @PostMapping("/deleteReview")
    public String deleteCustomerReview(
            @RequestParam Integer reviewId,
            HttpSession session
    ) {
        String loggedInCustomer = (String) session.getAttribute("loggedInCustomerName");

        if (loggedInCustomer == null) {
            return "redirect:/customers?action=login";
        }

        Review review = reviewController.getReviewById(reviewId);

        if (review != null && review.getCustomerName().equalsIgnoreCase(loggedInCustomer)) {
            reviewController.deleteReview(reviewId);
            return "redirect:/my-portal?status=deleted";
        }

        return "redirect:/my-portal?error=unauthorized";
    }
}
