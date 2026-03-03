package com.example.inizjob;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvHorizontalJobs;
    private JobAdapter jobAdapter;
    private List<Job> jobList;

    // UI components for greeting
    private TextView tvGreeting;

    // Firebase components
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 1. Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference();

        // 2. Find UI Components
        rvHorizontalJobs = view.findViewById(R.id.rvHorizontalJobs);
        tvGreeting = view.findViewById(R.id.tvGreeting);

        // 3. Load user name from Firebase
        loadUserName();

        // 4. Setup RecyclerView (ReverseLayout = true for Right-To-Left scrolling)
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true);
        rvHorizontalJobs.setLayoutManager(layoutManager);

        // 5. Create dummy data
        jobList = new ArrayList<>();
        jobList.add(new Job("מלצר/ית לאירועים", "גן אירועים 'האחוזה'", "תל אביב", "50 ₪/שעה"));
        jobList.add(new Job("עובד/ת דלפק", "רשת 'קפה קפה'", "הרצליה", "45 ₪/שעה"));
        jobList.add(new Job("בייביסיטר", "משפחת כהן", "מודיעין", "40 ₪/שעה"));

        jobAdapter = new JobAdapter(jobList);
        rvHorizontalJobs.setAdapter(jobAdapter);

        return view;
    }

    // Method to fetch the user's name from Firebase and display the greeting
    private void loadUserName() {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();

            // Read the "fullName" field from the database
            mDatabase.child("users").child(uid).child("fullName").get()
                    .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.isSuccessful() && task.getResult().exists()) {
                                String fullName = task.getResult().getValue(String.class);
                                tvGreeting.setText("שלום " + fullName + ",\nאיזו עבודה תרצה לחפש היום?");
                            } else {
                                // Fallback if name is not found
                                tvGreeting.setText("שלום,\nאיזו עבודה תרצה לחפש היום?");
                            }
                        }
                    });
        }
    }
}