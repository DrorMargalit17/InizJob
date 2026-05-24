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
 */
public class ProfileFragment extends Fragment {

    private ImageView imgProfileAvatar; // UI element for profile avatar image - changes color based on avatar type
    private TextView tvProfileName, tvProfileEmail, tvProfileTypeBadge; // text for displaying user info
    private TextView tvRowJobsText, tvRowBusinessCodeText; // UI elements for menu row text
    /** clickable layout containers acting as menu buttons */
    private LinearLayout rowEditProfile, rowJobs, rowBusinessCode, rowAbout, rowRights, rowLogout;

    /** Visual separators lines that hide/show alongside their respective rows */
    private View dividerBusinessCode, dividerJobs;
    private ProgressBar progressBarProfile; // UI element for progress bar

    /** The main container wrapping all content, hidden during data fetch */
    private LinearLayout layoutProfileContent;

    private FirebaseAuth mAuth; // Firebase Authentication instance
    private DatabaseReference mDatabase; // Firebase Realtime Database instance

    private String currentUserType = ""; // store user type (business/youth) to handle navigation logic

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    //Restarts the XML
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    // Initialize UI elements and set click listeners for all menu buttons
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase and Database references
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/").getReference();

        //Initialize UI elements related to the profile info
        imgProfileAvatar = view.findViewById(R.id.imgProfileAvatar);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfileTypeBadge = view.findViewById(R.id.tvProfileTypeBadge);
        progressBarProfile = view.findViewById(R.id.progressBarProfile);
        layoutProfileContent = view.findViewById(R.id.layoutProfileContent);

        //Initialize UI elements related to the jobs menu button and text
        rowEditProfile = view.findViewById(R.id.rowEditProfile);
        rowJobs = view.findViewById(R.id.rowJobs);
        tvRowJobsText = view.findViewById(R.id.tvRowJobsText);
        dividerJobs = view.findViewById(R.id.dividerJobs);

        //Initialize UI elements related to the business code row
        rowBusinessCode = view.findViewById(R.id.rowBusinessCode);
        tvRowBusinessCodeText = view.findViewById(R.id.tvRowBusinessCodeText);
        dividerBusinessCode = view.findViewById(R.id.dividerBusinessCode);

        //Initialize UI elements related to the other menu buttons
        rowAbout = view.findViewById(R.id.rowAbout);
        rowRights = view.findViewById(R.id.rowRights);
        rowLogout = view.findViewById(R.id.rowLogout);

        /*set click listeners for edit profile button
        changes to editProfile fragment when clicked*/
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

        /*set click listeners for saved jobs button
        changes to savedJobs fragment when clicked
        Only accessible for youth users!*/
        rowJobs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null && !User.TYPE_BUSINESS.equals(currentUserType)) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainFragmentContainer, new SavedJobsFragment())
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        /*set click listeners for info page button
        changes to infoPage fragment when clicked*/
        rowAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create the fragment
                InfoPageFragment infoFragment = new InfoPageFragment();
                //create the data bundle to pass to the fragment
                Bundle args = new Bundle();

                //gets the title and content from the string resources
                args.putString("INFO_TITLE", getString(R.string.about_title));
                args.putString("INFO_CONTENT", getString(R.string.about_content));
                //passes the bundle with the data to the fragment
                infoFragment.setArguments(args);

                //replace the current fragment with the new one
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainFragmentContainer, infoFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        /*set click listeners for rights page button
        changes to infoPage fragment when clicked*/
        rowRights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create the fragment
                InfoPageFragment infoFragment = new InfoPageFragment();
                //create the data bundle to pass to the fragment
                Bundle args = new Bundle();

                //gets the title and content from the string resources
                args.putString("INFO_TITLE", getString(R.string.rights_title));
                args.putString("INFO_CONTENT", getString(R.string.rights_content));
                //passes the bundle with the data to the fragment
                infoFragment.setArguments(args);

                //replace the current fragment with the new one
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainFragmentContainer, infoFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        /*set click listeners for logout button
        when clicked, call the performLogout method and
        return to the login screen*/
        rowLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //perform logout
                performLogout();
            }
        });

        /*Call the method to fetch the user profile data
        from real time database*/
        fetchUserProfile();
    }

    /*Fetches the current user's profile data from firebase
    real time database. Manages the loading state by
    showing/hiding the ProgressBar and main content */
    private void fetchUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            progressBarProfile.setVisibility(View.GONE);
            return;
        }

        String uid = currentUser.getUid(); // Get user ID
        // Fetch user data from the database, and create listener to update UI
        mDatabase.child("users").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            //Callback when data is loaded
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!isAdded() || getActivity() == null) {
                    return;
                }

                /*If task is successful, get the data and update the UI
                 based on the user profile data.*/
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

                //Hide progress bar and show content after the profile data is loaded
                progressBarProfile.setVisibility(View.GONE);
                layoutProfileContent.setVisibility(View.VISIBLE);
            }
        });
    }

    /* This method update the UI based on the user profile data
    * It gets the user's type and sets the badge and
    * menu buttons visibility accordingly. It sets the avatar color
    * based on the avatar type. It sets the business code if
    * the user is a business, and hide it if the user type is youth
    * */
    private void updateUI(User userProfile) {
        //stores the user type
        currentUserType = userProfile.type;

        //sets the user's full name and email
        tvProfileName.setText(userProfile.fullName);
        tvProfileEmail.setText(userProfile.email);

        // check avatar type and apply color based on user's choice
        if ("boy".equals(userProfile.avatarType)) {
            imgProfileAvatar.setColorFilter(Color.parseColor("#1E88E5"), PorterDuff.Mode.SRC_IN);
        } else if ("girl".equals(userProfile.avatarType)) {
            imgProfileAvatar.setColorFilter(Color.parseColor("#E53935"), PorterDuff.Mode.SRC_IN);
        } else {
            // Default purple if null or "default"
            imgProfileAvatar.setColorFilter(Color.parseColor("#6200EE"), PorterDuff.Mode.SRC_IN);
        }

        // Set badge based on user type
        if (User.TYPE_BUSINESS.equals(userProfile.type)) {
            tvProfileTypeBadge.setText("עסק");

            //Hides the jobs button for a business users
            rowJobs.setVisibility(View.GONE);
            dividerJobs.setVisibility(View.GONE);

            //Reveal the business code row and divider for business users
            rowBusinessCode.setVisibility(View.VISIBLE);
            dividerBusinessCode.setVisibility(View.VISIBLE);

            //sets business code visibility if exists
            if (userProfile.businessCode != null && !userProfile.businessCode.isEmpty()) {
                tvRowBusinessCodeText.setText(userProfile.businessCode);
            } else {
                tvRowBusinessCodeText.setText("Bussines Code Missing");
            }

        } else {
            /*sets user type to youth, reveal the jobs button
             and hides the business code row*/
            tvProfileTypeBadge.setText("נוער");
            rowJobs.setVisibility(View.VISIBLE);
            dividerJobs.setVisibility(View.VISIBLE);
            tvRowJobsText.setText("המשרות ששמרתי"); //changes the text of the button

            rowBusinessCode.setVisibility(View.GONE);
            dividerBusinessCode.setVisibility(View.GONE);
        }
    }

    /*This method logs the user out of the app when the
    logout button is clicked, and returns to the login screen*/
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