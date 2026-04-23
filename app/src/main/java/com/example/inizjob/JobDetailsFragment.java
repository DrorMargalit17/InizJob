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

/*
 * Class: JobDetailsFragment
 * Purpose: Displays complete job data mapped exactly from the Job object.
 * Logic: Strict compliance, no lambda expressions.
 */
public class JobDetailsFragment extends Fragment {

    private Job currentJob;

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
        ImageView btnClose = view.findViewById(R.id.btnClose);

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

        btnClose.setOnClickListener(new View.OnClickListener() {
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

        return view;
    }
}