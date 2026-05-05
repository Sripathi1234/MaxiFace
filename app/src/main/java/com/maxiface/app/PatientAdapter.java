package com.maxiface.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.ViewHolder> {

    List<String[]> patients;
    Context context;

    public PatientAdapter(Context context, List<String[]> patients) {
        this.context = context;
        this.patients = patients;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] p = patients.get(position);

        holder.tvName.setText(p[0]);
        holder.tvAge.setText(p[1]);
        holder.tvComplication.setText(p[2]);
        holder.tvRiskLevel.setText(p[3]);
        holder.tvRiskPct.setText(p[4]);
        holder.tvDate.setText(p[5]);

        // Risk badge color
        if (p[3].equals("High")) {
            holder.tvRiskLevel.setTextColor(Color.parseColor("#e74c3c"));
            holder.tvRiskLevel.setBackgroundColor(Color.parseColor("#ffe0e0"));
        } else if (p[3].equals("Medium")) {
            holder.tvRiskLevel.setTextColor(Color.parseColor("#f39c12"));
            holder.tvRiskLevel.setBackgroundColor(Color.parseColor("#fff3cd"));
        } else {
            holder.tvRiskLevel.setTextColor(Color.parseColor("#27ae60"));
            holder.tvRiskLevel.setBackgroundColor(Color.parseColor("#d4edda"));
        }

        // Click → open PatientDetailActivity
        holder.itemView.setOnClickListener(v -> openDetail(p));
    }

    void openDetail(String[] p) {
        try {
            // Safe parse risk percentage
            float riskPct = 0;
            try {
                String riskPctStr = p[4].replace("%", "").trim();
                if (!riskPctStr.isEmpty()) {
                    riskPct = Float.parseFloat(riskPctStr);
                }
            } catch (Exception ex) {
                riskPct = 0;
            }

            Intent intent = new Intent(context, PatientDetailActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("name", p[0] != null ? p[0] : "Unknown");
            intent.putExtra("age", p[1] != null ? p[1].replace(" yrs", "").trim() : "0");
            intent.putExtra("date", p[5] != null ? p[5] : "N/A");
            intent.putExtra("risk_level", p[3] != null ? p[3] : "Low");
            intent.putExtra("risk_percentage", riskPct);
            intent.putExtra("complication", p[2] != null ? p[2] : "Unknown");
            context.startActivity(intent);
        } catch (Exception e) {
            android.widget.Toast.makeText(context,
                    "Error opening detail: " + e.getMessage(),
                    android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAge, tvComplication, tvRiskLevel, tvRiskPct, tvDate;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAge = itemView.findViewById(R.id.tvAge);
            tvComplication = itemView.findViewById(R.id.tvComplication);
            tvRiskLevel = itemView.findViewById(R.id.tvRiskLevel);
            tvRiskPct = itemView.findViewById(R.id.tvRiskPct);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}