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
        void onSavedIdsLoaded(List<String> savedIds);
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

    public List<Job> applyAdvancedFilters(List<Job> allJobs, String searchText, String locations,
                                          double minSalary, String workField, String jobScope,
                                          int age, boolean noExpOnly, boolean travelOnly) {

        List<Job> filteredList = new ArrayList<>();
        String searchLower = searchText.toLowerCase().trim();

        for (Job job : allJobs) {
            boolean isMatch = true;

            // 1. Text Search Filter
            if (!searchLower.isEmpty()) {
                boolean textMatch = false;
                if (job.title != null) {
                    if (job.title.toLowerCase().contains(searchLower)) {
                        textMatch = true;
                    }
                }
                if (job.company != null) {
                    if (job.company.toLowerCase().contains(searchLower)) {
                        textMatch = true;
                    }
                }
                if (!textMatch) {
                    isMatch = false;
                }
            }

            // 2. Location Filter
            if (isMatch) {
                if (!locations.isEmpty()) {
                    boolean locMatch = false;
                    String[] splitLocs = locations.split(",");
                    for (String loc : splitLocs) {
                        String cleanLoc = loc.trim().toLowerCase();
                        if (job.location != null) {
                            if (job.location.toLowerCase().contains(cleanLoc)) {
                                locMatch = true;
                            }
                        }
                    }
                    if (!locMatch) {
                        isMatch = false;
                    }
                }
            }

            // 3. Minimum Salary Filter
            if (isMatch) {
                if (minSalary > 0) {
                    if (job.salary < minSalary) {
                        isMatch = false;
                    }
                }
            }

            // 4. Work Field Filter
            if (isMatch) {
                if (!workField.isEmpty()) {
                    if (!workField.equals("הכל")) {
                        if (job.workField == null) {
                            isMatch = false;
                        } else {
                            if (!job.workField.equals(workField)) {
                                isMatch = false;
                            }
                        }
                    }
                }
            }

            // 5. Job Scope Filter
            if (isMatch) {
                if (!jobScope.isEmpty()) {
                    if (!jobScope.equals("הכל")) {
                        if (job.jobScope == null) {
                            isMatch = false;
                        } else {
                            if (!job.jobScope.equals(jobScope)) {
                                isMatch = false;
                            }
                        }
                    }
                }
            }

            // 6. Age Filter
            if (isMatch) {
                if (age > 0) {
                    if (age < job.minAge) {
                        isMatch = false;
                    }
                }
            }

            // 7. Experience Filter
            if (isMatch) {
                if (noExpOnly) {
                    if (job.requiresExperience) {
                        isMatch = false;
                    }
                }
            }

            // 8. Travel Expenses Filter
            if (isMatch) {
                if (travelOnly) {
                    if (!job.travelExpenses) {
                        isMatch = false;
                    }
                }
            }

            if (isMatch) {
                filteredList.add(job);
            }
        }
        return filteredList;
    }
}