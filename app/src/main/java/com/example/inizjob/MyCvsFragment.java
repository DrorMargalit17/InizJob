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

/*
 * Class: MyCvsFragment
 * * Methods and Actions List:
 * 1. onCreateView - Inflates the layout file (fragment_my_cvs.xml).
 * 2. onViewCreated - Initializes UI components and click listeners.
 * 3. loadMyCvs - Fetches the list of CVs belonging to the user from Firebase.
 * 4. Bundle arguments - Used to pass the selected CV to the AddCvFragment for editing.
 */
public class MyCvsFragment extends Fragment {

    private RecyclerView rvMyCvs;
    private FloatingActionButton fabAddCv;
    private CvAdapter adapter;
    private List<Cv> cvList;

    public MyCvsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_cvs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvMyCvs = view.findViewById(R.id.rvMyCvs);
        fabAddCv = view.findViewById(R.id.fabAddCv);

        rvMyCvs.setLayoutManager(new LinearLayoutManager(getContext()));
        cvList = new ArrayList<>();

        // Initialize adapter and handle edit clicks
        adapter = new CvAdapter(getContext(), cvList, new CvAdapter.OnCvEditListener() {
            @Override
            public void onEditClick(Cv cv) {
                AddCvFragment editFragment = new AddCvFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("EDIT_CV", cv);
                editFragment.setArguments(bundle);

                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainFragmentContainer, editFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        rvMyCvs.setAdapter(adapter);

        // Handle floating action button to create a new CV
        fabAddCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainFragmentContainer, new AddCvFragment())
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        loadMyCvs();
    }

    /**
     * Retrieves the CVs created by the current user and updates the list.
     */
    private void loadMyCvs() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        String currentUid = currentUser.getUid();
        DatabaseReference cvsRef = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference("cvs");

        // Fetch only documents matching the user's ID
        Query myCvsQuery = cvsRef.orderByChild("ownerId").equalTo(currentUid);

        myCvsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cvList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Cv cv = dataSnapshot.getValue(Cv.class);
                    if (cv != null) {
                        cvList.add(cv);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading CVs", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}