package com.example.inizjob;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AuthActivity extends AppCompatActivity {

    private Button btnLoginTab, btnRegisterTab; // UI elements for switching between fragments
    private View lineLogin, lineRegister; // UI elements for visual indicators
    private FirebaseAuth mAuth; // Firebase Authentication instance
    private DatabaseReference mDatabase; // Firebase Realtime Database instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference();

        // Check Stay Connected status before loading UI
        checkStayConnected();

        btnLoginTab = findViewById(R.id.tabLogin);
        btnRegisterTab = findViewById(R.id.tabRegister);
        lineLogin = findViewById(R.id.indicatorLogin);
        lineRegister = findViewById(R.id.indicatorRegister);

        // Load LoginFragment by default
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new LoginFragment())
                .commit();

        btnLoginTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToLogin(true);
            }
        });

        btnRegisterTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToRegister();
            }
        });
    }

    // Method to check if user asked to stay connected. If so, fetch their type and move directly to MainActivity
    private void checkStayConnected() {
        SharedPreferences prefs = getSharedPreferences("InizJobPrefs", Context.MODE_PRIVATE); // Access SharedPreferences
        boolean stayConnected = prefs.getBoolean("STAY_CONNECTED", false); // Check if user asked to stay connected
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Check if user is logged in

        if (currentUser != null) {
            if (stayConnected) {
                // User wants to stay connected, fetch their type to start MainActivity
                String userId = currentUser.getUid();

                // Fetch user data from Firebase Realtime Database using user ID as a key
                mDatabase.child("users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            // User type found, start MainActivity
                            String userType = task.getResult().child("type").getValue(String.class);
                            if (userType != null) {
                                Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                                intent.putExtra("USER_TYPE", userType);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }
                });
            } else {
                // User did not check "Stay Connected", sign them out
                mAuth.signOut();
            }
        }
    }

    // Method to switch to the LoginFragment and update UI
    public void switchToLogin(boolean isYouth) {
        btnLoginTab.setTextColor(getResources().getColor(R.color.brand_purple));
        lineLogin.setBackgroundColor(getResources().getColor(R.color.brand_purple));
        btnRegisterTab.setTextColor(getResources().getColor(R.color.text_hint));
        lineRegister.setBackgroundColor(Color.TRANSPARENT);

        LoginFragment loginFragment = new LoginFragment();
        Bundle args = new Bundle(); // Create a Bundle to pass data to the fragment
        args.putBoolean("IS_YOUTH", isYouth); // Pass the isYouth value to the fragment
        loginFragment.setArguments(args); // Set the arguments in the fragment

        // Replace to LoginFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, loginFragment)
                .commit();
    }

    // Method to switch to the RegisterFragment and update UI
    public void switchToRegister() {
        btnRegisterTab.setTextColor(getResources().getColor(R.color.brand_purple));
        lineRegister.setBackgroundColor(getResources().getColor(R.color.brand_purple));
        btnLoginTab.setTextColor(getResources().getColor(R.color.text_hint));
        lineLogin.setBackgroundColor(Color.TRANSPARENT);

        // Replace to RegisterFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new RegisterFragment())
                .commit();
    }
}