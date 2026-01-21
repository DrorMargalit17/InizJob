package com.example.inizjob;

import android.graphics.Color;
import android.os.Bundle;
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

public class LoginFragment extends Fragment {

    // משתנה לבדיקה אם נבחר נוער
    boolean isYouthSelected = true;

    TextView toggleYouth, toggleBusiness;
    // שים לב: אנחנו שולטים ב-View החיצוני כדי להעלים את כל המסגרת
    View inputLayoutBusinessCode;
    Button btnLoginAction;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // חיבור רכיבים
        toggleYouth = view.findViewById(R.id.toggleYouth);
        toggleBusiness = view.findViewById(R.id.toggleBusiness);
        inputLayoutBusinessCode = view.findViewById(R.id.inputLayoutBusinessCode);
        btnLoginAction = view.findViewById(R.id.btnLoginAction);

        // הגדרת לחיצות
        toggleYouth.setOnClickListener(v -> {
            isYouthSelected = true;
            updateToggleUI();
        });

        toggleBusiness.setOnClickListener(v -> {
            isYouthSelected = false;
            updateToggleUI();
        });

        btnLoginAction.setOnClickListener(v -> {
            String type = isYouthSelected ? "נוער" : "עסק";
            Toast.makeText(getContext(), "מתחבר כ: " + type, Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    void updateToggleUI() {
        if (getContext() == null) return;

        if (isYouthSelected) {
            // נוער נבחר
            toggleYouth.setBackgroundResource(R.drawable.bg_gradient_button);
            toggleYouth.setTextColor(Color.WHITE);
            toggleBusiness.setBackgroundColor(Color.TRANSPARENT);
            toggleBusiness.setTextColor(ContextCompat.getColor(getContext(), R.color.brand_purple));

            // הסתרת קוד עסק
            inputLayoutBusinessCode.setVisibility(View.GONE);
        } else {
            // עסק נבחר
            toggleBusiness.setBackgroundResource(R.drawable.bg_gradient_button);
            toggleBusiness.setTextColor(Color.WHITE);
            toggleYouth.setBackgroundColor(Color.TRANSPARENT);
            toggleYouth.setTextColor(ContextCompat.getColor(getContext(), R.color.brand_purple));

            // הצגת קוד עסק
            inputLayoutBusinessCode.setVisibility(View.VISIBLE);
        }
    }
}