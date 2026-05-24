package com.example.inizjob;

import android.app.DatePickerDialog;
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
    //input fields for full name, phone, birth date and business code edit
    private TextInputEditText etFullName, etPhone, etBirthDate, etBusinessCode;
    //input layouts for birth date and business code edit
    private TextInputLayout layoutEditBirthDate, layoutEditBusinessCode;
    //UI elements that stores and show the avatar selection
    private ImageView imgSelectBoy, imgSelectGirl;
    //UI element for save button
    private MaterialButton btnSaveProfile;

    // UI element for back button navigation
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
    //load the XML and sets the click listeners for the buttons
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference();

        //If there's user logged in, get it's ID
        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        }

        // Initialize input fields view
        etFullName = view.findViewById(R.id.etEditFullName);
        etPhone = view.findViewById(R.id.etEditPhone);
        etBirthDate = view.findViewById(R.id.etEditBirthDate);
        etBusinessCode = view.findViewById(R.id.etEditBusinessCode);

        // Initialize layout views for birth date and business code
        layoutEditBirthDate = view.findViewById(R.id.layoutEditBirthDate);
        layoutEditBusinessCode = view.findViewById(R.id.layoutEditBusinessCode);

        //Initialize avatar selection views
        imgSelectBoy = view.findViewById(R.id.imgSelectBoy);
        imgSelectGirl = view.findViewById(R.id.imgSelectGirl);

        // Initialize save button and back button views
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnBackEditProfile = view.findViewById(R.id.btnBackEditProfile);

        // Load user existing data
        loadUserData();

        /*Setup Avatar Listeners based on the user's choice
        * update the avatar UI accordingly */
        imgSelectBoy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedAvatar = "boy";
                //update the avatar UI to boy
                updateAvatarUI();
            }
        });

        imgSelectGirl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedAvatar = "girl";
                //update the avatar UI to girl
                updateAvatarUI();
            }
        });

        //Setup listener for birth date input
        etBirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when clicked, perform the date picker method
                showDatePicker();
            }
        });

        //Setup listener for save profile changes button
        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when clicked, perform the save changes method
                saveProfileChanges();
            }
        });

        //Setup listener for back button
        // replace the fragment back to the previous one when clicked
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

    //This method updates the avatar UI based on the user's choice
    private void updateAvatarUI() {
        //Reset backgrounds
        imgSelectBoy.setBackgroundResource(R.drawable.bg_search_bar);
        imgSelectGirl.setBackgroundResource(R.drawable.bg_search_bar);

        // Highlight selection and apply color
        if ("boy".equals(selectedAvatar)) {
            imgSelectBoy.setBackgroundResource(R.drawable.bg_toggle_container);
        } else if ("girl".equals(selectedAvatar)) {
            imgSelectGirl.setBackgroundResource(R.drawable.bg_toggle_container);
        }
    }

    /*This method shows the date picker dialog
    when the birth date input is clicked*/
    private void showDatePicker() {
        if (getContext() == null) return;

        // Initialize the date picker
        Calendar calendar = Calendar.getInstance(); //create a calendar instance
        int currentYear = calendar.get(Calendar.YEAR) - 16; // Set the current year and age limit
        int currentMonth = calendar.get(Calendar.MONTH); // Set the current month
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Set the current day

        // Create and show the date picker dialog, and set the listener
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    // Callback when a date is selected, and update the input field after selection
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                        // Set the selected date to the input field
                        etBirthDate.setText(selectedDate);
                    }
                }, currentYear, currentMonth, currentDay);

        // Set the date range of the years for the date picker
        Calendar minAge = Calendar.getInstance();
        minAge.add(Calendar.YEAR, -18);
        Calendar maxAge = Calendar.getInstance();
        maxAge.add(Calendar.YEAR, -14);

        datePickerDialog.getDatePicker().setMinDate(minAge.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxAge.getTimeInMillis());
        // Show the date picker
        datePickerDialog.show();
    }

    /*This method gets the user data from the database based on his ID
    * and updates the UI accordingly to show the current user's data
    * before the user edits it */
    private void loadUserData() {
        if (userId == null) return;

        // Fetch user data from the database and create listener to update UI
        mDatabase.child("users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!isAdded() || getActivity() == null) return;

                // If task is successful, get the data and update the UI
                if (task.isSuccessful() && task.getResult() != null) {
                    User user = task.getResult().getValue(User.class);
                    if (user != null) {
                        etFullName.setText(user.fullName);
                        etPhone.setText(user.phone);
                        currentUserType = user.type;

                        //gets the avatar type from the user and sets it
                        if (user.avatarType != null) {
                            selectedAvatar = user.avatarType;
                        }
                        //update the avatar UI based on the user's choice
                        updateAvatarUI();

                        /* Checks if UI should show and populate birth date
                        * If the user is youth, show the birth date input and hide the business code input
                        * else, show the business code input and hide the birth date input */
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

    /*This method saves the user's changes to the database*/
    private void saveProfileChanges() {
        //gets full name and phone from the input fields
        String newName = etFullName.getText().toString().trim();
        String newPhone = etPhone.getText().toString().trim();

        //Checks If the fields are empty, If so, show a toast
        if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newPhone)) {
            Toast.makeText(getContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        /*Checks if the phone number is valid
        using the InputValidator class, If not, show a toast */
        if (!InputValidator.isValidPhone(newPhone)) {
            Toast.makeText(getContext(), "Invalid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        //updates the user's data trough a HashMap
        //using String as the key and "Object" as the new value
        Map<String, Object> updates = new HashMap<>(); //create a hashmap to hold the updates
        //add the updates (fullName, phone, avatarType) to the hashmap
        updates.put("fullName", newName);
        updates.put("phone", newPhone);
        updates.put("avatarType", selectedAvatar);

        //If user is Youth, update birthDate
        if (User.TYPE_YOUTH.equals(currentUserType)) {
            String newBirthDate = etBirthDate.getText().toString().trim();
            //Checks If the birth date is empty, If so, show a toast
            if (TextUtils.isEmpty(newBirthDate)) {
                Toast.makeText(getContext(), "Please select a date of birth", Toast.LENGTH_SHORT).show();
                return;
            }
            //add the updates (birthDate) to the hashmap
            updates.put("birthDate", newBirthDate);

        } else if (User.TYPE_BUSINESS.equals(currentUserType)) {
            String newBusinessCode = etBusinessCode.getText().toString().trim();
            //Checks If the business code is empty, If so, show a toast
            if (TextUtils.isEmpty(newBusinessCode)) {
                Toast.makeText(getContext(), "Please enter a business code.", Toast.LENGTH_SHORT).show();
                return;
            }
            //add the updates (businessCode) to the hashmap
            updates.put("businessCode", newBusinessCode);
        }

        /*updates the user's data in the database using
        updateChildern to change only what's in the HashMap
        and avoids memory leaks*/
        mDatabase.child("users").child(userId).updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            //Callback when data is updated
            public void onComplete(@NonNull Task<Void> task) {
                if (!isAdded() || getActivity() == null) return;

                //If task is successful, show a toast and replace the fragment
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    getActivity().getSupportFragmentManager().popBackStack();
                } else {
                    //show a toast mentioning there's an error
                    Toast.makeText(getContext(), "Error updating profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}