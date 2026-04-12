package com.example.inizjob;

import java.io.Serializable;

/*
 * Class: User
 * Purpose: Data model representing a user in the system.
 * Contains both Youth and Business fields.
 * * Fields:
 * - fullName: User's name or business name.
 * - email: User's login email.
 * - phone: Contact number.
 * - type: "Youth" or "Business". (Updated to English logic).
 * - businessCode: Relevant only for Business users.
 * - birthDate: Relevant only for Youth users.
 */
public class User implements Serializable {
    public String fullName;//User's name
    public String email;//User's email
    public String phone; //User's phone Number
    public String type; //User's type - "Business" or "Youth"
    public String businessCode; //Only for Business users - confirming business code
    public String birthDate; //Only for Youth users - confirming birth date


    public User() {
        //empty constructor for Firebase
    }

    //constructor of a user for Firebase
    public User(String fullName, String email, String phone, String type, String businessCode, String birthDate) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.type = type;
        this.businessCode = businessCode;
        this.birthDate = birthDate;
    }
}