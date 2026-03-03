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

import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ManageJobAdapter extends RecyclerView.Adapter<ManageJobAdapter.ManageViewHolder> {

    private List<Job> jobList;
    private Context context;
    private OnJobEditListener editListener;

    // Interface to handle edit clicks
    public interface OnJobEditListener {
        void onEditClick(Job job);
    }

    public ManageJobAdapter(Context context, List<Job> jobList, OnJobEditListener editListener) {
        this.context = context;
        this.jobList = jobList;
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public ManageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.manage_job_item, parent, false);
        return new ManageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ManageViewHolder holder, int position) {
        Job job = jobList.get(position);
        holder.tvManageTitle.setText(job.title);
        holder.tvManageLocation.setText(job.location);

        // Edit Logic
        holder.btnEditJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editListener != null) {
                    editListener.onEditClick(job);
                }
            }
        });

        // Delete Logic
        holder.btnDeleteJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (job.jobId != null) {
                    FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/")
                            .getReference("jobs")
                            .child(job.jobId)
                            .removeValue()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(context, "המשרה נמחקה בהצלחה", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public static class ManageViewHolder extends RecyclerView.ViewHolder {
        TextView tvManageTitle, tvManageLocation;
        ImageView btnDeleteJob, btnEditJob;

        public ManageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvManageTitle = itemView.findViewById(R.id.tvManageTitle);
            tvManageLocation = itemView.findViewById(R.id.tvManageLocation);
            btnDeleteJob = itemView.findViewById(R.id.btnDeleteJob);
            btnEditJob = itemView.findViewById(R.id.btnEditJob);
        }
    }
}