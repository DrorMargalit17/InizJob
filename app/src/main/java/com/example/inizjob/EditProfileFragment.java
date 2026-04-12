package com.example.inizjob;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class EditProfileFragment extends Fragment {

    private TextInputEditText etFullName, etPhone; // UI elements for input fields
    private LinearLayout layoutEditBirthDate; // UI element for birth date
    private Spinner spinnerDay, spinnerMonth, spinnerYear; // UI elements for date selection
    private MaterialButton btnSaveProfile; // UI element for save button

    private FirebaseAuth mAuth; // Firebase Authentication instance
    private DatabaseReference mDatabase; // Firebase Realtime Database instance
    private String userId; // User's Firebase ID
    private String currentUserType = ""; // checks if user is youth or business

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
        spinnerDay = view.findViewById(R.id.spinnerEditDay);
        spinnerMonth = view.findViewById(R.id.spinnerEditMonth);
        spinnerYear = view.findViewById(R.id.spinnerEditYear);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);


        populateSpinners();
        // Load user existing data
        loadUserData();

        //save changes
        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileChanges();
            }
        });

        return view;
    }


    // Creates the dropdown lists for the birth date selection
    private void populateSpinners() {
        if (getContext() == null) return;

        // Days 1-31
        ArrayList<String> days = new ArrayList<>();
        days.add("יום");
        for (int i = 1; i <= 31; i++) { days.add(String.valueOf(i)); }
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, days);
        spinnerDay.setAdapter(dayAdapter);

        // Months 1-12
        ArrayList<String> months = new ArrayList<>();
        months.add("חודש");
        for (int i = 1; i <= 12; i++) { months.add(String.valueOf(i)); }
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, months);
        spinnerMonth.setAdapter(monthAdapter);

        // Years 2026-2000
        ArrayList<String> years = new ArrayList<>();
        years.add("שנה");
        for (int i = 2026; i >= 2000; i--) { years.add(String.valueOf(i)); }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, years);
        spinnerYear.setAdapter(yearAdapter);
    }

    //gets user current data, and load it to the UI
    private void loadUserData() {
        if (userId == null) return;

        mDatabase.child("users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    User user = task.getResult().getValue(User.class);
                    if (user != null) {
                        etFullName.setText(user.fullName);
                        etPhone.setText(user.phone);
                        currentUserType = user.type;

                        // Checks if UI should show and populate birth date
                        if ("נוער".equals(currentUserType)) {
                            layoutEditBirthDate.setVisibility(View.VISIBLE);

                            // If there's birth date, populate Spinners
                            if (user.birthDate != null && user.birthDate.contains("/")) {
                                String[] parts = user.birthDate.split("/");
                                if (parts.length == 3) {
                                    setSpinnerValue(spinnerDay, parts[0]);
                                    setSpinnerValue(spinnerMonth, parts[1]);
                                    setSpinnerValue(spinnerYear, parts[2]);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    // Sets the selected value of a Spinner
    private void setSpinnerValue(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    //gets the new input data and saves it to the database
    private void saveProfileChanges() {
        String newName = etFullName.getText().toString().trim();
        String newPhone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newPhone)) {
            Toast.makeText(getContext(), "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        //updates the user's data trough a HashMap
        //using String as the key and "Object" as the new value
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", newName);
        updates.put("phone", newPhone);

        // If user is Youth, update birthDate
        if ("נוער".equals(currentUserType)) {
            if (spinnerDay.getSelectedItemPosition() == 0 ||
                    spinnerMonth.getSelectedItemPosition() == 0 ||
                    spinnerYear.getSelectedItemPosition() == 0) {
                Toast.makeText(getContext(), "אנא בחר תאריך לידה מלא", Toast.LENGTH_SHORT).show();
                return;
            }

            String newBirthDate = spinnerDay.getSelectedItem().toString() + "/" +
                    spinnerMonth.getSelectedItem().toString() + "/" +
                    spinnerYear.getSelectedItem().toString();
            updates.put("birthDate", newBirthDate);
        }

        /*update using updateChildern to change only
        what's in the HashMap and avoids memory leaks*/
        mDatabase.child("users").child(userId).updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "הפרופיל עודכן בהצלחה", Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) {
                        //return to profile
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                } else {
                    Toast.makeText(getContext(), "עדכון נכשל", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}