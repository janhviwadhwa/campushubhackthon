package com.abc.campushub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.abc.campushub.authentication.LoginActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class mainprofile extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    private TextView firstName, lastName, username, email, phone, gender;
    private Button logout, editProfileButton;
    private ImageView profileImage;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private StorageReference storageReference;
    private FirebaseStorage storage;
    private String userEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mainprofile);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Get user email
        if (auth.getCurrentUser() != null) {
            userEmail = auth.getCurrentUser().getEmail();
            Log.d(TAG, "User Email: " + userEmail);
        } else {
            Log.e(TAG, "User not logged in");
            return;
        }


        // Initialize UI elements
        logout = findViewById(R.id.logoutButton);
        editProfileButton = findViewById(R.id.editProfileButton);  // Assuming you have a button for editing profile
        logout.setOnClickListener(view -> {
            Log.d(TAG, "Logout button clicked");

            // Clear SharedPreferences
            SharedPreferences preferences = getSharedPreferences("UserSession", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();  // This removes all stored values
            editor.apply();

            // Sign out from Firebase
            auth.signOut();
            Log.d(TAG, "User signed out");

            // Navigate to LoginActivity and clear backstack
            Intent intent = new Intent(mainprofile.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });


        editProfileButton.setOnClickListener(
                view -> {
                    startActivity(new Intent(mainprofile.this, profile.class));
                }
        );
        profileImage = findViewById(R.id.profile_image);
        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);
        gender = findViewById(R.id.gender);  // Assuming you have a TextView for gender
        logout = findViewById(R.id.logoutButton); // If you have an update button

        // Load user data
        loadUserProfile();
    }

    private void loadUserProfile() {
        Log.d(TAG, "Fetching user profile for: " + userEmail);

        db.collection("users").document(userEmail).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Log.d(TAG, "User document found: " + documentSnapshot.getData());

                firstName.setText("First Name: " + documentSnapshot.getString("firstName"));
                lastName.setText("Last Name: " + documentSnapshot.getString("lastName"));
                username.setText("Username: " + documentSnapshot.getString("username"));
                email.setText("Email: " + userEmail);
                phone.setText("Phone: " + documentSnapshot.getString("phone"));
                gender.setText("Gender: " + documentSnapshot.getString("gender"));

                // Load profile image using Glide
                String imageUrl = documentSnapshot.getString("profileImage");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(this).load(imageUrl).into(profileImage);
                } else {
                    Log.e(TAG, "Profile image URL is empty or null");
                }
            } else {
                Log.e(TAG, "User document does not exist in Firestore");
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error loading profile", e));
    }

}
