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

    private List<Cv> cvList;
    private Context context;
    private OnCvEditListener editListener;

    public interface OnCvEditListener {
        void onEditClick(Cv cv);
    }

    public CvAdapter(Context context, List<Cv> cvList, OnCvEditListener editListener) {
        this.context = context;
        this.cvList = cvList;
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public CvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cv_item, parent, false);
        return new CvViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CvViewHolder holder, int position) {
        Cv cv = cvList.get(position);
        holder.tvCvTitle.setText("קורות חיים: " + cv.fullName);
        holder.tvCvPreview.setText("השכלה: " + cv.education);

        holder.btnEditCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editListener != null) {
                    editListener.onEditClick(cv);
                }
            }
        });

        holder.btnDeleteCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cv.cvId != null) {
                    FirebaseDatabase.getInstance("https://inizjob4586-default-rtdb.firebaseio.com/")
                            .getReference("cvs")
                            .child(cv.cvId)
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "קורות החיים נמחקו", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return cvList.size();
    }

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