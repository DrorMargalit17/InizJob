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

        //Initialize toggle view
        toggleYouth = view.findViewById(R.id.toggleYouth);
        toggleBusiness = view.findViewById(R.id.toggleBusiness);

        //Initialize input fields
        inputLayoutBusinessCode = view.findViewById(R.id.inputLayoutBusinessCode);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etBusinessCode = view.findViewById(R.id.etBusinessCode);

        //Initialize button, checkbox and loader
        btnLogin = view.findViewById(R.id.btnLogin);
        cbStayConnected = view.findViewById(R.id.cbStayConnected);
        progressBarLogin = view.findViewById(R.id.progressBarLogin);

        // Set default UI state based on isYouthSelected
        updateToggleUI();

        //setup listener for change UI to youth
        toggleYouth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isYouthSelected = true; //state that youth is selected
                //update UI to youth
                updateToggleUI();
            }
        });

        //setup listener for change UI to business
        toggleBusiness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isYouthSelected = false; //state that business is selected
                //update UI to business
                updateToggleUI();
            }
        });

        // setup listener for login button
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when button clicked, call method to perform login
                performLogin();
            }
        });
    }

    // Method to update UI based on the selected tab (user type)
    private void updateToggleUI() {
        if (getContext() == null) {
            return;
        }

        int purple = ContextCompat.getColor(getContext(), R.color.brand_purple);

        //If statement is true (youth selected), update UI to youth
        if (isYouthSelected) {
            toggleYouth.setBackgroundResource(R.drawable.bg_gradient_button);
            toggleYouth.setTextColor(Color.WHITE);
            toggleBusiness.setBackgroundColor(Color.TRANSPARENT);
            toggleBusiness.setTextColor(purple);
            inputLayoutBusinessCode.setVisibility(View.GONE);
            //else (business selected), update UI to business
        } else {
            toggleBusiness.setBackgroundResource(R.drawable.bg_gradient_button);
            toggleBusiness.setTextColor(Color.WHITE);
            toggleYouth.setBackgroundColor(Color.TRANSPARENT);
            toggleYouth.setTextColor(purple);
            inputLayoutBusinessCode.setVisibility(View.VISIBLE);
        }
    }

    //Method to perform login when login button is clicked
    private void performLogin() {
        //gets the email and password from the input fields
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String enteredBusinessCode = ""; //sets business code variable

        //If bussines code field is not empty, get the text
        if (etBusinessCode != null && etBusinessCode.getText() != null) {
            enteredBusinessCode = etBusinessCode.getText().toString().trim();
        }

        // iF email or password fields are empty, show error massage
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Please enter an email and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ensure business code is entered before attempting login
        if (!isYouthSelected && TextUtils.isEmpty(enteredBusinessCode)) {
            Toast.makeText(getContext(), "Business owner must enter a business code", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save "Stay Connected" preference
        if (getActivity() != null) {
            // Create a SharedPreferences instance
            SharedPreferences prefs = getActivity().getSharedPreferences("InizJobPrefs", Context.MODE_PRIVATE);
            // Create an Editor object to edit SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            // Save the checkbox state in SharedPreferences
            editor.putBoolean("STAY_CONNECTED", cbStayConnected.isChecked());
            editor.apply(); // Apply changes
        }

        /*Disable button after clicking once
        / and show loader to prevent multiple clicks */
        btnLogin.setEnabled(false);
        progressBarLogin.setVisibility(View.VISIBLE);

        //sign in with email and password and set listener
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    // Callback when login is complete
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Lifecycle safety check
                        if (!isAdded() || getActivity() == null) {
                            return;
                        }

                        if (task.isSuccessful()) {
                            // If login is successful, check user type
                            checkUserType();
                        } else {
                            // If login fails, release button, hide loader
                            // and show error
                            btnLogin.setEnabled(true);
                            progressBarLogin.setVisibility(View.GONE);
                            if (task.getException() != null) {
                                Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

        //If user is logged in, gets his ID
        String userId = mAuth.getCurrentUser().getUid();
        //gets the business code from the input field
        String enteredBusinessCode = etBusinessCode.getText() != null ? etBusinessCode.getText().toString().trim() : "";

        /*Fetch user data from Firebase Realtime Database pointing
        to users node and using user ID as a key */
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

                            //Check if the actual user type matches the selected tab
                            boolean isDbUserYouth = false;

                            // Convert userType to boolean
                            if (User.TYPE_YOUTH.equals(userType)) {
                                isDbUserYouth = true;
                            } else if (User.TYPE_BUSINESS.equals(userType)) {
                                isDbUserYouth = false;
                            }

                            // Check if the user type matches the selected tab
                            if (isYouthSelected == isDbUserYouth) {
                                //if bussines is selected, check the business code
                                if (!isYouthSelected) {
                                    //If bussines code is not correct, show error
                                    if (dbBusinessCode == null || !dbBusinessCode.equals(enteredBusinessCode)) {
                                        Toast.makeText(getContext(), "Incorrect business code", Toast.LENGTH_SHORT).show();
                                        //disconnect the user
                                        mAuth.signOut();
                                        btnLogin.setEnabled(true); // Enable the button
                                        progressBarLogin.setVisibility(View.GONE); // Hide the loader
                                        return;
                                    }
                                }

                                //Login approve, move to main activity
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                intent.putExtra("USER_TYPE", userType);
                                //clear the screens history so the user can't go back to login
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                getActivity().finish();
                            } else {
                                //Prevent login if tab mismatch
                                Toast.makeText(getContext(), "The user type does not match the selected tab.", Toast.LENGTH_SHORT).show();
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