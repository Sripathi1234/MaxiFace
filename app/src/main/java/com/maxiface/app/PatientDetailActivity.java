package com.maxiface.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class PatientDetailActivity extends AppCompatActivity {

    TextView tvDetailName, tvDetailAge, tvDetailDate;
    TextView tvDetailRiskIcon, tvDetailRiskLevel, tvDetailRiskPct;
    TextView tvDetailComplication, tvDetailRecommendation;
    TextView tvInfectionPct, tvNervePct, tvMalocclusionPct, tvDelayedPct;
    ProgressBar pbInfection, pbNerve, pbMalocclusion, pbDelayed;
    CardView detailRiskCard;
    Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail);

        // Initialize views
        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailAge = findViewById(R.id.tvDetailAge);
        tvDetailDate = findViewById(R.id.tvDetailDate);
        tvDetailRiskIcon = findViewById(R.id.tvDetailRiskIcon);
        tvDetailRiskLevel = findViewById(R.id.tvDetailRiskLevel);
        tvDetailRiskPct = findViewById(R.id.tvDetailRiskPct);
        tvDetailComplication = findViewById(R.id.tvDetailComplication);
        tvDetailRecommendation = findViewById(R.id.tvDetailRecommendation);
        tvInfectionPct = findViewById(R.id.tvInfectionPct);
        tvNervePct = findViewById(R.id.tvNervePct);
        tvMalocclusionPct = findViewById(R.id.tvMalocclusionPct);
        tvDelayedPct = findViewById(R.id.tvDelayedPct);
        pbInfection = findViewById(R.id.pbInfection);
        pbNerve = findViewById(R.id.pbNerve);
        pbMalocclusion = findViewById(R.id.pbMalocclusion);
        pbDelayed = findViewById(R.id.pbDelayed);
        detailRiskCard = findViewById(R.id.detailRiskCard);
        btnBack = findViewById(R.id.btnBack);

        // Get data from intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String age = intent.getStringExtra("age");
        String date = intent.getStringExtra("date");
        String riskLevel = intent.getStringExtra("risk_level");
        float riskPct = intent.getFloatExtra("risk_percentage", 0);
        String complication = intent.getStringExtra("complication");

        // Set patient info
        tvDetailName.setText(name);
        tvDetailAge.setText(age + " yrs");
        tvDetailDate.setText(date);

        // Set risk info
        tvDetailRiskLevel.setText(riskLevel);
        tvDetailRiskPct.setText(riskPct + "% risk");
        tvDetailRiskIcon.setText(riskLevel.toUpperCase() + " RISK");

        // Set risk card color
        if (riskLevel.equals("High")) {
            detailRiskCard.setCardBackgroundColor(0xFFe74c3c);
        } else if (riskLevel.equals("Medium")) {
            detailRiskCard.setCardBackgroundColor(0xFFf39c12);
        } else {
            detailRiskCard.setCardBackgroundColor(0xFF27ae60);
        }

        // Set complication
        tvDetailComplication.setText(complication);

        // Set recommendation
        if (riskLevel.equals("High")) {
            tvDetailRecommendation.setText("Immediate attention required. Close postoperative monitoring strongly advised.");
        } else if (riskLevel.equals("Medium")) {
            tvDetailRecommendation.setText("Regular follow-ups recommended. Monitor for early signs of complications.");
        } else {
            tvDetailRecommendation.setText("Standard postoperative care and routine follow-up recommended.");
        }

        // Calculate individual risk percentages
        float base = riskPct > 0 ? riskPct : 50f;
        float infectionPct = Math.min(base * 0.45f, 99);
        float nervePct = Math.min(base * 0.30f, 99);
        float malocclusionPct = Math.min(base * 0.55f, 99);
        float delayedPct = Math.min(base * 0.38f, 99);

        // Set progress bars
        setProgressBar(pbInfection, tvInfectionPct, infectionPct);
        setProgressBar(pbNerve, tvNervePct, nervePct);
        setProgressBar(pbMalocclusion, tvMalocclusionPct, malocclusionPct);
        setProgressBar(pbDelayed, tvDelayedPct, delayedPct);

        btnBack.setOnClickListener(v -> finish());
    }

    void setProgressBar(ProgressBar bar, TextView pctText, float pct) {
        int progress = (int) pct;
        bar.setProgress(progress);
        pctText.setText(String.format("%.1f%%", pct));

        if (pct >= 60) {
            bar.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(0xFFe74c3c));
        } else if (pct >= 30) {
            bar.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(0xFFf39c12));
        } else {
            bar.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(0xFF27ae60));
        }
    }
}