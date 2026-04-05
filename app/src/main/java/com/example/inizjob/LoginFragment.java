package com.example.inizjob;

import android.content.Intent;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/*
 * Class: LoginFragment
 * Purpose: Handles the user login process.
 * * Methods and Actions List:
 * 1. onCreate - Extracts arguments passed from other fragments.
 * 2. onCreateView - Inflates layout and initializes UI components.
 * 3. performLogin - Validates input and logs the user in via Firebase Authentication.
 * 4. checkUserType - Checks the Realtime Database to determine if the user is a Youth or a Business.
 * 5. updateToggleUI - Updates the visual toggle buttons based on the selected user type.
 */
public class LoginFragment extends Fragment {

    private boolean isYouthSelected = true;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private TextView toggleYouth, toggleBusiness;
    private View inputLayoutBusinessCode;
    private Button btnLoginAction;
    private TextInputEditText etEmail, etPassword;

    public LoginFragment() {
        // Required empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isYouthSelected = getArguments().getBoolean("IS_YOUTH", true);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference();

        toggleYouth = view.findViewById(R.id.toggleYouth);
        toggleBusiness = view.findViewById(R.id.toggleBusiness);
        inputLayoutBusinessCode = view.findViewById(R.id.inputLayoutBusinessCode);
        btnLoginAction = view.findViewById(R.id.btnLoginAction);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);

        updateToggleUI();

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

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            checkUserType();
                        } else {
                            if (task.getException() != null) {
                                Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void checkUserType() {
        if (mAuth.getCurrentUser() == null) {
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        mDatabase.child("users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!isAdded() || getActivity() == null) {
                    return;
                }

                if (task.isSuccessful() && task.getResult() != null) {
                    DataSnapshot snapshot = task.getResult();

                    if (snapshot.exists()) {
                        String userType = snapshot.child("type").getValue(String.class);

                        if (userType != null) {
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.putExtra("USER_TYPE", userType);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            getActivity().finish();
                        } else {
                            Toast.makeText(getContext(), "User type not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "User data missing", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    void updateToggleUI() {
        if (getContext() == null) {
            return;
        }
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