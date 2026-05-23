package com.example.inizjob;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/*
 * Class: JobDetailsFragment
 * Purpose: Displays complete job data and handles smart CV selection for SMS submission.
 * Logic: Strict compliance, no lambda expressions, lifecycle protected.
 */
public class JobDetailsFragment extends Fragment {

    private Job currentJob; // The selected job
    private FirebaseAuth mAuth; // Firebase Authentication instance
    private DatabaseReference mDatabase; // Firebase Realtime Database reference

    public JobDetailsFragment() {
        // Required empty constructor
    }

    @Override
    // Initialize the fragment with the selected job
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //Get the selected job from the arguments, using serializable to pass the job object
            currentJob = (Job) getArguments().getSerializable("SELECTED_JOB");
        }
    }

    @Nullable
    @Override
    // Inflate the layout and set up the UI components and set up listeners
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_job_details, container, false);

        // Check if the job is valid (there is a job object)
        if (currentJob == null) {
            // Handle the case where the job is null
            Toast.makeText(getContext(), "Error loading job", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Initialize Firebase Authentication and Realtime Database references
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference();

        //Connections between job description views and variables
        TextView tvTitle = view.findViewById(R.id.tvDetailTitle);
        TextView tvCompany = view.findViewById(R.id.tvDetailCompany);
        TextView tvLocation = view.findViewById(R.id.tvDetailLocation);
        TextView tvBusDesc = view.findViewById(R.id.tvDetailBusinessDesc);
        TextView tvJobDesc = view.findViewById(R.id.tvDetailDesc);
        TextView tvWorkField = view.findViewById(R.id.tvDetailWorkField);
        TextView tvScope = view.findViewById(R.id.tvDetailScope);
        TextView tvAge = view.findViewById(R.id.tvDetailAge);
        TextView tvExp = view.findViewById(R.id.tvDetailExperience);
        TextView tvSalary = view.findViewById(R.id.tvDetailSalary);
        TextView tvContact = view.findViewById(R.id.tvDetailContactName);

        //Connections between buttons and variables
        MaterialButton btnContact = view.findViewById(R.id.btnContact);
        MaterialButton btnApplySMS = view.findViewById(R.id.btnApplySMS);
        ImageButton btnBackJobDetails = view.findViewById(R.id.btnBackJobDetails);

        //set the data from the current_job object to the views accordingly
        tvTitle.setText(currentJob.title);
        tvCompany.setText(currentJob.company);
        tvLocation.setText(currentJob.location + " - " + currentJob.exactAddress);
        tvBusDesc.setText(currentJob.businessDescription);
        tvJobDesc.setText(currentJob.jobDescription);
        tvWorkField.setText("תחום עבודה: " + currentJob.workField);
        tvScope.setText("היקף משרה: " + currentJob.jobScope);
        tvAge.setText("גיל מינימלי: " + currentJob.minAge);

        // Check if the job requires experience and set the checkbox accordingly
        if (currentJob.requiresExperience) {
            tvExp.setText("ניסיון דרוש: כן");
        } else {
            tvExp.setText("ניסיון דרוש: לא");
        }

        tvSalary.setText("שכר: " + currentJob.salary + " ₪ / שעה");
        tvContact.setText("איש קשר: " + currentJob.contactName);

        // Initialize btnApplySMS visibility based on user type
        if (mAuth.getCurrentUser() != null) {
            //get user's ID, and check if it's a business user
            String uid = mAuth.getCurrentUser().getUid();
            //get the user's type and setup listener
            mDatabase.child("users").child(uid).child("type").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                // Callback when data is fetched from the database
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    //checks if the fragment is still active to prevent memory leaks
                    if (!isAdded() || getActivity() == null) return;
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String userType = task.getResult().getValue(String.class);
                        //If the current logged in user's type isn't business, show the button
                        if (!"Business".equals(userType) && !"עסק".equals(userType)) {
                            btnApplySMS.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }

        // Set up the back button listener
        btnBackJobDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            //When the back button is clicked, go back to the previous fragment
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        //Set up the contact button listener
        btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            //When the contact button is clicked, open the phone dialer
            public void onClick(View v) {
                // If the contact phone number is available, open the phone dialer
                if (currentJob.contactPhone != null && !currentJob.contactPhone.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    //gets the phone number and adds it to the intent
                    intent.setData(Uri.parse("tel:" + currentJob.contactPhone));
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "Phone number unavailable", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Set up the apply button listener
        btnApplySMS.setOnClickListener(new View.OnClickListener() {
            @Override
            //when the apply button is clicked, show the CV selection dialog
            public void onClick(View v) {
                //call the cv selection method to show the dialog
                handleCvSelectionAndSubmission();
            }
        });

        return view;
    }

    /**
     * Logic to fetch all user CVs and let them choose if multiple exist.
     */
    private void handleCvSelectionAndSubmission() {
        if (mAuth.getCurrentUser() == null) return;
        //get user's ID if logged in
        String uid = mAuth.getCurrentUser().getUid();

        /*point to the cvs node in the database and fetch all CVs that their ownerID is the
        current user's ID and set up listener */
        mDatabase.child("cvs").orderByChild("ownerId").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            // Callback when data is fetched from the database
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //checks if the fragment is still active to prevent memory leaks
                if (!isAdded() || getActivity() == null) return;

                // Create a list of CV objects from the fetched data that belong to the current user
                final List<Cv> userCvs = new ArrayList<>();
                //pass on each CV in the database and add it to the list if it's not null
                for (DataSnapshot child : snapshot.getChildren()) {
                    Cv cv = child.getValue(Cv.class);
                    if (cv != null) {
                        userCvs.add(cv);
                    }
                }

                // Check if there are any CVs available
                if (userCvs.isEmpty()) {
                    Toast.makeText(getContext(), "You must create a CV first.", Toast.LENGTH_LONG).show();
                    // If there is only one CV, send it directly without a dialog
                } else if (userCvs.size() == 1) {
                    //call the launchSMS method to send the CV to the user
                    launchSMS(userCvs.get(0).generatedText);
                } else {
                    //Multiple CVs exist, call the method to show selection dialog
                    showCvSelectionDialog(userCvs);
                }
            }

            @Override
            // Callback when an error occurs
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Data access error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Displays a dialog with the titles of the available CVs.
     */
    private void showCvSelectionDialog(final List<Cv> cvList) {
        if (getContext() == null) return;

        // Prepare the list of cvs titles for the dialog
        String[] titles = new String[cvList.size()];
        // pass on each CV title and add it to the list if it's not null
        for (int i = 0; i < cvList.size(); i++) {
            String title = cvList.get(i).cvTitle;
            // If the title is null or empty, use a default title
            if (title == null || title.isEmpty()) {
                titles[i] = "קורות חיים ללא כותרת #" + (i + 1);
            } else {
                titles[i] = title;
            }
        }

        // Create the dialog with the list of CV titles
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("איזה קורות חיים תרצה להגיש?"); // Set the title
        // Set the items and their click listener
        builder.setItems(titles, new DialogInterface.OnClickListener() {
            @Override
            // Callback when an item is selected
            public void onClick(DialogInterface dialog, int which) {
                //call the launchSMS method to send the selected CV to the user
                launchSMS(cvList.get(which).generatedText);
            }
        });

        // Set the negative button to cancel the dialog
        builder.setNegativeButton("ביטול", null);
        // Show the dialog
        builder.show();
    }

    /**
     * This method launches the SMS application with the selected CV.
     * First it builds a default opening message with the job title,
     * and than add the selected cv text.
     * */
    private void launchSMS(String cvText) {
        try {
            // Build the message with the job title and the selected CV text
            String phone = currentJob.contactPhone.trim(); // Get the phone number
            String message = "שלום " + currentJob.contactName + ",\n" +
                    "הגעתי דרך אפליקציית InizJob. אשמח להגיש מועמדות למשרה: " + currentJob.title + ".\n\n" +
                    "להלן קורות החיים שלי:\n\n" + cvText;

            // Create an intent to send an SMS to the selected phone number
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:" + phone));
            intent.putExtra("sms_body", message);
            startActivity(intent);

        } catch (Exception e) {
            // Handle any exceptions that may occur during the SMS launch
            Toast.makeText(getContext(), "Error opening the messaging app", Toast.LENGTH_SHORT).show();
        }
    }
}