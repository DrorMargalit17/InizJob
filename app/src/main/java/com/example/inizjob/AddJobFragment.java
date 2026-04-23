package com.example.inizjob;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/*
 * Class: AddJobFragment
 * Purpose: Strict implementation of a 3-step job posting wizard.
 * Logic: Fully synchronized with Job model, standard UI, and no lambda expressions.
 */
public class AddJobFragment extends Fragment {

    private LinearLayout page1, page2, page3;
    private TextView tvWizardTitle;

    private TextInputEditText etCompany, etExactAddress, etBusinessDesc;
    private AutoCompleteTextView etLocation , etWorkField;

    private TextInputEditText etTitle, etJobDesc, etMinAge;
    private AutoCompleteTextView etJobScope;
    private CheckBox cbRequiresExperience;

    private TextInputEditText etSalary, etContactName, etContactRole, etContactPhone, etBusinessId;

    private MaterialButton btnNext1, btnNext2, btnBack2, btnBack3, btnSaveJob;
    private DatabaseReference mDatabase;
    private Job jobToEdit = null;

    public AddJobFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey("EDIT_JOB")) {
            jobToEdit = (Job) getArguments().getSerializable("EDIT_JOB");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_job, container, false);

        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference("jobs");

        // Initialization
        page1 = view.findViewById(R.id.page1);
        page2 = view.findViewById(R.id.page2);
        page3 = view.findViewById(R.id.page3);
        tvWizardTitle = view.findViewById(R.id.tvWizardTitle);

        etCompany = view.findViewById(R.id.etCompany);
        etLocation = view.findViewById(R.id.etLocation);
        etWorkField = view.findViewById(R.id.etWorkField);
        etExactAddress = view.findViewById(R.id.etExactAddress);
        etBusinessDesc = view.findViewById(R.id.etBusinessDesc);

        etTitle = view.findViewById(R.id.etTitle);
        etJobScope = view.findViewById(R.id.etJobScope);
        etJobDesc = view.findViewById(R.id.etJobDesc);
        etMinAge = view.findViewById(R.id.etMinAge);
        cbRequiresExperience = view.findViewById(R.id.cbRequiresExperience);

        etSalary = view.findViewById(R.id.etSalary);
        etContactName = view.findViewById(R.id.etContactName);
        etContactRole = view.findViewById(R.id.etContactRole);
        etContactPhone = view.findViewById(R.id.etContactPhone);
        etBusinessId = view.findViewById(R.id.etBusinessId);

        btnNext1 = view.findViewById(R.id.btnNext1);
        btnNext2 = view.findViewById(R.id.btnNext2);
        btnBack2 = view.findViewById(R.id.btnBack2);
        btnBack3 = view.findViewById(R.id.btnBack3);
        btnSaveJob = view.findViewById(R.id.btnSaveJob);

        setupDropdowns();
        setupNavigationButtons();
        prefillDataIfEditing();

        return view;
    }

    private void setupDropdowns() {
        if (getContext() == null) return;
        String[] cities = {"תל אביב", "ירושלים", "חיפה", "ראשון לציון", "פתח תקווה", "רחובות", "אשדוד", "נתניה"};
        etLocation.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, cities));

        String[] fields = {"מסעדות ומזון", "מכירות ושירות לקוחות", "הדרכה וקייטנות", "שליחויות ולוגיסטיקה", "אחר"};
        etWorkField.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, fields));

        String[] scopes = {"משרה מלאה", "משרה חלקית", "משמרות", "פרויקט זמני"};
        etJobScope.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, scopes));
    }

    private void setupNavigationButtons() {
        btnNext1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etCompany.getText()) || TextUtils.isEmpty(etLocation.getText())) {
                    Toast.makeText(getContext(), "חובה למלא שם עסק ועיר", Toast.LENGTH_SHORT).show();
                    return;
                }
                page1.setVisibility(View.GONE);
                page2.setVisibility(View.VISIBLE);
                if (jobToEdit != null) {
                    tvWizardTitle.setText("עריכת משרה: שלב 2 מתוך 3");
                } else {
                    tvWizardTitle.setText("שלב 2 מתוך 3: תיאור ודרישות");
                }
            }
        });

        btnBack2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page2.setVisibility(View.GONE);
                page1.setVisibility(View.VISIBLE);
                if (jobToEdit != null) {
                    tvWizardTitle.setText("עריכת משרה: שלב 1 מתוך 3");
                } else {
                    tvWizardTitle.setText("שלב 1 מתוך 3: פרטי העסק");
                }
            }
        });

        btnNext2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etTitle.getText()) || TextUtils.isEmpty(etJobScope.getText())) {
                    Toast.makeText(getContext(), "חובה למלא כותרת והיקף משרה", Toast.LENGTH_SHORT).show();
                    return;
                }
                page2.setVisibility(View.GONE);
                page3.setVisibility(View.VISIBLE);
                if (jobToEdit != null) {
                    tvWizardTitle.setText("עריכת משרה: שלב 3 מתוך 3");
                } else {
                    tvWizardTitle.setText("שלב 3 מתוך 3: שכר ותנאים");
                }
            }
        });

        btnBack3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page3.setVisibility(View.GONE);
                page2.setVisibility(View.VISIBLE);
                if (jobToEdit != null) {
                    tvWizardTitle.setText("עריכת משרה: שלב 2 מתוך 3");
                } else {
                    tvWizardTitle.setText("שלב 2 מתוך 3: תיאור ודרישות");
                }
            }
        });

        btnSaveJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSaveJob();
            }
        });
    }

    private void prefillDataIfEditing() {
        if (jobToEdit != null) {
            tvWizardTitle.setText("עריכת משרה: שלב 1 מתוך 3");
            btnSaveJob.setText("שמור שינויים");
            etCompany.setText(jobToEdit.company);
            etLocation.setText(jobToEdit.location, false);
            etWorkField.setText(jobToEdit.workField, false);
            etExactAddress.setText(jobToEdit.exactAddress);
            etBusinessDesc.setText(jobToEdit.businessDescription);
            etTitle.setText(jobToEdit.title);
            etJobScope.setText(jobToEdit.jobScope, false);
            etJobDesc.setText(jobToEdit.jobDescription);
            etMinAge.setText(String.valueOf(jobToEdit.minAge));
            cbRequiresExperience.setChecked(jobToEdit.requiresExperience);
            etSalary.setText(String.valueOf(jobToEdit.salary));
            etContactName.setText(jobToEdit.contactName);
            etContactRole.setText(jobToEdit.contactRole);
            etContactPhone.setText(jobToEdit.contactPhone);
            etBusinessId.setText(jobToEdit.businessId);
        }
    }

    private void validateAndSaveJob() {
        if (TextUtils.isEmpty(etMinAge.getText()) || TextUtils.isEmpty(etSalary.getText())) {
            Toast.makeText(getContext(), "חובה למלא גיל ושכר", Toast.LENGTH_SHORT).show();
            return;
        }

        String company = etCompany.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String address = etExactAddress.getText().toString().trim();
        String busDesc = etBusinessDesc.getText().toString().trim();
        String title = etTitle.getText().toString().trim();
        String jobScope = etJobScope.getText().toString().trim();
        String jobDesc = etJobDesc.getText().toString().trim();
        String workField = etWorkField.getText().toString().trim();

        int age = Integer.parseInt(etMinAge.getText().toString());
        double salary = Double.parseDouble(etSalary.getText().toString());
        boolean requiresExp = cbRequiresExperience.isChecked();

        String cName = etContactName.getText().toString().trim();
        String cRole = etContactRole.getText().toString().trim();
        String cPhone = etContactPhone.getText().toString().trim();
        String busId = etBusinessId.getText().toString().trim();

        String jobId = (jobToEdit != null) ? jobToEdit.jobId : mDatabase.push().getKey();
        String ownerId = (jobToEdit != null) ? jobToEdit.ownerId : FirebaseAuth.getInstance().getCurrentUser().getUid();

        Job newJob = new Job(company, location, address, busDesc, title, jobDesc, age, jobScope, workField, requiresExp, salary, cName, cRole, cPhone, busId, ownerId, jobId);

        mDatabase.child(jobId).setValue(newJob).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "המשרה נשמרה בהצלחה!", Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                } else {
                    Toast.makeText(getContext(), "שגיאה בשמירת המשרה", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}