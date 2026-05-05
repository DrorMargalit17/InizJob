package com.example.inizjob;

import android.util.Patterns;
import java.util.Calendar;

/*
 * class: inputvalidator
 * purpose: provides static methods to validate user inputs (email, password, age, phone) based on specific rules.
 * follows the single responsibility principle (srp).
 */
public class InputValidator {

    /**
     * validates if the given string is a properly formatted email address.
     *
     * @param email the email string to check.
     * @return true if the email format is valid, false otherwise.
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        if (email.isEmpty()) {
            return false;
        }

        // uses android's built-in patterns class to verify standard email format
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * validates if the given password meets the security requirements:
     * - at least 6 characters long
     * - contains at least one uppercase letter (a-z)
     * - contains at least one lowercase letter (a-z)
     * - contains at least one number (0-9)
     * - contains at least one special character (e.g., !@#$%^&*)
     *
     * @param password the password string to check.
     * @return true if the password is valid, false otherwise.
     */
    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }

        // check minimum length
        if (password.length() < 6) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasSpecialChar = false;
        boolean hasDigit = false; // added check for digits

        // loop through each character to check conditions without using lambdas
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);

            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true; // flag digit found
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecialChar = true;
            }
        }

        // return true only if all conditions are met
        if (hasUppercase && hasLowercase && hasSpecialChar && hasDigit) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * validates if the user's age is exactly between 14 and 18.
     *
     * @param birthYear  the user's birth year.
     * @param birthMonth the user's birth month.
     * @param birthDay   the user's birth day.
     * @return true if age is between 14 and 18, false otherwise.
     */
    public static boolean isValidAge(int birthYear, int birthMonth, int birthDay) {
        Calendar today = Calendar.getInstance();
        int currentYear = today.get(Calendar.YEAR);
        int currentMonth = today.get(Calendar.MONTH) + 1; // calendar.month is 0-indexed (0-11)
        int currentDay = today.get(Calendar.DAY_OF_MONTH);

        int age = currentYear - birthYear;

        // adjust age if birthday hasn't occurred yet this year
        if (currentMonth < birthMonth) {
            age--;
        } else if (currentMonth == birthMonth && currentDay < birthDay) {
            age--;
        }

        if (age >= 14 && age <= 18) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * validates if the given string is a valid phone number.
     * it must contain exactly 9 or 10 digits.
     *
     * @param phone the phone string to check.
     * @return true if the phone format is valid, false otherwise.
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null) {
            return false;
        }

        String cleanPhone = phone.trim();

        // check length (must be 9 or 10)
        if (cleanPhone.length() != 9 && cleanPhone.length() != 10) {
            return false;
        }

        // check if all characters are digits
        for (int i = 0; i < cleanPhone.length(); i++) {
            if (!Character.isDigit(cleanPhone.charAt(i))) {
                return false;
            }
        }

        return true;
    }
}