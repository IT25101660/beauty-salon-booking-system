package com.example.beautysalonreview;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Spring MVC controller that maps HTTP requests to review-related Thymeleaf views.
 */
@Controller
public class ReviewWebController {

    private ReviewController reviewController = new ReviewController();

    // Shows the review submission form
    @GetMapping("/")
    public String showHomePage(Model model) {
        return "review-form";
    }

    // Handles new review form submission and redirects to home
    @PostMapping("/submitReview")
    public String submitReview(
            @RequestParam int reviewId,
            @RequestParam String customerName,
            @RequestParam String serviceName,
            @RequestParam(required = false) String stylistName,
            @RequestParam int rating,
            @RequestParam String comment,
            @RequestParam(defaultValue = "Public") String reviewType
    ) {
        Review review;
        if ("Verified".equalsIgnoreCase(reviewType)) {
            review = new VerifiedReview(reviewId, customerName, serviceName, stylistName, rating, comment);
        } else {
            review = new PublicReview(reviewId, customerName, serviceName, stylistName, rating, comment);
        }
        reviewController.addReview(review);
        return "redirect:/";
    }

    // Loads all reviews and passes them to the list view
    @GetMapping("/reviews")
    public String showReviewsPage(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String stylist,
            Model model
    ) {
        model.addAttribute("reviews", reviewController.getFilteredReviews(service, stylist));
        model.addAttribute("service", service == null ? "" : service);
        model.addAttribute("stylist", stylist == null ? "" : stylist);
        return "review-list";
    }

    // Admin review control page (separate view with admin-focused display)
    @GetMapping("/admin/reviews")
    public String showAdminReviews(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String stylist,
            Model model
    ) {
        model.addAttribute("reviews", reviewController.getFilteredReviews(service, stylist));
        model.addAttribute("service", service == null ? "" : service);
        model.addAttribute("stylist", stylist == null ? "" : stylist);
        return "admin-review-control";
    }

    @GetMapping("/admin/deleteReview")
    public String adminDeleteReview(@RequestParam int id) {
        reviewController.deleteReview(id);
        return "redirect:/admin/reviews";
    }

    @GetMapping("/admin/editReview")
    public String adminEditReview(@RequestParam int id, Model model) {
        Review review = reviewController.getReviewById(id);
        if (review == null) {
            return "redirect:/admin/reviews";
        }
        model.addAttribute("review", review);
        return "review-edit";
    }

    @PostMapping("/admin/updateReview")
    public String adminUpdateReview(
            @RequestParam int reviewId,
            @RequestParam int rating,
            @RequestParam String comment
    ) {
        reviewController.updateReview(reviewId, rating, comment);
        return "redirect:/admin/reviews";
    }
}