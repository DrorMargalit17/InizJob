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
 * Purpose: Adapter for displaying job items in a RecyclerView. Handles item clicks and favorite toggling.
 * * Methods and Actions List:
 * 1. onCreateViewHolder - Inflates the job_item layout.
 * 2. onBindViewHolder - Binds job data to the UI, checks if the job is saved, and sets click listeners.
 * 3. filterList - Updates the list of jobs when the user uses the search bar.
 * 4. updateSavedJobs - Updates the list of saved job IDs to refresh the star icons dynamically.
 */
public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private List<Job> jobList;
    private List<String> savedJobIds;
    private OnItemClickListener listener;
    private OnFavoriteClickListener favListener;

    public interface OnItemClickListener {
        void onItemClick(Job job);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Job job, boolean isCurrentlySaved);
    }

    public JobAdapter(List<Job> jobList, List<String> savedJobIds, OnItemClickListener listener, OnFavoriteClickListener favListener) {
        this.jobList = jobList;
        this.savedJobIds = savedJobIds;
        this.listener = listener;
        this.favListener = favListener;
    }

    public void filterList(List<Job> filteredList) {
        this.jobList = filteredList;
        notifyDataSetChanged();
    }

    public void updateSavedJobs(List<String> newSavedJobIds) {
        this.savedJobIds = newSavedJobIds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.job_item, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);
        holder.tvJobTitle.setText(job.title);
        holder.tvCompany.setText(job.company);
        holder.tvLocation.setText(job.location);
        holder.tvSalary.setText(String.valueOf(job.salary) + " ₪ / שעה");

        // Determine if this specific job is in the saved list
        boolean isSaved = false;
        if (savedJobIds != null) {
            if (savedJobIds.contains(job.jobId)) {
                isSaved = true;
            }
        }

        // Set the appropriate star icon based on the saved status
        if (isSaved) {
            holder.btnFavorite.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            holder.btnFavorite.setImageResource(android.R.drawable.btn_star_big_off);
        }

        // Handle item click (open details)
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(job);
                }
            }
        });

        // Handle favorite star click
        final boolean currentSavedState = isSaved;
        holder.btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (favListener != null) {
                    favListener.onFavoriteClick(job, currentSavedState);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView tvJobTitle, tvCompany, tvLocation, tvSalary;
        ImageView imgJob, btnFavorite;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvCompany = itemView.findViewById(R.id.tvCompany);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            imgJob = itemView.findViewById(R.id.imgJob);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}