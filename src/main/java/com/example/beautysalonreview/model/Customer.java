package com.example.beautysalonreview.model;

import com.example.beautysalonreview.model.*;
import com.example.beautysalonreview.controller.*;
import com.example.beautysalonreview.repository.*;
import com.example.beautysalonreview.util.*;

/**
 * Model class representing a Customer in the salon system.
 * OOP CONCEPT: Inheritance.
 * The Customer class extends (inherits from) the base User class,
 * acquiring all its common properties (id, name, email, password) while adding its own specific traits.
 */
public class Customer extends User {

    // OOP CONCEPT: Encapsulation.
    // This field is private so it cannot be altered directly from outside the class.
    private String customerType;

    /**
     * Constructor to initialize a new Customer object.
     * Uses the super() keyword to pass the common attributes up to the parent User class constructor.
     */
    public Customer(int userId, String name, String email, String password, String customerType) {
        super(userId, name, email, password); // Calls the constructor of the parent class (User)
        this.customerType = customerType;
    }

    // --- Getters and Setters for Encapsulation ---

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    /**
     * OOP CONCEPT: Polymorphism (Method Overriding).
     * This method overrides the abstract or default method defined in the parent User class
     * to provide a specialized greeting based on the customer's tier.
     */
    @Override
    public String getWelcomeMessage() {
        if ("Premium".equalsIgnoreCase(customerType)) {
            return "Welcome back, Premium Client! Enjoy your Luxe Priority service.";
        }
        return "Welcome, Regular Client! Thank you for choosing Lumiere Salon.";
    }

    // FIXED: Now returns pure data instead of raw HTML!
    // This helper method supplies clean data to the Thymeleaf frontend so the view can handle the CSS styling.
    public String getStatusBadge() {
        if ("Premium".equalsIgnoreCase(customerType)) {
            return "Premium";
        }
        return "Regular";
    }
}