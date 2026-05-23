package com.example.inizjob;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/*
 * Class: AddJobFragment
 * Purpose: Strict implementation of a 3-step job posting wizard.
 * Logic: Fully synchronized with Job model, standard UI, and no lambda expressions.
 * Includes a back navigation button for improved user experience.
 */
public class AddJobFragment extends Fragment {

    // UI elements for the wizard (form pages)
    private LinearLayout page1, page2, page3;

    // UI elements for the wizard title (form title)
    private TextView tvWizardTitle;

    // UI elements for the back button
    private ImageButton btnBackAddJob;

    /** UI elements for the form fields that are free text writing*/
    private TextInputEditText etCompany, etExactAddress, etBusinessDesc;
    private TextInputEditText etTitle, etJobDesc, etMinAge;
    private TextInputEditText etSalary, etContactName, etContactRole, etContactPhone, etBusinessId;

    /** UI elements for the form fields that are dropdowns*/
    private AutoCompleteTextView etLocation , etWorkField;

    private AutoCompleteTextView etJobScope;

    // UI element for the checkbox that indicates if the job requires experience
    private CheckBox cbRequiresExperience;

    // UI elements for the navigation buttons between the form pages and the save button
    private MaterialButton btnNext1, btnNext2, btnBack2, btnBack3, btnSaveJob;
    private DatabaseReference mDatabase; // Firebase Realtime Database reference

    //get job to edit if sent
    private Job jobToEdit = null;

    public AddJobFragment() {
        // Required empty constructor
    }

    @Override
    // Initialize the fragment with the selected job if it been sent to edit by the user
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey("EDIT_JOB")) {
            jobToEdit = (Job) getArguments().getSerializable("EDIT_JOB");
        }
    }

    @Nullable
    @Override
    // Inflate the layout and set up the UI components and set up listeners
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_job, container, false);

        // Initialize Firebase Realtime Database reference
        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference("jobs");

        // UI Initialization wizard
        page1 = view.findViewById(R.id.page1);
        page2 = view.findViewById(R.id.page2);
        page3 = view.findViewById(R.id.page3);
        tvWizardTitle = view.findViewById(R.id.tvWizardTitle);

        //Initialize the form fields
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

        // Initialize the navigation buttons
        btnNext1 = view.findViewById(R.id.btnNext1);
        btnNext2 = view.findViewById(R.id.btnNext2);
        btnBack2 = view.findViewById(R.id.btnBack2);
        btnBack3 = view.findViewById(R.id.btnBack3);
        btnBackAddJob = view.findViewById(R.id.btnBackAddJob);
        btnSaveJob = view.findViewById(R.id.btnSaveJob);

        //setup the dropdowns menus
        setupDropdowns();

        //setup the navigation buttons - called everytime a button is pressed
        setupNavigationButtons();

        //If the job is being edited, prefill the form fields before the user starts editing
        prefillDataIfEditing();

        return view;
    }

    /**This method is used to setup the dropdowns categories */
    private void setupDropdowns() {
        if (getContext() == null) return;

        //Cities list (matches FilterSystemFragment without "הכל")
        String[] cities = {"תל אביב", "ירושלים", "חיפה", "ראשון לציון", "פתח תקווה", "רחובות", "אשדוד", "נתניה", "באר שבע", "חולון", "בני ברק", "רמת גן", "אשקלון", "בת ים", "מודיעין", "הרצליה", "כפר סבא", "רעננה", "חדרה", "אילת", "אחר"};
        // set the dropdown adapter
        etLocation.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, cities));

        //Work Fields list
        String[] fields = {"מסעדות ומזון", "מכירות ושירות לקוחות", "הדרכה וקייטנות", "שליחויות ולוגיסטיקה", "אדמיניסטרציה ומזכירות", "אבטחה ושמירה", "מחשבים והייטק", "ייצור ותעשייה", "סופרמרקטים וקמעונאות", "אירועים והפקות", "אחר"};

        // set the dropdown adapter
        etWorkField.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, fields));

        //Job Scopes list
        String[] scopes = {"משרה מלאה", "משמרות",};
        // set the dropdown adapter
        etJobScope.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, scopes));
    }

    /**This method is used to setup the listeners for every navigation button in this fragment*/
    private void setupNavigationButtons() {
        // Back navigation button listener
        btnBackAddJob.setOnClickListener(new View.OnClickListener() {
            @Override
            // When the back button is clicked, go back to the previous fragment
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        //set up the navigation buttons listeners - page 1
        btnNext1.setOnClickListener(new View.OnClickListener() {
            @Override
            /*When the button is clicked, perform according to this steps:
            * 1. check that all field where being filled
            * 2. set page 2 visibility to visible, and hide page 1*/
            public void onClick(View v) {
                if (TextUtils.isEmpty(etCompany.getText()) || TextUtils.isEmpty(etLocation.getText())) {
                    Toast.makeText(getContext(), "חובה למלא שם עסק ועיר", Toast.LENGTH_SHORT).show();
                    return;
                }
                page1.setVisibility(View.GONE);
                page2.setVisibility(View.VISIBLE);
                // change title if not null
                if (jobToEdit != null) {
                    tvWizardTitle.setText("עריכת משרה: שלב 2 מתוך 3");
                } else {
                    tvWizardTitle.setText("שלב 2 מתוך 3: תיאור ודרישות");
                }
            }
        });

        //set up listener for return to previous page button
        btnBack2.setOnClickListener(new View.OnClickListener() {
            @Override
            /*When button is clicked, return to the previous page*/
            public void onClick(View v) {
                page2.setVisibility(View.GONE);
                page1.setVisibility(View.VISIBLE);
                // change title if not null
                if (jobToEdit != null) {
                    tvWizardTitle.setText("עריכת משרה: שלב 1 מתוך 3");
                } else {
                    tvWizardTitle.setText("שלב 1 מתוך 3: פרטי העסק");
                }
            }
        });

        //set up the navigation button listener - page 2
        btnNext2.setOnClickListener(new View.OnClickListener() {
            @Override
            /*When the button is clicked, perform according to this steps:
             * 1. check that all field where being filled
             * 2. set page 3 visibility to visible, and hide page 2*/
            public void onClick(View v) {
                if (TextUtils.isEmpty(etTitle.getText()) || TextUtils.isEmpty(etJobScope.getText())) {
                    Toast.makeText(getContext(), "חובה למלא כותרת והיקף משרה", Toast.LENGTH_SHORT).show();
                    return;
                }
                page2.setVisibility(View.GONE);
                page3.setVisibility(View.VISIBLE);
                // change title if not null
                if (jobToEdit != null) {
                    tvWizardTitle.setText("עריכת משרה: שלב 3 מתוך 3");
                } else {
                    tvWizardTitle.setText("שלב 3 מתוך 3: שכר ותנאים");
                }
            }
        });


        //set up listener for return to previous page button
        btnBack3.setOnClickListener(new View.OnClickListener() {
            @Override
            /*When button is clicked, return to the previous page*/
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

        //set up listener for save job button
        btnSaveJob.setOnClickListener(new View.OnClickListener() {
            @Override
            //when button clicked, save the job
            public void onClick(View v) {
                //call the method to validate the job data and save the job object to the database
                validateAndSaveJob();
            }
        });
    }

    /**This method is called when the edit job button got pressed
     * the method fills the jobs fields with the current data from the job object*/
    private void prefillDataIfEditing() {
        if (jobToEdit != null) {
            tvWizardTitle.setText("עריכת משרה: שלב 1 מתוך 3");//set up the edit job title
            btnSaveJob.setText("שמור שינויים");

            //set up the form fields with the job's current data
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

    /**This method is called when the save job button got pressed
     * the method checks that all fields where being filled, and all of them are valid
     * and then, the method saves the job object to the database*/
    private void validateAndSaveJob() {
        //checks that the salary and the minimum age fields where being filled, If not, show a toast message
        if (TextUtils.isEmpty(etMinAge.getText()) || TextUtils.isEmpty(etSalary.getText())) {
            Toast.makeText(getContext(), "Required to fill in age and salary", Toast.LENGTH_SHORT).show();
            return;
        }

        //checks that the salary and the minimum age numbers are valid
        int age = 0;
        double salary = 0;
        //try to convert the numbers to integers, If failed, show a toast message
        try {
            age = Integer.parseInt(etMinAge.getText().toString());
            salary = Double.parseDouble(etSalary.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Error: Please enter valid numbers in the age and salary fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        //get the data from the form fields
        String company = etCompany.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String address = etExactAddress.getText().toString().trim();
        String busDesc = etBusinessDesc.getText().toString().trim();
        String title = etTitle.getText().toString().trim();
        String jobScope = etJobScope.getText().toString().trim();
        String jobDesc = etJobDesc.getText().toString().trim();
        String workField = etWorkField.getText().toString().trim();
        boolean requiresExp = cbRequiresExperience.isChecked();

        String cName = etContactName.getText().toString().trim();
        String cRole = etContactRole.getText().toString().trim();
        String cPhone = etContactPhone.getText().toString().trim();
        String busId = etBusinessId.getText().toString().trim();

        String jobId; // Job ID
        String ownerId; // Owner ID - current user's ID

        //checks if the job is being edited or not
        if (jobToEdit != null) {
            // If the job is being edited, use the same ID
            jobId = jobToEdit.jobId;
            ownerId = jobToEdit.ownerId;
        } else {
            // If the job is being created, generate a new ID
            jobId = mDatabase.push().getKey();
            //get the current user's ID
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                ownerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            } else {
                ownerId = "unknown";
            }
        }

        //create the new job object
        Job newJob = new Job(company, location, address, busDesc, title, jobDesc, age, jobScope, workField, requiresExp, salary, cName, cRole, cPhone, busId, ownerId, jobId);

        //save the job object to the database and set up listener
        mDatabase.child(jobId).setValue(newJob).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            // Callback when the job is saved to the database
            public void onComplete(@NonNull Task<Void> task) {
                //checks if the fragment is still active to prevent memory leaks
                if (!isAdded() || getActivity() == null) {
                    return;
                }

                /*checks if the job was saved successfully, If so show a toast message and go back
                to the previous fragment */
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "The job was successfully saved!", Toast.LENGTH_SHORT).show();
                    getActivity().getSupportFragmentManager().popBackStack();
                } else {
                    Toast.makeText(getContext(), "Error saving job", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}