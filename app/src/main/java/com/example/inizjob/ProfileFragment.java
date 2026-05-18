package com.example.inizjob;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
 * * Update: Replaced "CV" logic with hiding the menu row for business owners entirely.
 * * Update: Added Avatar logic mapping to tint colors for visual feedback.
 * * Update: Added loading indicator to improve UX and prevent pop-in.
 */
public class ProfileFragment extends Fragment {

    private ImageView imgProfileAvatar;
    private TextView tvProfileName, tvProfileEmail, tvProfileTypeBadge;
    private TextView tvRowJobsText, tvRowBusinessCodeText;

    private LinearLayout rowEditProfile, rowJobs, rowBusinessCode, rowAbout, rowRights, rowLogout;
    private View dividerBusinessCode, dividerJobs;
    private ProgressBar progressBarProfile;
    private LinearLayout layoutProfileContent;

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
        imgProfileAvatar = view.findViewById(R.id.imgProfileAvatar);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfileTypeBadge = view.findViewById(R.id.tvProfileTypeBadge);
        progressBarProfile = view.findViewById(R.id.progressBarProfile);
        layoutProfileContent = view.findViewById(R.id.layoutProfileContent);

        // Menu Rows
        rowEditProfile = view.findViewById(R.id.rowEditProfile);

        rowJobs = view.findViewById(R.id.rowJobs);
        tvRowJobsText = view.findViewById(R.id.tvRowJobsText);
        dividerJobs = view.findViewById(R.id.dividerJobs);

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
                // Now only accessible to Youth users
                if (getActivity() != null && !User.TYPE_BUSINESS.equals(currentUserType)) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainFragmentContainer, new SavedJobsFragment())
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        rowAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InfoPageFragment infoFragment = new InfoPageFragment();
                Bundle args = new Bundle();

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
            progressBarProfile.setVisibility(View.GONE);
            return;
        }

        String uid = currentUser.getUid();
        mDatabase.child("users").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!isAdded() || getActivity() == null) {
                    return;
                }

                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot != null && snapshot.exists()) {
                        User userProfile = snapshot.getValue(User.class);
                        if (userProfile != null) {
                            updateUI(userProfile);
                        }
                    }
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error loading profile data", Toast.LENGTH_SHORT).show();
                    }
                }

                // Hide progress bar and show content after loading is done
                progressBarProfile.setVisibility(View.GONE);
                layoutProfileContent.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateUI(User userProfile) {
        currentUserType = userProfile.type;

        tvProfileName.setText(userProfile.fullName);
        tvProfileEmail.setText(userProfile.email);

        // Safely check avatar type and apply color tint to simulate different avatars
        if ("boy".equals(userProfile.avatarType)) {
            imgProfileAvatar.setColorFilter(Color.parseColor("#1E88E5"), PorterDuff.Mode.SRC_IN);
        } else if ("girl".equals(userProfile.avatarType)) {
            imgProfileAvatar.setColorFilter(Color.parseColor("#E53935"), PorterDuff.Mode.SRC_IN);
        } else {
            // Default purple if null or "default"
            imgProfileAvatar.setColorFilter(Color.parseColor("#6200EE"), PorterDuff.Mode.SRC_IN);
        }

        if (User.TYPE_BUSINESS.equals(userProfile.type)) {
            tvProfileTypeBadge.setText("עסק");

            // Hide the jobs/cv row entirely for business
            rowJobs.setVisibility(View.GONE);
            dividerJobs.setVisibility(View.GONE);

            rowBusinessCode.setVisibility(View.VISIBLE);
            dividerBusinessCode.setVisibility(View.VISIBLE);

            if (userProfile.businessCode != null && !userProfile.businessCode.isEmpty()) {
                tvRowBusinessCodeText.setText(userProfile.businessCode);
            } else {
                tvRowBusinessCodeText.setText("לא הוזן");
            }

        } else {
            // Youth gets Saved Jobs access in profile
            tvProfileTypeBadge.setText("נוער");
            rowJobs.setVisibility(View.VISIBLE);
            dividerJobs.setVisibility(View.VISIBLE);
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