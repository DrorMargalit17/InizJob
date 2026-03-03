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

        btnLoginTab = findViewById(R.id.tabLogin);
        btnRegisterTab = findViewById(R.id.tabRegister);
        lineLogin = findViewById(R.id.indicatorLogin);
        lineRegister = findViewById(R.id.indicatorRegister);

        // Load LoginFragment by default
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new LoginFragment())
                .commit();

        btnLoginTab.setOnClickListener(v -> switchToLogin(true));

        btnRegisterTab.setOnClickListener(v -> {
            btnRegisterTab.setTextColor(getResources().getColor(R.color.brand_purple));
            lineRegister.setBackgroundColor(getResources().getColor(R.color.brand_purple));
            btnLoginTab.setTextColor(getResources().getColor(R.color.text_hint));
            lineLogin.setBackgroundColor(Color.TRANSPARENT);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new RegisterFragment())
                    .commit();
        });
    }

    // Public method to allow RegisterFragment to switch back to LoginFragment
    // We pass 'isYouth' to know which toggle to select automatically
    public void switchToLogin(boolean isYouth) {
        // 1. Update the top tabs UI
        btnLoginTab.setTextColor(getResources().getColor(R.color.brand_purple));
        lineLogin.setBackgroundColor(getResources().getColor(R.color.brand_purple));
        btnRegisterTab.setTextColor(getResources().getColor(R.color.text_hint));
        lineRegister.setBackgroundColor(Color.TRANSPARENT);

        // 2. Pass the data to LoginFragment using a Bundle
        LoginFragment loginFragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putBoolean("IS_YOUTH", isYouth);
        loginFragment.setArguments(args);

        // 3. Switch the fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, loginFragment)
                .commit();
    }
}