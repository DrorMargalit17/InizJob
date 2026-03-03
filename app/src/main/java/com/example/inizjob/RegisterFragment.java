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

// ייבוא מחלקות Firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

// ייבוא מחלקות לניהול משימות
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class RegisterFragment extends Fragment {

    // משתנים ללוגיקה
    private boolean isYouthSelected = true;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // רכיבי UI
    private TextView toggleYouth, toggleBusiness;
    private View inputLayoutRegBusinessCode;
    private Button btnRegisterAction;

    // שדות הקלט
    private TextInputEditText etFullName, etEmail, etPhone, etPassword, etBusinessCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // 1. אתחול Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // אתחול Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // 2. חיבור לרכיבים ב-XML
        toggleYouth = view.findViewById(R.id.regToggleYouth);
        toggleBusiness = view.findViewById(R.id.regToggleBusiness);
        inputLayoutRegBusinessCode = view.findViewById(R.id.inputLayoutRegBusinessCode);
        btnRegisterAction = view.findViewById(R.id.btnRegisterAction);

        // חיבור שדות הטקסט לפי ה-ID שנתת ב-XML
        etFullName = view.findViewById(R.id.etFullName);
        etEmail = view.findViewById(R.id.etRegEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etPassword = view.findViewById(R.id.etRegPassword);
        etBusinessCode = view.findViewById(R.id.etRegBusinessCode);

        // 3. הגדרת מאזינים (Listeners)
        toggleYouth.setOnClickListener(v -> {
            isYouthSelected = true;
            updateToggleUI();
        });

        toggleBusiness.setOnClickListener(v -> {
            isYouthSelected = false;
            updateToggleUI();
        });

        btnRegisterAction.setOnClickListener(v -> {
            performRegistration();
        });

        return view;
    }

    private void performRegistration() {
        // 1. קבלת הנתונים מהשדות
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String businessCode = etBusinessCode.getText().toString().trim();

        // קביעת הסוג לפי הבחירה של המשתמש
        String type = isYouthSelected ? "נוער" : "עסק";

        // בדיקות תקינות
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(fullName)) {
            Toast.makeText(getContext(), "נא למלא את כל שדות החובה", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. יצירת המשתמש ב-Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // --- שמירת פרטים במסד הנתונים ---

                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            String userId = firebaseUser.getUid();

                            // יצירת אובייקט User
                            User newUser = new User(fullName, email, phone, type, businessCode);

                            // שמירה ב-Realtime Database
                            mDatabase.child("users").child(userId).setValue(newUser)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> dbTask) {
                                            if (dbTask.isSuccessful()) {
                                                Toast.makeText(getContext(), "נרשמת בהצלחה!", Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(getContext(), "הרישום הצליח אך שמירת הפרטים נכשלה", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        } else {
                            if (task.getException() != null) {
                                Toast.makeText(getContext(), "שגיאה ברישום: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
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