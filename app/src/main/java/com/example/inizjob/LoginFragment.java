package com.example.inizjob;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginFragment extends Fragment {

    private boolean isYouthSelected = true; // Sets User type to Youth by default
    private FirebaseAuth mAuth; // Firebase Authentication instance
    private DatabaseReference mDatabase; // Firebase Realtime Database instance

    private TextView toggleYouth, toggleBusiness; // UI elements for switching between fragments
    private View inputLayoutBusinessCode; // UI element for business code
    private Button btnLogin; // UI element for login button
    private TextInputEditText etEmail, etPassword, etBusinessCode; // UI elements for input fields
    private CheckBox cbStayConnected; // UI element for "Stay Connected" checkbox
    private ProgressBar progressBarLogin; // UI element for loading state

    public LoginFragment() {
        // Required empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //checks if data passes to fragment while being created
        if (getArguments() != null) {
            isYouthSelected = getArguments().getBoolean("IS_YOUTH", true);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference();

        toggleYouth = view.findViewById(R.id.toggleYouth);
        toggleBusiness = view.findViewById(R.id.toggleBusiness);
        inputLayoutBusinessCode = view.findViewById(R.id.inputLayoutBusinessCode);
        btnLogin = view.findViewById(R.id.btnLogin);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etBusinessCode = view.findViewById(R.id.etBusinessCode);
        cbStayConnected = view.findViewById(R.id.cbStayConnected);
        progressBarLogin = view.findViewById(R.id.progressBarLogin);

        // Set default UI state based on isYouthSelected
        updateToggleUI();

        //change to youth
        toggleYouth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isYouthSelected = true;
                updateToggleUI();
            }
        });

        // change to business
        toggleBusiness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isYouthSelected = false;
                updateToggleUI();
            }
        });

        // preform login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });
    }

    // Method to update UI based on isYouthSelected
    private void updateToggleUI() {
        if (getContext() == null) {
            return;
        }

        int purple = ContextCompat.getColor(getContext(), R.color.brand_purple);

        if (isYouthSelected) {
            toggleYouth.setBackgroundResource(R.drawable.bg_gradient_button);
            toggleYouth.setTextColor(Color.WHITE);
            toggleBusiness.setBackgroundColor(Color.TRANSPARENT);
            toggleBusiness.setTextColor(purple);
            inputLayoutBusinessCode.setVisibility(View.GONE);
        } else {
            toggleBusiness.setBackgroundResource(R.drawable.bg_gradient_button);
            toggleBusiness.setTextColor(Color.WHITE);
            toggleYouth.setBackgroundColor(Color.TRANSPARENT);
            toggleYouth.setTextColor(purple);
            inputLayoutBusinessCode.setVisibility(View.VISIBLE);
        }
    }

    // Method to perform login
    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String enteredBusinessCode = "";

        if (etBusinessCode != null && etBusinessCode.getText() != null) {
            enteredBusinessCode = etBusinessCode.getText().toString().trim();
        }

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "אנא הזן אימייל וסיסמה", Toast.LENGTH_SHORT).show();
            return;
        }

        // Security check: Ensure business code is entered before attempting login
        if (!isYouthSelected && TextUtils.isEmpty(enteredBusinessCode)) {
            Toast.makeText(getContext(), "בעל עסק חייב להזין קוד עסק", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save "Stay Connected" preference
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("InizJobPrefs", Context.MODE_PRIVATE); // Access SharedPreferences
            SharedPreferences.Editor editor = prefs.edit(); // Edit SharedPreferences
            editor.putBoolean("STAY_CONNECTED", cbStayConnected.isChecked()); // Save checkbox state
            editor.apply(); // Apply changes
        }

        // Disable button and show loader to prevent multiple clicks
        btnLogin.setEnabled(false);
        progressBarLogin.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Lifecycle safety check
                        if (!isAdded() || getActivity() == null) {
                            return;
                        }

                        if (task.isSuccessful()) {
                            checkUserType();
                        } else {
                            btnLogin.setEnabled(true);
                            progressBarLogin.setVisibility(View.GONE);
                            if (task.getException() != null) {
                                Toast.makeText(getContext(), "שגיאה: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    // Method to check user type and route to MainActivity
    private void checkUserType() {
        if (mAuth.getCurrentUser() == null) {
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        String enteredBusinessCode = etBusinessCode.getText() != null ? etBusinessCode.getText().toString().trim() : "";

        // Fetch user data from Firebase Realtime Database using user ID as a key
        mDatabase.child("users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                // Check if fragment is still "alive" & attached to the activity to prevent crashes & memory leaks
                if (!isAdded() || getActivity() == null) {
                    return;
                }

                if (task.isSuccessful() && task.getResult() != null) {
                    //take the snapshot of the data
                    DataSnapshot snapshot = task.getResult();

                    if (snapshot.exists()) {
                        // Get user type and business code from the database
                        String userType = snapshot.child("type").getValue(String.class);
                        String dbBusinessCode = snapshot.child("businessCode").getValue(String.class);

                        if (userType != null) {

                            // Check if the actual user type matches the selected tab
                            boolean isDbUserYouth = false;

                            if (User.TYPE_YOUTH.equals(userType)) {
                                isDbUserYouth = true;
                            } else if (User.TYPE_BUSINESS.equals(userType)) {
                                isDbUserYouth = false;
                            }

                            if (isYouthSelected == isDbUserYouth) {
                                // Strict verification for Business Users
                                if (!isYouthSelected) {
                                    if (dbBusinessCode == null || !dbBusinessCode.equals(enteredBusinessCode)) {
                                        Toast.makeText(getContext(), "קוד העסק שגוי", Toast.LENGTH_SHORT).show();
                                        mAuth.signOut(); // Disconnect the unauthorized user
                                        btnLogin.setEnabled(true);
                                        progressBarLogin.setVisibility(View.GONE);
                                        return;
                                    }
                                }

                                // Login fully approved
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                intent.putExtra("USER_TYPE", userType);
                                //clear the screens history so the user can't go back to login
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                getActivity().finish();
                            } else {
                                // Prevent login if tab mismatch
                                Toast.makeText(getContext(), "סוג המשתמש אינו תואם ללשונית שנבחרה", Toast.LENGTH_SHORT).show();
                                mAuth.signOut(); // Disconnect the incorrect user session
                                btnLogin.setEnabled(true);
                                progressBarLogin.setVisibility(View.GONE);
                            }

                        } else {
                            // Handle the case where user type is not found in the database
                            Toast.makeText(getContext(), "User type not found", Toast.LENGTH_SHORT).show();
                            btnLogin.setEnabled(true);
                            progressBarLogin.setVisibility(View.GONE);
                        }
                    } else {
                        // Handle the case where user data is missing from the database
                        Toast.makeText(getContext(), "User data missing", Toast.LENGTH_SHORT).show();
                        btnLogin.setEnabled(true);
                        progressBarLogin.setVisibility(View.GONE);
                    }
                } else {
                    // Handle the case where the task is not successful
                    Toast.makeText(getContext(), "Task not successful", Toast.LENGTH_SHORT).show();
                    btnLogin.setEnabled(true);
                    progressBarLogin.setVisibility(View.GONE);
                }
            }
        });
    }
}