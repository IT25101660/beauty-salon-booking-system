package com.example.beautysalonreview.model;

import com.example.beautysalonreview.model.*;
import com.example.beautysalonreview.controller.*;
import com.example.beautysalonreview.repository.*;
import com.example.beautysalonreview.util.*;




public class Customer extends User {

    private String customerType;

    public Customer(int userId, String name, String email, String password, String customerType) {
        super(userId, name, email, password);
        this.customerType = customerType;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    @Override
    public String getWelcomeMessage() {
        if ("Premium".equalsIgnoreCase(customerType)) {
            return "Welcome back, Premium Client! Enjoy your Luxe Priority service.";
        }
        return "Welcome, Regular Client! Thank you for choosing Lumiere Salon.";
    }

    // FIXED: Now returns pure data instead of raw HTML!
    public String getStatusBadge() {
        if ("Premium".equalsIgnoreCase(customerType)) {
            return "Premium";
        }
        return "Regular";
    }
}
