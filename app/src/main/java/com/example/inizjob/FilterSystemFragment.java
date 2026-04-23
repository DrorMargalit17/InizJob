package com.example.inizjob;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

/*
 * Class: FilterSystemFragment
 * Purpose: Provides a bottom sheet interface for users to apply smart filters.
 * * Interaction: Passes selected criteria (City, Work Field, Scope) back to HomeFragment.
 */
public class FilterSystemFragment extends BottomSheetDialogFragment {

    private AutoCompleteTextView filterCity, filterWorkField, filterJobScope;
    private MaterialButton btnApplyFilters, btnClearFilters;
    private FilterSystemListener mListener;

    public interface FilterSystemListener {
        void onFiltersApplied(String city, String workField, String jobScope);
        void onClearFilters(); // Added to support clearing filters
    }

    public void setFilterSystemListener(FilterSystemListener listener) {
        this.mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_system, container, false);

        filterCity = view.findViewById(R.id.filterCity);
        filterWorkField = view.findViewById(R.id.filterWorkField);
        filterJobScope = view.findViewById(R.id.filterJobScope);
        btnApplyFilters = view.findViewById(R.id.btnApplyFilters);
        btnClearFilters = view.findViewById(R.id.btnClearFilters);

        setupDropdowns();

        btnApplyFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    String city = filterCity.getText().toString();
                    String workField = filterWorkField.getText().toString();
                    String jobScope = filterJobScope.getText().toString();

                    // Convert "הכל" or empty to empty string for logic handling
                    if (city.equals("הכל")) city = "";
                    if (workField.equals("הכל")) workField = "";
                    if (jobScope.equals("הכל")) jobScope = "";

                    mListener.onFiltersApplied(city, workField, jobScope);
                    dismiss();
                }
            }
        });

        btnClearFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClearFilters();
                    dismiss();
                }
            }
        });

        return view;
    }

    private void setupDropdowns() {
        if (getContext() == null) return;

        // Cities (Must match AddJobFragment list)
        String[] cities = new String[]{"הכל", "תל אביב", "ירושלים", "חיפה", "ראשון לציון", "פתח תקווה", "רחובות", "אשדוד", "נתניה"};
        filterCity.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, cities));

        // Work Fields
        String[] fields = new String[]{"הכל", "מסעדות ומזון", "מכירות ושירות לקוחות", "הדרכה וקייטנות", "אחר"};
        filterWorkField.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, fields));

        // Job Scopes
        String[] scopes = new String[]{"הכל", "משרה מלאה", "משרה חלקית", "משמרות", "פרויקט זמני"};
        filterJobScope.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, scopes));
    }
}