package com.example.inizjob;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/*
 * Class: EditProfileFragment
 * Purpose: Allows the user to edit their profile information (Name, Phone, Business Code).
 * * Methods and Actions List:
 * 1. onCreateView - Inflates the edit profile layout.
 * 2. onViewCreated - Initializes UI components, sets click listeners without lambdas, and triggers data fetching.
 * 3. loadCurrentUserData - Fetches the user's current data from Firebase to pre-fill the form fields securely.
 * 4. saveProfileData - Validates inputs and explicitly updates specific fields in the Firebase database using Maps.
 */
public class EditProfileFragment extends Fragment {

    private TextInputEditText etEditName, etEditEmail, etEditPhone, etEditBusinessCode;
    private TextInputLayout layoutEditBusinessCode;
    private MaterialButton btnSaveProfile;
    private ImageButton btnBackEditProfile;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private String currentUserType = "";

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference();

        etEditName = view.findViewById(R.id.etEditName);
        etEditEmail = view.findViewById(R.id.etEditEmail);
        etEditPhone = view.findViewById(R.id.etEditPhone);
        etEditBusinessCode = view.findViewById(R.id.etEditBusinessCode);
        layoutEditBusinessCode = view.findViewById(R.id.layoutEditBusinessCode);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnBackEditProfile = view.findViewById(R.id.btnBackEditProfile);

        btnBackEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileData();
            }
        });

        loadCurrentUserData();
    }

    private void loadCurrentUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        String uid = currentUser.getUid();
        mDatabase.child("users").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        if (task.getResult().exists()) {
                            User userProfile = task.getResult().getValue(User.class);
                            if (userProfile != null) {
                                etEditName.setText(userProfile.fullName);
                                etEditEmail.setText(userProfile.email);
                                etEditPhone.setText(userProfile.phone);

                                currentUserType = userProfile.type;

                                if ("עסק".equals(currentUserType)) {
                                    layoutEditBusinessCode.setVisibility(View.VISIBLE);
                                    if (userProfile.businessCode != null) {
                                        etEditBusinessCode.setText(userProfile.businessCode);
                                    }
                                } else {
                                    layoutEditBusinessCode.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error loading user data", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void saveProfileData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        String newName = etEditName.getText().toString().trim();
        String newPhone = etEditPhone.getText().toString().trim();
        String newBusinessCode = etEditBusinessCode.getText().toString().trim();

        if (TextUtils.isEmpty(newName)) {
            Toast.makeText(getContext(), "Please fill in your name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(newPhone)) {
            Toast.makeText(getContext(), "Please fill in your phone", Toast.LENGTH_SHORT).show();
            return;
        }

        // Using a Map ensures we only update specific fields and do not overwrite unrelated data
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", newName);
        updates.put("phone", newPhone);

        if ("עסק".equals(currentUserType)) {
            updates.put("businessCode", newBusinessCode);
        }

        btnSaveProfile.setEnabled(false);

        String uid = currentUser.getUid();
        mDatabase.child("users").child(uid).updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (getActivity() == null) {
                    return;
                }

                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    getActivity().getSupportFragmentManager().popBackStack();
                } else {
                    btnSaveProfile.setEnabled(true);
                    Toast.makeText(getContext(), "Error saving profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}