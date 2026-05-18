package com.example.inizjob;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Calendar;

/*
 * class: registerfragment
 * purpose: handles new user registration.
 * * methods and actions list:
 * 1. oncreateview - initializes layout and ui references, including birth date dialog logic.
 * 2. showDatePicker - opens a native android calendar locked between ages 14 and 18.
 * 3. performregistration - validates form (including date for youth or code for business) and creates a new user.
 * 4. updatetoggleui - switches visuals based on youth/business selection.
 */
public class RegisterFragment extends Fragment {

    private boolean isYouthSelected = true; // Default to Youth
    private FirebaseAuth mAuth; // Firebase Authentication instance
    private DatabaseReference mDatabase; // Firebase Realtime Database instance

    private TextView toggleYouth, toggleBusiness; // UI elements for switching between fragments
    private TextInputLayout businessLayout; // UI element for business code
    private TextInputLayout layoutBirthDate; // UI element for birth date container
    private Button btnRegister; // UI element for registration button
    private TextInputEditText etFullName, etEmail, etPhone, etPassword, etBusinessCode, etBirthDate; // UI elements for input fields

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference();

        toggleYouth = view.findViewById(R.id.regToggleYouth);
        toggleBusiness = view.findViewById(R.id.regToggleBusiness);
        businessLayout = view.findViewById(R.id.businessLayout);
        layoutBirthDate = view.findViewById(R.id.layoutBirthDate);
        btnRegister = view.findViewById(R.id.btnRegister);

        etFullName = view.findViewById(R.id.etFullName);
        etEmail = view.findViewById(R.id.etRegEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etPassword = view.findViewById(R.id.etRegPassword);
        etBusinessCode = view.findViewById(R.id.etRegBusinessCode);
        etBirthDate = view.findViewById(R.id.etBirthDate);

        // Open DatePicker when birth date field is clicked
        etBirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // change to youth
        toggleYouth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isYouthSelected = true;
                updateToggleUI();
            }
        });

        //change to business
        toggleBusiness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isYouthSelected = false;
                updateToggleUI();
            }
        });

        //perform registration
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegistration();
            }
        });

        // set default UI state
        updateToggleUI();

        return view;
    }

    private void showDatePicker() {
        if (getContext() == null) return;

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Month is 0-indexed in Calendar
                        String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                        etBirthDate.setText(selectedDate);
                    }
                }, currentYear - 16, currentMonth, currentDay); // Default open showing age 16

        // Set limits for the calendar (14 to 18 years old)
        Calendar minAge = Calendar.getInstance();
        minAge.add(Calendar.YEAR, -18);
        Calendar maxAge = Calendar.getInstance();
        maxAge.add(Calendar.YEAR, -14);

        datePickerDialog.getDatePicker().setMinDate(minAge.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxAge.getTimeInMillis());

        datePickerDialog.show();
    }

    // Method to register to the app
    private void performRegistration() {
        //gets the data from the input fields
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String businessCode = etBusinessCode.getText().toString().trim();
        String birthDate = "";

        //checks that all fields are filled
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(fullName) || TextUtils.isEmpty(phone)) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // validate email format using inputvalidator
        if (!InputValidator.isValidEmail(email)) {
            Toast.makeText(getContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // validate phone format using inputvalidator
        if (!InputValidator.isValidPhone(phone)) {
            Toast.makeText(getContext(), "Phone number must contain 9 or 10 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        // validate password complexity using inputvalidator
        if (!InputValidator.isValidPassword(password)) {
            Toast.makeText(getContext(), "Password must be at least 6 characters, contain uppercase, lowercase, a number, and a special character", Toast.LENGTH_LONG).show();
            return;
        }

        //checks if user is youth or business
        String type;
        if (isYouthSelected) {
            type = User.TYPE_YOUTH;
            String selectedDate = etBirthDate.getText().toString().trim();

            if (TextUtils.isEmpty(selectedDate)) {
                Toast.makeText(getContext(), "Please select a full birth date", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                String[] parts = selectedDate.split("/");
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int year = Integer.parseInt(parts[2]);

                // Validate age to ensure it is between 14 and 18 using InputValidator
                if (!InputValidator.isValidAge(year, month, day)) {
                    Toast.makeText(getContext(), "Registration is only allowed for ages 14 to 18", Toast.LENGTH_LONG).show();
                    return;
                }
                birthDate = selectedDate;
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error reading birth date", Toast.LENGTH_SHORT).show();
                return;
            }
            businessCode = ""; // Clear for youth
        } else {
            type = User.TYPE_BUSINESS;
            // checks businessCode field is not empty
            if (TextUtils.isEmpty(businessCode)) {
                Toast.makeText(getContext(), "Please enter a business code", Toast.LENGTH_SHORT).show();
                return;
            }
            birthDate = ""; // Clear for business
        }

        // Disable button to prevent multiple clicks and crashes
        btnRegister.setEnabled(false);

        //Creating final variables to prevent data from changing during the registration process
        final String finalType = type;
        final String finalBusinessCode = businessCode;
        final String savedBirthDate = birthDate;

        // Firebase registration
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Protect against memory leaks and fragment destruction
                        if (!isAdded() || getActivity() == null) {
                            return;
                        }

                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser == null) {
                                btnRegister.setEnabled(true);
                                return;
                            }

                            String userId = firebaseUser.getUid();

                            // Creating new user WITH the default avatar type for clean DB architecture
                            User newUser = new User(fullName, email, phone, finalType, finalBusinessCode, savedBirthDate, "default");

                            mDatabase.child("users").child(userId).setValue(newUser)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> dbTask) {
                                            // Protect against memory leaks
                                            if (!isAdded() || getActivity() == null) {
                                                return;
                                            }

                                            if (dbTask.isSuccessful()) {
                                                Toast.makeText(getContext(), "Registration Successful!", Toast.LENGTH_LONG).show();
                                                mAuth.signOut();
                                                btnRegister.setEnabled(true);
                                                //return to login
                                                if (getActivity() instanceof AuthActivity) {
                                                    ((AuthActivity) getActivity()).switchToLogin(isYouthSelected);
                                                }
                                            } else {
                                                btnRegister.setEnabled(true);
                                                Toast.makeText(getContext(), "Saving details failed.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        } else {
                            // notify the user of a failure during registration
                            btnRegister.setEnabled(true);
                            if (task.getException() != null) {
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    Toast.makeText(getContext(), "This email is already registered. Please login.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }
                });
    }

    void updateToggleUI() {
        if (getContext() == null) {
            return;
        }
        int purple = ContextCompat.getColor(getContext(), R.color.brand_purple);

        //Changes UI if user type is youth
        if (isYouthSelected) {
            toggleYouth.setBackgroundResource(R.drawable.bg_gradient_button);
            toggleYouth.setTextColor(Color.WHITE);
            toggleBusiness.setBackgroundColor(Color.TRANSPARENT);
            toggleBusiness.setTextColor(purple);

            // Show Date, Hide businessCode
            if (businessLayout != null) businessLayout.setVisibility(View.GONE);
            if (layoutBirthDate != null) layoutBirthDate.setVisibility(View.VISIBLE);
            // Changes UI if user type is business
        } else {
            toggleBusiness.setBackgroundResource(R.drawable.bg_gradient_button);
            toggleBusiness.setTextColor(Color.WHITE);
            toggleYouth.setBackgroundColor(Color.TRANSPARENT);
            toggleYouth.setTextColor(purple);

            // Show businessCode, Hide Date
            if (businessLayout != null) businessLayout.setVisibility(View.VISIBLE);
            if (layoutBirthDate != null) layoutBirthDate.setVisibility(View.GONE);
        }
    }
}