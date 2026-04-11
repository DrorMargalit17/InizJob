package com.example.inizjob;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

/*
 * Class: RegisterFragment
 * Purpose: Handles new user registration.
 * * Methods and Actions List:
 * 1. onCreateView - Initializes layout and UI references, including birth date spinners.
 * 2. populateSpinners - Fills the Day, Month, and Year dropdown menus.
 * 3. performRegistration - Validates form (including date for Youth or Code for Business) and creates a new user.
 * 4. updateToggleUI - Switches visuals based on Youth/Business selection.
 */
public class RegisterFragment extends Fragment {

    private boolean isYouthSelected = true; // Default to Youth
    private FirebaseAuth mAuth; // Firebase Authentication instance
    private DatabaseReference mDatabase; // Firebase Realtime Database instance

    private TextView toggleYouth, toggleBusiness; // UI elements for switching between fragments
    private View businessLayout; // UI element for business code
    private LinearLayout birthDateLayout; // UI element for birth date
    private Spinner spinnerDay, spinnerMonth, spinnerYear; // UI elements for date selection
    private Button btnRegister; // UI element for registration button
    private TextInputEditText etFullName, etEmail, etPhone, etPassword, etBusinessCode; // UI elements for input fields

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference();

        toggleYouth = view.findViewById(R.id.regToggleYouth);
        toggleBusiness = view.findViewById(R.id.regToggleBusiness);
        businessLayout = view.findViewById(R.id.businessLayout);
        birthDateLayout = view.findViewById(R.id.birthDateLayout);
        btnRegister = view.findViewById(R.id.btnRegister);

        etFullName = view.findViewById(R.id.etFullName);
        etEmail = view.findViewById(R.id.etRegEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etPassword = view.findViewById(R.id.etRegPassword);
        etBusinessCode = view.findViewById(R.id.etRegBusinessCode);

        spinnerDay = view.findViewById(R.id.spinnerDay);
        spinnerMonth = view.findViewById(R.id.spinnerMonth);
        spinnerYear = view.findViewById(R.id.spinnerYear);

        populateSpinners();

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

    // Creates the dropdown lists for the birth date selection
    private void populateSpinners() {
        if (getContext() == null) {
            return;
        }

        // Days
        ArrayList<String> days = new ArrayList<>();
        days.add("יום");
        for (int i = 1; i <= 31; i++) {
            days.add(String.valueOf(i));
        }
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDay.setAdapter(dayAdapter);

        // Months
        ArrayList<String> months = new ArrayList<>();
        months.add("חודש");
        for (int i = 1; i <= 12; i++) {
            months.add(String.valueOf(i));
        }
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // Years
        ArrayList<String> years = new ArrayList<>();
        years.add("שנה");
        for (int i = 2026; i >= 2000; i--) {
            years.add(String.valueOf(i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);
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

        //checks if user is youth or business
        String type;
        if (isYouthSelected) {
            type = "נוער";
            // checks that a full date has been selected
            if (spinnerDay.getSelectedItemPosition() == 0 || spinnerMonth.getSelectedItemPosition() == 0 || spinnerYear.getSelectedItemPosition() == 0) {
                Toast.makeText(getContext(), "Please select a full birth date", Toast.LENGTH_SHORT).show();
                return;
            }
            businessCode = ""; // Clear for youth
            birthDate = spinnerDay.getSelectedItem().toString() + "/" +
                    spinnerMonth.getSelectedItem().toString() + "/" +
                    spinnerYear.getSelectedItem().toString();
        } else {
            type = "עסק";
            // checks businessCode field is not empty
            if (TextUtils.isEmpty(businessCode)) {
                Toast.makeText(getContext(), "Please enter a business code", Toast.LENGTH_SHORT).show();
                return;
            }
            birthDate = ""; // Clear for business
        }

        //checks that all fields are filled
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(fullName)) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }


        //Creating final variables to prevent data from changing during the registration process
        final String finalType = type;
        final String finalBusinessCode = businessCode;
        final String savedBirthDate = birthDate;

        // Firebase registration
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser == null) {
                                return;
                            }

                            String userId = firebaseUser.getUid();

                            //creating new user
                            User newUser = new User(fullName, email, phone, finalType, finalBusinessCode, savedBirthDate);

                            mDatabase.child("users").child(userId).setValue(newUser)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> dbTask) {
                                            if (dbTask.isSuccessful()) {
                                                Toast.makeText(getContext(), "Registration Successful!", Toast.LENGTH_LONG).show();
                                                mAuth.signOut();
                                                //return to login
                                                if (getActivity() instanceof AuthActivity) {
                                                    ((AuthActivity) getActivity()).switchToLogin(isYouthSelected);
                                                }
                                            } else {
                                                // notify the user of the failure
                                                Toast.makeText(getContext(), "Saving details failed.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        } else {
                            // notify the user of a failure during registration
                            if (task.getException() != null) {
                                Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
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
            if (birthDateLayout != null) birthDateLayout.setVisibility(View.VISIBLE);
            // Changes UI if user type is business
        } else {
            toggleBusiness.setBackgroundResource(R.drawable.bg_gradient_button);
            toggleBusiness.setTextColor(Color.WHITE);
            toggleYouth.setBackgroundColor(Color.TRANSPARENT);
            toggleYouth.setTextColor(purple);

            // Show businessCode, Hide Date
            if (businessLayout != null) businessLayout.setVisibility(View.VISIBLE);
            if (birthDateLayout != null) birthDateLayout.setVisibility(View.GONE);
        }
    }
}