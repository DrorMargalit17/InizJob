package com.example.inizjob;

import java.io.Serializable;

public class Job implements Serializable {
    public String company;
    public String location;
    public String exactAddress;
    public String businessDescription;
    public String logoUrl;
    public String imageUrl;

    public String title;
    public String jobDescription;
    public int minAge;
    public String prerequisites;
    public String hoursAndDays;
    public boolean flexibilityCommitment;

    public double salary;
    public String conditions;
    public String contactName;
    public String contactRole;
    public String contactPhone;
    public String businessId;

    // מזהים חדשים לניהול המשרה
    public String ownerId;
    public String jobId;

    public Job() {}

    public Job(String company, String location, String exactAddress, String businessDescription,
               String title, String jobDescription, int minAge, String prerequisites,
               String hoursAndDays, boolean flexibilityCommitment, double salary,
               String conditions, String contactName, String contactRole,
               String contactPhone, String businessId, String ownerId, String jobId) {
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
        this.ownerId = ownerId;
        this.jobId = jobId;
    }
}