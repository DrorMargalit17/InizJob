package com.example.inizjob;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/*
 * Class: JobAdapter
 * Purpose: Handles displaying jobs in the main list.
 */
public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    /*The main list containing all job objects to be displayed*/
    private List<Job> jobList;

    /*A list of job IDs that the current user has saved. (favorited) */
    private List<String> savedJobIds;

    /*Stores the user type (business/youth) to conditionally hide/show features*/
    private String userType;

    /*Callback interface for handling clicks on the entire job item*/
    private OnItemClickListener listener;

    /*Callback interface for handling clicks specifically on the favorite star button */
    private OnFavoriteClickListener favListener;

    /**
     * Interface definition for a callback to be activated when a job item is clicked.
     */
    public interface OnItemClickListener {
        void onItemClick(Job job);
    }

    /**
     * Interface definition for a callback to be activated when the favorite icon is clicked.
     */
    public interface OnFavoriteClickListener {
        void onFavoriteClick(Job job, boolean isCurrentlySaved);
    }

    // Constructor of the adapter
    public JobAdapter(List<Job> jobList, List<String> savedJobIds, String userType, OnItemClickListener listener, OnFavoriteClickListener favListener) {
        this.jobList = jobList;
        this.savedJobIds = savedJobIds;
        this.userType = userType;
        this.listener = listener;
        this.favListener = favListener;
    }

    /**
     * Updates the main job list with a newly filtered list and refreshes the UI.
     * @param filteredList The new list of jobs that matched the search criteria.
     */
    public void filterList(List<Job> filteredList) {
        this.jobList = filteredList;
        notifyDataSetChanged();
    }

    /**
     * Updates the list of saved job IDs (e.g., when the user favorites a new job)
     * and triggers a UI refresh to update the star icons.
     * @param newSavedJobIds The updated list of saved job IDs.
     */
    public void updateSavedJobs(List<String> newSavedJobIds) {
        this.savedJobIds = newSavedJobIds;
        notifyDataSetChanged();
    }

    /**
     * Dynamically updates the user type and refreshes the list to apply UI rules
     * (like hiding the favorite button for businesses).
     * @param userType The new user type string.
     */
    public void setUserType(String userType) {
        this.userType = userType;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    /**
     * Creates the ViewHolder for a single list item.
     * This is called only when the RecyclerView needs a new View to display on screen.
     */

    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.job_item, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    /**
     * Binds the specific data of a Job object to the UI elements of the ViewHolder.
     * Handles text injection, UI conditional rendering (favorite button visibility),
     * and sets up the click listeners.
     */
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        // Get the current job object
        Job job = jobList.get(position);

        // Set the text of the UI elements based on the job object
        holder.tvJobTitle.setText(job.title);
        holder.tvCompany.setText(job.company);

        holder.tvLocation.setText(job.location);
        holder.tvSalary.setText(String.valueOf(job.salary) + " ₪ / שעה");

        // Conditionally hide the favorite button for Business users
        if (User.TYPE_BUSINESS.equals(userType)) {
            holder.btnFavorite.setVisibility(View.GONE);
        } else {
            holder.btnFavorite.setVisibility(View.VISIBLE);
            boolean isSaved = false;
            // Check if the job is saved
            if (savedJobIds != null && savedJobIds.contains(job.jobId)) {
                isSaved = true;
            }

            // Set the star icon based on the saved status
            if (isSaved) {
                holder.btnFavorite.setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                holder.btnFavorite.setImageResource(android.R.drawable.btn_star_big_off);
            }

            final boolean currentSavedState = isSaved;
            // Set up the click listener for the favorite button
            holder.btnFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                // When the favorite button is clicked, trigger the callback with the job object
                public void onClick(View v) {
                    if (favListener != null) {
                        favListener.onFavoriteClick(job, currentSavedState);
                    }
                }
            });
        }

        // Set up the click listener for the entire job item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            // When the item is clicked, trigger the callback with the job object
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(job);
                }
            }
        });
    }

    // Get the number of items in the list so the RecyclerView knows how many items to display
    @Override
    public int getItemCount() {
        return jobList.size();
    }

    /**
     * A structural class that holds references to the UI elements within a job_item layout.
     * Used to avoid repeated and expensive findViewById() calls during scrolling.
     */
    public static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView tvJobTitle, tvCompany, tvLocation, tvSalary;
        ImageView btnFavorite;

        // Constructor of the ViewHolder
        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvCompany = itemView.findViewById(R.id.tvCompany);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}