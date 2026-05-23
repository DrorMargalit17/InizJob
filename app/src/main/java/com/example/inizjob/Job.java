package com.example.inizjob;

import java.io.Serializable;

/*
 * Class: Job
 * Purpose: Data model for a job object.
 * Includes: company, location (City), exactAddress, businessDescription, title, jobDescription,
 * minAge, jobScope, workField, requiresExperience, salary, contact info, and IDs.
 */
public class Job implements Serializable {

    public String company; // Company name
    public String location; // City of the job
    public String exactAddress; // Exact address of the job
    public String businessDescription; // Business description

    public String title; // Job title
    public String jobDescription; // Job description
    public int minAge; // Minimum age requirement

    public String jobScope; // Job scope
    public String workField; // Work field
    public boolean requiresExperience; // Requires experience

    public double salary; // Salary
    public String contactName; // Contact name
    public String contactRole; // Contact role
    public String contactPhone; // Contact phone number
    public String businessId; // Business ID

    public String ownerId; // Owner ID
    public String jobId; // Job ID

    public Job() {
        // Required empty constructor
    }

    // Constructor with all fields
    public Job(String company, String location, String exactAddress, String businessDescription,
               String title, String jobDescription, int minAge,
               String jobScope, String workField, boolean requiresExperience,
               double salary, String contactName, String contactRole,
               String contactPhone, String businessId, String ownerId, String jobId) {

        this.company = company;
        this.location = location;
        this.exactAddress = exactAddress;
        this.businessDescription = businessDescription;
        this.title = title;
        this.jobDescription = jobDescription;
        this.minAge = minAge;
        this.jobScope = jobScope;
        this.workField = workField;
        this.requiresExperience = requiresExperience;
        this.salary = salary;
        this.contactName = contactName;
        this.contactRole = contactRole;
        this.contactPhone = contactPhone;
        this.businessId = businessId;
        this.ownerId = ownerId;
        this.jobId = jobId;
    }
}