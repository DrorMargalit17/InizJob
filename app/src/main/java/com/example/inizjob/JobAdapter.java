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
 * Update: Conditionally hides the favorite star if the user is a Business.
 */
public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private List<Job> jobList;
    private List<String> savedJobIds;
    private String userType; // Added userType to determine visibility
    private OnItemClickListener listener;
    private OnFavoriteClickListener favListener;

    public interface OnItemClickListener {
        void onItemClick(Job job);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Job job, boolean isCurrentlySaved);
    }

    public JobAdapter(List<Job> jobList, List<String> savedJobIds, String userType, OnItemClickListener listener, OnFavoriteClickListener favListener) {
        this.jobList = jobList;
        this.savedJobIds = savedJobIds;
        this.userType = userType;
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

    // Method to update userType dynamically from HomeFragment
    public void setUserType(String userType) {
        this.userType = userType;
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

        // Logic to hide the favorite button for Business users
        if (User.TYPE_BUSINESS.equals(userType)) {
            holder.btnFavorite.setVisibility(View.GONE);
        } else {
            holder.btnFavorite.setVisibility(View.VISIBLE);
            boolean isSaved = false;
            if (savedJobIds != null && savedJobIds.contains(job.jobId)) {
                isSaved = true;
            }

            if (isSaved) {
                holder.btnFavorite.setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                holder.btnFavorite.setImageResource(android.R.drawable.btn_star_big_off);
            }

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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(job);
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
        ImageView btnFavorite;

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