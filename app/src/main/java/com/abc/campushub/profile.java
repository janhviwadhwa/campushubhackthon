package com.abc.campushub;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class profile extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String UPLOAD_URL = "https://campus-hub-20in.onrender.com/profile/upload-profile-image";

    private EditText firstName, lastName, username, email, phone;
    private Spinner genderSpinner;
    private Button updateProfile;
    private ImageView profileImage;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private StorageReference storageReference;
    private FirebaseStorage storage;
    private String userEmail;
    private Uri imageUri;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Get current user
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userEmail = user.getEmail();
        }

        // Initialize UI elements
        profileImage = findViewById(R.id.profile_image);
        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);
        genderSpinner = findViewById(R.id.gender);
        updateProfile = findViewById(R.id.change_password);

        // Set email field as read-only
        if (userEmail != null) {
            email.setText(userEmail);
            email.setEnabled(false);
        }

        // Setup Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        // Load existing profile data
        loadUserProfile();

        // Handle profile image selection
        profileImage.setOnClickListener(v -> openGallery());

        // Handle Update Profile button click
        updateProfile.setOnClickListener(v -> updateUserProfile());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
                uploadImageToAPI(bitmap); // Upload image to API
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToAPI(Bitmap bitmap) {
        if (imageUri != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Uploading...");
            progressDialog.show();

            new Thread(() -> {
                try {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byte[] imageData = byteArrayOutputStream.toByteArray();

                    OkHttpClient client = new OkHttpClient();

                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("file", UUID.randomUUID().toString() + ".jpg",
                                    RequestBody.create(MediaType.parse("image/jpeg"), imageData))
                            .addFormDataPart("username", username.getText().toString().trim()) // Ensure username is sent
                            .build();

                    Request request = new Request.Builder()
                            .url(UPLOAD_URL)
                            .post(requestBody)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            progressDialog.dismiss();
                            runOnUiThread(() ->
                                    Toast.makeText(profile.this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            progressDialog.dismiss();
                            String responseBody = response.body().string();

                            if (response.isSuccessful()) {
                                try {
                                    JSONObject jsonObject = new JSONObject(responseBody);
                                    String imageUrl = jsonObject.getString("image_url"); // Extract URL from API response

                                    runOnUiThread(() -> {
                                        saveImageToFirestore(imageUrl); // Save the URL in Firestore
                                        Toast.makeText(profile.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                                    });

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Log.e("UPLOAD_ERROR", "Response Code: " + response.code() + ", Body: " + responseBody);
                                runOnUiThread(() ->
                                        Toast.makeText(profile.this, "Upload Failed: " + responseBody, Toast.LENGTH_LONG).show()
                                );
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }


    private void saveImageToFirestore(String imageUrl) {
        if (userEmail == null) return;

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("profileImage", imageUrl);

        db.collection("users").document(userEmail)
                .set(updateData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Profile image updated"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating profile image", e));
    }

    private void loadUserProfile() {
        db.collection("users").document(userEmail).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                firstName.setText(documentSnapshot.getString("firstName"));
                lastName.setText(documentSnapshot.getString("lastName"));
                username.setText(documentSnapshot.getString("username"));
                phone.setText(documentSnapshot.getString("phone"));
                genderSpinner.setSelection(getGenderIndex(documentSnapshot.getString("gender")));

                String imageUrl = documentSnapshot.getString("profileImage");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(this).load(imageUrl).into(profileImage);
                }
            }
        }).addOnFailureListener(e -> Log.e("Firestore", "Error loading profile", e));
    }

    private int getGenderIndex(String gender) {
        if (gender == null) return 0;
        String[] genderOptions = getResources().getStringArray(R.array.gender_options);
        for (int i = 0; i < genderOptions.length; i++) {
            if (genderOptions[i].equalsIgnoreCase(gender)) return i;
        }
        return 0;
    }

    private void updateUserProfile() {
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("firstName", firstName.getText().toString().trim());
        userProfile.put("lastName", lastName.getText().toString().trim());
        userProfile.put("username", username.getText().toString().trim());
        userProfile.put("phone", phone.getText().toString().trim());
        userProfile.put("gender", genderSpinner.getSelectedItem().toString());
        db.collection("users").document(userEmail).set(userProfile, SetOptions.merge()).addOnSuccessListener(aVoid -> Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show());
    }
}
