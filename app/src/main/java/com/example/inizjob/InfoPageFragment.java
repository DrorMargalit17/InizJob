package com.example.inizjob;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/*
 * Class: InfoPageFragment
 * Purpose: A generic fragment to display informational text dynamically based on provided arguments.
 * * Methods and Actions List:
 * 1. onCreate - Extracts the title and content strings passed via Bundle arguments.
 * 2. onCreateView - Inflates the layout for the info page.
 * 3. onViewCreated - Initializes UI, sets the text, and handles the back button click.
 */
public class InfoPageFragment extends Fragment {

    private String pageTitle = "";
    private String pageContent = "";

    public InfoPageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey("INFO_TITLE")) {
                pageTitle = getArguments().getString("INFO_TITLE");
            }
            if (getArguments().containsKey("INFO_CONTENT")) {
                pageContent = getArguments().getString("INFO_CONTENT");
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvInfoTitle = view.findViewById(R.id.tvInfoTitle);
        TextView tvInfoContent = view.findViewById(R.id.tvInfoContent);
        ImageButton btnBackInfo = view.findViewById(R.id.btnBackInfo);

        tvInfoTitle.setText(pageTitle);
        tvInfoContent.setText(pageContent);

        btnBackInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
    }
}