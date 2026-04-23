package com.example.inizjob;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
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
 * * Methods and Actions List:
 * 1. onCreateView - Initializes UI components, listeners, and the repository.
 * 2. startDataObservation - Subscribes to real-time updates for jobs and user-saved favorites.
 * 3. loadUserData - Retrieves user profile info to update the greeting and FAB visibility.
 * 4. refreshDisplayList - Re-applies filters to the master job list and updates the RecyclerView.
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

    private JobRepository repository;
    private DatabaseReference mUserRef;

    // Filter state variables for the simplified FilterSystem
    private String filterCity = "", filterField = "", filterScope = "";

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

        // Sets recycleView to be horizontal and reverse (RTL support)
        rvHorizontalJobs.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true));

        // Connects between job data to visual cards of jobs
        jobAdapter = new JobAdapter(new ArrayList<Job>(), savedJobIds, new JobAdapter.OnItemClickListener() {
            @Override
            /**
             * Navigates to JobDetailsFragment when a job is clicked.
             * - Packs the selected Job object into a Bundle.
             * - Replaces the current fragment with the details view.
             * - Adds to back stack to allow the user to return to the home screen.
             */
            public void onItemClick(Job job) {
                // Create a new instance of the details fragment
                JobDetailsFragment detailsFragment = new JobDetailsFragment();

                // Pass the selected job data to the fragment using a Bundle
                Bundle bundle = new Bundle();
                bundle.putSerializable("SELECTED_JOB", job);
                detailsFragment.setArguments(bundle);

                // Perform the fragment transition if the activity is available
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainFragmentContainer, detailsFragment) // Switch to details view
                            .addToBackStack(null)// Enable "Back" button functionality
                            .commit(); // Execute the change
                }
            }
        }, new JobAdapter.OnFavoriteClickListener() {
            @Override
            /**
             * This listener performs the following actions:
             * 1. Checks if a user is currently authenticated via FirebaseAuth.
             * 2. If authenticated, retrieves the User ID (UID) and Job ID.
             * 3. Calls the JobRepository to toggle the favorite status (add or remove) based on its current state.
             */
            public void onFavoriteClick(Job job, boolean isCurrentlySaved) {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    // Update the user's favorite status in the remote repository
                    repository.toggleFavorite(FirebaseAuth.getInstance().getCurrentUser().getUid(), job.jobId, isCurrentlySaved);
                }
            }
        });

        /**
         * Finalizes the HomeFragment setup:
         * 1. Attaches the initialized adapter to the RecyclerView.
         * 2. Initializes UI listeners and search functionality.
         * 3. Loads user-specific profile data and greeting.
         * 4. Starts real-time observation for job and favorite updates.
         */
        rvHorizontalJobs.setAdapter(jobAdapter);

        setupListeners();
        loadUserData();
        startDataObservation();

        return view;
    }

    private void setupListeners() {
        btnOpenAdvancedFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initializing the new FilterSystemFragment with its specific listener
                FilterSystemFragment filterSheet = new FilterSystemFragment();
                filterSheet.setFilterSystemListener(new FilterSystemFragment.FilterSystemListener() {
                    @Override
                    public void onFiltersApplied(String city, String workField, String jobScope) {
                        // Store the selected simplified criteria
                        filterCity = city;
                        filterField = workField;
                        filterScope = jobScope;
                        refreshDisplayList();
                    }

                    @Override
                    public void onClearFilters() {
                        // Reset all filter criteria and search text
                        filterCity = "";
                        filterField = "";
                        filterScope = "";
                        etSearchJobs.setText(""); // This will trigger the TextWatcher and refresh the list
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
            // Gets user's Id from firebase
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            // Access the specific user node in the database
            mUserRef.child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DataSnapshot snapshot = task.getResult();
                        if (snapshot.exists()) {
                            // Set personalized welcome message
                            String fullName = snapshot.child("fullName").getValue(String.class);
                            if (fullName != null) {
                                tvGreeting.setText("שלום " + fullName + ",\nאיזו עבודה תרצה לחפש היום?");
                            }

                            // Toggle FAB visibility based on user account type
                            String type = snapshot.child("type").getValue(String.class);
                            if ("עסק".equals(type)) {
                                fabAddJob.setVisibility(View.VISIBLE);
                            } else {
                                fabAddJob.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            });
        }
    }

    private void startDataObservation() {
        // Subscribe to all job postings from the repository
        repository.fetchAllJobs(new JobRepository.DataStatus() {
            @Override
            public void onDataLoaded(List<Job> jobs) {
                // Update the master list and refresh the filtered display
                allJobsList = jobs;
                refreshDisplayList();
            }

            @Override
            public void onSavedIdsLoaded(List<String> savedIds) {
                // Update the adapter's knowledge of which jobs are favorited
                jobAdapter.updateSavedJobs(savedIds);
            }

            @Override
            public void onError(String error) {
                // Notify the user in case of a data retrieval failure
                if (getContext() != null) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Synchronize the user's specific saved jobs if logged in
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            repository.loadSavedJobIds(FirebaseAuth.getInstance().getCurrentUser().getUid(), new JobRepository.DataStatus() {
                // Ensure the UI reflects the user's latest favorites
                @Override public void onDataLoaded(List<Job> jobs) {}
                @Override public void onSavedIdsLoaded(List<String> savedIds) { jobAdapter.updateSavedJobs(savedIds); }
                @Override public void onError(String error) {}
            });
        }
    }

    private void refreshDisplayList() {
        // Apply all active filters to the master list of jobs using exactly 5 parameters
        List<Job> filtered = repository.applyAdvancedFilters(
                allJobsList,
                etSearchJobs.getText().toString(),
                filterCity,
                filterField,
                filterScope
        );
        // Update the adapter with the filtered results to refresh the UI
        jobAdapter.filterList(filtered);
    }
}