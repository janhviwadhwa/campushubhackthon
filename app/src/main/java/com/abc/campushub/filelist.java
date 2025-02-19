package com.abc.campushub;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class filelist extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton btnAddFile;
    private MyListAdapter fileAdapter;
    private List<String> fileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_filelist);

        // Handle system insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        recyclerView = findViewById(R.id.recyclerView);
        btnAddFile = findViewById(R.id.btnAddFile);

        // Initialize File List
        fileList = new ArrayList<>();
        fileList.add("Document 1.pdf");
        fileList.add("Lecture Notes.docx");
        fileList.add("Assignment.xlsx");

        // Set up RecyclerView
        fileAdapter = new MyListAdapter(fileList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(fileAdapter);

        // Handle FloatingActionButton Click
        btnAddFile.setOnClickListener(view -> {
            Toast.makeText(this, "Add File Clicked", Toast.LENGTH_SHORT).show();
        });
    }
}
