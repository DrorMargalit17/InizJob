package com.example.inizjob;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private List<Job> jobList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Job job);
    }

    public JobAdapter(List<Job> jobList, OnItemClickListener listener) {
        this.jobList = jobList;
        this.listener = listener;
    }

    // Method to update the list when searching
    public void filterList(List<Job> filteredList) {
        this.jobList = filteredList;
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
        ImageView imgJob;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvCompany = itemView.findViewById(R.id.tvCompany);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            imgJob = itemView.findViewById(R.id.imgJob);
        }
    }
}