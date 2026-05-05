package com.maxiface.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

public class OnboardingActivity extends AppCompatActivity {

    ViewPager2 viewPager;
    Button btnNext;
    TextView tvSkip;
    LinearLayout dotsLayout;
    OnboardingAdapter adapter;
    int totalSlides = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        tvSkip = findViewById(R.id.tvSkip);
        dotsLayout = findViewById(R.id.dotsLayout);

        adapter = new OnboardingAdapter(this);
        viewPager.setAdapter(adapter);

        setupDots(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setupDots(position);
                if (position == totalSlides - 1) {
                    btnNext.setText("Get Started");
                    tvSkip.setVisibility(View.GONE);
                } else {
                    btnNext.setText("Next");
                    tvSkip.setVisibility(View.VISIBLE);
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < totalSlides - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                finishOnboarding();
            }
        });

        tvSkip.setOnClickListener(v -> finishOnboarding());
    }

    void setupDots(int currentPage) {
        dotsLayout.removeAllViews();
        for (int i = 0; i < totalSlides; i++) {
            TextView dot = new TextView(this);
            dot.setText(i == currentPage ? "●" : "○");
            dot.setTextSize(16);
            dot.setTextColor(i == currentPage ? 0xFFFFFFFF : 0x80FFFFFF);
            dot.setPadding(4, 0, 4, 0);
            dotsLayout.addView(dot);
        }
    }

    void finishOnboarding() {
        SharedPreferences prefs = getSharedPreferences("MaxiFacePrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("onboarding_done", true).apply();
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }
}