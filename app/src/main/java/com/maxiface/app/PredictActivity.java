package com.maxiface.app;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class PredictActivity extends AppCompatActivity {

    EditText etPatientName, etAge, etNumFractures, etTimeToSurgery, etSurgeryDuration;
    Spinner spinnerGender, spinnerDiabetes, spinnerHypertension, spinnerSmoking, spinnerFractureType;
    Button btnPredict, btnBack, btnSelectCT;
    TextView tvCTFileName;

    Uri selectedCTUri = null;
    String ctFileName = "";

    String API_URL = "http://10.0.2.2:5000/api/predict";

    ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK
                        && result.getData() != null) {
                    selectedCTUri = result.getData().getData();
                    ctFileName = getFileName(selectedCTUri);
                    tvCTFileName.setText("Selected: " + ctFileName);
                    tvCTFileName.setTextColor(0xFF27ae60);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predict);

        etPatientName = findViewById(R.id.etPatientName);
        etAge = findViewById(R.id.etAge);
        etNumFractures = findViewById(R.id.etNumFractures);
        etTimeToSurgery = findViewById(R.id.etTimeToSurgery);
        etSurgeryDuration = findViewById(R.id.etSurgeryDuration);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerDiabetes = findViewById(R.id.spinnerDiabetes);
        spinnerHypertension = findViewById(R.id.spinnerHypertension);
        spinnerSmoking = findViewById(R.id.spinnerSmoking);
        spinnerFractureType = findViewById(R.id.spinnerFractureType);
        btnPredict = findViewById(R.id.btnPredict);
        btnBack = findViewById(R.id.btnBack);
        btnSelectCT = findViewById(R.id.btnSelectCT);
        tvCTFileName = findViewById(R.id.tvCTFileName);

        String[] yesNo = {"No", "Yes"};
        String[] genderArr = {"Female", "Male"};
        String[] fractureTypes = {"Mandibular", "Maxillary", "Zygomatic", "Orbital"};

        spinnerGender.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, genderArr));
        spinnerDiabetes.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, yesNo));
        spinnerHypertension.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, yesNo));
        spinnerSmoking.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, yesNo));
        spinnerFractureType.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, fractureTypes));

        btnBack.setOnClickListener(v -> finish());

        btnSelectCT.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            imagePickerLauncher.launch(intent);
        });

        btnPredict.setOnClickListener(v -> predict());
    }

    String getFileName(Uri uri) {
        String result = "ct_scan.jpg";
        try {
            Cursor cursor = getContentResolver()
                    .query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) result = cursor.getString(idx);
                cursor.close();
            }
        } catch (Exception e) {
            result = "ct_scan.jpg";
        }
        return result;
    }

    byte[] readBytes(Uri uri) throws IOException {
        InputStream is = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) != -1) bos.write(buf, 0, n);
        is.close();
        return bos.toByteArray();
    }

    void predict() {
        String patientName = etPatientName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String numFractures = etNumFractures.getText().toString().trim();
        String timeToSurgery = etTimeToSurgery.getText().toString().trim();
        String surgeryDuration = etSurgeryDuration.getText().toString().trim();

        if (patientName.isEmpty() || age.isEmpty() || numFractures.isEmpty()
                || timeToSurgery.isEmpty() || surgeryDuration.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPredict.setEnabled(false);
        btnPredict.setText("Analyzing...");

        int gender = spinnerGender.getSelectedItemPosition();
        int diabetes = spinnerDiabetes.getSelectedItemPosition();
        int hypertension = spinnerHypertension.getSelectedItemPosition();
        int smoking = spinnerSmoking.getSelectedItemPosition();
        int fractureType = spinnerFractureType.getSelectedItemPosition();

        new Thread(() -> {
            try {
                String boundary = "Boundary" + System.currentTimeMillis();
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setRequestProperty("Content-Type",
                        "multipart/form-data; boundary=" + boundary);

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                // Write text fields
                String[] fieldNames = {"patient_name","age","gender","diabetes",
                        "hypertension","smoking","fracture_type","num_fractures",
                        "time_to_surgery_hours","surgery_duration_mins","doctor_id"};
                String[] fieldValues = {patientName, age,
                        String.valueOf(gender), String.valueOf(diabetes),
                        String.valueOf(hypertension), String.valueOf(smoking),
                        String.valueOf(fractureType), numFractures,
                        timeToSurgery, surgeryDuration,
                        com.google.firebase.auth.FirebaseAuth.getInstance()
                                .getCurrentUser().getUid()};

                for (int i = 0; i < fieldNames.length; i++) {
                    dos.writeBytes("--" + boundary + "\r\n");
                    dos.writeBytes("Content-Disposition: form-data; name=\""
                            + fieldNames[i] + "\"\r\n\r\n");
                    dos.writeBytes(fieldValues[i] + "\r\n");
                }

                // Write CT scan file if selected
                if (selectedCTUri != null) {
                    byte[] fileBytes = readBytes(selectedCTUri);
                    dos.writeBytes("--" + boundary + "\r\n");
                    dos.writeBytes("Content-Disposition: form-data; name=\"ct_scan\"; filename=\""
                            + ctFileName + "\"\r\n");
                    dos.writeBytes("Content-Type: image/jpeg\r\n\r\n");
                    dos.write(fileBytes);
                    dos.writeBytes("\r\n");
                }

                dos.writeBytes("--" + boundary + "--\r\n");
                dos.flush();
                dos.close();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) response.append(line);

                JSONObject json = new JSONObject(response.toString());
                String riskLevel = json.getString("risk_level");
                float riskPct = (float) json.getDouble("risk_percentage");
                String complication = json.optString("complication", "Unknown");
                float infectionPct = (float) json.optDouble("infection_pct", 10);
                float nervePct = (float) json.optDouble("nerve_pct", 10);
                float malocclusionPct = (float) json.optDouble("malocclusion_pct", 10);
                float delayedPct = (float) json.optDouble("delayed_pct", 10);

                runOnUiThread(() -> {
                    Intent intent = new Intent(this, ResultActivity.class);
                    intent.putExtra("patient_name", patientName);
                    intent.putExtra("risk_level", riskLevel);
                    intent.putExtra("risk_percentage", riskPct);
                    intent.putExtra("complication", complication);
                    intent.putExtra("infection_pct", infectionPct);
                    intent.putExtra("nerve_pct", nervePct);
                    intent.putExtra("malocclusion_pct", malocclusionPct);
                    intent.putExtra("delayed_pct", delayedPct);
                    startActivity(intent);
                    btnPredict.setEnabled(true);
                    btnPredict.setText("Get Prediction");
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    btnPredict.setEnabled(true);
                    btnPredict.setText("Get Prediction");
                });
            }
        }).start();
    }
}