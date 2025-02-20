package com.abc.campushub.authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.abc.campushub.R;
import com.abc.campushub.filelist;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignUp extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private Button btnSignUp;
    private TextView logintxtbtn;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private static final String API_KEY = "913de7c20f0d418286235c6f122a317b"; // Replace with your email verification API key
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

        // Check if the user is already logged in
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            navigateToMainPage();
        }
        mAuth = FirebaseAuth.getInstance();
        logintxtbtn = findViewById(R.id.logintxtbtn);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        progressBar = findViewById(R.id.progressBar);
        btnSignUp.setOnClickListener(view -> registerUser());
        logintxtbtn.setOnClickListener(view -> startActivity(new Intent(SignUp.this, LoginActivity.class)));
    }
    private void navigateToMainPage() {
        Intent intent = new Intent(SignUp.this, filelist.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter all details!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Validate email authenticity before proceeding
        verifyEmail(email, new EmailVerificationCallback() {
            @Override
            public void onResult(boolean isValid) {
                runOnUiThread(() -> {
                    if (isValid) {
                        // Email is real, proceed with Firebase signup
                        createFirebaseUser(email, password);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SignUp.this, "Invalid or fake email! Please use a real email.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void createFirebaseUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification();
                        }
                        Toast.makeText(SignUp.this, "Signup successful! Verify your email.", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(SignUp.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(SignUp.this, "Signup failed! " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void verifyEmail(String email, EmailVerificationCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                String apiUrl = "https://api.zerobounce.net/v2/validate?api_key=" + API_KEY + "&email=" + email;
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                String status = jsonResponse.getString("status");

                // Valid if status is "valid"
                callback.onResult("valid".equals(status));

            } catch (Exception e) {
                e.printStackTrace();
                callback.onResult(false);
            }
        });
    }

    interface EmailVerificationCallback {
        void onResult(boolean isValid);
    }
}
