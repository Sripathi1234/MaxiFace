
package com.maxiface.app;

import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Wait 2.5 seconds then navigate
        new Handler().postDelayed(() -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null
                    && auth.getCurrentUser().isEmailVerified()) {
                // Logged in - check if onboarding done
                SharedPreferences prefs = getSharedPreferences("MaxiFacePrefs", MODE_PRIVATE);
                boolean onboardingDone = prefs.getBoolean("onboarding_done", false);
                if (!onboardingDone) {
                    startActivity(new Intent(this, OnboardingActivity.class));
                } else {
                    startActivity(new Intent(this, DashboardActivity.class));
                }
            } else {
                // Not logged in - go to login directly
                startActivity(new Intent(this, MainActivity.class));
            }
            finish();
        }, 2500);
    }
}