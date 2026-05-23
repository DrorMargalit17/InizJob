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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Class: MyJobsFragment
 * Purpose: Retrieves and displays a list of jobs specifically posted by the currently logged-in business user.
 */
public class MyJobsFragment extends Fragment {

    private RecyclerView rvMyJobs; // UI element for RecyclerView
    private ManageJobAdapter adapter; // Adapter for RecyclerView
    private List<Job> myJobsList;// List to store jobs

    // UI element for FAB (floating action button)
    private FloatingActionButton fabAddJobMyJobs;

    // Database references and listeners to handle memory management
    private Query myJobsQuery; // Query to filter jobs by ownerId
    private ValueEventListener myJobsListener; // Listener for real-time updates

    public MyJobsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    //Reset the xml, Initialize UI elements and set up listeners
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_jobs, container, false);

        //Initialize recycler view and set layout manager
        rvMyJobs = view.findViewById(R.id.rvMyJobs);
        rvMyJobs.setLayoutManager(new LinearLayoutManager(getContext()));

        //Initialize the action button
        fabAddJobMyJobs = view.findViewById(R.id.fabAddJobMyJobs);

        //Create new arrayList to store jobs
        myJobsList = new ArrayList<>();

        //Initialize adapter with the Edit click listener
        adapter = new ManageJobAdapter(getContext(), myJobsList, new ManageJobAdapter.OnJobEditListener() {
            @Override
            // Callback when the edit button is clicked
            /*when the edit button on a job is clicked,
            * the method opens the AddJobFragment and passes the job
            * details for editing*/
            public void onEditClick(Job job) {
                // Create a new instance of the AddJobFragment
                AddJobFragment editFragment = new AddJobFragment();
                // Pass the job details to the fragment using a bundle
                Bundle bundle = new Bundle();
                //transfer the data in the bundle using serializable
                bundle.putSerializable("EDIT_JOB", job);
                // Set the bundle as arguments for the fragment
                editFragment.setArguments(bundle);

                // Replace the current fragment with the addJob fragment
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainFragmentContainer, editFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        // Set the adapter for the RecyclerView
        rvMyJobs.setAdapter(adapter);

        //setup listener for the FAB to open AddJobFragment
        fabAddJobMyJobs.setOnClickListener(new View.OnClickListener() {
            @Override
            //when clicked, open AddJobFragment
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainFragmentContainer, new AddJobFragment())
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        //Loads all the jobs the user has created
        loadMyJobs();

        return view;
    }

    /*This method points to the jobs node in the database,
    search for jobs that added by the current user
    (using the user ID to identify them)
     and add them to the myJobslist */
    private void loadMyJobs() {
        // Check if their is a user logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        // Get the current user's UID
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Get a reference to the "jobs" node in the database
        DatabaseReference jobsRef = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference("jobs");

        /*Real-time query to fetch only the jobs where
        ownerId matches the current user ID */
        myJobsQuery = jobsRef.orderByChild("ownerId").equalTo(currentUid);

        //Create a listener to handle real-time updates
        myJobsListener = new ValueEventListener() {
            @Override
            // Callback when data is changed in the database
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the list and add new jobs
                myJobsList.clear();
                /*Passing the data from the database to the list */
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Job job = dataSnapshot.getValue(Job.class);
                    if (job != null) {
                        myJobsList.add(job);
                    }
                }
                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged();
            }

            @Override
            // Callback when the query is cancelled (e.g., database error)
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading jobs", Toast.LENGTH_SHORT).show();
                }
            }
        };

        //Activates the data flow by adding the listener to the query
        myJobsQuery.addValueEventListener(myJobsListener);
    }

    // Prevents memory leaks by removing the listener when
    // the fragment is destroyed
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (myJobsQuery != null && myJobsListener != null) {
            myJobsQuery.removeEventListener(myJobsListener);
        }
    }
}