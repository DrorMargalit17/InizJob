package com.example.inizjob;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

/*
 * Class: AuthActivity
 * Purpose: Manages the authentication screens and switches between Login and Register fragments.
 * * Methods and Actions List:
 * 1. onCreate - Initializes UI elements and sets the default fragment to Login.
 * 2. switchToLogin - Switches the current view to the LoginFragment and passes user type preference.
 */
public class AuthActivity extends AppCompatActivity {

    Button btnLoginTab, btnRegisterTab;
    View lineLogin, lineRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        btnLoginTab = findViewById(R.id.tabLogin);
        btnRegisterTab = findViewById(R.id.tabRegister);
        lineLogin = findViewById(R.id.indicatorLogin);
        lineRegister = findViewById(R.id.indicatorRegister);

        // Load LoginFragment by default
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new LoginFragment())
                .commit();

        // Explicit OnClickListener without Lambdas
        btnLoginTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToLogin(true);
            }
        });

        // Explicit OnClickListener without Lambdas
        btnRegisterTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRegisterTab.setTextColor(getResources().getColor(R.color.brand_purple));
                lineRegister.setBackgroundColor(getResources().getColor(R.color.brand_purple));
                btnLoginTab.setTextColor(getResources().getColor(R.color.text_hint));
                lineLogin.setBackgroundColor(Color.TRANSPARENT);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new RegisterFragment())
                        .commit();
            }
        });
    }

    public void switchToLogin(boolean isYouth) {
        btnLoginTab.setTextColor(getResources().getColor(R.color.brand_purple));
        lineLogin.setBackgroundColor(getResources().getColor(R.color.brand_purple));
        btnRegisterTab.setTextColor(getResources().getColor(R.color.text_hint));
        lineRegister.setBackgroundColor(Color.TRANSPARENT);

        LoginFragment loginFragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putBoolean("IS_YOUTH", isYouth);
        loginFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, loginFragment)
                .commit();
    }
}