package com.example.inizjob;

public class User {
    // Public variables so Firebase can easily read and write them
    public String fullName;
    public String email;
    public String phone;
    public String type; // "נוער" (Youth) or "עסק" (Business)
    public String businessCode; // Relevant only for business

    // Empty constructor - mandatory for Firebase!
    public User() {
    }

    // Full constructor for our convenience
    public User(String fullName, String email, String phone, String type, String businessCode) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.type = type;
        this.businessCode = businessCode;
    }
}