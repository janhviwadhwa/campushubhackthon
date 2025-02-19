package com.abc.campushub.authentication;

import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUp extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private Button btnSignUp;
    private TextView logintxtbtn;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        logintxtbtn = findViewById(R.id.logintxtbtn);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        progressBar = findViewById(R.id.progressBar);
        btnSignUp.setOnClickListener(view -> registerUser());
        logintxtbtn.setOnClickListener(View -> startActivity(new Intent(SignUp.this, LoginActivity.class)));
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter all details!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
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
}
