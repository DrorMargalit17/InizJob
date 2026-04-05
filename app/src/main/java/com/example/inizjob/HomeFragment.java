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

/*
 * Class: HomeFragment
 * Purpose: Main dashboard displaying available jobs. Handles real-time search and favorite toggling.
 * * Methods and Actions List:
 * 1. onCreateView - Inflates the layout and initializes components.
 * 2. filterJobs - Filters the job list based on search text.
 * 3. loadUserData - Fetches the user name and type to greet them properly.
 * 4. loadSavedJobsFromFirebase - Listens to the user's saved jobs node to keep the UI stars updated.
 * 5. loadJobsFromFirebase - Loads all available jobs and sets their unique IDs.
 */
public class HomeFragment extends Fragment {

    private RecyclerView rvHorizontalJobs;
    private JobAdapter jobAdapter;
    private List<Job> jobList;
    private List<String> savedJobIds;

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
        savedJobIds = new ArrayList<>();

        jobAdapter = new JobAdapter(jobList, savedJobIds, new JobAdapter.OnItemClickListener() {
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
        }, new JobAdapter.OnFavoriteClickListener() {
            @Override
            public void onFavoriteClick(Job job, boolean isCurrentlySaved) {
                if (job.jobId == null || job.jobId.isEmpty()) {
                    Toast.makeText(getContext(), "שגיאה: למשרה זו חסר מזהה מערכת", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mAuth.getCurrentUser() != null) {
                    String uid = mAuth.getCurrentUser().getUid();
                    if (isCurrentlySaved) {
                        mDatabase.child("saved_jobs").child(uid).child(job.jobId).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(getContext(), "שגיאה בהסרת השמירה", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        mDatabase.child("saved_jobs").child(uid).child(job.jobId).setValue(true)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(getContext(), "שגיאה בשמירת המשרה", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }
            }
        });

        rvHorizontalJobs.setAdapter(jobAdapter);

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
        loadSavedJobsFromFirebase();
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

    private void filterJobs(String text) {
        List<Job> filteredList = new ArrayList<>();
        for (Job job : jobList) {
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

    private void loadSavedJobsFromFirebase() {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            mDatabase.child("saved_jobs").child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    savedJobIds.clear();
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        savedJobIds.add(childSnapshot.getKey());
                    }
                    jobAdapter.updateSavedJobs(savedJobIds);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
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
                        job.jobId = jobSnapshot.getKey();
                        jobList.add(job);
                    }
                }
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