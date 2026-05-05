package com.maxiface.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.ViewHolder> {

    String[] icons = {"🦷", "🔬", "📊"};
    String[] titles = {
            "Welcome to MaxiFace",
            "Enter Patient Data",
            "Get Instant Results"
    };
    String[] descriptions = {
            "An ML-powered app designed for maxillofacial surgeons to predict postoperative complications before surgery.",
            "Simply enter patient details like age, medical history, fracture type and optionally upload a CT scan image.",
            "Get instant risk prediction with detailed breakdown of infection, nerve damage, malocclusion and healing risks."
    };

    Context context;

    public OnboardingAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvIcon.setText(icons[position]);
        holder.tvTitle.setText(titles[position]);
        holder.tvDesc.setText(descriptions[position]);
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvTitle, tvDesc;
        ViewHolder(View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvSlideIcon);
            tvTitle = itemView.findViewById(R.id.tvSlideTitle);
            tvDesc = itemView.findViewById(R.id.tvSlideDesc);
        }
    }
}