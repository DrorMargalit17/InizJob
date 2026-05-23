package com.example.inizjob;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class CvAdapter extends RecyclerView.Adapter<CvAdapter.CvViewHolder> {

    /*The main list containing all cv objects to be displayed*/
    private List<Cv> cvList;

    /*The application or activity context, required for inflating layouts and showing Toasts */
    private Context context;

    /*Callback interface for handling clicks on the edit button */
    private OnCvEditListener editListener;

    // Interface definition for a callback to be activated when the edit button is clicked.
    public interface OnCvEditListener {
        void onEditClick(Cv cv);
    }

    // Constructor of the adapter
    public CvAdapter(Context context, List<Cv> cvList, OnCvEditListener editListener) {
        this.context = context;
        this.cvList = cvList;
        this.editListener = editListener;
    }

    @NonNull
    @Override
    /**
     * Creates the ViewHolder for a single list item.
     * This is called only when the RecyclerView needs a new View to display on screen.
     */
    public CvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cv_item, parent, false);
        return new CvViewHolder(view);
    }

    @Override
    /**
     * Binds the specific data of a Cv object to the UI elements of the ViewHolder.
     * Handles text injection, and sets up the click listeners.
     */
    public void onBindViewHolder(@NonNull CvViewHolder holder, int position) {
        // Get the current cv object
        Cv cv = cvList.get(position);

        // // Set the text of the UI elements based on the cv object
        if (cv.cvTitle != null && !cv.cvTitle.isEmpty()) {
            holder.tvCvTitle.setText(cv.cvTitle);
        } else {
            // Set a default title if the cvTitle is null or empty
            holder.tvCvTitle.setText("קורות חיים אישיים");
        }

        holder.tvCvPreview.setText("השכלה: " + cv.education);

        // Set up the click listener for the edit button
        holder.btnEditCv.setOnClickListener(new View.OnClickListener() {
            @Override
            /** when the button is clicked, trigger the callback with the cv object,
            and navigate to the AddCvFragment for editing*/
            public void onClick(View v) {
                if (editListener != null) {
                    editListener.onEditClick(cv);
                }
            }
        });

        // Set up the click listener for the delete button
        holder.btnDeleteCv.setOnClickListener(new View.OnClickListener() {
            @Override
            // when the button is clicked, delete the cv from the database
            public void onClick(View v) {
                if (cv.cvId != null) {
                    //point to the cv node, get to the specific cv using the cvId and delete it
                    FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/")
                            .getReference("cvs")
                            .child(cv.cvId)
                            .removeValue()
                            //set up listener when the cv is deleted
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                //callback when the cv is deleted
                                public void onComplete(@NonNull Task<Void> task) {
                                    //if the cv was deleted successfully, show a toast
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "Cv deleted", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    @Override
    // Get the number of items in the list so the RecyclerView knows how many items to display
    public int getItemCount() {
        return cvList.size();
    }

    /** A structural class that holds references to the UI elements within a cv_item layout. */
    public static class CvViewHolder extends RecyclerView.ViewHolder {
        TextView tvCvTitle, tvCvPreview;
        ImageView btnDeleteCv, btnEditCv;

        public CvViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCvTitle = itemView.findViewById(R.id.tvCvTitle);
            tvCvPreview = itemView.findViewById(R.id.tvCvPreview);
            btnDeleteCv = itemView.findViewById(R.id.btnDeleteCv);
            btnEditCv = itemView.findViewById(R.id.btnEditCv);
        }
    }
}