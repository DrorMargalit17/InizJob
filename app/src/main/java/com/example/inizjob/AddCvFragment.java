package com.example.inizjob;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
 * 5. handleBack - Safely returns to the previous screen.
 */
public class AddCvFragment extends Fragment {

    private TextInputEditText etCvSummary, etCvEducation, etCvExperience, etCvSkills,
            etCvAchievements, etCvTraits, etCvUnique;
    private MaterialButton btnGenerateAndSaveCV;
    private ImageButton btnBackCv;
    private TextView tvGeneratedCvResult;

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

        btnBackCv = view.findViewById(R.id.btnBackCv);
        etCvSummary = view.findViewById(R.id.etCvSummary);
        etCvEducation = view.findViewById(R.id.etCvEducation);
        etCvExperience = view.findViewById(R.id.etCvExperience);
        etCvSkills = view.findViewById(R.id.etCvSkills);
        etCvAchievements = view.findViewById(R.id.etCvAchievements);
        etCvTraits = view.findViewById(R.id.etCvTraits);
        etCvUnique = view.findViewById(R.id.etCvUnique);
        btnGenerateAndSaveCV = view.findViewById(R.id.btnGenerateAndSaveCV);
        tvGeneratedCvResult = view.findViewById(R.id.tvGeneratedCvResult);

        btnBackCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
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
    }

    private void populateFields(Cv cv) {
        etCvSummary.setText(cv.summary);
        etCvEducation.setText(cv.education);
        etCvExperience.setText(cv.experience);
        etCvSkills.setText(cv.skills);
        etCvAchievements.setText(cv.achievements);
        etCvTraits.setText(cv.traits);
        etCvUnique.setText(cv.uniqueDetail);
        tvGeneratedCvResult.setVisibility(View.VISIBLE);
        tvGeneratedCvResult.setText(cv.generatedText);
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
                if (task.isSuccessful() && task.getResult().exists()) {
                    Cv draft = task.getResult().getValue(Cv.class);
                    if (draft != null) {
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
        aiManager.generateCvText(userFullName, userPhone, userEmail, currentData, new AiCallback() {
            @Override
            public void onSuccess(String result) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvGeneratedCvResult.setVisibility(View.VISIBLE);
                        tvGeneratedCvResult.setText(result);
                        btnGenerateAndSaveCV.setEnabled(true);
                        saveToFirebase(currentData, result);
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnGenerateAndSaveCV.setEnabled(true);
                        Toast.makeText(getContext(), "AI Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void saveToFirebase(Cv data, String aiText) {
        String uid = mAuth.getCurrentUser().getUid();
        String cvId = (cvToEdit != null) ? cvToEdit.cvId : mDatabase.child("cvs").push().getKey();

        data.cvId = cvId;
        data.ownerId = uid;
        data.fullName = userFullName;
        data.phone = userPhone;
        data.email = userEmail;
        data.generatedText = aiText;

        mDatabase.child("cvs").child(cvId).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "קורות החיים נשמרו!", Toast.LENGTH_SHORT).show();
                    // Clear draft after successful save
                    mDatabase.child("cv_drafts").child(uid).removeValue();
                }
            }
        });
    }
}