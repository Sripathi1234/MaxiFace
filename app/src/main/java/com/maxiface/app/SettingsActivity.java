package com.maxiface.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button btnBack = findViewById(R.id.btnBack);
        LinearLayout editProfileOption = findViewById(R.id.editProfileOption);
        Button btnLogout = findViewById(R.id.btnLogout);

        btnBack.setOnClickListener(v -> finish());

        editProfileOption.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
            finishAffinity();
        });
    }
}