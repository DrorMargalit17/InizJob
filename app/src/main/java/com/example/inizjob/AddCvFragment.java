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

    // View Containers for Pages
    private LinearLayout cvPage1, cvPage2;
    private TextView tvCvFormTitle;

    private TextInputEditText etCvTitle, etCvSummary, etCvEducation, etCvExperience, etCvSkills,
            etCvAchievements, etCvTraits, etCvUnique, etGeneratedCvResult;

    private MaterialButton btnGenerateAndSaveCV, btnSaveFinalCv, btnSkipToResult, btnBackToForm;
    private ImageButton btnBackCv;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private GeminiAiManager aiManager;

    private String userFullName = "", userEmail = "", userPhone = "";
    private Cv cvToEdit = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey("EDIT_CV")) {
            cvToEdit = (Cv) getArguments().getSerializable("EDIT_CV");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_cv, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference();
        aiManager = new GeminiAiManager();

        cvPage1 = view.findViewById(R.id.cvPage1);
        cvPage2 = view.findViewById(R.id.cvPage2);
        tvCvFormTitle = view.findViewById(R.id.tvCvFormTitle);

        btnBackCv = view.findViewById(R.id.btnBackCv);
        etCvTitle = view.findViewById(R.id.etCvTitle);
        etCvSummary = view.findViewById(R.id.etCvSummary);
        etCvEducation = view.findViewById(R.id.etCvEducation);
        etCvExperience = view.findViewById(R.id.etCvExperience);
        etCvSkills = view.findViewById(R.id.etCvSkills);
        etCvAchievements = view.findViewById(R.id.etCvAchievements);
        etCvTraits = view.findViewById(R.id.etCvTraits);
        etCvUnique = view.findViewById(R.id.etCvUnique);

        btnGenerateAndSaveCV = view.findViewById(R.id.btnGenerateAndSaveCV);
        btnSkipToResult = view.findViewById(R.id.btnSkipToResult);
        btnSaveFinalCv = view.findViewById(R.id.btnSaveFinalCv);
        btnBackToForm = view.findViewById(R.id.btnBackToForm);
        etGeneratedCvResult = view.findViewById(R.id.etGeneratedCvResult);

        // Top Back Button Logic
        btnBackCv.setOnClickListener(new View.OnClickListener() {
            @Override
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

        // Skip to Result Logic (visible only if editing)
        btnSkipToResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPage2();
            }
        });

        if (cvToEdit != null) {
            populateFields(cvToEdit);
        } else {
            fetchUserData();
            loadDraftIfAvailable();
        }

        setupDraftLogic();

        btnGenerateAndSaveCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareAiRequest();
            }
        });

        btnSaveFinalCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = etCvTitle.getText().toString().trim();
                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(getContext(), "נא לחזור אחורה ולהזין כותרת לקורות החיים", Toast.LENGTH_SHORT).show();
                    return;
                }

                Cv currentData = new Cv();
                currentData.cvTitle = title;
                currentData.summary = etCvSummary.getText().toString().trim();
                currentData.education = etCvEducation.getText().toString().trim();
                currentData.experience = etCvExperience.getText().toString().trim();
                currentData.skills = etCvSkills.getText().toString().trim();
                currentData.achievements = etCvAchievements.getText().toString().trim();
                currentData.traits = etCvTraits.getText().toString().trim();
                currentData.uniqueDetail = etCvUnique.getText().toString().trim();

                String finalAiText = etGeneratedCvResult.getText().toString().trim();
                if (TextUtils.isEmpty(finalAiText)) {
                    Toast.makeText(getContext(), "הטקסט ריק, אנא צור קורות חיים קודם", Toast.LENGTH_SHORT).show();
                    return;
                }

                saveToFirebase(currentData, finalAiText);
            }
        });
    }

    private void goToPage2() {
        cvPage1.setVisibility(View.GONE);
        cvPage2.setVisibility(View.VISIBLE);
        tvCvFormTitle.setText("עריכת קורות החיים");
    }

    private void goBackToPage1() {
        cvPage2.setVisibility(View.GONE);
        cvPage1.setVisibility(View.VISIBLE);
        tvCvFormTitle.setText("בניית קורות חיים מקצועיים");
    }

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

        btnGenerateAndSaveCV.setText("עדכון ויצירה מחדש");

        // Setup editing environment
        if (cv.generatedText != null && !cv.generatedText.isEmpty()) {
            btnSkipToResult.setVisibility(View.VISIBLE);
            // Automatically jump to Page 2 when opening an existing CV to edit
            goToPage2();
        }

        userFullName = cv.fullName;
        userPhone = cv.phone;
        userEmail = cv.email;
    }

    private void setupDraftLogic() {
        TextWatcher draftWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                saveCurrentAsDraft();
            }
        };
        etCvTitle.addTextChangedListener(draftWatcher);
        etCvSummary.addTextChangedListener(draftWatcher);
        etCvEducation.addTextChangedListener(draftWatcher);
        etCvExperience.addTextChangedListener(draftWatcher);
        etCvSkills.addTextChangedListener(draftWatcher);
        etCvAchievements.addTextChangedListener(draftWatcher);
        etCvTraits.addTextChangedListener(draftWatcher);
        etCvUnique.addTextChangedListener(draftWatcher);
    }

    private void saveCurrentAsDraft() {
        if (mAuth.getCurrentUser() == null || cvToEdit != null) return;
        String uid = mAuth.getCurrentUser().getUid();

        Cv draft = new Cv();
        draft.cvTitle = etCvTitle.getText().toString();
        draft.summary = etCvSummary.getText().toString();
        draft.education = etCvEducation.getText().toString();
        draft.experience = etCvExperience.getText().toString();
        draft.skills = etCvSkills.getText().toString();
        draft.achievements = etCvAchievements.getText().toString();
        draft.traits = etCvTraits.getText().toString();
        draft.uniqueDetail = etCvUnique.getText().toString();

        mDatabase.child("cv_drafts").child(uid).setValue(draft);
    }

    private void loadDraftIfAvailable() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        mDatabase.child("cv_drafts").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!isAdded() || getActivity() == null) return;

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

    private void fetchUserData() {
        if (mAuth.getCurrentUser() == null) return;
        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!isAdded() || getActivity() == null) return;

                if (task.isSuccessful() && task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    userFullName = snapshot.child("fullName").getValue(String.class);
                    userEmail = snapshot.child("email").getValue(String.class);
                    userPhone = snapshot.child("phone").getValue(String.class);
                }
            }
        });
    }

    private void prepareAiRequest() {
        Cv currentData = new Cv();
        currentData.summary = etCvSummary.getText().toString().trim();
        currentData.education = etCvEducation.getText().toString().trim();
        currentData.experience = etCvExperience.getText().toString().trim();
        currentData.skills = etCvSkills.getText().toString().trim();
        currentData.achievements = etCvAchievements.getText().toString().trim();
        currentData.traits = etCvTraits.getText().toString().trim();
        currentData.uniqueDetail = etCvUnique.getText().toString().trim();

        if (TextUtils.isEmpty(currentData.summary) || TextUtils.isEmpty(currentData.education)) {
            Toast.makeText(getContext(), "נא למלא לפחות תמצית והשכלה", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGenerateAndSaveCV.setEnabled(false);
        btnGenerateAndSaveCV.setText("מייצר קורות חיים... ממתין ל-AI");

        aiManager.generateCvText(userFullName, userPhone, userEmail, currentData, new AiCallback() {
            @Override
            public void onSuccess(String result) {
                if (!isAdded() || getActivity() == null) return;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Move to Page 2
                        goToPage2();
                        etGeneratedCvResult.setText(result);

                        btnGenerateAndSaveCV.setEnabled(true);
                        btnGenerateAndSaveCV.setText("עדכון ויצירה מחדש");
                        btnSkipToResult.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                if (!isAdded() || getActivity() == null) return;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnGenerateAndSaveCV.setEnabled(true);
                        btnGenerateAndSaveCV.setText("צור קורות חיים בעזרת AI");
                        Toast.makeText(getContext(), "AI Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void saveToFirebase(Cv data, String aiText) {
        String uid = mAuth.getCurrentUser().getUid();

        String cvId;
        if (cvToEdit != null) {
            cvId = cvToEdit.cvId;
        } else {
            cvId = mDatabase.child("cvs").push().getKey();
        }

        data.cvId = cvId;
        data.ownerId = uid;
        data.fullName = userFullName;
        data.phone = userPhone;
        data.email = userEmail;
        data.generatedText = aiText;

        mDatabase.child("cvs").child(cvId).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!isAdded() || getActivity() == null) return;

                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "קורות החיים נשמרו!", Toast.LENGTH_SHORT).show();
                    mDatabase.child("cv_drafts").child(uid).removeValue();

                    getActivity().getSupportFragmentManager().popBackStack();
                } else {
                    Toast.makeText(getContext(), "שגיאה בשמירת קורות חיים", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}