package com.abc.campushub;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class filelist extends AppCompatActivity {
    private static final int PICK_FILE_REQUEST = 2;
    private FloatingActionButton btnAddFile;
    private static final String FILES_URL = "https://campus-hub-20in.onrender.com/files";
    private RecyclerView recyclerView;
    private MyListAdapter fileListAdapter;
    private List<FileModel> fileList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private OkHttpClient client = new OkHttpClient();

    private static final String UPLOAD_URL = "https://campus-hub-20in.onrender.com/upload";
    private Uri selectedFileUri;  // To store selected file URI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filelist);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fileListAdapter = new MyListAdapter(this, fileList);

        recyclerView.setAdapter(fileListAdapter);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setEnabled(false);
        fetchFileList();
        swipeRefreshLayout.setEnabled(true);
        btnAddFile = findViewById(R.id.btnAddFile);

        btnAddFile.setOnClickListener(view -> openFileChooser());
        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchFileList();  // Refresh file list on swipe down
        });
    }


    // Open File Picker
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                showFileUploadDialog();
            }
        }
    }

    // Show Input Fields in AlertDialog
    private void showFileUploadDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter File Details");

        // Create a vertical layout for input fields
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        // Username input
        EditText edtUsername = new EditText(this);
        edtUsername.setHint("Enter Username");
        layout.addView(edtUsername);

        // Tags input
        EditText edtTags = new EditText(this);
        edtTags.setHint("Enter Tags (comma-separated)");
        layout.addView(edtTags);

        builder.setView(layout);

        // Add "Upload" button
        builder.setPositiveButton("Upload", (dialog, which) -> {
            String username = edtUsername.getText().toString().trim();
            String tags = edtTags.getText().toString().trim();

            if (username.isEmpty() || tags.isEmpty()) {
                Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedFileUri != null) {
                File file = getFileFromUri(selectedFileUri);
                if (file != null) {
                    uploadFileToBackend(file, username, tags);
                } else {
                    Toast.makeText(this, "Error accessing file!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Add "Cancel" button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Show the dialog
        builder.show();
    }

    // Convert Uri to File
    private File getFileFromUri(Uri uri) {
        File file = null;
        try {
            String fileName = getFileName(uri);
            File cacheDir = getCacheDir();
            file = new File(cacheDir, fileName);

            try (InputStream inputStream = getContentResolver().openInputStream(uri); OutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    // Get File Name from Uri
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void uploadFileToBackend(File file, String username, String tags) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("file", file.getName(), RequestBody.create(file, MediaType.parse("application/octet-stream"))).addFormDataPart("username", username).addFormDataPart("tags", tags).build();

                Request request = new Request.Builder().url(UPLOAD_URL).post(requestBody).build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body() != null ? response.body().string() : "No response";

                runOnUiThread(() -> {
                    Log.d("Upload Response", "Full Response: " + responseBody);
                    if (response.isSuccessful()) {
                        Toast.makeText(filelist.this, "File Uploaded Successfully!", Toast.LENGTH_LONG).show();

                        // Extract file URL from API response
                        String fileUrl = extractFileUrl(responseBody);

                        if (fileUrl.isEmpty()) {
                            Log.e("Upload Error", "File URL is empty! Check API response.");
                            Toast.makeText(filelist.this, "File URL is missing in API response!", Toast.LENGTH_LONG).show();
                        } else {
                            saveFileMetadataToFirestore(username, tags, fileUrl);
                        }
                    } else {
                        Toast.makeText(filelist.this, "Upload Failed!\nResponse: " + responseBody, Toast.LENGTH_LONG).show();
                        Log.e("Upload Failure", "Response: " + responseBody);
                    }
                });

            } catch (IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(filelist.this, "Upload Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("Upload Error", "Failed to upload file", e);
                });
            }
        }).start();
    }

    // Extract file URL from API response (Modify based on your API response structure)
    // Extract file URL from API response (Modify based on your API response structure)
    private String extractFileUrl(String responseBody) {
        try {
            JSONObject jsonObject = new JSONObject(responseBody);

            // Print all keys for debugging
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Log.d("API Response Key", "Key: " + key + " -> Value: " + jsonObject.get(key));
            }

            // Adjust this based on your API response key
            if (jsonObject.has("file_url")) {  // ✅ Corrected key
                return jsonObject.getString("file_url");
            } else if (jsonObject.has("fileUrl")) {
                return jsonObject.getString("fileUrl");
            } else if (jsonObject.has("url")) {
                return jsonObject.getString("url");
            } else if (jsonObject.has("link")) {
                return jsonObject.getString("link");
            } else {
                Log.e("API Error", "No valid file URL key found!");
                return "";
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }


    // Save file metadata to Firestore
    private void saveFileMetadataToFirestore(String username, String tags, String fileUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> fileData = new HashMap<>();
        fileData.put("username", username);
        fileData.put("tags", tags);
        fileData.put("fileUrl", fileUrl);
        fileData.put("timestamp", System.currentTimeMillis()); // Add timestamp for sorting
        db.collection("uploaded_files").add(fileData).addOnSuccessListener(documentReference -> {
            Toast.makeText(filelist.this, "File details saved to Firestore!", Toast.LENGTH_SHORT).show();
            Log.d("Firestore Success", "Document ID: " + documentReference.getId());
        }).addOnFailureListener(e -> {
            Toast.makeText(filelist.this, "Failed to save details: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("Firestore Error", "Error saving file data", e);
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }


    private void fetchFileList() {
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setRefreshing(true);
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = new Request.Builder().url(FILES_URL).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (e instanceof SocketTimeoutException) {
                    Log.e("FileListActivity", "Timeout occurred, retrying...");
                    fetchFileList();  // Retry the request
                } else {
                    Log.e("FileListActivity", "Error fetching file list", e);
                    runOnUiThread(() ->
                            Toast.makeText(filelist.this, "Failed to load files", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    swipeRefreshLayout.setRefreshing(false);
                    String responseBody = response.body().string();
                    Log.d("FileListActivity", "Response: " + responseBody);

                    List<FileModel> fetchedFiles = parseJson(responseBody);
                    runOnUiThread(() -> {
                        fileList.clear();
                        fileList.addAll(fetchedFiles);
                        fileListAdapter.notifyDataSetChanged();
                    });
                }
            }
        });
    }
    private List<FileModel> parseJson(String jsonResponse) {
        List<FileModel> fileList = new ArrayList<>();
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray jsonArray = jsonObject.getAsJsonArray("files");

            for (JsonElement element : jsonArray) {
                JsonObject fileObject = element.getAsJsonObject();
                String fileName = fileObject.get("file_name").getAsString();
                String fileUrl = fileObject.get("file_url").getAsString();
                fileList.add(new FileModel(fileName, fileUrl));
            }
        } catch (JsonSyntaxException | IllegalStateException e) {
            Log.e("FileListActivity", "Error parsing JSON", e);
        }
        return fileList;
    }


}
