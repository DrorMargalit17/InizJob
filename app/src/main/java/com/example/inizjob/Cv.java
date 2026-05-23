package com.example.inizjob;

import java.io.Serializable;

/*
 * Class: Cv
 * Purpose: Data model for a CV object.
 */
public class Cv implements Serializable {
    public String cvId; // Unique identifier for the CV
    public String ownerId; // ID of the user who owns the CV
    public String fullName; // Full name of the user
    public String phone; // Phone number of the user
    public String email; // Email address of the user

    public String cvTitle; // Title of the CV

    public String summary;  // Professionalism Summary for the CV
    public String education; // Education details for the CV
    public String experience; // Experience details for the CV
    public String skills; // Skills details for the CV
    public String achievements; // Achievements details for the CV
    public String traits; // Traits details for the CV
    public String uniqueDetail; // Unique details for the CV

    public String generatedText; // Generated text for the CV

    public Cv() {
        //Required empty constructor
    }

    // Constructor with all fields
    public Cv(String cvId, String ownerId, String fullName, String phone, String email, String cvTitle,
              String summary, String education, String experience, String skills,
              String achievements, String traits, String uniqueDetail, String generatedText) {
        this.cvId = cvId;
        this.ownerId = ownerId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.cvTitle = cvTitle;
        this.summary = summary;
        this.education = education;
        this.experience = experience;
        this.skills = skills;
        this.achievements = achievements;
        this.traits = traits;
        this.uniqueDetail = uniqueDetail;
        this.generatedText = generatedText;
    }
}