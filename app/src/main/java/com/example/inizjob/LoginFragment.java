package com.example.inizjob;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
// וודא שהייבוא הזה קיים, אחרת TextInputEditText יהיה אדום
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginFragment extends Fragment {

    private boolean isYouthSelected = true;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private TextView toggleYouth, toggleBusiness;
    private View inputLayoutBusinessCode;
    private Button btnLoginAction;
    private TextInputEditText etEmail, etPassword;

    public LoginFragment() {
        // בנאי ריק חובה
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        toggleYouth = view.findViewById(R.id.toggleYouth);
        toggleBusiness = view.findViewById(R.id.toggleBusiness);
        inputLayoutBusinessCode = view.findViewById(R.id.inputLayoutBusinessCode);
        btnLoginAction = view.findViewById(R.id.btnLoginAction);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);

        toggleYouth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isYouthSelected = true;
                updateToggleUI();
            }
        });

        toggleBusiness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isYouthSelected = false;
                updateToggleUI();
            }
        });

        btnLoginAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        return view;
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        Log.d("LOGIN_DEBUG", "נסיו התחברות עם: " + email); // בדיקה שהלחיצה עובדת

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "נא להזין אימייל וסיסמה", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("LOGIN_DEBUG", "התחברות הצליחה ב-Auth!");
                            checkUserType();
                        } else {
                            Log.e("LOGIN_DEBUG", "שגיאת Auth: " + task.getException().getMessage());
                            Toast.makeText(getContext(), "שגיאה: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void checkUserType() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        mDatabase.child("users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                // בדיקה שהפרגמנט עדיין קיים כדי למנוע קריסה
                if (!isAdded() || getActivity() == null) return;

                if (task.isSuccessful() && task.getResult() != null) {
                    DataSnapshot snapshot = task.getResult();

                    if (snapshot.exists()) {
                        try {
                            // שליפה ישירה של השדה 'type' - עוקף את הצורך במחלקת User
                            String userType = snapshot.child("type").getValue(String.class);

                            if (userType != null) {
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                intent.putExtra("USER_TYPE", userType);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                getActivity().finish();
                            } else {
                                Toast.makeText(getContext(), "סוג משתמש לא מוגדר בדאטה-בייס", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e("FirebaseError", "Error parsing: " + e.getMessage());
                        }
                    } else {
                        Toast.makeText(getContext(), "מידע משתמש חסר בדאטה-בייס", Toast.LENGTH_SHORT).show();
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
            inputLayoutBusinessCode.setVisibility(View.GONE);
        } else {
            toggleBusiness.setBackgroundResource(R.drawable.bg_gradient_button);
            toggleBusiness.setTextColor(Color.WHITE);
            toggleYouth.setBackgroundColor(Color.TRANSPARENT);
            toggleYouth.setTextColor(purple);
            inputLayoutBusinessCode.setVisibility(View.VISIBLE);
        }
    }
}