package com.example.inizjob;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private TextInputEditText etFullName, etPhone, etBirthDate, etBusinessCode; // UI elements for input fields
    private TextInputLayout layoutEditBirthDate, layoutEditBusinessCode;
    private ImageView imgSelectBoy, imgSelectGirl;
    private MaterialButton btnSaveProfile; // UI element for save button

    // UI element for back navigation
    private ImageButton btnBackEditProfile;

    private FirebaseAuth mAuth; // Firebase Authentication instance
    private DatabaseReference mDatabase; // Firebase Realtime Database instance
    private String userId; // User's Firebase ID
    private String currentUserType = ""; // checks if user is youth or business
    private String selectedAvatar = "default"; // default avatar

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference();

        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        }

        // Initialize Views
        etFullName = view.findViewById(R.id.etEditFullName);
        etPhone = view.findViewById(R.id.etEditPhone);

        layoutEditBirthDate = view.findViewById(R.id.layoutEditBirthDate);
        etBirthDate = view.findViewById(R.id.etEditBirthDate);

        layoutEditBusinessCode = view.findViewById(R.id.layoutEditBusinessCode);
        etBusinessCode = view.findViewById(R.id.etEditBusinessCode);

        imgSelectBoy = view.findViewById(R.id.imgSelectBoy);
        imgSelectGirl = view.findViewById(R.id.imgSelectGirl);

        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnBackEditProfile = view.findViewById(R.id.btnBackEditProfile);

        // Load user existing data
        loadUserData();

        // Setup Avatar Listeners
        imgSelectBoy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedAvatar = "boy";
                updateAvatarUI();
            }
        });

        imgSelectGirl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedAvatar = "girl";
                updateAvatarUI();
            }
        });

        // Setup DatePicker
        etBirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        //save changes
        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileChanges();
            }
        });

        // Handle back button click
        btnBackEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        return view;
    }

    private void updateAvatarUI() {
        // Reset backgrounds
        imgSelectBoy.setBackgroundResource(R.drawable.bg_search_bar);
        imgSelectGirl.setBackgroundResource(R.drawable.bg_search_bar);

        // Highlight selection
        if ("boy".equals(selectedAvatar)) {
            imgSelectBoy.setBackgroundResource(R.drawable.bg_toggle_container);
        } else if ("girl".equals(selectedAvatar)) {
            imgSelectGirl.setBackgroundResource(R.drawable.bg_toggle_container);
        }
    }

    private void showDatePicker() {
        if (getContext() == null) return;

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR) - 16;
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                        etBirthDate.setText(selectedDate);
                    }
                }, currentYear, currentMonth, currentDay);

        Calendar minAge = Calendar.getInstance();
        minAge.add(Calendar.YEAR, -18);
        Calendar maxAge = Calendar.getInstance();
        maxAge.add(Calendar.YEAR, -14);

        datePickerDialog.getDatePicker().setMinDate(minAge.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxAge.getTimeInMillis());
        datePickerDialog.show();
    }

    //gets user current data, and load it to the UI
    private void loadUserData() {
        if (userId == null) return;

        mDatabase.child("users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!isAdded() || getActivity() == null) return;

                if (task.isSuccessful() && task.getResult() != null) {
                    User user = task.getResult().getValue(User.class);
                    if (user != null) {
                        etFullName.setText(user.fullName);
                        etPhone.setText(user.phone);
                        currentUserType = user.type;

                        // Handle Avatar selection gracefully
                        if (user.avatarType != null) {
                            selectedAvatar = user.avatarType;
                        }
                        updateAvatarUI();

                        // Checks if UI should show and populate birth date
                        if (User.TYPE_YOUTH.equals(currentUserType)) {
                            layoutEditBirthDate.setVisibility(View.VISIBLE);
                            if (user.birthDate != null) {
                                etBirthDate.setText(user.birthDate);
                            }
                        } else if (User.TYPE_BUSINESS.equals(currentUserType)) {
                            layoutEditBusinessCode.setVisibility(View.VISIBLE);
                            if (user.businessCode != null) {
                                etBusinessCode.setText(user.businessCode);
                            }
                        }
                    }
                }
            }
        });
    }

    //gets the new input data and saves it to the database
    private void saveProfileChanges() {
        String newName = etFullName.getText().toString().trim();
        String newPhone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newPhone)) {
            Toast.makeText(getContext(), "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate using InputValidator
        if (!InputValidator.isValidPhone(newPhone)) {
            Toast.makeText(getContext(), "מספר הטלפון לא חוקי", Toast.LENGTH_SHORT).show();
            return;
        }

        //updates the user's data trough a HashMap
        //using String as the key and "Object" as the new value
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", newName);
        updates.put("phone", newPhone);
        updates.put("avatarType", selectedAvatar);

        // If user is Youth, update birthDate
        if (User.TYPE_YOUTH.equals(currentUserType)) {
            String newBirthDate = etBirthDate.getText().toString().trim();
            if (TextUtils.isEmpty(newBirthDate)) {
                Toast.makeText(getContext(), "אנא בחר תאריך לידה", Toast.LENGTH_SHORT).show();
                return;
            }
            updates.put("birthDate", newBirthDate);

        } else if (User.TYPE_BUSINESS.equals(currentUserType)) {
            String newBusinessCode = etBusinessCode.getText().toString().trim();
            if (TextUtils.isEmpty(newBusinessCode)) {
                Toast.makeText(getContext(), "אנא הזן קוד עסק", Toast.LENGTH_SHORT).show();
                return;
            }
            updates.put("businessCode", newBusinessCode);
        }

        /*update using updateChildern to change only
        what's in the HashMap and avoids memory leaks*/
        mDatabase.child("users").child(userId).updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!isAdded() || getActivity() == null) return;

                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "הפרופיל עודכן בהצלחה", Toast.LENGTH_SHORT).show();
                    getActivity().getSupportFragmentManager().popBackStack();
                } else {
                    Toast.makeText(getContext(), "עדכון נכשל", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}