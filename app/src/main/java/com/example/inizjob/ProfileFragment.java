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
 * 2. onViewCreated - Maps all UI elements and sets click listeners for menu rows.
 * 3. fetchUserProfile - Retrieves the connected user's details from Firebase.
 * 4. updateUI - Populates the header and dynamically hides/shows menu rows based on user type (Youth/Business).
 * 5. performLogout - Securely logs out the user and clears navigation history.
 */
public class ProfileFragment extends Fragment {

    private TextView tvProfileName, tvProfileEmail, tvProfileTypeBadge;
    private TextView tvRowJobsText, tvRowBusinessCodeText;

    private LinearLayout rowEditProfile, rowJobs, rowCv, rowBusinessCode, rowContact, rowAbout, rowRights, rowLogout;
    private View dividerCv, dividerBusinessCode;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // משתנה ששומר את סוג המשתמש כדי לדעת לאן לנווט בלחיצה על כרטיס המשרות
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
        rowCv = view.findViewById(R.id.rowCv);
        dividerCv = view.findViewById(R.id.dividerCv);
        rowBusinessCode = view.findViewById(R.id.rowBusinessCode);
        tvRowBusinessCodeText = view.findViewById(R.id.tvRowBusinessCodeText);
        dividerBusinessCode = view.findViewById(R.id.dividerBusinessCode);
        rowContact = view.findViewById(R.id.rowContact);
        rowAbout = view.findViewById(R.id.rowAbout);
        rowRights = view.findViewById(R.id.rowRights);
        rowLogout = view.findViewById(R.id.rowLogout);

        // Set explicit click listeners (No Lambdas)
        rowEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "עריכת פרופיל - בקרוב...", Toast.LENGTH_SHORT).show();
            }
        });

        // עדכון הלחיצה על שורת המשרות בהתאם לסוג המשתמש
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

        rowCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainFragmentContainer, new MyCvsFragment())
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        rowContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "יצירת קשר - בקרוב...", Toast.LENGTH_SHORT).show();
            }
        });

        rowAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String aboutTitle = "אודות InizJob";
                String aboutContent = "ברוכים הבאים ל-InizJob!\n\n" +
                        "InizJob היא פלטפורמה חדשנית וחכמה שנועדה לגשר על הפער שבין בני נוער המחפשים תעסוקה ראשונה, לבין בעלי עסקים מקומיים הזקוקים לעובדים איכותיים ונמרצים.\n\n" +
                        "המטרה שלנו היא ליצור סביבה בטוחה, נגישה והוגנת. דרך האפליקציה, בני הנוער יכולים למצוא משרות שמתאימות במיוחד עבורם, ליצור קורות חיים מקצועיים תוך שניות בעזרת בינה מלאכותית (AI), ולנהל את המשרות המועדפות עליהם.\n\n" +
                        "במקביל, בעלי העסקים נהנים ממערכת ניהול משרות נוחה ויעילה, שמאפשרת להם להגיע בדיוק לקהל היעד הרלוונטי באזורם.\n\n" +
                        "פרויקט זה פותח במסגרת פרויקט גמר (5 יח\"ל) בהנדסת תוכנה. אנו מאמינים בשוויון הזדמנויות ובשקיפות מלאה לגבי זכויות עובדים.\n\n" +
                        "גרסה 1.0.0";

                InfoPageFragment infoFragment = new InfoPageFragment();
                Bundle args = new Bundle();
                args.putString("INFO_TITLE", aboutTitle);
                args.putString("INFO_CONTENT", aboutContent);
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
                String rightsTitle = "זכויות בני נוער עובדים";
                String rightsContent = "החוק בישראל מגן עליכם. חשוב שתדעו מה מגיע לכם כשאתם יוצאים לעבוד:\n\n" +
                        "1. שכר מינימום (לשעה):\n" +
                        "• עד גיל 16: 25.28 ₪\n" +
                        "• גיל 16 עד 17: 27.09 ₪\n" +
                        "• גיל 17 עד 18: 29.97 ₪\n\n" +
                        "2. תקופת התלמדות (חפיפה):\n" +
                        "חובה על המעסיק לשלם לכם על כל שעת התלמדות או 'טסט', גם אם בסוף לא התקבלתם לעבודה. אין דבר כזה 'התלמדות בחינם'.\n\n" +
                        "3. שעות עבודה:\n" +
                        "• מותר להעסיק נוער עד 8 שעות ביום (או 9 שעות במקום שעובדים בו 5 ימים בשבוע).\n" +
                        "• אסור להעסיק בני נוער בשעות נוספות בשום מצב.\n\n" +
                        "4. הפסקות:\n" +
                        "ביום עבודה שאורכו מעל 6 שעות, מגיעה לכם הפסקה של 45 דקות לפחות (מתוכה חצי שעה רצופה). זמן ההפסקה הוא לרוב על חשבונכם, אלא אם המעסיק דרש שתישארו בעמדה.\n\n" +
                        "5. עבודת לילה:\n" +
                        "• מתחת לגיל 16: אסור לעבוד אחרי השעה 20:00.\n" +
                        "• מעל גיל 16: אסור לעבוד אחרי השעה 22:00. (בחופשות רשמיות מותר עד חצות או עד 1:00 בלילה, רק בתנאי שהמעסיק דואג לכם להסעה הביתה).\n\n" +
                        "6. נסיעות:\n" +
                        "בנוסף לשכר, מגיע לכם החזר הוצאות נסיעה עבור כל יום עבודה (לפי עלות חופשי-יומי או כרטיסייה, הנמוך מביניהם).\n\n" +
                        "* המידע מוגש כהמלצה בלבד ויש להתעדכן בתקנות משרד העבודה המעודכנות.";

                InfoPageFragment infoFragment = new InfoPageFragment();
                Bundle args = new Bundle();
                args.putString("INFO_TITLE", rightsTitle);
                args.putString("INFO_CONTENT", rightsContent);
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
                        Toast.makeText(getContext(), "שגיאה בטעינת הנתונים", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void updateUI(User userProfile) {
        // שומרים את סוג המשתמש שחזר ממסד הנתונים כדי להשתמש בו בניווט
        currentUserType = userProfile.type;

        tvProfileName.setText(userProfile.fullName);
        tvProfileEmail.setText(userProfile.email);
        tvProfileTypeBadge.setText(userProfile.type);

        // Dynamic Menu Logic based on User Type
        if ("עסק".equals(userProfile.type)) {
            // Business logic
            tvRowJobsText.setText("ניהול המשרות שלי");

            // Hide CV section completely
            rowCv.setVisibility(View.GONE);
            dividerCv.setVisibility(View.GONE);

            // Show Business Code
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
            // Youth logic
            tvRowJobsText.setText("המשרות ששמרתי");

            // Show CV section
            rowCv.setVisibility(View.VISIBLE);
            dividerCv.setVisibility(View.VISIBLE);

            // Hide Business Code completely
            rowBusinessCode.setVisibility(View.GONE);
            dividerBusinessCode.setVisibility(View.GONE);
        }
    }

    private void performLogout() {
        mAuth.signOut();
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), AuthActivity.class);
            // Clear navigation stack to prevent returning
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }
}