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
        // Set the theme before creating the activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        /* Enable offline persistence securely before any other Firebase instance is called
        /This is critical for preventing crashes on restricted networks */
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
            // Ignored if persistence was already enabled during activity restart
        }

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference();

        // Check Stay Connected status before loading UI
        checkStayConnected();

        //Initialize button views
        btnLoginTab = findViewById(R.id.tabLogin);
        btnRegisterTab = findViewById(R.id.tabRegister);

        //Initialize line views
        lineLogin = findViewById(R.id.indicatorLogin);
        lineRegister = findViewById(R.id.indicatorRegister);

        // Load LoginFragment by default
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new LoginFragment())
                .commit();

        //setup listener for login button
        btnLoginTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace to LoginFragment
                switchToLogin(true);
            }
        });

        //setup listener for register button
        btnRegisterTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace to RegisterFragment
                switchToRegister();
            }
        });
    }

    /* Method to check if user asked to stay connected.
    If so, fetch their type and move directly to MainActivity.
     */
    private void checkStayConnected() {
        //creating shared preferences to check if user asked to stay connected
        SharedPreferences prefs = getSharedPreferences("InizJobPrefs", Context.MODE_PRIVATE);
        //checks if user asked to stay connected
        boolean stayConnected = prefs.getBoolean("STAY_CONNECTED", false);
        //checks if user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            //If Statement is true, get's the user Id
            if (stayConnected) {
                String userId = currentUser.getUid();

                // Fetch user data from Firebase Realtime Database using user ID as a key
                mDatabase.child("users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    // Callback when data is loaded
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            //store the user type to navigate to the right activity
                            String userType = task.getResult().child("type").getValue(String.class);
                            if (userType != null) {
                                //Move to main activity, according to user type
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