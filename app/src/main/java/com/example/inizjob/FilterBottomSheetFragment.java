package com.example.inizjob;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/*
 * Class: FilterBottomSheetFragment
 * Purpose: Provides a bottom sheet interface for users to input advanced job filtering criteria.
 * * Methods and Actions List:
 * 1. onCreateView - Inflates the layout for the bottom sheet.
 * 2. onViewCreated - Initializes UI, sets up dropdown menus, and explicit button click listeners.
 * 3. applyFilters - Extracts all inputs, securely parses numbers, and passes data back via the interface.
 */
public class FilterBottomSheetFragment extends BottomSheetDialogFragment {

    private TextInputEditText etFilterLocation, etFilterSalary, etFilterAge;
    private AutoCompleteTextView etFilterWorkField, etFilterJobScope;
    private CheckBox cbFilterNoExperience, cbFilterTravel;
    private MaterialButton btnApplyFilters, btnClearFilters;

    private FilterListener listener;

    public interface FilterListener {
        void onApplyFilters(String locations, double minSalary, String workField, String jobScope, int age, boolean noExpOnly, boolean travelOnly);
        void onClearFilters();
    }

    public FilterBottomSheetFragment(FilterListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etFilterLocation = view.findViewById(R.id.etFilterLocation);
        etFilterSalary = view.findViewById(R.id.etFilterSalary);
        etFilterAge = view.findViewById(R.id.etFilterAge);
        etFilterWorkField = view.findViewById(R.id.etFilterWorkField);
        etFilterJobScope = view.findViewById(R.id.etFilterJobScope);
        cbFilterNoExperience = view.findViewById(R.id.cbFilterNoExperience);
        cbFilterTravel = view.findViewById(R.id.cbFilterTravel);
        btnApplyFilters = view.findViewById(R.id.btnApplyFilters);
        btnClearFilters = view.findViewById(R.id.btnClearFilters);

        setupDropdowns();

        btnApplyFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyFilters();
            }
        });

        btnClearFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClearFilters();
                }
                dismiss();
            }
        });
    }

    private void setupDropdowns() {
        if (getContext() != null) {
            String[] workFieldsList = new String[] {
                    "הכל", "מסעדות ומזון", "מכירות ושירות לקוחות", "הדרכה וקייטנות",
                    "שליחויות ולוגיסטיקה", "אדמיניסטרציה וכללי", "אחר"
            };
            ArrayAdapter<String> workFieldAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, workFieldsList);
            etFilterWorkField.setAdapter(workFieldAdapter);

            String[] jobScopeList = new String[] {
                    "הכל", "משרה מלאה", "משרה חלקית", "משמרות", "פרויקט זמני"
            };
            ArrayAdapter<String> scopeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, jobScopeList);
            etFilterJobScope.setAdapter(scopeAdapter);
        }
    }

    private void applyFilters() {
        String locations = etFilterLocation.getText().toString().trim();
        String workField = etFilterWorkField.getText().toString().trim();
        String jobScope = etFilterJobScope.getText().toString().trim();

        double minSalary = 0.0;
        String salaryText = etFilterSalary.getText().toString().trim();
        if (!TextUtils.isEmpty(salaryText)) {
            try {
                minSalary = Double.parseDouble(salaryText);
            } catch (NumberFormatException e) {
                minSalary = 0.0;
            }
        }

        int age = 0;
        String ageText = etFilterAge.getText().toString().trim();
        if (!TextUtils.isEmpty(ageText)) {
            try {
                age = Integer.parseInt(ageText);
            } catch (NumberFormatException e) {
                age = 0;
            }
        }

        boolean noExpOnly = cbFilterNoExperience.isChecked();
        boolean travelOnly = cbFilterTravel.isChecked();

        if (listener != null) {
            listener.onApplyFilters(locations, minSalary, workField, jobScope, age, noExpOnly, travelOnly);
        }

        dismiss();
    }
}