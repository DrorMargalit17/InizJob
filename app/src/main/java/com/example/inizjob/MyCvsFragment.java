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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Class: MyCvsFragment
 * Purpose: Retrieves and displays a list of CVs specifically created by the currently logged-in youth user.
 */
public class MyCvsFragment extends Fragment {

    // UI element for recycler view
    private RecyclerView rvMyCvs;
    // UI element for floating action button
    private FloatingActionButton fabAddCv;
    // Adapter for the recycler view
    private CvAdapter adapter;
    // List of CVs
    private List<Cv> cvList;

    // Database references and listeners to handle memory management securely
    private Query myCvsQuery; // Query to filter CVs by ownerId
    private ValueEventListener myCvsListener; // Listener for real-time updates

    public MyCvsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    // Inflates the layout for this fragment
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_cvs, container, false);
    }

    @Override
    // Initializes the UI components and click listeners
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the recycler view and set the layout manager
        rvMyCvs = view.findViewById(R.id.rvMyCvs);
        rvMyCvs.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the floating action button (FAB)
        fabAddCv = view.findViewById(R.id.fabAddCv);

        // Create new empty arrayList of cv's
        cvList = new ArrayList<>();

        // Initialize adapter and handle the edit button clicks
        adapter = new CvAdapter(getContext(), cvList, new CvAdapter.OnCvEditListener() {
            @Override
            /**
             * This method is called when the edit button is clicked.
             * It wraps the CV data in a bundle and passes it to the AddCvFragment for editing.
             */
            public void onEditClick(Cv cv) {
                // Create a new instance of AddCvFragment and pass the selected CV
                AddCvFragment editFragment = new AddCvFragment();
                // Pass the selected CV as an argument
                Bundle bundle = new Bundle();
                // Put the selected CV in the bundle
                bundle.putSerializable("EDIT_CV", cv);
                // Set the arguments of the fragment and pass the bundle
                editFragment.setArguments(bundle);

                // Replace the current fragment with the addCvFragment
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainFragmentContainer, editFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        // Set the adapter for the recycler view
        rvMyCvs.setAdapter(adapter);

        // setup listener for the floating action button
        fabAddCv.setOnClickListener(new View.OnClickListener() {
            @Override
            // when the button is clicked, navigate to the AddCvFragment
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainFragmentContainer, new AddCvFragment())
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        // Loads the cv's that the user created when the fragment is created
        loadMyCvs();
    }

    /**
     * Fetches the list of CVs belonging to the current user from Firebase.
     * Uses a real-time query filtered by the user's ID and updates the RecyclerView.
     */
    private void loadMyCvs() {
        // Check if there's a user logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        // Get the user's ID
        String currentUid = currentUser.getUid();
        // Reference to the "cvs" node in the database
        DatabaseReference cvsRef = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference("cvs");

        // Fetch all the cv's that their ownerId is equal to the current User ID
        myCvsQuery = cvsRef.orderByChild("ownerId").equalTo(currentUid);

        // Listen for changes in the "cvs" node
        myCvsListener = new ValueEventListener() {
            @Override
            // Called when the data changes in the "cvs" node
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the list
                cvList.clear();
                // adding to the list all the cv's that their ownerId is equal to the current User ID
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Cv cv = dataSnapshot.getValue(Cv.class);
                    if (cv != null) {
                        // Add the CV to the list
                        cvList.add(cv);
                    }
                }
                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged();
            }

            @Override
            // Called when a database error occurs
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading CVs", Toast.LENGTH_SHORT).show();
                }
            }
        };

        // Activates the data flow by adding the listener to the query
        myCvsQuery.addValueEventListener(myCvsListener);
    }

    // Prevents memory leaks by removing the listener when the fragment is destroyed
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (myCvsQuery != null && myCvsListener != null) {
            myCvsQuery.removeEventListener(myCvsListener);
        }
    }
}