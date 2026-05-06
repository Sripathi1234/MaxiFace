package com.maxiface.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    TextView tvWelcome, tvSpecialty, tvTotalPatients, tvHighRisk, tvLowRisk;
    LinearLayout cardNewPrediction, cardPatientHistory, cardAIAssistant, cardCriticalAlerts;
    LinearLayout navHome, navPredict, navHistory, navProfile;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        try {
            // Initialize Views
            tvWelcome = findViewById(R.id.tvWelcome);
            tvSpecialty = findViewById(R.id.tvSpecialty);
            tvTotalPatients = findViewById(R.id.tvTotalPatients);
            tvHighRisk = findViewById(R.id.tvHighRisk);
            tvLowRisk = findViewById(R.id.tvLowRisk);

            cardNewPrediction = findViewById(R.id.cardNewPrediction);
            cardPatientHistory = findViewById(R.id.cardPatientHistory);
            cardAIAssistant = findViewById(R.id.cardAIAssistant);
            cardCriticalAlerts = findViewById(R.id.cardCriticalAlerts);

            navHome = findViewById(R.id.navHome);
            navPredict = findViewById(R.id.navPredict);
            navHistory = findViewById(R.id.navHistory);
            navProfile = findViewById(R.id.navProfile);

            // Set up doctor info from Firebase
            if (user != null) {
                String name = user.getDisplayName();
                if (name == null || name.isEmpty()) {
                    String email = user.getEmail();
                    if (email != null && email.contains("@")) {
                        name = email.split("@")[0];
                    } else {
                        name = "Anderson";
                    }
                }
                if (name.length() > 0) {
                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                }
                tvWelcome.setText("Welcome, Dr. " + name);
                tvSpecialty.setText("Maxillofacial Surgery Specialist");
            }

            // Update statistics
            tvTotalPatients.setText("156");
            tvHighRisk.setText("23");
            tvLowRisk.setText("108");

            // Feature Card Click Listeners
            cardNewPrediction.setOnClickListener(v ->
                    startActivity(new Intent(this, PredictActivity.class)));

            cardPatientHistory.setOnClickListener(v ->
                    startActivity(new Intent(this, HistoryActivity.class)));

            cardAIAssistant.setOnClickListener(v ->
                    startActivity(new Intent(this, AIAssistantActivity.class)));

            cardCriticalAlerts.setOnClickListener(v ->
                    startActivity(new Intent(this, CriticalAlertsActivity.class)));

            // Bottom Navigation Listeners
            navHome.setOnClickListener(v -> {
                // Already on home
            });

            navPredict.setOnClickListener(v ->
                    startActivity(new Intent(this, PredictActivity.class)));

            navHistory.setOnClickListener(v ->
                    startActivity(new Intent(this, HistoryActivity.class)));

            navProfile.setOnClickListener(v ->
                    startActivity(new Intent(this, ProfileActivity.class)));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}