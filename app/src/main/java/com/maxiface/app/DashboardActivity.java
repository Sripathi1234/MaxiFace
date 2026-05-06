package com.maxiface.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DashboardActivity extends AppCompatActivity {

    TextView tvWelcome, tvSpecialty;
    TextView tvTotalPatients, tvHighRiskCount, tvLowRiskCount;
    LinearLayout llRecentActivity;
    TextView tvNoRecentActivity;
    CardView cardNewPrediction, cardHistory, cardAiAssistant, cardCriticalAlerts;
    LinearLayout navHome, navPredict, navHistory, navProfile;
    ImageButton btnProfileIcon;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        loadDoctorProfile();
        loadStatistics();
        loadRecentActivity();
        setupClickListeners();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvSpecialty = findViewById(R.id.tvSpecialty);
        tvTotalPatients = findViewById(R.id.tvTotalPatients);
        tvHighRiskCount = findViewById(R.id.tvHighRiskCount);
        tvLowRiskCount = findViewById(R.id.tvLowRiskCount);
        llRecentActivity = findViewById(R.id.llRecentActivity);
        tvNoRecentActivity = findViewById(R.id.tvNoRecentActivity);
        cardNewPrediction = findViewById(R.id.cardNewPrediction);
        cardHistory = findViewById(R.id.cardHistory);
        cardAiAssistant = findViewById(R.id.cardAiAssistant);
        cardCriticalAlerts = findViewById(R.id.cardCriticalAlerts);
        navHome = findViewById(R.id.navHome);
        navPredict = findViewById(R.id.navPredict);
        navHistory = findViewById(R.id.navHistory);
        navProfile = findViewById(R.id.navProfile);
        btnProfileIcon = findViewById(R.id.btnProfileIcon);
    }

    private void loadDoctorProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // Set display name from Firebase Auth first (fast)
        String displayName = user.getDisplayName();
        String name = buildDoctorName(displayName, user.getEmail());
        tvWelcome.setText("Welcome, Dr. " + name);

        // Fetch specialty from Firestore users collection
        SharedPreferences prefs = getSharedPreferences("MaxiFacePrefs", MODE_PRIVATE);
        String savedSpecialty = prefs.getString("specialization", "");
        if (!savedSpecialty.isEmpty()) {
            tvSpecialty.setText(savedSpecialty);
        }

        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String firestoreName = document.getString("name");
                        if (firestoreName != null && !firestoreName.isEmpty()) {
                            tvWelcome.setText("Welcome, Dr. " + firestoreName);
                        }
                        String specialty = document.getString("specialty");
                        if (specialty != null && !specialty.isEmpty()) {
                            tvSpecialty.setText(specialty);
                        }
                    }
                });
    }

    private void loadStatistics() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("patients")
                .whereEqualTo("doctorId", user.getUid())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int total = querySnapshot.size();
                    int high = 0, low = 0, medium = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String riskLevel = doc.getString("riskLevel");
                        if (riskLevel == null) riskLevel = doc.getString("risk_level");
                        if ("High".equalsIgnoreCase(riskLevel)) high++;
                        else if ("Low".equalsIgnoreCase(riskLevel)) low++;
                        else if ("Medium".equalsIgnoreCase(riskLevel)) medium++;
                    }

                    tvTotalPatients.setText(String.valueOf(total));
                    tvHighRiskCount.setText(String.valueOf(high));
                    tvLowRiskCount.setText(String.valueOf(low + medium));
                })
                .addOnFailureListener(e -> {
                    tvTotalPatients.setText("0");
                    tvHighRiskCount.setText("0");
                    tvLowRiskCount.setText("0");
                });
    }

    private void loadRecentActivity() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("patients")
                .whereEqualTo("doctorId", user.getUid())
                .limit(5)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Map<String, Object>> items = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        items.add(doc.getData());
                    }
                    renderRecentActivity(items);
                })
                .addOnFailureListener(e -> showNoRecentActivity());
    }

    private void renderRecentActivity(List<Map<String, Object>> items) {
        llRecentActivity.removeAllViews();

        if (items.isEmpty()) {
            showNoRecentActivity();
            return;
        }

        tvNoRecentActivity.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);

            View row = inflater.inflate(R.layout.item_recent_activity, llRecentActivity, false);

            TextView tvName = row.findViewById(R.id.tvRecentPatientName);
            TextView tvTime = row.findViewById(R.id.tvRecentTime);
            TextView tvPct = row.findViewById(R.id.tvRecentRiskPct);
            TextView tvBadge = row.findViewById(R.id.tvRecentRiskBadge);

            // Patient name + age
            String patientName = getStringField(item, "patientName", "name", "Unknown");
            String age = getStringField(item, "patientAge", "age", "");
            tvName.setText(age.isEmpty() ? patientName : patientName + " \u2022 " + age + "y");

            // Timestamp
            Object ts = item.get("timestamp");
            if (ts instanceof com.google.firebase.Timestamp) {
                long millis = ((com.google.firebase.Timestamp) ts).toDate().getTime();
                tvTime.setText(getRelativeTime(millis));
            } else {
                String dateStr = getStringField(item, "created_at", "date", "");
                tvTime.setText(dateStr.isEmpty() ? "Recently" : dateStr);
            }

            // Risk percentage
            String riskPct = getStringField(item, "riskPercentage", "risk_percentage", "0");
            try {
                float pct = Float.parseFloat(riskPct.replace("%", "").trim());
                tvPct.setText(String.format(Locale.US, "%.1f%%", pct));
            } catch (NumberFormatException ex) {
                tvPct.setText(riskPct.isEmpty() ? "—" : riskPct + "%");
            }

            // Risk badge
            String riskLevel = getStringField(item, "riskLevel", "risk_level", "Low");
            applyRiskBadge(tvBadge, riskLevel);

            // Divider between items (except last)
            if (i < items.size() - 1) {
                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(Color.parseColor("#E0E0E0"));
                llRecentActivity.addView(divider);
            }

            llRecentActivity.addView(row);
        }
    }

    private void showNoRecentActivity() {
        llRecentActivity.removeAllViews();
        tvNoRecentActivity.setVisibility(View.VISIBLE);
    }

    private void applyRiskBadge(TextView badge, String riskLevel) {
        badge.setText(riskLevel);
        if ("High".equalsIgnoreCase(riskLevel)) {
            badge.setTextColor(Color.parseColor("#e74c3c"));
            badge.setBackgroundResource(R.drawable.bg_risk_badge_high);
        } else if ("Medium".equalsIgnoreCase(riskLevel)) {
            badge.setTextColor(Color.parseColor("#f39c12"));
            badge.setBackgroundResource(R.drawable.bg_risk_badge_medium);
        } else {
            badge.setTextColor(Color.parseColor("#27ae60"));
            badge.setBackgroundResource(R.drawable.bg_risk_badge_low);
        }
    }

    private String buildDoctorName(String displayName, String email) {
        String name = displayName;
        if (name == null || name.isEmpty()) {
            if (email != null && email.contains("@")) {
                name = email.split("@")[0];
            } else {
                name = "Doctor";
            }
        }
        if (!name.isEmpty()) {
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        return name;
    }

    private String getRelativeTime(long millis) {
        long diffMs = System.currentTimeMillis() - millis;
        long mins = TimeUnit.MILLISECONDS.toMinutes(diffMs);
        if (mins < 60) return mins <= 1 ? "Just now" : mins + " minutes ago";
        long hours = TimeUnit.MILLISECONDS.toHours(diffMs);
        if (hours < 24) return hours == 1 ? "1 hour ago" : hours + " hours ago";
        long days = TimeUnit.MILLISECONDS.toDays(diffMs);
        return days == 1 ? "Yesterday" : days + " days ago";
    }

    private String getStringField(Map<String, Object> map, String key1, String key2, String def) {
        Object v = map.get(key1);
        if (v == null) v = map.get(key2);
        return v != null ? String.valueOf(v) : def;
    }

    private void setupClickListeners() {
        // Feature cards
        cardNewPrediction.setOnClickListener(v ->
                startActivity(new Intent(this, PredictActivity.class)));

        cardHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));

        cardAiAssistant.setOnClickListener(v ->
                startActivity(new Intent(this, AIAssistantActivity.class)));

        cardCriticalAlerts.setOnClickListener(v ->
                startActivity(new Intent(this, CriticalAlertsActivity.class)));

        // Header profile icon
        btnProfileIcon.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        // Bottom navigation
        navHome.setOnClickListener(v -> { /* already on home */ });

        navPredict.setOnClickListener(v ->
                startActivity(new Intent(this, PredictActivity.class)));

        navHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));

        navProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }
}
