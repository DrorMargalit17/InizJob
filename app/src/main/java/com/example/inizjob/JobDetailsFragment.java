package com.example.inizjob;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class JobDetailsFragment extends Fragment {

    private Job currentJob;

    public JobDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the Job object passed from HomeFragment
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

        // 1. Connect UI
        ImageView btnClose = view.findViewById(R.id.btnClose);
        TextView tvDetailTitle = view.findViewById(R.id.tvDetailTitle);
        TextView tvDetailCompany = view.findViewById(R.id.tvDetailCompany);
        TextView tvDetailLocation = view.findViewById(R.id.tvDetailLocation);
        TextView tvDetailDesc = view.findViewById(R.id.tvDetailDesc);
        TextView tvDetailPrereq = view.findViewById(R.id.tvDetailPrereq);
        TextView tvDetailAge = view.findViewById(R.id.tvDetailAge);
        TextView tvDetailHours = view.findViewById(R.id.tvDetailHours);
        TextView tvDetailFlex = view.findViewById(R.id.tvDetailFlex);
        TextView tvDetailSalary = view.findViewById(R.id.tvDetailSalary);
        TextView tvDetailConditions = view.findViewById(R.id.tvDetailConditions);
        TextView tvDetailContactName = view.findViewById(R.id.tvDetailContactName);
        MaterialButton btnContact = view.findViewById(R.id.btnContact);

        // 2. Set Close Button Logic
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        // 3. Set Text Values
        tvDetailTitle.setText(currentJob.title);
        tvDetailCompany.setText(currentJob.company);
        tvDetailLocation.setText(currentJob.location + " - " + currentJob.exactAddress);
        tvDetailDesc.setText(currentJob.jobDescription);
        tvDetailPrereq.setText(currentJob.prerequisites);
        tvDetailAge.setText("גיל מינימלי: " + currentJob.minAge);
        tvDetailHours.setText(currentJob.hoursAndDays);
        tvDetailSalary.setText("שכר: " + currentJob.salary + " ₪ / שעה");
        tvDetailConditions.setText(currentJob.conditions);
        tvDetailContactName.setText("איש קשר: " + currentJob.contactName + " (" + currentJob.contactRole + ")");

        if (currentJob.flexibilityCommitment) {
            tvDetailFlex.setVisibility(View.VISIBLE);
        } else {
            tvDetailFlex.setVisibility(View.GONE);
        }

        // 4. Contact Button Logic (Opens Phone Dialer)
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

        return view;
    }
}