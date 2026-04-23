package com.example.inizjob;

import java.io.Serializable;

/*
 * Class: Job
 * Purpose: Data model for a job posting.
 * Includes: company, location (City), exactAddress, businessDescription, title, jobDescription,
 * minAge, jobScope, workField, requiresExperience, salary, contact info, and IDs.
 */
public class Job implements Serializable {

    public String company;
    public String location;
    public String exactAddress;
    public String businessDescription;

    public String title;
    public String jobDescription;
    public int minAge;

    public String jobScope;
    public String workField;
    public boolean requiresExperience;

    public double salary;
    public String contactName;
    public String contactRole;
    public String contactPhone;
    public String businessId;

    public String ownerId;
    public String jobId;

    public Job() {
        // Required for Firebase
    }

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