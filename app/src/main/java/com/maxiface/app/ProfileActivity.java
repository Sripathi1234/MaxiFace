package com.maxiface.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProfileActivity extends AppCompatActivity {

    TextView tvProfileName, tvProfileEmail, tvTotalPatients, tvHighRiskCount, tvLowRiskCount;
    EditText etSpecialization, etHospital, etPhone;
    Button btnSaveProfile, btnBackProfile;
    SharedPreferences prefs;
    String API_URL = "http://10.0.2.2:5000/api/patients?doctor_id=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvTotalPatients = findViewById(R.id.tvTotalPatients);
        tvHighRiskCount = findViewById(R.id.tvHighRiskCount);
        tvLowRiskCount = findViewById(R.id.tvLowRiskCount);
        etSpecialization = findViewById(R.id.etSpecialization);
        etHospital = findViewById(R.id.etHospital);
        etPhone = findViewById(R.id.etPhone);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnBackProfile = findViewById(R.id.btnBackProfile);

        prefs = getSharedPreferences("MaxiFacePrefs", MODE_PRIVATE);

        // Load Firebase user info
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) {
                String email = user.getEmail();
                name = email != null ? email.split("@")[0] : "Doctor";
                name = name.substring(0,1).toUpperCase() + name.substring(1);
            }
            tvProfileName.setText("Dr. " + name);
            tvProfileEmail.setText(user.getEmail());

            // Load stats
            loadStats(user.getUid());
        }

        // Load saved profile data
        etSpecialization.setText(prefs.getString("specialization", ""));
        etHospital.setText(prefs.getString("hospital", ""));
        etPhone.setText(prefs.getString("phone", ""));

        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnBackProfile.setOnClickListener(v -> finish());
    }

    void saveProfile() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("specialization", etSpecialization.getText().toString().trim());
        editor.putString("hospital", etHospital.getText().toString().trim());
        editor.putString("phone", etPhone.getText().toString().trim());
        editor.apply();
        Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
    }

    void loadStats(String doctorId) {
        new Thread(() -> {
            try {
                URL url = new URL(API_URL + doctorId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) response.append(line);

                JSONObject json = new JSONObject(response.toString());
                JSONArray patients = json.getJSONArray("patients");

                int total = patients.length();
                int high = 0, low = 0;
                for (int i = 0; i < patients.length(); i++) {
                    String risk = patients.getJSONObject(i).getString("risk_level");
                    if (risk.equals("High")) high++;
                    else if (risk.equals("Low")) low++;
                }

                int finalHigh = high;
                int finalLow = low;
                runOnUiThread(() -> {
                    tvTotalPatients.setText(String.valueOf(total));
                    tvHighRiskCount.setText(String.valueOf(finalHigh));
                    tvLowRiskCount.setText(String.valueOf(finalLow));
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        tvTotalPatients.setText("0"));
            }
        }).start();
    }
}