package com.example.inizjob;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
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

/*
 * Class: AddCvFragment
 * * Methods and Actions:
 * 1. onViewCreated - Connects UI and sets up listeners.
 * 2. setupDraftLogic - Monitors text changes and saves drafts to Firebase.
 * 3. loadDraftIfAvailable - Loads unfinished work from the "cv_drafts" node.
 * 4. prepareAiRequest - Gathers all inputs and calls GeminiAiManager.
 * 5. handleBack - Safely returns to the previous screen or previous page.
 */
public class AddCvFragment extends Fragment {

    // UI elements for the two pages of the form
    private LinearLayout cvPage1, cvPage2;

    //Title for the cv creation form
    private TextView tvCvFormTitle;

    // UI elements for the form fields
    private TextInputEditText etCvTitle, etCvSummary, etCvEducation, etCvExperience, etCvSkills,
            etCvAchievements, etCvTraits, etCvUnique, etGeneratedCvResult;

    //UI elements for the buttons
    private MaterialButton btnGenerateAndSaveCV, btnSaveFinalCv, btnSkipToResult, btnBackToForm;

    // UI element for the back button
    private ImageButton btnBackCv;

    // Firebase references and managers
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // AI manager for text generation
    private GeminiAiManager aiManager;

    //User's info for the cv
    private String userFullName = "", userEmail = "", userPhone = "";

    //gets cv to edit if sent
    private Cv cvToEdit = null;

    @Override
    //Initialize the fragment with the selected cv if it been sent to edit by the user (EDIT_CV = key)
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey("EDIT_CV")) {
            cvToEdit = (Cv) getArguments().getSerializable("EDIT_CV");
        }
    }

    @Nullable
    @Override
    // Inflate the layout and set up the UI components and set up listeners
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_cv, container, false);
    }

    @Override
    // Set up the UI components and set up listeners
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase and AI manager
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference();
        aiManager = new GeminiAiManager();

        // Initialize UI elements related to the form
        cvPage1 = view.findViewById(R.id.cvPage1);
        cvPage2 = view.findViewById(R.id.cvPage2);
        tvCvFormTitle = view.findViewById(R.id.tvCvFormTitle);

        // Initialize UI elements related to the form fields
        etCvTitle = view.findViewById(R.id.etCvTitle);
        etCvSummary = view.findViewById(R.id.etCvSummary);
        etCvEducation = view.findViewById(R.id.etCvEducation);
        etCvExperience = view.findViewById(R.id.etCvExperience);
        etCvSkills = view.findViewById(R.id.etCvSkills);
        etCvAchievements = view.findViewById(R.id.etCvAchievements);
        etCvTraits = view.findViewById(R.id.etCvTraits);
        etCvUnique = view.findViewById(R.id.etCvUnique);

        // Initialize UI elements related to the buttons
        btnGenerateAndSaveCV = view.findViewById(R.id.btnGenerateAndSaveCV);
        btnSkipToResult = view.findViewById(R.id.btnSkipToResult);
        btnSaveFinalCv = view.findViewById(R.id.btnSaveFinalCv);
        btnBackToForm = view.findViewById(R.id.btnBackToForm);
        btnBackCv = view.findViewById(R.id.btnBackCv);

        // Initialize UI elements related to the generated text
        etGeneratedCvResult = view.findViewById(R.id.etGeneratedCvResult);


        // set up listener for the back button
        btnBackCv.setOnClickListener(new View.OnClickListener() {
            @Override
            // When the back button is clicked, go back to the previous fragment
            public void onClick(View v) {
                if (cvPage2.getVisibility() == View.VISIBLE) {
                    goBackToPage1();
                } else {
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                }
            }
        });

        // Bottom "Back to Form" button (Page 2)
        btnBackToForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBackToPage1();
            }
        });

        //set up listener for the skip to result button (visible only if editing)
        btnSkipToResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPage2();
            }
        });

        /*If there's a cv to edit, populate the fields with the current data,
        else fetch user data and load draft if available */
        if (cvToEdit != null) {
            populateFields(cvToEdit);
        } else {
            // Fetch user data
            fetchUserData();
            // Load any unfinished work from the "cv_drafts" node if available
            loadDraftIfAvailable();
        }

        // Set up draft logic - attach listeners to the form fields to monitor changes
        setupDraftLogic();

        //set up listener for the generate and save button
        btnGenerateAndSaveCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send a request to the Gemini AI API
                prepareAiRequest();
            }
        });

        //set up listener for the save button
        btnSaveFinalCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the title from the title field and validate it
                String title = etCvTitle.getText().toString().trim();
                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(getContext(), "Please go back and enter a title for your cv.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create a new Cv object with the user's inputs
                Cv currentData = new Cv();
                // Set the Cv object fields
                currentData.cvTitle = title;
                currentData.summary = etCvSummary.getText().toString().trim();
                currentData.education = etCvEducation.getText().toString().trim();
                currentData.experience = etCvExperience.getText().toString().trim();
                currentData.skills = etCvSkills.getText().toString().trim();
                currentData.achievements = etCvAchievements.getText().toString().trim();
                currentData.traits = etCvTraits.getText().toString().trim();
                currentData.uniqueDetail = etCvUnique.getText().toString().trim();

                // Get the generated text from the text field and validate it
                String finalAiText = etGeneratedCvResult.getText().toString().trim();
                if (TextUtils.isEmpty(finalAiText)) {
                    Toast.makeText(getContext(), "The text is empty, please create a resume first.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Save the final CV to Firebase
                saveToFirebase(currentData, finalAiText);
            }
        });
    }

    /**
     * Switches the UI view to display Page 2 (The AI-generated result screen).
     */
    private void goToPage2() {
        cvPage1.setVisibility(View.GONE);
        cvPage2.setVisibility(View.VISIBLE);
        tvCvFormTitle.setText("עריכת קורות החיים");
    }

    /**
     * Switches the UI view back to Page 1 (The raw input form).
     */
    private void goBackToPage1() {
        cvPage2.setVisibility(View.GONE);
        cvPage1.setVisibility(View.VISIBLE);
        tvCvFormTitle.setText("בניית קורות חיים מקצועיים");
    }

    /**
     * Populates all form fields with the data from the provided CV object.
     * Instantly navigates the user to Page 2 to review the existing generated text.
     * @param cv The existing CV object passed for editing.
     */
    private void populateFields(Cv cv) {
        etCvTitle.setText(cv.cvTitle);
        etCvSummary.setText(cv.summary);
        etCvEducation.setText(cv.education);
        etCvExperience.setText(cv.experience);
        etCvSkills.setText(cv.skills);
        etCvAchievements.setText(cv.achievements);
        etCvTraits.setText(cv.traits);
        etCvUnique.setText(cv.uniqueDetail);

        etGeneratedCvResult.setText(cv.generatedText);
        //update the button text if a cv is being edited
        btnGenerateAndSaveCV.setText("עדכון ויצירה מחדש");

        // Setup editing environment
        if (cv.generatedText != null && !cv.generatedText.isEmpty()) {
            btnSkipToResult.setVisibility(View.VISIBLE);
            // Automatically jump to Page 2 when opening an existing CV to edit
            goToPage2();
        }

        // Set the user data
        userFullName = cv.fullName;
        userPhone = cv.phone;
        userEmail = cv.email;
    }

    /**
     * Attaches a TextWatcher to all input fields.
     * Every time the user types, it automatically triggers a draft save.
     */
    private void setupDraftLogic() {
        // Listen for changes in the form fields
        TextWatcher draftWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                // Save the current draft
                saveCurrentAsDraft();
            }
        };
        // Add the listeners to every of the form fields to monitor changes
        etCvTitle.addTextChangedListener(draftWatcher);
        etCvSummary.addTextChangedListener(draftWatcher);
        etCvEducation.addTextChangedListener(draftWatcher);
        etCvExperience.addTextChangedListener(draftWatcher);
        etCvSkills.addTextChangedListener(draftWatcher);
        etCvAchievements.addTextChangedListener(draftWatcher);
        etCvTraits.addTextChangedListener(draftWatcher);
        etCvUnique.addTextChangedListener(draftWatcher);
    }

    /**
     * Saves the current text from all input fields to a specific "cv_drafts" node in Firebase.
     * Ensures that if the app crashes or the user leaves, their progress isn't lost.
     * This logic is skipped if the user is editing an already saved CV.
     */
    private void saveCurrentAsDraft() {
        // Check if the user is logged in and if there's a draft to save
        if (mAuth.getCurrentUser() == null || cvToEdit != null) return;
        // Get the user's UID
        String uid = mAuth.getCurrentUser().getUid();

        // Create a new draft object
        Cv draft = new Cv();
        //Set the fields of the draft object
        draft.cvTitle = etCvTitle.getText().toString();
        draft.summary = etCvSummary.getText().toString();
        draft.education = etCvEducation.getText().toString();
        draft.experience = etCvExperience.getText().toString();
        draft.skills = etCvSkills.getText().toString();
        draft.achievements = etCvAchievements.getText().toString();
        draft.traits = etCvTraits.getText().toString();
        draft.uniqueDetail = etCvUnique.getText().toString();

        // Save the draft to the "cv_drafts" node in Firebase
        mDatabase.child("cv_drafts").child(uid).setValue(draft);
    }

    /**
     * Checks Firebase on startup for any unfinished CV drafts associated with the user.
     * If found, it populates the form so the user can continue where they left off.
     */
    private void loadDraftIfAvailable() {
        if (mAuth.getCurrentUser() == null) return;
        // Get the user's UID
        String uid = mAuth.getCurrentUser().getUid();

        // Fetch the draft from the "cv_drafts" node in Firebase (if available) and set up a listener
        mDatabase.child("cv_drafts").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            // Callback when the draft is fetched from Firebase
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                // Check if the fragment is still active to prevent memory leaks
                if (!isAdded() || getActivity() == null) return;

                // If the draft exists, populate the form fields with its current data
                if (task.isSuccessful() && task.getResult().exists()) {
                    Cv draft = task.getResult().getValue(Cv.class);
                    if (draft != null) {
                        etCvTitle.setText(draft.cvTitle);
                        etCvSummary.setText(draft.summary);
                        etCvEducation.setText(draft.education);
                        etCvExperience.setText(draft.experience);
                        etCvSkills.setText(draft.skills);
                        etCvAchievements.setText(draft.achievements);
                        etCvTraits.setText(draft.traits);
                        etCvUnique.setText(draft.uniqueDetail);
                    }
                }
            }
        });
    }

    /**
     * Fetches the user's permanent profile details (Name, Email, Phone) from Firebase.
     * These details are required to construct the final CV document.
     */
    private void fetchUserData() {
        if (mAuth.getCurrentUser() == null) return;

        // Fetch the user's data from Firebase and set up a listener
        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            // Callback when the user's data is fetched from Firebase
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                // Check if the fragment is still active to prevent memory leaks
                if (!isAdded() || getActivity() == null) return;

                //If the task is successful and the data exists, set the user's data
                if (task.isSuccessful() && task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    userFullName = snapshot.child("fullName").getValue(String.class);
                    userEmail = snapshot.child("email").getValue(String.class);
                    userPhone = snapshot.child("phone").getValue(String.class);
                }
            }
        });
    }

    /**
     * Collects all user inputs, validates them, and sends a request to the Gemini AI API.
     * Modifies the UI to show a loading state, and safely updates the UI on the main thread
     * once the AI responds using runOnUiThread.
     */
    private void prepareAiRequest() {
        // Create a new Cv object with the user's inputs
        Cv currentData = new Cv();
        currentData.summary = etCvSummary.getText().toString().trim();
        currentData.education = etCvEducation.getText().toString().trim();
        currentData.experience = etCvExperience.getText().toString().trim();
        currentData.skills = etCvSkills.getText().toString().trim();
        currentData.achievements = etCvAchievements.getText().toString().trim();
        currentData.traits = etCvTraits.getText().toString().trim();
        currentData.uniqueDetail = etCvUnique.getText().toString().trim();

        // Check if the user has filled in all required fields (at least summery and education)
        if (TextUtils.isEmpty(currentData.summary) || TextUtils.isEmpty(currentData.education)) {
            Toast.makeText(getContext(), "Please fill in at least a summary and education", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable the button once it's clicked to prevent multiple requests
        btnGenerateAndSaveCV.setEnabled(false);
        // Update the button text to show a loading state
        btnGenerateAndSaveCV.setText("מייצר קורות חיים... ממתין ל-AI");

        // Send the request to the Gemini AI API
        aiManager.generateCvText(userFullName, userPhone, userEmail, currentData, new AiCallback() {
            @Override
            // Callback when the AI responds
            public void onSuccess(String result) {
                // Check if the fragment is still active to prevent memory leaks
                if (!isAdded() || getActivity() == null) return;

                // Update the UI on the main thread
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    // Run this code on the main thread
                    public void run() {
                        // Move to Page 2
                        goToPage2();
                        // Set the generated text from the AI response into the text field at page 2
                        etGeneratedCvResult.setText(result);

                        // Enable the button again
                        btnGenerateAndSaveCV.setEnabled(true);
                        // Update the button text
                        btnGenerateAndSaveCV.setText("עדכון ויצירה מחדש");
                        // Show the "Skip to Result" button
                        btnSkipToResult.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            // Callback when the AI encounters an error
            public void onFailure(String errorMessage) {
                // Check if the fragment is still active to prevent memory leaks
                if (!isAdded() || getActivity() == null) return;

                // Update the UI on the main thread
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    // Run this code on the main thread
                    public void run() {
                        // Enable the button again
                        btnGenerateAndSaveCV.setEnabled(true);
                        // Update the button text
                        btnGenerateAndSaveCV.setText("צור קורות חיים בעזרת AI");
                        // Show an error message
                        Toast.makeText(getContext(), "AI Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    /**
     * Saves the finalized CV (including raw inputs and AI text) to the main "cvs" node.
     * Upon success, it deletes any temporary drafts to clear up database space
     * and returns the user to the previous screen.
     * @param data The Cv object containing the raw input data.
     * @param aiText The finalized text generated by the AI or edited by the user.
     */
    private void saveToFirebase(Cv data, String aiText) {
        // Get the user's UID
        String uid = mAuth.getCurrentUser().getUid();

        String cvId;
        // If there's a cv to edit, use its ID, else create a new one
        if (cvToEdit != null) {
            cvId = cvToEdit.cvId;
        } else {
            cvId = mDatabase.child("cvs").push().getKey();
        }

        // Set the CV data and save it to Firebase
        data.cvId = cvId;
        data.ownerId = uid;
        data.fullName = userFullName;
        data.phone = userPhone;
        data.email = userEmail;
        data.generatedText = aiText;

        // Save the CV to the "cvs" node in Firebase and set up a listener
        mDatabase.child("cvs").child(cvId).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            // Callback when the CV is saved to Firebase
            public void onComplete(@NonNull Task<Void> task) {
                // Check if the fragment is still active to prevent memory leaks
                if (!isAdded() || getActivity() == null) return;

                // If the save was successful, show a success message and delete any temporary drafts
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Cv saved!", Toast.LENGTH_SHORT).show();
                    // Delete any temporary drafts
                    mDatabase.child("cv_drafts").child(uid).removeValue();

                    // Return to the previous screen
                    getActivity().getSupportFragmentManager().popBackStack();
                } else {
                    // If the save failed, show an error message
                    Toast.makeText(getContext(), "Error saving CV", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}