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
import java.util.ArrayList;

/**
 * Controller class responsible for handling all Customer-related HTTP requests.
 * This acts as the 'Controller' in the MVC (Model-View-Controller) architecture,
 * bridging the HTML frontend with the flat-file database backend.
 */
@Controller
public class CustomerWebController {

    // Instantiating the File Managers to handle database CRUD operations.
    // Encapsulation: These are private and final so they cannot be tampered with outside this class.
    private final CustomerFileManager customerFileManager = new CustomerFileManager();
    private final AppointmentFileManager appointmentFileManager = new AppointmentFileManager();
    private final ReviewFileManager reviewFileManager = new ReviewFileManager();

    /**
     * Handles GET requests to display specific HTML pages based on the 'action' parameter.
     */
    @GetMapping("/customers")
    public String handleCustomersGet(
            @RequestParam(defaultValue = "list") String action,
            @RequestParam(required = false) Integer userId,
            HttpSession session,
            Model model
    ) {
        // SECURITY CHECK: If the user is not logging in or registering, and they aren't staff, kick them out to login.
        if (!"public-register".equalsIgnoreCase(action)
                && !"login".equalsIgnoreCase(action)
                && session.getAttribute("staffRole") == null) {
            return "redirect:/staff-login";
        }

        // Retrieve the role of the logged-in staff member to verify access rights.
        String role = (String) session.getAttribute("staffRole");

        try {
            // Routing logic based on the requested action URL parameter
            switch (action.toLowerCase()) {
                case "register":
                    // Only Managers can register customers internally
                    if (!"MANAGER".equals(role)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    model.addAttribute("generatedUserId", customerFileManager.generateNextCustomerId());
                    return "customer-register";

                case "public-register":
                    // Public registration page for new clients
                    model.addAttribute("generatedUserId", customerFileManager.generateNextCustomerId());
                    return "public-register";

                case "login":
                    return "customer-login";

                case "edit":
                case "update":
                    // Fetch existing customer data to pre-fill the update form
                    if (!"MANAGER".equals(role)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    if (userId != null) {
                        Customer existing = findById(userId);
                        if (existing != null) {
                            model.addAttribute("customer", existing);
                            return "customer-update"; // Returns the update HTML view
                        }
                    }
                    return "redirect:/customers?action=list";

                case "delete":
                    // Deletes customer from the text file database
                    if (!"MANAGER".equals(role)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    if (userId != null) {
                        customerFileManager.deleteCustomer(userId);
                    }
                    return "redirect:/customers?action=list";

                case "list":
                default:
                    // Default action: Read all customers and send the list to the admin dashboard view
                    List<Customer> customers = customerFileManager.readAllCustomers();
                    model.addAttribute("customers", customers);
                    return "customer-list";
            }
        } catch (IOException e) {
            System.err.println("File Error in Customer Controller: " + e.getMessage());
            return "redirect:/customers?action=list";
        }
    }

    /**
     * Handles POST requests for form submissions (Creating, Updating, Logging in).
     */
    @PostMapping("/customers")
    public String handleCustomersPost(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String newPassword, // NEW: Added parameter for the modal
            @RequestParam(required = false) String customerType,
            HttpSession session
    ) {
        // SECURITY CHECK: Allow logged-in customers to change their password via the portal
        if (!"public-register".equalsIgnoreCase(action)
                && !"login".equalsIgnoreCase(action)
                && !"change-password".equalsIgnoreCase(action) // Added bypass for password change
                && session.getAttribute("staffRole") == null) {
            return "redirect:/staff-login";
        }

        String role = (String) session.getAttribute("staffRole");

        if (action == null || action.isBlank()) {
            return "redirect:/customers?action=list";
        }

        try {
            switch (action.toLowerCase()) {
                case "change-password": // NEW: Logic to process the modal update from Customer Portal
                    String loggedInEmail = (String) session.getAttribute("loggedInCustomerEmail");
                    if (loggedInEmail == null) {
                        return "redirect:/customers?action=login";
                    }
                    if (newPassword != null && !newPassword.isBlank()) {
                        // Iterates through database to find the logged-in user and updates their password
                        List<Customer> allCusts = customerFileManager.readAllCustomers();
                        for (Customer c : allCusts) {
                            if (c.getEmail().equalsIgnoreCase(loggedInEmail)) {
                                c.setPassword(newPassword);
                                customerFileManager.updateCustomer(c);
                                break;
                            }
                        }
                    }
                    return "redirect:/my-portal?passwordStatus=updated";

                case "register":
                    // Admin logic for creating a new customer account manually
                    if (!"MANAGER".equals(role)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    if (name != null && email != null && customerType != null) {
                        int newId = customerFileManager.generateNextCustomerId();
                        String defaultPassword = "lumiere2026";
                        Customer customer = new Customer(newId, name, email, defaultPassword, customerType);
                        customerFileManager.saveCustomer(customer);
                    }
                    break;

                case "public-register":
                    // Public logic for a user creating their own account
                    if (name != null && email != null && password != null && customerType != null) {
                        int newId = customerFileManager.generateNextCustomerId();
                        Customer customer = new Customer(newId, name, email, password, customerType);
                        customerFileManager.saveCustomer(customer);
                        return "redirect:/customers?action=login";
                    }
                    break;

                case "login":
                    // Authentication process: Verifies credentials against the text file database
                    if (email != null && password != null) {
                        Customer authenticated = findByEmailAndPassword(email, password);
                        if (authenticated != null) {
                            // Creates a secure session for the user
                            session.setAttribute("loggedInCustomerEmail", authenticated.getEmail());
                            session.setAttribute("loggedInCustomerName", authenticated.getName());
                            return "redirect:/my-portal";
                        }
                    }
                    return "redirect:/customers?action=login&error=1";

                case "edit":
                case "update":
                    // Modifies existing customer data
                    if (!"MANAGER".equals(role)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    if (userId != null) {
                        Customer existing = findById(userId);
                        if (existing != null) {
                            if (name != null) existing.setName(name);
                            if (email != null) existing.setEmail(email);
                            if (password != null) existing.setPassword(password);
                            if (customerType != null) existing.setCustomerType(customerType);
                            customerFileManager.updateCustomer(existing);
                        }
                    }
                    break;
            }
        } catch (IOException ignored) {
            return "redirect:/customers?action=list";
        }

        return "redirect:/customers?action=list";
    }

    /**
     * Renders the Customer Portal dashboard.
     * Extracts only the specific appointments and reviews belonging to the logged-in user.
     */
    @GetMapping("/my-portal")
    public String showCustomerPortal(HttpSession session, Model model) {
        String userEmail = (String) session.getAttribute("loggedInCustomerEmail");
        String userName = (String) session.getAttribute("loggedInCustomerName");

        // Protects the portal route from unauthorized access
        if (userEmail == null) {
            return "redirect:/customers?action=login";
        }

        model.addAttribute("userName", userName);

        try {
            // Uses Java Streams to filter the global appointments list down to just this specific customer's bookings
            List<Appointment> myAppointments = appointmentFileManager.readAllAppointments().stream()
                    .filter(appt -> userName.equalsIgnoreCase(appt.getCustomerName()))
                    .collect(java.util.stream.Collectors.toList());
            model.addAttribute("myAppointments", myAppointments);

            // Uses Java Streams to filter the global reviews list down to just this specific customer's feedback
            List<Review> myReviews = reviewFileManager.readAllReviews().stream()
                    .filter(rev -> userName.equalsIgnoreCase(rev.getCustomerName()))
                    .collect(java.util.stream.Collectors.toList());
            model.addAttribute("myReviews", myReviews);

        } catch (Exception e) {
            // Failsafe: Provides empty lists if the file reading fails so the UI doesn't crash
            model.addAttribute("myAppointments", new ArrayList<>());
            model.addAttribute("myReviews", new ArrayList<>());
        }

        return "customer-portal";
    }

    /**
     * Destroys the user session to log them out safely.
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Clears all session attributes (security best practice)
        return "redirect:/customers?action=login";
    }

    /**
     * Helper Method: Finds a specific customer by their ID.
     */
    private Customer findById(int userId) throws IOException {
        return customerFileManager.readAllCustomers().stream()
                .filter(customer -> customer.getUserId() == userId)
                .findFirst().orElse(null);
    }

    /**
     * Helper Method: Validates login credentials against the database.
     */
    private Customer findByEmailAndPassword(String email, String password) throws IOException {
        return customerFileManager.readAllCustomers().stream()
                .filter(c -> c.getEmail().equalsIgnoreCase(email) && c.getPassword().equals(password))
                .findFirst().orElse(null);
    }
}