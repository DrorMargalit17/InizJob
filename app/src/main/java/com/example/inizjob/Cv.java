package com.example.inizjob;

import java.io.Serializable;

/*
 * Class: Cv
 * Purpose: Data model for a Curriculum Vitae object.
 * * Fields:
 * - summary: Professional profile/objective.
 * - education: Schools and majors.
 * - experience: Work and volunteering history.
 * - skills: Technical and practical skills.
 * - achievements: Projects and initiatives.
 * - traits: Personal strengths and characteristics.
 * - uniqueDetail: Personal unique information.
 */
public class Cv implements Serializable {
    public String cvId;
    public String ownerId;
    public String fullName;
    public String phone;
    public String email;

    // New Expanded Categories
    public String summary;      // תמצית מקצועית
    public String education;    // השכלה
    public String experience;   // ניסיון תעסוקתי והתנדבות
    public String skills;       // כישורים ומיומנויות
    public String achievements; // הישגים ויוזמות
    public String traits;       // תכונות ויכולות אישיות
    public String uniqueDetail; // מידע נוסף וייחוד אישי

    public String generatedText;

    public Cv() {
    }

    public Cv(String cvId, String ownerId, String fullName, String phone, String email,
              String summary, String education, String experience, String skills,
              String achievements, String traits, String uniqueDetail, String generatedText) {
        this.cvId = cvId;
        this.ownerId = ownerId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
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