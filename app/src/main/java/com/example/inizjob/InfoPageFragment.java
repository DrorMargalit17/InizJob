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
 * Purpose: A generic fragment to display informational text
 * dynamically based on provided arguments.
 */
public class InfoPageFragment extends Fragment {

    //Store the title and content strings, reset them to empty strings
    private String pageTitle = "";
    private String pageContent = "";

    public InfoPageFragment() {
        // Required empty public constructor
    }

    @Override
    //Extract the title and content strings passed with Bundle arguments
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
    //Reset the XML
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info_page, container, false);
    }

    @Override
    //Initialize UI, set the text, and setup listeners
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Initialize Title and content text views
        TextView tvInfoTitle = view.findViewById(R.id.tvInfoTitle);
        TextView tvInfoContent = view.findViewById(R.id.tvInfoContent);

        //Initialize back button
        ImageButton btnBackInfo = view.findViewById(R.id.btnBackInfo);

        /*Set the title and content text views accordingly
        to the passed arguments from the bundle */
        tvInfoTitle.setText(pageTitle);
        tvInfoContent.setText(pageContent);

        //Setup listener for back button
        btnBackInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            //when clicked, returns to previous fragment
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
    }
}