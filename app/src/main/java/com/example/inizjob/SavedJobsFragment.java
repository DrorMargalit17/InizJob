package com.example.inizjob;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * * Methods and Actions List:
 * 1. onCreateView - Inflates the layout and sets up the RecyclerView.
 * 2. loadSavedJobsData - Fetches both the list of saved IDs and the actual job objects, handling save state cleanly.
 */
public class SavedJobsFragment extends Fragment {

    private RecyclerView rvSavedJobs;
    private JobAdapter adapter;
    private List<Job> savedJobsList;
    private List<String> savedJobIds;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public SavedJobsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_jobs, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference();

        rvSavedJobs = view.findViewById(R.id.rvSavedJobs);
        rvSavedJobs.setLayoutManager(new LinearLayoutManager(getContext()));

        savedJobsList = new ArrayList<>();
        savedJobIds = new ArrayList<>();

        adapter = new JobAdapter(savedJobsList, savedJobIds, new JobAdapter.OnItemClickListener() {
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
                    Toast.makeText(getContext(), "שגיאה: למשרה זו חסר מזהה", Toast.LENGTH_SHORT).show();
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

        rvSavedJobs.setAdapter(adapter);

        loadSavedJobsData();

        return view;
    }

    private void loadSavedJobsData() {
        if (mAuth.getCurrentUser() == null) {
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        mDatabase.child("saved_jobs").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshotIds) {
                savedJobIds.clear();
                for (DataSnapshot childSnapshot : snapshotIds.getChildren()) {
                    savedJobIds.add(childSnapshot.getKey());
                }
                adapter.updateSavedJobs(savedJobIds);

                mDatabase.child("jobs").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshotJobs) {
                        savedJobsList.clear();
                        for (DataSnapshot jobSnapshot : snapshotJobs.getChildren()) {
                            Job job = jobSnapshot.getValue(Job.class);
                            if (job != null) {
                                job.jobId = jobSnapshot.getKey();

                                if (savedJobIds.contains(job.jobId)) {
                                    savedJobsList.add(job);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "שגיאה בטעינת הנתונים", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}