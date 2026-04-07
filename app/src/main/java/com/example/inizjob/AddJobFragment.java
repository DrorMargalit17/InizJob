package com.example.inizjob;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/*
 * Class: AddJobFragment
 * Purpose: A multi-step form (Wizard) allowing business owners to add or edit job postings.
 * * Methods and Actions List:
 * 1. onCreate - Checks if existing Job data is passed for editing.
 * 2. onCreateView - Inflates the layout and initializes all views.
 * 3. setupDropdowns - Prepares the auto-complete logic for cities, work fields, and job scopes.
 * 4. prefillDataIfEditing - Fills the form with existing data if in Edit mode.
 * 5. setupNavigationButtons - Manages the Next/Back logic between wizard pages using full explicit conditions.
 * 6. validateAndSaveJob - Validates fields and saves data securely to Firebase using full if-else conditions.
 */
public class AddJobFragment extends Fragment {

    private LinearLayout page1, page2, page3;
    private TextView tvWizardTitle;

    private TextInputEditText etCompany, etExactAddress, etBusinessDesc;
    private AutoCompleteTextView etLocation, etWorkField;

    private TextInputEditText etTitle, etJobDesc, etMinAge, etPrerequisites, etHours;
    private AutoCompleteTextView etJobScope;
    private CheckBox cbFlexibility, cbRequiresExperience;

    private TextInputEditText etSalary, etConditions, etContactName, etContactRole, etContactPhone, etBusinessId;
    private CheckBox cbTravelExpenses;

    private Button btnNext1, btnNext2, btnBack2, btnBack3, btnSaveJob;
    private DatabaseReference mDatabase;
    private Job jobToEdit = null;

    public AddJobFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey("EDIT_JOB")) {
                jobToEdit = (Job) getArguments().getSerializable("EDIT_JOB");
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_job, container, false);

        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference("jobs");

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
        etPrerequisites = view.findViewById(R.id.etPrerequisites);
        etHours = view.findViewById(R.id.etHours);
        cbFlexibility = view.findViewById(R.id.cbFlexibility);

        etSalary = view.findViewById(R.id.etSalary);
        cbTravelExpenses = view.findViewById(R.id.cbTravelExpenses);
        etConditions = view.findViewById(R.id.etConditions);
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
        if (getContext() == null) {
            return;
        }

        String[] israeliCities = new String[]{
                "תל אביב", "ירושלים", "חיפה", "ראשון לציון", "פתח תקווה", "אשדוד", "נתניה",
                "באר שבע", "חולון", "בני ברק", "רמת גן", "רחובות", "אשקלון", "בת ים",
                "מודיעין-מכבים-רעות", "כפר סבא", "הרצליה", "חדרה", "רעננה", "מודיעין עילית",
                "הוד השרון", "קריית עטא", "נהריה", "קריית גת", "אילת"
        };
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, israeliCities);
        etLocation.setAdapter(cityAdapter);

        String[] workFieldsList = new String[] {
                "מסעדות ומזון", "מכירות ושירות לקוחות", "הדרכה וקייטנות",
                "שליחויות ולוגיסטיקה", "אדמיניסטרציה וכללי", "אחר"
        };
        ArrayAdapter<String> workFieldAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, workFieldsList);
        etWorkField.setAdapter(workFieldAdapter);

        String[] jobScopeList = new String[] {
                "משרה מלאה", "משרה חלקית", "משמרות", "פרויקט זמני"
        };
        ArrayAdapter<String> scopeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, jobScopeList);
        etJobScope.setAdapter(scopeAdapter);
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
            etPrerequisites.setText(jobToEdit.prerequisites);
            etHours.setText(jobToEdit.hoursAndDays);
            cbFlexibility.setChecked(jobToEdit.flexibilityCommitment);

            etSalary.setText(String.valueOf(jobToEdit.salary));
            cbTravelExpenses.setChecked(jobToEdit.travelExpenses);
            etConditions.setText(jobToEdit.conditions);
            etContactName.setText(jobToEdit.contactName);
            etContactRole.setText(jobToEdit.contactRole);
            etContactPhone.setText(jobToEdit.contactPhone);
            etBusinessId.setText(jobToEdit.businessId);
        }
    }

    private void setupNavigationButtons() {
        btnNext1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etCompany.getText().toString())) {
                    Toast.makeText(getContext(), "חובה להזין שם עסק", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(etLocation.getText().toString())) {
                    Toast.makeText(getContext(), "חובה להזין עיר", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(etWorkField.getText().toString())) {
                    Toast.makeText(getContext(), "חובה לבחור תחום עבודה", Toast.LENGTH_SHORT).show();
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
                if (!cbFlexibility.isChecked()) {
                    Toast.makeText(getContext(), "חובה לסמן התחייבות לגמישות מול בני הנוער", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(etJobScope.getText().toString())) {
                    Toast.makeText(getContext(), "חובה לבחור היקף משרה", Toast.LENGTH_SHORT).show();
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

    private void validateAndSaveJob() {
        int age = 0;
        try {
            age = Integer.parseInt(etMinAge.getText().toString());
            if (age < 14) {
                Toast.makeText(getContext(), "הגיל המינימלי חייב להיות לפחות 14", Toast.LENGTH_SHORT).show();
                return;
            }
            if (age > 18) {
                Toast.makeText(getContext(), "הגיל המינימלי חייב להיות עד 18", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "נא להזין גיל תקין (מספר)", Toast.LENGTH_SHORT).show();
            return;
        }

        double salary = 0;
        try {
            salary = Double.parseDouble(etSalary.getText().toString());
            if (salary < 23.0) {
                Toast.makeText(getContext(), "שגיאה: השכר נמוך משכר המינימום המותר בחוק לנוער!", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "נא להזין שכר תקין במספרים", Toast.LENGTH_SHORT).show();
            return;
        }

        String company = etCompany.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String workField = etWorkField.getText().toString().trim();
        String address = etExactAddress.getText().toString().trim();
        String busDesc = etBusinessDesc.getText().toString().trim();

        String title = etTitle.getText().toString().trim();
        String jobScope = etJobScope.getText().toString().trim();
        String jobDesc = etJobDesc.getText().toString().trim();
        boolean requiresExperience = cbRequiresExperience.isChecked();
        String prereq = etPrerequisites.getText().toString().trim();
        String hours = etHours.getText().toString().trim();
        boolean isFlexible = cbFlexibility.isChecked();

        boolean travelExpenses = cbTravelExpenses.isChecked();
        String cond = etConditions.getText().toString().trim();
        String cName = etContactName.getText().toString().trim();
        String cRole = etContactRole.getText().toString().trim();
        String cPhone = etContactPhone.getText().toString().trim();
        String busId = etBusinessId.getText().toString().trim();

        String jobId;
        if (jobToEdit != null) {
            jobId = jobToEdit.jobId;
        } else {
            jobId = mDatabase.push().getKey();
        }

        String ownerId;
        if (jobToEdit != null) {
            ownerId = jobToEdit.ownerId;
        } else {
            ownerId = "";
        }

        if (ownerId.isEmpty()) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                ownerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }
        }

        Job newJob = new Job(company, location, address, busDesc, title, jobDesc, age, prereq, hours, isFlexible,
                jobScope, travelExpenses, workField, requiresExperience,
                salary, cond, cName, cRole, cPhone, busId, ownerId, jobId);

        if (jobId != null) {
            mDatabase.child(jobId).setValue(newJob).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        String message;
                        if (jobToEdit != null) {
                            message = "המשרה עודכנה בהצלחה!";
                        } else {
                            message = "המשרה פורסמה בהצלחה!";
                        }

                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();

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
}