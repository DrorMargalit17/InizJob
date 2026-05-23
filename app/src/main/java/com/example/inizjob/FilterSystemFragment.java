package com.example.inizjob;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

/*
 * Class: FilterSystemFragment
 * Purpose: Provides a bottom sheet interface for users to apply smart filters.
 * * Interaction: Passes selected criteria (City, Work Field, Scope) back to HomeFragment.
 */
public class FilterSystemFragment extends BottomSheetDialogFragment {

    //input fields that includes dropdowns of the filters
    private AutoCompleteTextView filterCity, filterWorkField, filterJobScope;

    //UI elements for buttons to apply and reset filters
    private MaterialButton btnApplyFilters, btnClearFilters;

    //Listener to communicate with HomeFragment
    private FilterSystemListener mListener;


    //create interface to communicate with HomeFragment
    public interface FilterSystemListener {
        // Callback when filters are applied
        void onFiltersApplied(String city, String workField, String jobScope);
        // Callback when filters are cleared
        void onClearFilters();
    }

    // Constructor to set the listener
    public void setFilterSystemListener(FilterSystemListener listener) {
        this.mListener = listener;
    }

    @Nullable
    @Override
    //Restarts the XML, sets the click listeners for the buttons
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_system, container, false);

        //Initialize the filter input fields
        filterCity = view.findViewById(R.id.filterCity);
        filterWorkField = view.findViewById(R.id.filterWorkField);
        filterJobScope = view.findViewById(R.id.filterJobScope);

        //Initialize the buttons
        btnApplyFilters = view.findViewById(R.id.btnApplyFilters);
        btnClearFilters = view.findViewById(R.id.btnClearFilters);

        //call the method to setup the dropdowns
        setupDropdowns();

        //call the method to initialize ux logic
        // to update button states dynamically
        setupFilterUX();

        //setup listener for apply filters button
        btnApplyFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    // Get the selected filter values
                    String city = filterCity.getText().toString();
                    String workField = filterWorkField.getText().toString();
                    String jobScope = filterJobScope.getText().toString();

                    // Convert "הכל" or empty to empty string for logic handling
                    if (city.equals("הכל")) city = "";
                    if (workField.equals("הכל")) workField = "";
                    if (jobScope.equals("הכל")) jobScope = "";

                    // Pass the selected values back to HomeFragment
                    mListener.onFiltersApplied(city, workField, jobScope);
                    // Close the dialog
                    dismiss();
                }
            }
        });

        //setup listener for clear filters button
        btnClearFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    // Clear the filter values
                    mListener.onClearFilters();
                    // Close the dialog
                    dismiss();
                }
            }
        });

        return view;
    }

    //setup the dropdowns categories
    private void setupDropdowns() {
        if (getContext() == null) return;

        // Expanded Cities list
        String[] cities = new String[]{"הכל", "תל אביב", "ירושלים", "חיפה", "ראשון לציון", "פתח תקווה", "רחובות", "אשדוד", "נתניה", "באר שבע", "חולון", "בני ברק", "רמת גן", "אשקלון", "בת ים", "מודיעין", "הרצליה", "כפר סבא", "רעננה", "חדרה", "אילת", "אחר"};
        filterCity.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, cities));

        // Expanded Work Fields list
        String[] fields = new String[]{"הכל", "מסעדות ומזון", "מכירות ושירות לקוחות", "הדרכה וקייטנות", "שליחויות ולוגיסטיקה", "אדמיניסטרציה ומזכירות", "אבטחה ושמירה", "מחשבים והייטק", "ייצור ותעשייה", "סופרמרקטים וקמעונאות", "אירועים והפקות", "אחר"};
        filterWorkField.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, fields));

        // Job Scopes
        String[] scopes = new String[]{"הכל", "משרה מלאה", "משמרות"};
        filterJobScope.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, scopes));
    }

    //This method setup dynamic color change logic based on user input
    private void setupFilterUX() {
        // Create a TextWatcher to update the button state dynamically
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                updateApplyButtonState();
            }
        };

        // Add the TextWatcher to the input fields
        filterCity.addTextChangedListener(watcher);
        filterWorkField.addTextChangedListener(watcher);
        filterJobScope.addTextChangedListener(watcher);

        //Update the button state when the fragment is created
        updateApplyButtonState();
    }

    // This method checks if at least one filter is active,
    // and updates the button state accordingly
    private void updateApplyButtonState() {
        if (getContext() == null) return;

        // Get the selected filter values
        String city = filterCity.getText().toString();
        String workField = filterWorkField.getText().toString();
        String jobScope = filterJobScope.getText().toString();

        //state if at least one filter is active
        boolean hasActiveFilter = (!city.isEmpty() && !city.equals("הכל")) ||
                (!workField.isEmpty() && !workField.equals("הכל")) ||
                (!jobScope.isEmpty() && !jobScope.equals("הכל"));

        //if statement is true, enable the button. else, disable it
        if (hasActiveFilter) {
            btnApplyFilters.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
            btnApplyFilters.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.brand_purple)));
        } else {
            btnApplyFilters.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
            btnApplyFilters.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E0E0E0")));
        }
    }
}