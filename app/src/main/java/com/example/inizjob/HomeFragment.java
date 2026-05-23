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
 * Passes the user type to JobAdapter to conditionally hide features.
 */
public class HomeFragment extends Fragment{

    private RecyclerView rvVerticalJobs; // UI element for displaying job listings
    private JobAdapter jobAdapter; // Adapter for job listings
    private List<Job> allJobsList; // List of all jobs
    private List<String> savedJobIds; // List of saved job IDs

    private TextView tvGreeting; // UI element for greeting text in the top
    private EditText etSearchJobs; // UI element for the search bar in the top
    private FloatingActionButton fabAddJob; // UI element for adding a new job - visible for business users only
    private LinearLayout btnOpenAdvancedFilters; // UI element for opening advanced filters page
    private ProgressBar progressBarHome; // UI element for loading indicator

    private JobRepository repository; // Repository for job data
    private DatabaseReference mUserRef; // Firebase Realtime Database reference for users

    private String filterCity = "", filterField = "", filterScope = ""; // Filters for advanced search
    private String currentUserType = ""; // store user type (business/youth)

    private boolean isUserLoaded = false; // flags to check if user data is loaded
    private boolean isJobsLoaded = false; // flags to check if job data is loaded

    public HomeFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    /* Restart the XML, attach Elements to variables,
    create JobRepository (to manage job data), and declare recycleView and Job adapter
    * */
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize repository and Firebase references
        repository = new JobRepository();
        mUserRef = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference("users");

        //creates new lists
        allJobsList = new ArrayList<>();
        savedJobIds = new ArrayList<>();

        //Initialize UI elements
        tvGreeting = view.findViewById(R.id.tvGreeting);
        etSearchJobs = view.findViewById(R.id.etSearchJobs);
        fabAddJob = view.findViewById(R.id.fabAddJob);
        btnOpenAdvancedFilters = view.findViewById(R.id.btnOpenAdvancedFilters);
        progressBarHome = view.findViewById(R.id.progressBarHome);

        // Initialize recycler view and set layout manager
        rvVerticalJobs = view.findViewById(R.id.rvVerticalJobs);
        rvVerticalJobs.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize adapter to display jobs
        jobAdapter = new JobAdapter(new ArrayList<Job>(), savedJobIds, currentUserType, new JobAdapter.OnItemClickListener() {
            @Override
            // opening the job details fragment for when user clicks on a job.
            //The method using bundle to pass the selected job data and serializable to pass the job object
            public void onItemClick(Job job) {
                JobDetailsFragment detailsFragment = new JobDetailsFragment();
                Bundle bundle = new Bundle(); // Bundle to pass data
                bundle.putSerializable("SELECTED_JOB", job); // Pass the selected job
                detailsFragment.setArguments(bundle); // Set the bundle in the fragment

                // Replace the current fragment with the details fragment
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainFragmentContainer, detailsFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
            //listen to the interface from the adapter and setup the click listener
        }, new JobAdapter.OnFavoriteClickListener() {
            @Override
            /**
            This method is called when the favorite button is clicked.
            and saves the job ID to the database in the saved_jobs node.
            the method use the updateFavorite method from the job repository
            to update the favorite status of the job
             */
            public void onFavoriteClick(Job job, boolean isCurrentlySaved) {
                // if user is logged in, save the job ID
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    // Update the favorite status in the database (in the saved_jobs node)
                    repository.updateFavorite(FirebaseAuth.getInstance().getCurrentUser().getUid(), job.jobId, isCurrentlySaved);
                }
            }
        });

        // Set the adapter
        rvVerticalJobs.setAdapter(jobAdapter);

        //setup all the listeners
        setupListeners();
        //load user data
        loadUserData();
        //start data observation
        startDataObservation();

        return view;
    }

    // Method to check if both data sources are ready before showing the list
    private void checkAndHideLoader() {
        if (isUserLoaded && isJobsLoaded) {
            progressBarHome.setVisibility(View.GONE);
            rvVerticalJobs.setVisibility(View.VISIBLE);
        }
    }


    // Sets up listeners for filter button and search bar and the favorite button
    private void setupListeners() {
        btnOpenAdvancedFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterSystemFragment filterSheet = new FilterSystemFragment();
                filterSheet.setFilterSystemListener(new FilterSystemFragment.FilterSystemListener() {
                    @Override
                    // perform changes based on the selected filters
                    public void onFiltersApplied(String city, String workField, String jobScope) {
                        filterCity = city;
                        filterField = workField;
                        filterScope = jobScope;
                        refreshDisplayList(); // Refresh the job list
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

        // Listens for changes in the search bar
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

    /* gets user data from the database, add the user name to the greeting,
    gets user type to conditionally hide/show features. when method finish,
    checkAndHideLoader is called to check if both data sources are ready
    before showing the list
    * */
    private void loadUserData() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // Fetch user data from the database and setup listener
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            mUserRef.child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                // Callback when data is fetched from the database
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    //check if the fragment is still active for preventing memory leaks
                    if (!isAdded() || getActivity() == null) {
                        return;
                    }

                    //gets user Full Name and add it to the greeting
                    if (task.isSuccessful() && task.getResult() != null) {
                        DataSnapshot snapshot = task.getResult();
                        if (snapshot.exists()) {
                            String fullName = snapshot.child("fullName").getValue(String.class);
                            if (fullName != null) {
                                tvGreeting.setText("שלום " + fullName + ",\nאיזו עבודה תרצה לחפש היום?");
                            }

                            //gets user type
                            currentUserType = snapshot.child("type").getValue(String.class);
                            // Update adapter with new user type to conditionally hide/show features
                            jobAdapter.setUserType(currentUserType);

                            if (User.TYPE_BUSINESS.equals(currentUserType)) {
                                fabAddJob.setVisibility(View.VISIBLE);
                            } else {
                                fabAddJob.setVisibility(View.GONE);
                            }
                        }
                    }
                    isUserLoaded = true;
                    //check if both data sources are ready before showing the list
                    checkAndHideLoader();
                }
            });
        } else {
            isUserLoaded = true;
            //check if both data sources are ready before showing the list
            checkAndHideLoader();
        }
    }

    /*calls the jobRepository to fetch all jobs and saved job IDs */
    private void startDataObservation() {
        /*Fetches all jobs using the method from the job repository*/
        repository.fetchAllJobs(new JobRepository.DataStatus() {
            @Override
            //Callback when jobs data is loaded
            public void onDataLoaded(List<Job> jobs) {
                //checks if the fragment is still active to prevent memory leaks
                if (!isAdded() || getActivity() == null) {
                    return;
                }
                //update the allJobsList with the fetched jobs
                allJobsList = jobs;
                //refresh the display list
                refreshDisplayList();

                //Mark as loaded
                isJobsLoaded = true;
                //check if both data sources are ready before showing the list
                checkAndHideLoader();
            }

            @Override
            // Callback when saved job IDs are loaded
            public void onSavedIdsLoaded(List<String> savedIds) {
                //checks if the fragment is still active to prevent memory leaks
                if (!isAdded() || getActivity() == null) {
                    return;
                }
                //Update adapter with the fetched saved job IDs
                jobAdapter.updateSavedJobs(savedIds);
            }

            @Override
            // Callback when an error occurs
            public void onError(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
                //Mark as loaded
                isJobsLoaded = true;
                //check if both data sources are ready before showing the list
                checkAndHideLoader();
            }
        });

        // Fetch saved job IDs from the repository
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            repository.loadSavedJobIds(FirebaseAuth.getInstance().getCurrentUser().getUid(), new JobRepository.DataStatus() {
                //callbacks when data is loaded
                @Override public void onDataLoaded(List<Job> jobs) {}

                //Callback when saved job IDs are loaded
                @Override public void onSavedIdsLoaded(List<String> savedIds) {
                    //checks if the fragment is still active to prevent memory leaks
                    if (!isAdded() || getActivity() == null) {
                        return;
                    }
                    //Update adapter with the fetched saved job IDs
                    jobAdapter.updateSavedJobs(savedIds);
                }
                // Callback when an error occurs
                @Override public void onError(String error) {}
            });
        }
    }

    /*this method runs everytime that the user types in the search bar
    or choose a filter. she takes the full list, transfer it to the repository
    to filter, and then update the adapter with the updated list */
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