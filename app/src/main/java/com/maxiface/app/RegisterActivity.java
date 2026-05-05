package com.maxiface.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    EditText etName, etEmail, etPassword, etConfirmPassword;
    Button btnRegister;
    TextView tvLogin, tvError, tvSuccess;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        tvError = findViewById(R.id.tvError);
        tvSuccess = findViewById(R.id.tvSuccess);

        btnRegister.setOnClickListener(v -> register());
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    void register() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate fields
        if (name.isEmpty() || email.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill all fields");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match!");
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Registering...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // Save display name
                    com.google.firebase.auth.UserProfileChangeRequest profileUpdates =
                            new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                    authResult.getUser().updateProfile(profileUpdates);

                    // Send verification email
                    authResult.getUser().sendEmailVerification()
                            .addOnCompleteListener(task -> {
                                // Sign out immediately after registration
                                mAuth.signOut();

                                tvError.setVisibility(View.GONE);
                                tvSuccess.setTextColor(0xFF27ae60);
                                tvSuccess.setText("Registration successful! Please verify your email before logging in. Check your inbox.");
                                tvSuccess.setVisibility(View.VISIBLE);
                                btnRegister.setEnabled(true);
                                btnRegister.setText("Register");

                                // Go to login after 3 seconds
                                new android.os.Handler().postDelayed(() -> {
                                    startActivity(new Intent(this, MainActivity.class));
                                    finish();
                                }, 3000);
                            });
                })
                .addOnFailureListener(e -> {
                    showError(e.getMessage());
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Register");
                });
    }

    void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
        tvSuccess.setVisibility(View.GONE);
    }
}