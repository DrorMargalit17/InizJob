package com.example.inizjob;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class AuthActivity extends AppCompatActivity {

    Button btnLoginTab, btnRegisterTab;
    View lineLogin, lineRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // 1. Connect to UI components
        btnLoginTab = findViewById(R.id.tabLogin);
        btnRegisterTab = findViewById(R.id.tabRegister);
        lineLogin = findViewById(R.id.indicatorLogin);
        lineRegister = findViewById(R.id.indicatorRegister);

        // 2. Initial load - show Login screen by default
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new LoginFragment())
                .commit();

        // 3. Click listener for "Login" tab
        btnLoginTab.setOnClickListener(v -> {
            // Update UI colors to show Login is selected
            btnLoginTab.setTextColor(getResources().getColor(R.color.brand_purple));
            lineLogin.setBackgroundColor(getResources().getColor(R.color.brand_purple));
            btnRegisterTab.setTextColor(getResources().getColor(R.color.text_hint));
            lineRegister.setBackgroundColor(Color.TRANSPARENT);

            // Load LoginFragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new LoginFragment())
                    .commit();
        });

        // 4. Click listener for "Register" tab
        btnRegisterTab.setOnClickListener(v -> {
            // Update UI colors to show Register is selected
            btnRegisterTab.setTextColor(getResources().getColor(R.color.brand_purple));
            lineRegister.setBackgroundColor(getResources().getColor(R.color.brand_purple));
            btnLoginTab.setTextColor(getResources().getColor(R.color.text_hint));
            lineLogin.setBackgroundColor(Color.TRANSPARENT);

            // Load RegisterFragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new RegisterFragment())
                    .commit();
        });
    }
}