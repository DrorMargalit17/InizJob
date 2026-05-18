package com.example.inizjob;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/*
 * Class: HomeFragment
 * Purpose: Main interface for users to browse and filter jobs. Uses JobRepository for data management.
 * Update: Passes the user type to JobAdapter to conditionally hide features.
 */
public class HomeFragment extends Fragment {

    private RecyclerView rvHorizontalJobs;
    private JobAdapter jobAdapter;
    private List<Job> allJobsList;
    private List<String> savedJobIds;

    private TextView tvGreeting;
    private EditText etSearchJobs;
    private FloatingActionButton fabAddJob;
    private LinearLayout btnOpenAdvancedFilters;
    private ProgressBar progressBarHome;

    private JobRepository repository;
    private DatabaseReference mUserRef;

    private String filterCity = "", filterField = "", filterScope = "";
    private String currentUserType = ""; // Store user type

    // Sync flags for smooth loading UI
    private boolean isUserLoaded = false;
    private boolean isJobsLoaded = false;

    public HomeFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        repository = new JobRepository();
        mUserRef = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference("users");

        allJobsList = new ArrayList<>();
        savedJobIds = new ArrayList<>();

        tvGreeting = view.findViewById(R.id.tvGreeting);
        rvHorizontalJobs = view.findViewById(R.id.rvHorizontalJobs);
        etSearchJobs = view.findViewById(R.id.etSearchJobs);
        fabAddJob = view.findViewById(R.id.fabAddJob);
        btnOpenAdvancedFilters = view.findViewById(R.id.btnOpenAdvancedFilters);
        progressBarHome = view.findViewById(R.id.progressBarHome);

        rvHorizontalJobs.setLayoutManager(new LinearLayoutManager(getContext()));

        jobAdapter = new JobAdapter(new ArrayList<Job>(), savedJobIds, currentUserType, new JobAdapter.OnItemClickListener() {
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
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    repository.toggleFavorite(FirebaseAuth.getInstance().getCurrentUser().getUid(), job.jobId, isCurrentlySaved);
                }
            }
        });

        rvHorizontalJobs.setAdapter(jobAdapter);

        setupListeners();
        loadUserData();
        startDataObservation();

        return view;
    }

    // Method to check if both data sources are ready before showing the list
    private void checkAndHideLoader() {
        if (isUserLoaded && isJobsLoaded) {
            progressBarHome.setVisibility(View.GONE);
            rvHorizontalJobs.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        btnOpenAdvancedFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterSystemFragment filterSheet = new FilterSystemFragment();
                filterSheet.setFilterSystemListener(new FilterSystemFragment.FilterSystemListener() {
                    @Override
                    public void onFiltersApplied(String city, String workField, String jobScope) {
                        filterCity = city;
                        filterField = workField;
                        filterScope = jobScope;
                        refreshDisplayList();
                    }

                    @Override
                    public void onClearFilters() {
                        filterCity = "";
                        filterField = "";
                        filterScope = "";
                        etSearchJobs.setText("");
                    }
                });
                filterSheet.show(getChildFragmentManager(), "FilterSystemSheet");
            }
        });

        etSearchJobs.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { refreshDisplayList(); }
        });

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
    }

    private void loadUserData() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            mUserRef.child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!isAdded() || getActivity() == null) {
                        return;
                    }

                    if (task.isSuccessful() && task.getResult() != null) {
                        DataSnapshot snapshot = task.getResult();
                        if (snapshot.exists()) {
                            String fullName = snapshot.child("fullName").getValue(String.class);
                            if (fullName != null) {
                                tvGreeting.setText("שלום " + fullName + ",\nאיזו עבודה תרצה לחפש היום?");
                            }

                            currentUserType = snapshot.child("type").getValue(String.class);
                            // Update adapter with new user type
                            jobAdapter.setUserType(currentUserType);

                            if (User.TYPE_BUSINESS.equals(currentUserType)) {
                                fabAddJob.setVisibility(View.VISIBLE);
                            } else {
                                fabAddJob.setVisibility(View.GONE);
                            }
                        }
                    }
                    isUserLoaded = true;
                    checkAndHideLoader();
                }
            });
        } else {
            isUserLoaded = true;
            checkAndHideLoader();
        }
    }

    private void startDataObservation() {
        repository.fetchAllJobs(new JobRepository.DataStatus() {
            @Override
            public void onDataLoaded(List<Job> jobs) {
                if (!isAdded() || getActivity() == null) {
                    return;
                }
                allJobsList = jobs;
                refreshDisplayList();

                isJobsLoaded = true;
                checkAndHideLoader();
            }

            @Override
            public void onSavedIdsLoaded(List<String> savedIds) {
                if (!isAdded() || getActivity() == null) {
                    return;
                }
                jobAdapter.updateSavedJobs(savedIds);
            }

            @Override
            public void onError(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
                isJobsLoaded = true;
                checkAndHideLoader();
            }
        });

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            repository.loadSavedJobIds(FirebaseAuth.getInstance().getCurrentUser().getUid(), new JobRepository.DataStatus() {
                @Override public void onDataLoaded(List<Job> jobs) {}
                @Override public void onSavedIdsLoaded(List<String> savedIds) {
                    if (!isAdded() || getActivity() == null) {
                        return;
                    }
                    jobAdapter.updateSavedJobs(savedIds);
                }
                @Override public void onError(String error) {}
            });
        }
    }

    private void refreshDisplayList() {
        List<Job> filtered = repository.applyAdvancedFilters(
                allJobsList,
                etSearchJobs.getText().toString(),
                filterCity,
                filterField,
                filterScope
        );
        jobAdapter.filterList(filtered);
    }
}