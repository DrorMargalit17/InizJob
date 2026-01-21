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

        // 1. חיבור לרכיבים
        btnLoginTab = findViewById(R.id.tabLogin);
        btnRegisterTab = findViewById(R.id.tabRegister);
        lineLogin = findViewById(R.id.indicatorLogin);
        lineRegister = findViewById(R.id.indicatorRegister);

        // 2. טעינה ראשונית - התחברות
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new LoginFragment())
                .commit();

        // 3. לחיצה על "התחברות"
        btnLoginTab.setOnClickListener(v -> {
            // עיצוב
            btnLoginTab.setTextColor(getResources().getColor(R.color.brand_purple));
            lineLogin.setBackgroundColor(getResources().getColor(R.color.brand_purple));
            btnRegisterTab.setTextColor(getResources().getColor(R.color.text_hint));
            lineRegister.setBackgroundColor(Color.TRANSPARENT);

            // טעינת מסך
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new LoginFragment())
                    .commit();
        });

        // 4. לחיצה על "הרשמה"
        btnRegisterTab.setOnClickListener(v -> {
            // עיצוב
            btnRegisterTab.setTextColor(getResources().getColor(R.color.brand_purple));
            lineRegister.setBackgroundColor(getResources().getColor(R.color.brand_purple));
            btnLoginTab.setTextColor(getResources().getColor(R.color.text_hint));
            lineLogin.setBackgroundColor(Color.TRANSPARENT);

            // טעינת מסך
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new RegisterFragment())
                    .commit();
        });
    }
}