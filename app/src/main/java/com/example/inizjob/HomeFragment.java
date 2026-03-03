package com.example.inizjob;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvHorizontalJobs;
    private JobAdapter jobAdapter;
    private List<Job> jobList; // The original full list

    private TextView tvGreeting;
    private EditText etSearchJobs;
    private FloatingActionButton fabAddJob;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference();

        rvHorizontalJobs = view.findViewById(R.id.rvHorizontalJobs);
        tvGreeting = view.findViewById(R.id.tvGreeting);
        etSearchJobs = view.findViewById(R.id.etSearchJobs);
        fabAddJob = view.findViewById(R.id.fabAddJob);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true);
        rvHorizontalJobs.setLayoutManager(layoutManager);

        jobList = new ArrayList<>();

        jobAdapter = new JobAdapter(jobList, new JobAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Job job) {
                JobDetailsFragment detailsFragment = new JobDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("SELECTED_JOB", job);
                detailsFragment.setArguments(bundle);

                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainFragmentContainer, detailsFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        rvHorizontalJobs.setAdapter(jobAdapter);

        // Add logic for real-time search filtering
        etSearchJobs.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterJobs(s.toString());
            }
        });

        loadUserData();
        loadJobsFromFirebase();

        fabAddJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainFragmentContainer, new AddJobFragment())
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        return view;
    }

    // Filter the original list and update the adapter
    private void filterJobs(String text) {
        List<Job> filteredList = new ArrayList<>();
        for (Job job : jobList) {
            // Check if search text matches job title or company name
            if (job.title.toLowerCase().contains(text.toLowerCase()) ||
                    job.company.toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(job);
            }
        }
        jobAdapter.filterList(filteredList);
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();

            mDatabase.child("users").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        DataSnapshot snapshot = task.getResult();

                        String fullName = snapshot.child("fullName").getValue(String.class);
                        if (fullName != null) {
                            tvGreeting.setText("שלום " + fullName + ",\nאיזו עבודה תרצה לחפש היום?");
                        } else {
                            tvGreeting.setText("שלום,\nאיזו עבודה תרצה לחפש היום?");
                        }

                        String type = snapshot.child("type").getValue(String.class);
                        if ("עסק".equals(type)) {
                            fabAddJob.setVisibility(View.VISIBLE);
                        } else {
                            fabAddJob.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }
    }

    private void loadJobsFromFirebase() {
        mDatabase.child("jobs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                jobList.clear();
                for (DataSnapshot jobSnapshot : snapshot.getChildren()) {
                    Job job = jobSnapshot.getValue(Job.class);
                    if (job != null) {
                        jobList.add(job);
                    }
                }
                // Apply the search filter automatically when data refreshes
                String currentSearch = etSearchJobs.getText().toString();
                filterJobs(currentSearch);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading jobs: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}