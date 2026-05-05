package com.maxiface.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class ResultActivity extends AppCompatActivity {

    TextView tvPatientName, tvRiskIcon, tvRiskLevel, tvRiskPercentage;
    TextView tvInfectionLabel, tvInfectionPct;
    TextView tvNerveLabel, tvNervePct;
    TextView tvMalocclusionLabel, tvMalocclusionPct;
    TextView tvDelayedLabel, tvDelayedPct;
    TextView tvComplication, tvRecommendation;
    ProgressBar progressInfection, progressNerve, progressMalocclusion, progressDelayed;
    Button btnNewPrediction, btnDashboard;
    CardView riskCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Initialize views
        tvPatientName = findViewById(R.id.tvPatientName);
        tvRiskIcon = findViewById(R.id.tvRiskIcon);
        tvRiskLevel = findViewById(R.id.tvRiskLevel);
        tvRiskPercentage = findViewById(R.id.tvRiskPercentage);
        tvInfectionLabel = findViewById(R.id.tvInfectionLabel);
        tvInfectionPct = findViewById(R.id.tvInfectionPct);
        tvNerveLabel = findViewById(R.id.tvNerveLabel);
        tvNervePct = findViewById(R.id.tvNervePct);
        tvMalocclusionLabel = findViewById(R.id.tvMalocclusionLabel);
        tvMalocclusionPct = findViewById(R.id.tvMalocclusionPct);
        tvDelayedLabel = findViewById(R.id.tvDelayedLabel);
        tvDelayedPct = findViewById(R.id.tvDelayedPct);
        tvComplication = findViewById(R.id.tvComplication);
        tvRecommendation = findViewById(R.id.tvRecommendation);
        progressInfection = findViewById(R.id.progressInfection);
        progressNerve = findViewById(R.id.progressNerve);
        progressMalocclusion = findViewById(R.id.progressMalocclusion);
        progressDelayed = findViewById(R.id.progressDelayed);
        btnNewPrediction = findViewById(R.id.btnNewPrediction);
        btnDashboard = findViewById(R.id.btnDashboard);
        riskCard = findViewById(R.id.riskCard);

        // Get data from intent
        String patientName = getIntent().getStringExtra("patient_name");
        String riskLevel = getIntent().getStringExtra("risk_level");
        float riskPct = getIntent().getFloatExtra("risk_percentage", 0);
        String complication = getIntent().getStringExtra("complication");
        float infectionPct = getIntent().getFloatExtra("infection_pct", 0);
        float nervePct = getIntent().getFloatExtra("nerve_pct", 0);
        float malocclusionPct = getIntent().getFloatExtra("malocclusion_pct", 0);
        float delayedPct = getIntent().getFloatExtra("delayed_pct", 0);

        // Set patient name
        tvPatientName.setText("Patient: " + patientName);

        // Set risk level
        tvRiskLevel.setText(riskLevel);
        tvRiskPercentage.setText(riskPct + "% risk");
        tvRiskIcon.setText(riskLevel.toUpperCase() + " RISK");

        // Set risk card color
        if (riskLevel.equals("High")) {
            riskCard.setCardBackgroundColor(0xFFe74c3c);
            tvRecommendation.setText("Immediate attention required. Close postoperative monitoring strongly advised. Consider prophylactic antibiotics.");
        } else if (riskLevel.equals("Medium")) {
            riskCard.setCardBackgroundColor(0xFFf39c12);
            tvRecommendation.setText("Moderate risk detected. Regular postoperative follow-ups recommended. Monitor for early signs of complications.");
        } else {
            riskCard.setCardBackgroundColor(0xFF27ae60);
            tvRecommendation.setText("Low risk of complications. Standard postoperative care and routine follow-up recommended.");
        }

        // Set complication
        tvComplication.setText(complication);

        // Set risk bars
        setRiskItem(progressInfection, tvInfectionPct, tvInfectionLabel, infectionPct);
        setRiskItem(progressNerve, tvNervePct, tvNerveLabel, nervePct);
        setRiskItem(progressMalocclusion, tvMalocclusionPct, tvMalocclusionLabel, malocclusionPct);
        setRiskItem(progressDelayed, tvDelayedPct, tvDelayedLabel, delayedPct);

        // Buttons
        btnNewPrediction.setOnClickListener(v -> {
            startActivity(new Intent(this, PredictActivity.class));
            finish();
        });

        btnDashboard.setOnClickListener(v -> {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        });
    }

    void setRiskItem(ProgressBar bar, TextView pctText,
                     TextView label, float pct) {
        bar.setProgress((int) pct);
        pctText.setText(String.format("%.1f%%", pct));

        if (pct >= 60) {
            label.setText("High");
            label.setTextColor(0xFFe74c3c);
            bar.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(0xFFe74c3c));
        } else if (pct >= 30) {
            label.setText("Medium");
            label.setTextColor(0xFFf39c12);
            bar.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(0xFFf39c12));
        } else {
            label.setText("Low");
            label.setTextColor(0xFF27ae60);
            bar.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(0xFF27ae60));
        }
    }
}