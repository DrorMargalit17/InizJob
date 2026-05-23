package com.example.inizjob;

import java.io.Serializable;

/*
 * Class: User
 * Purpose: Data model representing a user in the system.
 */
public class User implements Serializable {
    // Constants for User Types (Single Source of Truth)
    public static final String TYPE_YOUTH = "Youth";
    public static final String TYPE_BUSINESS = "Business";

    public String fullName;//User's name
    public String email;//User's email
    public String phone; //User's phone Number
    public String type; //User's type - "Business" or "Youth"
    public String businessCode; //Only for Business users - confirming business code
    public String birthDate; //Only for Youth users - confirming birth date
    public String avatarType; //User's profile avatar selection

    public User() {
        //empty constructor for Firebase
    }

    // Single, fully featured constructor for new architecture
    public User(String fullName, String email, String phone, String type, String businessCode, String birthDate, String avatarType) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.type = type;
        this.businessCode = businessCode;
        this.birthDate = birthDate;
        this.avatarType = avatarType;
    }
}