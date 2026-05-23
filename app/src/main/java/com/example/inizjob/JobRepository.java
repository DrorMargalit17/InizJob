package com.example.inizjob;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

/*
 * Class: JobRepository
 * Purpose: this class is responsible for managing the data related to jobs.
 */
public class JobRepository {

    private DatabaseReference mDatabase; // Firebase Realtime Database reference
    private DatabaseReference mSavedJobsRef; // Firebase Realtime Database reference for saved jobs

    // Interface to communicate and checks on the data loading process
    public interface DataStatus {
        // Callback methods for data loading
        void onDataLoaded(List<Job> jobs);
        // Critical: Added for HomeFragment sync
        void onSavedIdsLoaded(List<String> savedIds);
        // Error handling
        void onError(String errorMessage);
    }

    // Constructor for the repository, initializes the database references
    public JobRepository() {
        this.mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference("jobs");
        this.mSavedJobsRef = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference("saved_jobs");
    }

    /* this method listens to the jobs node in the database in real time.
    when data changes, it create a list of jobs.
    The method use the DataStatus interface to send the list of jobs to the UI. */
    public void fetchAllJobs(final DataStatus status) {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            /* Callback method for data changes. update the new data for the job */
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //create new list of jobs
                List<Job> jobList = new ArrayList<>();
                //passing the data from the database to the list
                for (DataSnapshot jobSnapshot : snapshot.getChildren()) {
                    Job job = jobSnapshot.getValue(Job.class);
                    if (job != null) {
                        job.jobId = jobSnapshot.getKey();
                        jobList.add(job);
                    }
                }
                //send the list of jobs to the UI
                status.onDataLoaded(jobList);
            }

            @Override
            // Callback method for database errors
            public void onCancelled(@NonNull DatabaseError error) {
                //send error message to the UI
                status.onError(error.getMessage());
            }
        });
    }

    /* This method gets the current user's list of saved job IDs (favorite jobs) */
    public void loadSavedJobIds(String userId, final DataStatus status) {
        // Listen to the saved_jobs node for the current user and add a listener
        mSavedJobsRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            // Callback method for data changes in the saved_jobs node
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Create a list of saved job IDs
                List<String> savedIds = new ArrayList<>();
                // Passing the data from the database to the list
                for (DataSnapshot child : snapshot.getChildren()) {
                    savedIds.add(child.getKey());
                }
                // Send the list of saved job IDs to the UI
                status.onSavedIdsLoaded(savedIds);
            }

            @Override
            // Callback method for database errors
            public void onCancelled(@NonNull DatabaseError error) {
                // Send error message to the UI
                status.onError(error.getMessage());
            }
        });
    }

    /* This method adds or removes a job from the user's saved jobs list in firebase.
    If the job is already saved, it removes it. If not, it adds it. */
    public void updateFavorite(String userId, String jobId, boolean isCurrentlySaved) {
        if (isCurrentlySaved) {
            // Remove the job from the user's saved jobs list
            mSavedJobsRef.child(userId).child(jobId).removeValue();
        } else {
            // Add the job to the user's saved jobs list
            mSavedJobsRef.child(userId).child(jobId).setValue(true);
        }
    }

    /* This method gets the current jobs list from the home fragment,
    and apply advanced filters to it based on user input. when the method finish,
    it return the filtered list and update the UI in the home fragment.
     */
    public List<Job> applyAdvancedFilters(List<Job> allJobs, String searchText, String city, String workField, String jobScope) {
        // Create a new list to store the filtered jobs
        List<Job> filteredList = new ArrayList<>();
        // Convert the search text to lowercase and trim it
        String searchLower = searchText.toLowerCase().trim();

        //pass over all jobs in the list to check if they match the filters
        for (Job job : allJobs) {
            boolean isMatch = true;

            //Text Search Filter
            if (!searchLower.isEmpty()) {
                boolean textMatch = false;
                if (job.title != null && job.title.toLowerCase().contains(searchLower)) textMatch = true;
                if (job.company != null && job.company.toLowerCase().contains(searchLower)) textMatch = true;
                if (job.workField != null && job.workField.toLowerCase().contains(searchLower)) textMatch = true;
                if (!textMatch) isMatch = false;
            }

            //City Filter (matches location field in Job model)
            if (isMatch && !city.isEmpty()) {
                if (job.location == null || !job.location.equals(city)) {
                    isMatch = false;
                }
            }

            //Work Field Filter
            if (isMatch && !workField.isEmpty()) {
                if (job.workField == null || !job.workField.equals(workField)) {
                    isMatch = false;
                }
            }

            //Job Scope Filter
            if (isMatch && !jobScope.isEmpty()) {
                if (job.jobScope == null || !job.jobScope.equals(jobScope)) {
                    isMatch = false;
                }
            }

            if (isMatch) {
                filteredList.add(job);
            }
        }
        return filteredList;
    }
}