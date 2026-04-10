package com.example.inizjob;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/*
 * Class: ProfileFragment
 * Purpose: Manages the user profile screen, displaying user info and a dynamic settings menu.
 * * Methods and Actions List:
 * 1. onCreateView - Inflates the layout for the profile screen.
 * 2. onViewCreated - Maps UI elements and sets explicit click listeners for the remaining menu rows.
 * 3. fetchUserProfile - Retrieves the connected user's details from Firebase Realtime Database.
 * 4. updateUI - Populates the header and dynamically hides/shows the business code row based on user type.
 * 5. performLogout - Securely logs out the user and clears navigation history.
 */
public class ProfileFragment extends Fragment {

    private TextView tvProfileName, tvProfileEmail, tvProfileTypeBadge;
    private TextView tvRowJobsText, tvRowBusinessCodeText;

    private LinearLayout rowEditProfile, rowJobs, rowBusinessCode, rowAbout, rowRights, rowLogout;
    private View dividerBusinessCode;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private String currentUserType = "";

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference();

        // Header Views
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfileTypeBadge = view.findViewById(R.id.tvProfileTypeBadge);

        // Menu Rows
        rowEditProfile = view.findViewById(R.id.rowEditProfile);
        rowJobs = view.findViewById(R.id.rowJobs);
        tvRowJobsText = view.findViewById(R.id.tvRowJobsText);

        rowBusinessCode = view.findViewById(R.id.rowBusinessCode);
        tvRowBusinessCodeText = view.findViewById(R.id.tvRowBusinessCodeText);
        dividerBusinessCode = view.findViewById(R.id.dividerBusinessCode);

        rowAbout = view.findViewById(R.id.rowAbout);
        rowRights = view.findViewById(R.id.rowRights);
        rowLogout = view.findViewById(R.id.rowLogout);

        // Explicit listeners without Lambdas
        rowEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainFragmentContainer, new EditProfileFragment())
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        rowJobs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("עסק".equals(currentUserType)) {
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.mainFragmentContainer, new MyJobsFragment())
                                .addToBackStack(null)
                                .commit();
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.mainFragmentContainer, new SavedJobsFragment())
                                .addToBackStack(null)
                                .commit();
                    }
                }
            }
        });

        rowAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InfoPageFragment infoFragment = new InfoPageFragment();
                Bundle args = new Bundle();

                // Fetching strings securely from resources
                args.putString("INFO_TITLE", getString(R.string.about_title));
                args.putString("INFO_CONTENT", getString(R.string.about_content));
                infoFragment.setArguments(args);

                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainFragmentContainer, infoFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        rowRights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InfoPageFragment infoFragment = new InfoPageFragment();
                Bundle args = new Bundle();

                // Fetching strings securely from resources
                args.putString("INFO_TITLE", getString(R.string.rights_title));
                args.putString("INFO_CONTENT", getString(R.string.rights_content));
                infoFragment.setArguments(args);

                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainFragmentContainer, infoFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        rowLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogout();
            }
        });

        fetchUserProfile();
    }

    private void fetchUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        String uid = currentUser.getUid();
        mDatabase.child("users").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot != null) {
                        if (snapshot.exists()) {
                            User userProfile = snapshot.getValue(User.class);
                            if (userProfile != null) {
                                updateUI(userProfile);
                            }
                        }
                    }
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error loading profile data", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void updateUI(User userProfile) {
        currentUserType = userProfile.type;

        tvProfileName.setText(userProfile.fullName);
        tvProfileEmail.setText(userProfile.email);
        tvProfileTypeBadge.setText(userProfile.type);

        if ("עסק".equals(userProfile.type)) {
            tvRowJobsText.setText("ניהול המשרות שלי");

            rowBusinessCode.setVisibility(View.VISIBLE);
            dividerBusinessCode.setVisibility(View.VISIBLE);

            if (userProfile.businessCode != null) {
                if (!userProfile.businessCode.isEmpty()) {
                    tvRowBusinessCodeText.setText(userProfile.businessCode);
                } else {
                    tvRowBusinessCodeText.setText("לא הוזן");
                }
            } else {
                tvRowBusinessCodeText.setText("לא הוזן");
            }

        } else {
            tvRowJobsText.setText("המשרות ששמרתי");
            rowBusinessCode.setVisibility(View.GONE);
            dividerBusinessCode.setVisibility(View.GONE);
        }
    }

    private void performLogout() {
        mAuth.signOut();
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), AuthActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }
}