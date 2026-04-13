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
    public String showHomePage() {
        return "review-form";
    }

    // Handles new review form submission and redirects to home
    @PostMapping("/submitReview")
    public String submitReview(
            @RequestParam int reviewId,
            @RequestParam String customerName,
            @RequestParam String serviceName,
            @RequestParam int rating,
            @RequestParam String comment
    ) {
        Review review = new Review(reviewId, customerName, serviceName, rating, comment);
        reviewController.addReview(review);
        return "redirect:/";
    }

    // Loads all reviews and passes them to the list view
    @GetMapping("/reviews")
    public String showReviewsPage(Model model) {
        model.addAttribute("reviews", reviewController.getAllReviews());
        return "review-list";
    }

    // Loads a single review by ID and shows the edit form
    @GetMapping("/editReview")
    public String showEditPage(@RequestParam int id, Model model) {
        Review review = reviewController.getReviewById(id);
        model.addAttribute("review", review);
        return "review-edit";
    }

    // Processes the edit form and redirects to the reviews list
    @PostMapping("/updateReview")
    public String updateReview(
            @RequestParam int reviewId,
            @RequestParam int rating,
            @RequestParam String comment
    ) {
        reviewController.updateReview(reviewId, rating, comment);
        return "redirect:/reviews";
    }

    // Deletes a review by ID and redirects to the reviews list
    @GetMapping("/deleteReview")
    public String deleteReview(@RequestParam int id) {
        reviewController.deleteReview(id);
        return "redirect:/reviews";
    }
}