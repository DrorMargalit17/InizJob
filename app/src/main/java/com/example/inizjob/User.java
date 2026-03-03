package com.example.inizjob;

public class User {
    // משתנים ציבוריים כדי שפיירבייס יוכל לקרוא ולכתוב אותם בקלות
    public String fullName;
    public String email;
    public String phone;
    public String type; // "נוער" או "עסק"
    public String businessCode; // רלוונטי רק לעסק

    // בנאי ריק - חובה בשביל פיירבייס! [cite: 839]
    public User() {
    }

    // בנאי מלא לנוחות שלנו [cite: 841]
    public User(String fullName, String email, String phone, String type, String businessCode) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.type = type;
        this.businessCode = businessCode;
    }
}