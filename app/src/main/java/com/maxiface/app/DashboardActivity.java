package com.maxiface.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    TextView tvWelcome;
    Button btnNewPrediction, btnHistory, btnLogout;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        tvWelcome = findViewById(R.id.tvWelcome);
        btnNewPrediction = findViewById(R.id.btnNewPrediction);
        btnHistory = findViewById(R.id.btnHistory);
        btnLogout = findViewById(R.id.btnLogout);

        if (user != null) {
            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) {
                String email = user.getEmail();
                if (email != null && email.contains("@")) {
                    name = email.split("@")[0];
                } else {
                    name = "Doctor";
                }
            }
            if (name.length() > 0) {
                name = name.substring(0,1).toUpperCase() + name.substring(1);
            }
            tvWelcome.setText("Welcome, Dr. " + name + " \uD83D\uDC4B");
        }

        btnNewPrediction.setOnClickListener(v ->
                startActivity(new Intent(this, PredictActivity.class)));

        btnHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));

        Button btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        Button btnAbout = findViewById(R.id.btnAbout);
        btnAbout.setOnClickListener(v ->
                startActivity(new Intent(this, AboutActivity.class)));

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}