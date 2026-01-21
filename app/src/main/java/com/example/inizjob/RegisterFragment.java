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

public class RegisterFragment extends Fragment {

    boolean isYouthSelected = true;

    TextView toggleYouth, toggleBusiness;
    View inputLayoutRegBusinessCode;
    Button btnRegisterAction;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        toggleYouth = view.findViewById(R.id.regToggleYouth);
        toggleBusiness = view.findViewById(R.id.regToggleBusiness);
        inputLayoutRegBusinessCode = view.findViewById(R.id.inputLayoutRegBusinessCode);
        btnRegisterAction = view.findViewById(R.id.btnRegisterAction);

        toggleYouth.setOnClickListener(v -> {
            isYouthSelected = true;
            updateToggleUI();
        });

        toggleBusiness.setOnClickListener(v -> {
            isYouthSelected = false;
            updateToggleUI();
        });

        btnRegisterAction.setOnClickListener(v -> {
            String type = isYouthSelected ? "נוער" : "עסק";
            Toast.makeText(getContext(), "נרשם כ: " + type, Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    void updateToggleUI() {
        if (getContext() == null) return;

        if (isYouthSelected) {
            toggleYouth.setBackgroundResource(R.drawable.bg_gradient_button);
            toggleYouth.setTextColor(Color.WHITE);
            toggleBusiness.setBackgroundColor(Color.TRANSPARENT);
            toggleBusiness.setTextColor(ContextCompat.getColor(getContext(), R.color.brand_purple));

            inputLayoutRegBusinessCode.setVisibility(View.GONE);
        } else {
            toggleBusiness.setBackgroundResource(R.drawable.bg_gradient_button);
            toggleBusiness.setTextColor(Color.WHITE);
            toggleYouth.setBackgroundColor(Color.TRANSPARENT);
            toggleYouth.setTextColor(ContextCompat.getColor(getContext(), R.color.brand_purple));

            inputLayoutRegBusinessCode.setVisibility(View.VISIBLE);
        }
    }
}