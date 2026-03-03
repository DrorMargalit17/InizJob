package com.example.inizjob;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class homeFragment extends Fragment {

    private RecyclerView rvHorizontalJobs;
    private JobAdapter jobAdapter;
    private List<Job> jobList;

    public homeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 2. Find the RecyclerView
        rvHorizontalJobs = view.findViewById(R.id.rvHorizontalJobs);

        // 3. Set horizontal layout manager for side-scrolling
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvHorizontalJobs.setLayoutManager(layoutManager);

        // 4. Create temporary fake data to test the UI
        jobList = new ArrayList<>();
        jobList.add(new Job("מלצר/ית לאירועים", "גן אירועים 'האחוזה'", "תל אביב", "50 ₪/שעה"));
        jobList.add(new Job("עובד/ת דלפק", "רשת 'קפה קפה'", "הרצליה", "45 ₪/שעה"));
        jobList.add(new Job("בייביסיטר", "משפחת כהן", "מודיעין", "40 ₪/שעה"));

        // 5. Attach the adapter to the RecyclerView
        jobAdapter = new JobAdapter(jobList);
        rvHorizontalJobs.setAdapter(jobAdapter);

        return view;
    }
}