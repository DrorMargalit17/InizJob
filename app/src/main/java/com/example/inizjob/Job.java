package com.example.inizjob;

import java.io.Serializable;

// "implements Serializable" allows us to pass the whole Job object between fragments
public class Job implements Serializable {
    // Page 1: Business Details
    public String company;
    public String location;
    public String exactAddress;
    public String businessDescription;
    public String logoUrl;
    public String imageUrl;

    // Page 2: Job Requirements
    public String title;
    public String jobDescription;
    public int minAge;
    public String prerequisites;
    public String hoursAndDays;
    public boolean flexibilityCommitment;

    // Page 3: Salary & Conditions
    public double salary;
    public String conditions;
    public String contactName;
    public String contactRole;
    public String contactPhone;
    public String businessId;

    // Empty constructor - mandatory for Firebase
    public Job() {}

    // Full constructor
    public Job(String company, String location, String exactAddress, String businessDescription,
               String title, String jobDescription, int minAge, String prerequisites,
               String hoursAndDays, boolean flexibilityCommitment, double salary,
               String conditions, String contactName, String contactRole,
               String contactPhone, String businessId) {
        this.company = company;
        this.location = location;
        this.exactAddress = exactAddress;
        this.businessDescription = businessDescription;
        this.title = title;
        this.jobDescription = jobDescription;
        this.minAge = minAge;
        this.prerequisites = prerequisites;
        this.hoursAndDays = hoursAndDays;
        this.flexibilityCommitment = flexibilityCommitment;
        this.salary = salary;
        this.conditions = conditions;
        this.contactName = contactName;
        this.contactRole = contactRole;
        this.contactPhone = contactPhone;
        this.businessId = businessId;
    }
}