package com.maxiface.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

public class MainActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvError, tvRegister;
    com.google.android.gms.common.SignInButton btnGoogleSignIn;

    FirebaseAuth mAuth;
    GoogleSignInClient googleSignInClient;
    static final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvError = findViewById(R.id.tvError);
        tvRegister = findViewById(R.id.tvRegister);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLogin.setOnClickListener(v -> loginWithEmail());
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                tvError.setText("Enter your email first then click Forgot Password");
                tvError.setVisibility(View.VISIBLE);
                return;
            }
            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused -> {
                        tvError.setTextColor(0xFF27ae60);
                        tvError.setText("Password reset email sent! Check your inbox.");
                        tvError.setVisibility(View.VISIBLE);
                    })
                    .addOnFailureListener(e -> {
                        tvError.setText("Error: " + e.getMessage());
                        tvError.setVisibility(View.VISIBLE);
                    });
        });
        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    void loginWithEmail() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            tvError.setText("Please fill all fields");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    if (authResult.getUser().isEmailVerified()) {
                        android.content.SharedPreferences prefs =
                                getSharedPreferences("MaxiFacePrefs", MODE_PRIVATE);
                        boolean onboardingDone = prefs.getBoolean("onboarding_done", false);
                        if (!onboardingDone) {
                            startActivity(new Intent(this, OnboardingActivity.class));
                        } else {
                            startActivity(new Intent(this, DashboardActivity.class));
                        }
                        finish();
                    }
                    else {
                        tvError.setText("Please verify your email first! Check your inbox.");
                        tvError.setVisibility(View.VISIBLE);
                        mAuth.signOut();
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Login");
                    }
                })
                .addOnFailureListener(e -> {
                    tvError.setText("Invalid email or password");
                    tvError.setVisibility(View.VISIBLE);
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");
                });
    }

    void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    startActivity(new Intent(this, DashboardActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show());
    }
}