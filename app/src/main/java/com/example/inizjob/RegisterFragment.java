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
 * class: RegisterFragment
 * purpose: handles new user registration.
 */
public class RegisterFragment extends Fragment {

    private boolean isYouthSelected = true; // Default to Youth
    private FirebaseAuth mAuth; // Firebase Authentication instance
    private DatabaseReference mDatabase; // Firebase Realtime Database instance

    private TextView toggleYouth, toggleBusiness; // UI elements for switching between fragments
    //UI elements for input layouts for birth date and business code
    private TextInputLayout businessLayout, layoutBirthDate;
    private Button btnRegister; // UI element for registration button

    //UI elements for input fields
    private TextInputEditText etFullName, etEmail, etPhone, etPassword, etBusinessCode, etBirthDate;

    @Nullable
    @Override
    /*restart the XML, sets the click listeners for the buttons*/
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference();

        //Initialize toggles
        toggleYouth = view.findViewById(R.id.regToggleYouth);
        toggleBusiness = view.findViewById(R.id.regToggleBusiness);

        //Initialize input layout
        businessLayout = view.findViewById(R.id.businessLayout);
        layoutBirthDate = view.findViewById(R.id.layoutBirthDate);

        //Initialize input fields
        etFullName = view.findViewById(R.id.etFullName);
        etEmail = view.findViewById(R.id.etRegEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etPassword = view.findViewById(R.id.etRegPassword);
        etBusinessCode = view.findViewById(R.id.etRegBusinessCode);
        etBirthDate = view.findViewById(R.id.etBirthDate);

        //Initialize register Button
        btnRegister = view.findViewById(R.id.btnRegister);


        //setup listener to open DatePicker when birth date field is clicked
        etBirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open date picker
                showDatePicker();
            }
        });

        // setup listener for change UI to youth
        toggleYouth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isYouthSelected = true; // set to youth
                //call the method and update UI to youth
                updateToggleUI();
            }
        });

        //setup listener for change UI to business
        toggleBusiness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isYouthSelected = false; // set to business
                //call the method and update UI to business
                updateToggleUI();
            }
        });

        //setup listener for performing registration
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when button clicked, call method to perform registration
                performRegistration();
            }
        });

        //set default UI state to youth
        updateToggleUI();

        return view;
    }

    /*This method shows the date picker dialog
    when the birth date input is clicked*/
    private void showDatePicker() {
        if (getContext() == null) return;

        // Initialize the date picker
        Calendar calendar = Calendar.getInstance(); //create a calendar instance
        int currentYear = calendar.get(Calendar.YEAR) - 16; // Set the current year and age limit
        int currentMonth = calendar.get(Calendar.MONTH); // Set the current month
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Set the current day

        // Create and show the date picker dialog, and set the listener
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    // Callback when a date is selected, and update the input field after selection
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                        // Set the selected date to the input field
                        etBirthDate.setText(selectedDate);
                    }
                }, currentYear - 16, currentMonth, currentDay); // Default open showing age 16

        // Set the date range of the years for the date picker
        Calendar minAge = Calendar.getInstance();
        minAge.add(Calendar.YEAR, -18);
        Calendar maxAge = Calendar.getInstance();
        maxAge.add(Calendar.YEAR, -14);

        datePickerDialog.getDatePicker().setMinDate(minAge.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxAge.getTimeInMillis());
        // Show the date picker
        datePickerDialog.show();
    }

    // This method performs the registration process
    private void performRegistration() {
        //gets the data from the input fields
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String businessCode = etBusinessCode.getText().toString().trim();
        String birthDate = "";

        //checks that password and email fields are filled
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(fullName) || TextUtils.isEmpty(phone)) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // validate email format using inputValidator
        if (!InputValidator.isValidEmail(email)) {
            Toast.makeText(getContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // validate phone format using inputValidator
        if (!InputValidator.isValidPhone(phone)) {
            Toast.makeText(getContext(), "Phone number must contain 9 or 10 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        // validate password complexity using inputValidator
        if (!InputValidator.isValidPassword(password)) {
            Toast.makeText(getContext(), "Password must be at least 6 characters, contain uppercase, lowercase, a number, and a special character", Toast.LENGTH_LONG).show();
            return;
        }

        //checks if user is youth or business
        String type;
        if (isYouthSelected) {
            type = User.TYPE_YOUTH;
            //If youth, validate birth date
            String selectedDate = etBirthDate.getText().toString().trim();

            // Check if birth date is empty, If so, show a toast
            if (TextUtils.isEmpty(selectedDate)) {
                Toast.makeText(getContext(), "Please select a full birth date", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // Parse birth date to validate age
                String[] parts = selectedDate.split("/");
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int year = Integer.parseInt(parts[2]);

                // Validate age to ensure it is between 14 and 18 using InputValidator
                if (!InputValidator.isValidAge(year, month, day)) {
                    Toast.makeText(getContext(), "Registration is only allowed for ages 14 to 18", Toast.LENGTH_LONG).show();
                    return;
                }
                // Set birth date
                birthDate = selectedDate;
                //If birth date is not valid, show a toast
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error reading birth date", Toast.LENGTH_SHORT).show();
                return;
            }
            businessCode = ""; // Clear for youth
        } else {
            //If business, validate business code
            type = User.TYPE_BUSINESS;
            // checks businessCode field is not empty
            if (TextUtils.isEmpty(businessCode)) {
                Toast.makeText(getContext(), "Please enter a business code", Toast.LENGTH_SHORT).show();
                return;
            }
            birthDate = ""; // Clear for business
        }

        // Disable button after clicking once
        // Prevents multiple clicks and crashes
        btnRegister.setEnabled(false);

        //Creating final variables to prevent data from changing
        // during the registration process
        final String finalType = type;
        final String finalBusinessCode = businessCode;
        final String savedBirthDate = birthDate;

        // Firebase registration, setup listener
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    //callback when registration is successful
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Protect against memory leaks and fragment destruction
                        if (!isAdded() || getActivity() == null) {
                            return;
                        }

                        //If firebase user is null, enable button and return
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser == null) {
                                btnRegister.setEnabled(true);
                                return;
                            }

                            //If firebase user is not null, get the user ID
                            String userId = firebaseUser.getUid();

                            //Creating new user with the default avatar type for clean DB architecture
                            User newUser = new User(fullName, email, phone, finalType, finalBusinessCode, savedBirthDate, "default");

                            //database reference points to the users node and saves the new user with the user ID
                            mDatabase.child("users").child(userId).setValue(newUser)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        //callback when data is saved
                                        public void onComplete(@NonNull Task<Void> dbTask) {
                                            // Protect against memory leaks
                                            if (!isAdded() || getActivity() == null) {
                                                return;
                                            }

                                            //If data is saved (register success),
                                            // enable button and show toast
                                            if (dbTask.isSuccessful()) {
                                                Toast.makeText(getContext(), "Registration Successful!", Toast.LENGTH_LONG).show();
                                                mAuth.signOut();
                                                btnRegister.setEnabled(true);
                                                //once registered, switch to login fragment automatically
                                                if (getActivity() instanceof AuthActivity) {
                                                    ((AuthActivity) getActivity()).switchToLogin(isYouthSelected);
                                                }
                                            } else {
                                                //If register process failed, enable button and show toast
                                                btnRegister.setEnabled(true);
                                                Toast.makeText(getContext(), "Saving details failed.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        } else {
                            // notify the user of a failure during registration
                            //related to email address already in use
                            btnRegister.setEnabled(true); // Enable button
                            if (task.getException() != null) {
                                /*Alerting the user that's he already registered and
                                transfer to login screen */
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    Toast.makeText(getContext(), "This email is already registered. Please login.", Toast.LENGTH_LONG).show();
                                } else {
                                    // show a toast mentioning there's an error
                                    Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }
                });
    }

    //This method used to update the UI based on the user's choice
    //register as youth or business
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