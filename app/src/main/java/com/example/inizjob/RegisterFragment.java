package com.example.inizjob;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class RegisterFragment extends Fragment {

    private boolean isYouthSelected = true;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private TextView toggleYouth, toggleBusiness;
    private View inputLayoutRegBusinessCode;
    private Button btnRegisterAction;
    private TextInputEditText etFullName, etEmail, etPhone, etPassword, etBusinessCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference();

        toggleYouth = view.findViewById(R.id.regToggleYouth);
        toggleBusiness = view.findViewById(R.id.regToggleBusiness);
        inputLayoutRegBusinessCode = view.findViewById(R.id.inputLayoutRegBusinessCode);
        btnRegisterAction = view.findViewById(R.id.btnRegisterAction);

        etFullName = view.findViewById(R.id.etFullName);
        etEmail = view.findViewById(R.id.etRegEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etPassword = view.findViewById(R.id.etRegPassword);
        etBusinessCode = view.findViewById(R.id.etRegBusinessCode);

        toggleYouth.setOnClickListener(v -> {
            isYouthSelected = true;
            updateToggleUI();
        });

        toggleBusiness.setOnClickListener(v -> {
            isYouthSelected = false;
            updateToggleUI();
        });

        btnRegisterAction.setOnClickListener(v -> performRegistration());

        return view;
    }

    private void performRegistration() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String businessCode = etBusinessCode.getText().toString().trim();

        String type = isYouthSelected ? "נוער" : "עסק";

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(fullName)) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser == null) return;

                            String userId = firebaseUser.getUid();
                            User newUser = new User(fullName, email, phone, type, businessCode);

                            mDatabase.child("users").child(userId).setValue(newUser)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> dbTask) {
                                            if (dbTask.isSuccessful()) {
                                                Toast.makeText(getContext(), "Registration Successful!", Toast.LENGTH_LONG).show();

                                                // 1. Sign out (because Firebase auto-signs in after register)
                                                mAuth.signOut();

                                                // 2. Switch to Login Fragment via AuthActivity
                                                if (getActivity() instanceof AuthActivity) {
                                                    ((AuthActivity) getActivity()).switchToLogin(isYouthSelected);
                                                }
                                            } else {
                                                Toast.makeText(getContext(), "Saving details failed.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        } else {
                            if (task.getException() != null) {
                                Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    void updateToggleUI() {
        if (getContext() == null) return;
        int purple = ContextCompat.getColor(getContext(), R.color.brand_purple);

        if (isYouthSelected) {
            toggleYouth.setBackgroundResource(R.drawable.bg_gradient_button);
            toggleYouth.setTextColor(Color.WHITE);
            toggleBusiness.setBackgroundColor(Color.TRANSPARENT);
            toggleBusiness.setTextColor(purple);
            inputLayoutRegBusinessCode.setVisibility(View.GONE);
        } else {
            toggleBusiness.setBackgroundResource(R.drawable.bg_gradient_button);
            toggleBusiness.setTextColor(Color.WHITE);
            toggleYouth.setBackgroundColor(Color.TRANSPARENT);
            toggleYouth.setTextColor(purple);
            inputLayoutRegBusinessCode.setVisibility(View.VISIBLE);
        }
    }
}