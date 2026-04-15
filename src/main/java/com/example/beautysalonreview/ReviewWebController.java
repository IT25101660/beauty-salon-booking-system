package com.example.beautysalonreview;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

/**
 * Spring MVC controller that maps HTTP requests to review-related Thymeleaf views.
 */
@Controller
public class ReviewWebController {

    private ReviewController reviewController = new ReviewController();

    // Shows the review submission form
    @GetMapping("/")
    public String showHomePage(Model model) {
        model.addAttribute("reviewId", "");
        model.addAttribute("customerName", "");
        model.addAttribute("serviceName", "");
        model.addAttribute("stylistName", "");
        model.addAttribute("rating", 5);
        model.addAttribute("comment", "");
        model.addAttribute("reviewType", "Public");
        return "review-form";
    }

    // Handles new review form submission and shows token on the form page
    @PostMapping("/submitReview")
    public String submitReview(
            @RequestParam int reviewId,
            @RequestParam String customerName,
            @RequestParam String serviceName,
            @RequestParam(required = false) String stylistName,
            @RequestParam int rating,
            @RequestParam String comment,
            @RequestParam(defaultValue = "Public") String reviewType,
            Model model
    ) {
        String ownerToken = UUID.randomUUID().toString();
        Review review;
        if ("Verified".equalsIgnoreCase(reviewType)) {
            review = new VerifiedReview(reviewId, customerName, serviceName, stylistName, rating, comment, ownerToken);
        } else {
            review = new PublicReview(reviewId, customerName, serviceName, stylistName, rating, comment, ownerToken);
        }
        reviewController.addReview(review);

        model.addAttribute("createdToken", ownerToken);
        model.addAttribute("createdReviewId", reviewId);
        model.addAttribute("createdManageUrl", "/editReview?id=" + reviewId + "&token=" + ownerToken);

        // Clear form fields after submit (keeps POST->render flow; still warn users not to refresh)
        model.addAttribute("reviewId", "");
        model.addAttribute("customerName", "");
        model.addAttribute("serviceName", "");
        model.addAttribute("stylistName", "");
        model.addAttribute("rating", 5);
        model.addAttribute("comment", "");
        model.addAttribute("reviewType", "Public");

        return "review-form";
    }

    // Loads all reviews and passes them to the list view
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

    // Validates token and redirects to edit page
    @GetMapping("/manageReview")
    public String manageReview(@RequestParam int id, @RequestParam String token) {
        Review review = reviewController.getReviewById(id);
        if (review == null) {
            return "redirect:/reviews?error=notfound";
        }
        if (review.getOwnerToken() == null || review.getOwnerToken().isBlank() || !review.getOwnerToken().equals(token)) {
            return "redirect:/reviews?error=invalid";
        }
        return "redirect:/editReview?id=" + id + "&token=" + token;
    }

    // Loads a single review by ID and shows the edit form
    @GetMapping("/editReview")
    public String showEditPage(@RequestParam int id, @RequestParam String token, Model model) {
        Review review = reviewController.getReviewById(id);
        if (review == null) {
            return "redirect:/reviews?error=notfound";
        }
        if (review.getOwnerToken() == null || review.getOwnerToken().isBlank() || !review.getOwnerToken().equals(token)) {
            return "redirect:/reviews?error=invalid";
        }
        model.addAttribute("review", review);
        model.addAttribute("token", token);
        model.addAttribute("formAction", "/updateReview");
        return "review-edit";
    }

    // Processes the edit form and redirects to the reviews list
    @PostMapping("/updateReview")
    public String updateReview(
            @RequestParam int reviewId,
            @RequestParam int rating,
            @RequestParam String comment,
            @RequestParam String token
    ) {
        Review review = reviewController.getReviewById(reviewId);
        if (review == null) {
            return "redirect:/reviews?error=notfound";
        }
        if (review.getOwnerToken() == null || review.getOwnerToken().isBlank() || !review.getOwnerToken().equals(token)) {
            return "redirect:/reviews?error=invalid";
        }
        reviewController.updateReview(reviewId, rating, comment);
        return "redirect:/reviews";
    }

    // Deletes a review by ID and redirects to the reviews list
    @GetMapping("/deleteReview")
    public String deleteReview(@RequestParam int id, @RequestParam String token) {
        Review review = reviewController.getReviewById(id);
        if (review == null) {
            return "redirect:/reviews?error=notfound";
        }
        if (review.getOwnerToken() == null || review.getOwnerToken().isBlank() || !review.getOwnerToken().equals(token)) {
            return "redirect:/reviews?error=invalid";
        }
        reviewController.deleteReview(id);
        return "redirect:/reviews";
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
        model.addAttribute("token", "");
        model.addAttribute("formAction", "/admin/updateReview");
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