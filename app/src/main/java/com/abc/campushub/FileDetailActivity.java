package com.abc.campushub;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import java.util.Arrays;

public class FileDetailActivity extends AppCompatActivity {
    public static final String EXTRA_FILE_NAME = "extra_file_name";
    public static final String EXTRA_FILE_URL = "extra_file_url";
    public static final String EXTRA_FILE_TAGS = "extra_file_tags";

    private String fileName, fileUrl;
    private String[] fileTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_detail);

        ImageView filePreview = findViewById(R.id.filePreview);
        TextView tvFileName = findViewById(R.id.tvFileName);
        TextView tvFileTags = findViewById(R.id.tvFileTags);
        Button btnDownload = findViewById(R.id.btnDownload);
        Button btnShare = findViewById(R.id.btnShare);

        Intent intent = getIntent();
        fileName = intent.getStringExtra(EXTRA_FILE_NAME);
        fileUrl = intent.getStringExtra(EXTRA_FILE_URL);
        fileTags = intent.getStringArrayExtra(EXTRA_FILE_TAGS);

        tvFileName.setText(fileName);

        if (fileTags != null && fileTags.length > 0) {
            tvFileTags.setText("Tags: " + String.join(", ", fileTags));
        } else {
            tvFileTags.setText("No tags available");
        }

        if (fileUrl.endsWith(".jpg") || fileUrl.endsWith(".jpeg") || fileUrl.endsWith(".png")) {
            Glide.with(this).load(fileUrl).placeholder(R.drawable.img).into(filePreview);
        } else {
            filePreview.setImageResource(R.drawable.img);
        }

        btnDownload.setOnClickListener(v -> downloadFile());
        btnShare.setOnClickListener(v -> shareFile());
    }

    private void downloadFile() {
        try {
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(fileUrl);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            downloadManager.enqueue(request);
            Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void shareFile() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this file: " + fileName);
        shareIntent.putExtra(Intent.EXTRA_TEXT, fileUrl);
        startActivity(Intent.createChooser(shareIntent, "Share file via"));
    }
}
