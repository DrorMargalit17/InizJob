package com.example.inizjob;

import android.util.Patterns;
import java.util.Calendar;

/*
 * class: inputvalidator
 * purpose: provides static methods to validate user inputs
 *  (email, password, age, phone) based on specific rules.
 */
public class InputValidator {

    //This method used to validate email format
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        if (email.isEmpty()) {
            return false;
        }

        //uses android's built-in patterns class to verify standard email format
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /*This method used to validate password format*/
    public static boolean isValidPassword(String password) {
        //check if password is null
        if (password == null) {
            return false;
        }

        // check minimum length
        if (password.length() < 6) {
            return false;
        }

        // initialize flags for each condition
        boolean hasUppercase = false; // flag uppercase found
        boolean hasLowercase = false; // flag lowercase found
        boolean hasSpecialChar = false; // flag special character found
        boolean hasDigit = false; // flag digit found

        // loop through each character to check conditions
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            // check for uppercase letter
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
                // check for lowercase letter
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
                // check for digit
            } else if (Character.isDigit(c)) {
                hasDigit = true;
                // check for special character
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

    /*This method used to validate age format*/
    public static boolean isValidAge(int birthYear, int birthMonth, int birthDay) {
        // get current date
        Calendar today = Calendar.getInstance(); //create a calendar instance
        int currentYear = today.get(Calendar.YEAR);// Set the current year
        int currentMonth = today.get(Calendar.MONTH) + 1; // Set the current month
        int currentDay = today.get(Calendar.DAY_OF_MONTH); // Set the current day

        int age = currentYear - birthYear; // calculate age

        // adjust age if birthday hasn't occurred yet this year
        if (currentMonth < birthMonth) {
            age--;
        } else if (currentMonth == birthMonth && currentDay < birthDay) {
            age--;
        }

        // check if age is between 14 and 18
        if (age >= 14 && age <= 18) {
            return true;
        } else {
            return false;
        }
    }

    /*This method used to validate phone format*/
    public static boolean isValidPhone(String phone) {
        // check if phone is null
        if (phone == null) {
            return false;
        }

        // remove leading and trailing spaces
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