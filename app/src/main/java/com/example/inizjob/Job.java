package com.example.inizjob;

import java.io.Serializable;

/*
 * Class: Job
 * Purpose: Data model representing a job posting.
 * * Fields List:
 * - company, location, exactAddress, businessDescription: Business info.
 * - title, jobDescription, minAge, prerequisites, hoursAndDays, flexibilityCommitment: Job details.
 * - jobScope, travelExpenses, workField, requiresExperience: New filtering criteria.
 * - salary, conditions, contactName, contactRole, contactPhone, businessId: Salary and contact info.
 * - ownerId, jobId: Database identifiers.
 */
public class Job implements Serializable {

    public String company;
    public String location;
    public String exactAddress;
    public String businessDescription;

    public String title;
    public String jobDescription;
    public int minAge;
    public String prerequisites;
    public String hoursAndDays;
    public boolean flexibilityCommitment;

    // --- New Fields for Smart Filtering ---
    public String jobScope;           // היקף משרה (מלאה/חלקית/משמרות)
    public boolean travelExpenses;    // החזר נסיעות (כן/לא)
    public String workField;          // תחום עבודה (מסעדות/מכירות וכו')
    public boolean requiresExperience; // דרוש ניסיון מקדים (כן/לא)
    // --------------------------------------

    public double salary;
    public String conditions;
    public String contactName;
    public String contactRole;
    public String contactPhone;
    public String businessId;

    public String ownerId;
    public String jobId;

    public Job() {
        // Required empty constructor for Firebase
    }

    public Job(String company, String location, String exactAddress, String businessDescription,
               String title, String jobDescription, int minAge, String prerequisites,
               String hoursAndDays, boolean flexibilityCommitment,
               String jobScope, boolean travelExpenses, String workField, boolean requiresExperience,
               double salary, String conditions, String contactName, String contactRole,
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

        this.jobScope = jobScope;
        this.travelExpenses = travelExpenses;
        this.workField = workField;
        this.requiresExperience = requiresExperience;

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