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

public class LoginActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private TextView txtsignup;
    private Button btnLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

        // Check if the user is already logged in
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            navigateToMainPage();
        }

        mAuth = FirebaseAuth.getInstance();
        txtsignup = findViewById(R.id.txtSignUpbtn);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        btnLogin.setOnClickListener(view -> loginUser());
        txtsignup.setOnClickListener(
                view -> {
                    Intent intent = new Intent(LoginActivity.this, SignUp.class);
                    startActivity(intent);
                }
        );
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter all details!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserSession();
                            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_LONG).show();
                            navigateToMainPage();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed! " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }

    private void navigateToMainPage() {
        Intent intent = new Intent(LoginActivity.this, filelist.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
