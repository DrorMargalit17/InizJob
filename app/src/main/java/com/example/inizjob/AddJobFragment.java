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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddJobFragment extends Fragment {

    // UI Layouts for pages
    private LinearLayout page1, page2, page3;
    private TextView tvWizardTitle;

    // Page 1 Inputs
    private TextInputEditText etCompany, etExactAddress, etBusinessDesc;
    private AutoCompleteTextView etLocation;

    // Page 2 Inputs
    private TextInputEditText etTitle, etJobDesc, etMinAge, etPrerequisites, etHours;
    private CheckBox cbFlexibility;

    // Page 3 Inputs
    private TextInputEditText etSalary, etConditions, etContactName, etContactRole, etContactPhone, etBusinessId;

    // Buttons
    private Button btnNext1, btnNext2, btnBack2, btnBack3, btnSaveJob;

    private DatabaseReference mDatabase;

    public AddJobFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_job, container, false);

        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference("jobs");

        // Initialize Layouts and Title
        page1 = view.findViewById(R.id.page1);
        page2 = view.findViewById(R.id.page2);
        page3 = view.findViewById(R.id.page3);
        tvWizardTitle = view.findViewById(R.id.tvWizardTitle);

        // Initialize Inputs Page 1
        etCompany = view.findViewById(R.id.etCompany);
        etLocation = view.findViewById(R.id.etLocation);
        etExactAddress = view.findViewById(R.id.etExactAddress);
        etBusinessDesc = view.findViewById(R.id.etBusinessDesc);

        // Initialize Inputs Page 2
        etTitle = view.findViewById(R.id.etTitle);
        etJobDesc = view.findViewById(R.id.etJobDesc);
        etMinAge = view.findViewById(R.id.etMinAge);
        etPrerequisites = view.findViewById(R.id.etPrerequisites);
        etHours = view.findViewById(R.id.etHours);
        cbFlexibility = view.findViewById(R.id.cbFlexibility);

        // Initialize Inputs Page 3
        etSalary = view.findViewById(R.id.etSalary);
        etConditions = view.findViewById(R.id.etConditions);
        etContactName = view.findViewById(R.id.etContactName);
        etContactRole = view.findViewById(R.id.etContactRole);
        etContactPhone = view.findViewById(R.id.etContactPhone);
        etBusinessId = view.findViewById(R.id.etBusinessId);

        // Initialize Buttons
        btnNext1 = view.findViewById(R.id.btnNext1);
        btnNext2 = view.findViewById(R.id.btnNext2);
        btnBack2 = view.findViewById(R.id.btnBack2);
        btnBack3 = view.findViewById(R.id.btnBack3);
        btnSaveJob = view.findViewById(R.id.btnSaveJob);

        setupCityDropdown();
        setupNavigationButtons();

        return view;
    }

    private void setupCityDropdown() {
        String[] israeliCities = new String[]{
                "תל אביב", "ירושלים", "חיפה", "ראשון לציון", "פתח תקווה", "אשדוד", "נתניה",
                "באר שבע", "חולון", "בני ברק", "רמת גן", "רחובות", "אשקלון", "בת ים",
                "מודיעין-מכבים-רעות", "כפר סבא", "הרצליה", "חדרה", "רעננה", "מודיעין עילית",
                "הוד השרון", "קריית עטא", "נהריה", "קריית גת", "אילת"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, israeliCities);
        etLocation.setAdapter(adapter);
    }

    private void setupNavigationButtons() {
        btnNext1.setOnClickListener(v -> {
            if (TextUtils.isEmpty(etCompany.getText().toString()) || TextUtils.isEmpty(etLocation.getText().toString())) {
                Toast.makeText(getContext(), "חובה להזין לפחות שם עסק ועיר", Toast.LENGTH_SHORT).show();
                return;
            }
            page1.setVisibility(View.GONE);
            page2.setVisibility(View.VISIBLE);
            tvWizardTitle.setText("שלב 2 מתוך 3: תיאור ודרישות");
        });

        btnBack2.setOnClickListener(v -> {
            page2.setVisibility(View.GONE);
            page1.setVisibility(View.VISIBLE);
            tvWizardTitle.setText("שלב 1 מתוך 3: פרטי העסק");
        });

        btnNext2.setOnClickListener(v -> {
            if (!cbFlexibility.isChecked()) {
                Toast.makeText(getContext(), "חובה לסמן התחייבות לגמישות מול בני הנוער", Toast.LENGTH_SHORT).show();
                return;
            }
            page2.setVisibility(View.GONE);
            page3.setVisibility(View.VISIBLE);
            tvWizardTitle.setText("שלב 3 מתוך 3: שכר ותנאים");
        });

        btnBack3.setOnClickListener(v -> {
            page3.setVisibility(View.GONE);
            page2.setVisibility(View.VISIBLE);
            tvWizardTitle.setText("שלב 2 מתוך 3: תיאור ודרישות");
        });

        btnSaveJob.setOnClickListener(v -> validateAndSaveJob());
    }

    private void validateAndSaveJob() {
        int age = 0;
        try {
            age = Integer.parseInt(etMinAge.getText().toString());
            if (age < 14 || age > 18) {
                Toast.makeText(getContext(), "הגיל המינימלי חייב להיות בין 14 ל-18", Toast.LENGTH_SHORT).show();
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
        String address = etExactAddress.getText().toString().trim();
        String busDesc = etBusinessDesc.getText().toString().trim();

        String title = etTitle.getText().toString().trim();
        String jobDesc = etJobDesc.getText().toString().trim();
        String prereq = etPrerequisites.getText().toString().trim();
        String hours = etHours.getText().toString().trim();
        boolean isFlexible = cbFlexibility.isChecked();

        String cond = etConditions.getText().toString().trim();
        String cName = etContactName.getText().toString().trim();
        String cRole = etContactRole.getText().toString().trim();
        String cPhone = etContactPhone.getText().toString().trim();
        String busId = etBusinessId.getText().toString().trim();

        String jobId = mDatabase.push().getKey();
        Job newJob = new Job(company, location, address, busDesc, title, jobDesc, age, prereq, hours, isFlexible, salary, cond, cName, cRole, cPhone, busId);

        if (jobId != null) {
            mDatabase.child(jobId).setValue(newJob).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "המשרה פורסמה בהצלחה!", Toast.LENGTH_LONG).show();
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                } else {
                    Toast.makeText(getContext(), "שגיאה בפרסום המשרה", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}