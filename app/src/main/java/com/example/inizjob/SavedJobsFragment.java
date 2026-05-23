package com.example.inizjob;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/*
 * Class: SavedJobsFragment
 * Purpose: Displays a list of jobs that the youth user has favorited (saved).
 */
public class SavedJobsFragment extends Fragment {

    //recyclerView for displaying saved jobs
    private RecyclerView rvSavedJobs;

    // Adapter for the RecyclerView
    private JobAdapter adapter;

    // Lists to store the data of the saved jobs objects
    private List<Job> savedJobsList;

    // Lists to store the data of the saved jobs Id's - saved in the database
    private List<String> savedJobIds;

    private FirebaseAuth mAuth; // Firebase Authentication instance
    private DatabaseReference mDatabase; // Firebase Realtime Database instance
    /* Button to navigate back to the previous screen */
    private ImageButton btnBackSavedJobs;
    /* Reference specifically for the user's saved jobs node to manage listeners */
    private DatabaseReference savedJobsRef;
    /*The listener attached to the saved jobs node. Stored here to remove it later and prevent memory leaks */
    private ValueEventListener savedJobsListener;

    public SavedJobsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    /*
     * Initializes the fragment's UI, sets up the RecyclerView and Adapter,
     * and handles click events for navigation and removing jobs from favorites.
     */
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_jobs, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference();

        //Initialize savedJobs list and adapter
        rvSavedJobs = view.findViewById(R.id.rvSavedJobs);
        rvSavedJobs.setLayoutManager(new LinearLayoutManager(getContext()));

        //Initialize back button
        btnBackSavedJobs = view.findViewById(R.id.btnBackSavedJobs);

        /*setup listener for back button handle*/
        btnBackSavedJobs.setOnClickListener(new View.OnClickListener() {
            @Override
            //replace the fragment back to the previous one when clicked
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        //Initialize lists
        savedJobsList = new ArrayList<>();
        savedJobIds = new ArrayList<>();

        //Initialize the adapter with the lists
        adapter = new JobAdapter(savedJobsList, savedJobIds, User.TYPE_YOUTH, new JobAdapter.OnItemClickListener() {
            @Override
            /*when a job is clicked,
            replace the fragment with the details fragment
            using bundle to pass the job object*/
            public void onItemClick(Job job) {
                //create the fragment
                JobDetailsFragment detailsFragment = new JobDetailsFragment();
                //create the data bundle to pass to the fragment
                Bundle bundle = new Bundle();
                /*passes the job object to the fragment,
                using the key "SELECTED_JOB" to identify it,
                using serializable to pass the object*/
                bundle.putSerializable("SELECTED_JOB", job);
                //passes the bundle with the data to the fragment
                detailsFragment.setArguments(bundle);

                //replace the current fragment with the new one
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
            //This method is called when the favorite button is clicked,
            // and handles the save/remove logic
            public void onFavoriteClick(Job job, boolean isCurrentlySaved) {
                //checks if the job id is null or empty, if so, show a toast and return
                if (job.jobId == null || job.jobId.isEmpty()) {
                    Toast.makeText(getContext(), "Error: This job is missing an ID.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mAuth.getCurrentUser() != null) {
                    //If user is logged in, gets his ID
                    String uid = mAuth.getCurrentUser().getUid();
                    /*If the job is already saved, point to the saved_jobs node
                     in the database, find the job with the given ID
                     and remove it from the database */
                    if (isCurrentlySaved) {
                        mDatabase.child("saved_jobs").child(uid).child(job.jobId).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    //Callback when data is removed
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()) {
                                            // If there's an error, show a toast
                                            Toast.makeText(getContext(), "Error removing saved job", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                    /*point to the saved_jobs node in the database,
                    find the job with the given ID and save it to the database */
                        mDatabase.child("saved_jobs").child(uid).child(job.jobId).setValue(true)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(getContext(), "Error saving job", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }
            }
        });

        // Sets the adapter for the RecyclerView
        rvSavedJobs.setAdapter(adapter);

        // Loads the data for the saved jobs
        loadSavedJobsData();

        return view;
    }

    private void loadSavedJobsData() {
        if (mAuth.getCurrentUser() == null) {
            return;
        }

        //Gets the user's ID from Firebase Authentication
        String uid = mAuth.getCurrentUser().getUid();
        //point to the saved_jobs node in the database based on the user's ID
        savedJobsRef = mDatabase.child("saved_jobs").child(uid);

        // Listener to handle changes in the saved_jobs node
        savedJobsListener = new ValueEventListener() {
            @Override
            //Callback when data is loaded
            public void onDataChange(@NonNull DataSnapshot snapshotIds) {
                if (!isAdded() || getActivity() == null) return; // Lifecycle protection

                // Clear the lists and update the adapter
                savedJobIds.clear();
                //Passing on all the saved job ids and adding them to the list
                for (DataSnapshot childSnapshot : snapshotIds.getChildren()) {
                    savedJobIds.add(childSnapshot.getKey());
                }
                //update the adapter with the new list of saved job ids
                adapter.updateSavedJobs(savedJobIds);

                //point to the jobs node in the database
                // Listener to handle changes in the jobs node
                mDatabase.child("jobs").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    //Callback when data is loaded
                    public void onDataChange(@NonNull DataSnapshot snapshotJobs) {
                        if (!isAdded() || getActivity() == null) return; // Lifecycle protection

                        // Clear the list and update the adapter
                        savedJobsList.clear();
                        /* Passing on all jobs and check if their id is in the saved job ids list
                        * if so, add them to the saved jobs list */
                        for (DataSnapshot jobSnapshot : snapshotJobs.getChildren()) {
                            Job job = jobSnapshot.getValue(Job.class);
                            if (job != null) {
                                //get the job id from the snapshot
                                job.jobId = jobSnapshot.getKey();

                                //check if the job id is in the saved job ids list
                                if (savedJobIds.contains(job.jobId)) {
                                    //add the job to the saved jobs list if statement is true
                                    savedJobsList.add(job);
                                }
                            }
                        }
                        //update the adapter with the new list of saved jobs
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    //Callback when data is loaded, but failed
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (getContext() != null) {
                            // If there's an error, show a toast
                            Toast.makeText(getContext(), "Error loading data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            //Callback when data is loaded, but failed
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        // Add the listener to the saved_jobs node
        savedJobsRef.addValueEventListener(savedJobsListener);
    }

    /*This method Prevents memory leaks by removing
    the listener when the fragment is destroyed */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (savedJobsRef != null && savedJobsListener != null) {
            savedJobsRef.removeEventListener(savedJobsListener);
        }
    }
}