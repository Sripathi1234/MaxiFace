package com.maxiface.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView tvEmpty;
    Button btnBack;
    String API_URL = "http://10.0.2.2:5000/api/patients?doctor_id=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnBack = findViewById(R.id.btnBack);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        btnBack.setOnClickListener(v -> finish());

        loadPatients();
    }

    void loadPatients() {
        new Thread(() -> {
            try {
                com.google.firebase.auth.FirebaseUser user =
                        com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                String doctorId = user != null ? user.getUid() : "1";
                URL url = new URL(API_URL + doctorId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                JSONObject json = new JSONObject(response.toString());
                JSONArray patients = json.getJSONArray("patients");

                List<String[]> patientList = new ArrayList<>();
                for (int i = 0; i < patients.length(); i++) {
                    JSONObject p = patients.getJSONObject(i);
                    String date = p.getString("created_at");
                    if (date.length() > 10) date = date.substring(0, 10);

                    patientList.add(new String[]{
                            p.getString("name"),
                            p.getString("age") + " yrs",
                            p.getString("predicted_complication"),
                            p.getString("risk_level"),
                            p.optString("risk_percentage", "0") + "%",
                            date
                    });
                }

                runOnUiThread(() -> {
                    if (patientList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        recyclerView.setAdapter(
                                new PatientAdapter(this, patientList));
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this,
                                "Error loading patients: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}