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

/*
 * Class: ManageJobAdapter
 * Purpose: Binds job data to the RecyclerView for business owners to manage their posts.
 */
public class ManageJobAdapter extends RecyclerView.Adapter<ManageJobAdapter.ManageViewHolder> {

    /*The main list containing all job objects to be displayed*/
    private List<Job> jobList;

    /*The application or activity context, required for inflating layouts and showing Toasts */
    private Context context;

    /*Callback interface for handling clicks on the edit button */
    private OnJobEditListener editListener;

    // Interface definition for a callback to be activated when the edit button is clicked.
    public interface OnJobEditListener {
        void onEditClick(Job job);
    }

    // Constructor of the adapter
    public ManageJobAdapter(Context context, List<Job> jobList, OnJobEditListener editListener) {
        this.context = context;
        this.jobList = jobList;
        this.editListener = editListener;
    }

    @NonNull
    @Override
    /**
     * Creates the ViewHolder for a single list item.
     * This is called only when the RecyclerView needs a new View to display on screen.
     */
    public ManageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.manage_job_item, parent, false);
        return new ManageViewHolder(view);
    }
    @Override
    /**
     * Binds the specific data of a Job object to the UI elements of the ViewHolder.
     * Handles text injection, and sets up the click listeners.
     */
    public void onBindViewHolder(@NonNull ManageViewHolder holder, int position) {
        // Get the current job object
        Job job = jobList.get(position);

        // Set the text of the UI elements based on the job object
        holder.tvManageTitle.setText(job.title);
        holder.tvManageLocation.setText(job.exactAddress);

        /* Set up the click listener for the edit button
        (when the button is clicked, trigger the callback with the job object) */
        holder.btnEditJob.setOnClickListener(new View.OnClickListener() {
            @Override
            //when the button is clicked, trigger the callback with the job object
            //and navigate to the AddJobFragment for editing
            public void onClick(View v) {
                if (editListener != null) {
                    editListener.onEditClick(job);
                }
            }
        });

        // Set up the click listener for the delete button
        holder.btnDeleteJob.setOnClickListener(new View.OnClickListener() {
            @Override
            //when the button is clicked, delete the job from the database
            public void onClick(View v) {
                if (job.jobId != null) {
                    //point to the job node, get to the specific job using the jobId and delete it
                    FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/")
                            .getReference("jobs")
                            .child(job.jobId)
                            .removeValue()
                            //set up listener when the job is deleted
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                //callback when the job is deleted
                                public void onComplete(@NonNull Task<Void> task) {
                                    //if the job was deleted successfully, show a toast
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "The job was successfully deleted.", Toast.LENGTH_SHORT).show();
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
        return jobList.size();
    }

    // A structural class that holds references to the UI elements within a manage_job_item layout.
    public static class ManageViewHolder extends RecyclerView.ViewHolder {
        TextView tvManageTitle, tvManageLocation;
        ImageView btnDeleteJob, btnEditJob;

        // Constructor of the ViewHolder
        public ManageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvManageTitle = itemView.findViewById(R.id.tvManageTitle);
            tvManageLocation = itemView.findViewById(R.id.tvManageLocation);
            btnDeleteJob = itemView.findViewById(R.id.btnDeleteJob);
            btnEditJob = itemView.findViewById(R.id.btnEditJob);
        }
    }
}