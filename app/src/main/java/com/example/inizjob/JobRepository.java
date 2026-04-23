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
 * Purpose: Centralizes all Firebase database operations for Jobs and provides filtering logic.
 * * Methods and Actions List:
 * 1. fetchAllJobs - Listens to all jobs in the database and updates via the DataStatus interface.
 * 2. loadSavedJobIds - Retrieves the current user's list of saved job IDs.
 * 3. toggleFavorite - Adds or removes a job ID from the user's "saved_jobs" node.
 * 4. applyAdvancedFilters - Processes a list of jobs against various criteria using explicit if-else statements.
 */
public class JobRepository {

    private DatabaseReference mDatabase;
    private DatabaseReference mSavedJobsRef;

    public interface DataStatus {
        void onDataLoaded(List<Job> jobs);
        void onSavedIdsLoaded(List<String> savedIds); // Critical: Re-added for HomeFragment sync
        void onError(String errorMessage);
    }

    public JobRepository() {
        this.mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference("jobs");
        this.mSavedJobsRef = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference("saved_jobs");
    }

    public void fetchAllJobs(final DataStatus status) {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Job> jobList = new ArrayList<>();
                for (DataSnapshot jobSnapshot : snapshot.getChildren()) {
                    Job job = jobSnapshot.getValue(Job.class);
                    if (job != null) {
                        job.jobId = jobSnapshot.getKey();
                        jobList.add(job);
                    }
                }
                status.onDataLoaded(jobList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                status.onError(error.getMessage());
            }
        });
    }

    public void loadSavedJobIds(String userId, final DataStatus status) {
        mSavedJobsRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> savedIds = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    savedIds.add(child.getKey());
                }
                status.onSavedIdsLoaded(savedIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                status.onError(error.getMessage());
            }
        });
    }

    public void toggleFavorite(String userId, String jobId, boolean isCurrentlySaved) {
        if (isCurrentlySaved) {
            mSavedJobsRef.child(userId).child(jobId).removeValue();
        } else {
            mSavedJobsRef.child(userId).child(jobId).setValue(true);
        }
    }

    public List<Job> applyAdvancedFilters(List<Job> allJobs, String searchText, String city, String workField, String jobScope) {
        List<Job> filteredList = new ArrayList<>();
        String searchLower = searchText.toLowerCase().trim();

        for (Job job : allJobs) {
            boolean isMatch = true;

            // 1. Text Search Filter (Now includes workField as requested)
            if (!searchLower.isEmpty()) {
                boolean textMatch = false;
                if (job.title != null && job.title.toLowerCase().contains(searchLower)) textMatch = true;
                if (job.company != null && job.company.toLowerCase().contains(searchLower)) textMatch = true;
                if (job.workField != null && job.workField.toLowerCase().contains(searchLower)) textMatch = true;
                if (!textMatch) isMatch = false;
            }

            // 2. City Filter (matches location field in Job model)
            if (isMatch && !city.isEmpty()) {
                if (job.location == null || !job.location.equals(city)) {
                    isMatch = false;
                }
            }

            // 3. Work Field Filter
            if (isMatch && !workField.isEmpty()) {
                if (job.workField == null || !job.workField.equals(workField)) {
                    isMatch = false;
                }
            }

            // 4. Job Scope Filter
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