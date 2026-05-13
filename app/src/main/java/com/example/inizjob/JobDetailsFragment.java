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

    private Job currentJob;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public JobDetailsFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentJob = (Job) getArguments().getSerializable("SELECTED_JOB");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_job_details, container, false);

        if (currentJob == null) {
            Toast.makeText(getContext(), "שגיאה בטעינת המשרה", Toast.LENGTH_SHORT).show();
            return view;
        }

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference();

        // Connections
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

        MaterialButton btnContact = view.findViewById(R.id.btnContact);
        MaterialButton btnApplySMS = view.findViewById(R.id.btnApplySMS);
        ImageButton btnBackJobDetails = view.findViewById(R.id.btnBackJobDetails);

        // Setting Data
        tvTitle.setText(currentJob.title);
        tvCompany.setText(currentJob.company);
        tvLocation.setText(currentJob.location + " - " + currentJob.exactAddress);
        tvBusDesc.setText(currentJob.businessDescription);
        tvJobDesc.setText(currentJob.jobDescription);
        tvWorkField.setText("תחום עבודה: " + currentJob.workField);
        tvScope.setText("היקף משרה: " + currentJob.jobScope);
        tvAge.setText("גיל מינימלי: " + currentJob.minAge);

        if (currentJob.requiresExperience) {
            tvExp.setText("ניסיון דרוש: כן");
        } else {
            tvExp.setText("ניסיון דרוש: לא");
        }

        tvSalary.setText("שכר: " + currentJob.salary + " ₪ / שעה");
        tvContact.setText("איש קשר: " + currentJob.contactName);

        // Check user type to show/hide Apply button
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            mDatabase.child("users").child(uid).child("type").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!isAdded() || getActivity() == null) return;
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String userType = task.getResult().getValue(String.class);
                        if (!"Business".equals(userType) && !"עסק".equals(userType)) {
                            btnApplySMS.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }

        btnBackJobDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentJob.contactPhone != null && !currentJob.contactPhone.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + currentJob.contactPhone));
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "מספר טלפון לא זמין", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnApplySMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        String uid = mAuth.getCurrentUser().getUid();

        mDatabase.child("cvs").orderByChild("ownerId").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getActivity() == null) return;

                final List<Cv> userCvs = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Cv cv = child.getValue(Cv.class);
                    if (cv != null) {
                        userCvs.add(cv);
                    }
                }

                if (userCvs.isEmpty()) {
                    Toast.makeText(getContext(), "עליך ליצור קורות חיים קודם לכן", Toast.LENGTH_LONG).show();
                } else if (userCvs.size() == 1) {
                    // Only one CV exists, send it directly
                    launchSMS(userCvs.get(0).generatedText);
                } else {
                    // Multiple CVs exist, show selection dialog
                    showCvSelectionDialog(userCvs);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "שגיאה בגישה לנתונים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Displays a dialog with the titles of the available CVs.
     */
    private void showCvSelectionDialog(final List<Cv> cvList) {
        if (getContext() == null) return;

        // Prepare the list of titles for the dialog
        String[] titles = new String[cvList.size()];
        for (int i = 0; i < cvList.size(); i++) {
            String title = cvList.get(i).cvTitle;
            if (title == null || title.isEmpty()) {
                titles[i] = "קורות חיים ללא כותרת #" + (i + 1);
            } else {
                titles[i] = title;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("איזה קורות חיים תרצה להגיש?");
        builder.setItems(titles, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 'which' is the index of the selected item
                launchSMS(cvList.get(which).generatedText);
            }
        });

        builder.setNegativeButton("ביטול", null);
        builder.show();
    }

    private void launchSMS(String cvText) {
        try {
            String phone = currentJob.contactPhone.trim();
            String message = "שלום " + currentJob.contactName + ",\n" +
                    "הגעתי דרך אפליקציית InizJob. אשמח להגיש מועמדות למשרה: " + currentJob.title + ".\n\n" +
                    "להלן קורות החיים שלי:\n\n" + cvText;

            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:" + phone));
            intent.putExtra("sms_body", message);
            startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(getContext(), "שגיאה בפתיחת אפליקציית ההודעות", Toast.LENGTH_SHORT).show();
        }
    }
}