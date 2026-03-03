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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyJobsFragment extends Fragment {

    private RecyclerView rvMyJobs;
    private ManageJobAdapter adapter;
    private List<Job> myJobsList;

    public MyJobsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_jobs, container, false);

        rvMyJobs = view.findViewById(R.id.rvMyJobs);
        rvMyJobs.setLayoutManager(new LinearLayoutManager(getContext()));

        myJobsList = new ArrayList<>();

        // Initialize adapter with the Edit click listener
        adapter = new ManageJobAdapter(getContext(), myJobsList, new ManageJobAdapter.OnJobEditListener() {
            @Override
            public void onEditClick(Job job) {
                // Open AddJobFragment and pass the job for editing
                AddJobFragment editFragment = new AddJobFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("EDIT_JOB", job);
                editFragment.setArguments(bundle);

                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainFragmentContainer, editFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        rvMyJobs.setAdapter(adapter);

        loadMyJobs();

        return view;
    }

    private void loadMyJobs() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference jobsRef = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference("jobs");

        Query myJobsQuery = jobsRef.orderByChild("ownerId").equalTo(currentUid);

        myJobsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myJobsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Job job = dataSnapshot.getValue(Job.class);
                    if (job != null) {
                        myJobsList.add(job);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "שגיאה בטעינת משרות", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}