package com.example.inizjob;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class RegisterFragment extends Fragment {

    // Variables for logic
    private boolean isYouthSelected = true;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // UI Components
    private TextView toggleYouth, toggleBusiness;
    private View inputLayoutRegBusinessCode;
    private Button btnRegisterAction;

    // Input Fields
    private TextInputEditText etFullName, etEmail, etPhone, etPassword, etBusinessCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // 1. Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference();

        // 2. Connect UI components to XML
        toggleYouth = view.findViewById(R.id.regToggleYouth);
        toggleBusiness = view.findViewById(R.id.regToggleBusiness);
        inputLayoutRegBusinessCode = view.findViewById(R.id.inputLayoutRegBusinessCode);
        btnRegisterAction = view.findViewById(R.id.btnRegisterAction);

        etFullName = view.findViewById(R.id.etFullName);
        etEmail = view.findViewById(R.id.etRegEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etPassword = view.findViewById(R.id.etRegPassword);
        etBusinessCode = view.findViewById(R.id.etRegBusinessCode);

        // 3. Set click listeners for toggle buttons
        toggleYouth.setOnClickListener(v -> {
            isYouthSelected = true;
            updateToggleUI();
        });

        toggleBusiness.setOnClickListener(v -> {
            isYouthSelected = false;
            updateToggleUI();
        });

        // 4. Set click listener for Register button
        btnRegisterAction.setOnClickListener(v -> {
            performRegistration();
        });

        return view;
    }

    // Method to handle the registration process
    private void performRegistration() {
        // 1. Get text from input fields
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String businessCode = etBusinessCode.getText().toString().trim();

        // Determine user type
        String type = isYouthSelected ? "נוער" : "עסק";

        // Basic validation checks
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(fullName)) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Create the user in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registration successful, now save additional details to Realtime Database
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser == null) return;

                            String userId = firebaseUser.getUid();

                            // Create a User object
                            User newUser = new User(fullName, email, phone, type, businessCode);

                            // Save the object to the database under "users -> userId"
                            mDatabase.child("users").child(userId).setValue(newUser)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> dbTask) {
                                            if (dbTask.isSuccessful()) {
                                                Toast.makeText(getContext(), "Registration Successful!", Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(getContext(), "Registration worked, but saving details failed.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        } else {
                            // Registration failed
                            if (task.getException() != null) {
                                Toast.makeText(getContext(), "Registration Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    // Method to change the colors of the toggle buttons based on selection
    void updateToggleUI() {
        if (getContext() == null) return;
        int purple = ContextCompat.getColor(getContext(), R.color.brand_purple);

        if (isYouthSelected) {
            toggleYouth.setBackgroundResource(R.drawable.bg_gradient_button);
            toggleYouth.setTextColor(Color.WHITE);
            toggleBusiness.setBackgroundColor(Color.TRANSPARENT);
            toggleBusiness.setTextColor(purple);
            inputLayoutRegBusinessCode.setVisibility(View.GONE);
        } else {
            toggleBusiness.setBackgroundResource(R.drawable.bg_gradient_button);
            toggleBusiness.setTextColor(Color.WHITE);
            toggleYouth.setBackgroundColor(Color.TRANSPARENT);
            toggleYouth.setTextColor(purple);
            inputLayoutRegBusinessCode.setVisibility(View.VISIBLE);
        }
    }
}