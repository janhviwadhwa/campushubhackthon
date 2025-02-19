package com.abc.campushub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.FileViewHolder> {
    private List<FileModel> fileList;
    private Context context;

    public MyListAdapter(Context context, List<FileModel> fileList) {
        this.context = context;
        this.fileList = fileList;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileModel file = fileList.get(position);
        holder.fileName.setText(file.getFileName());

        // Load image using Glide
        Glide.with(context)
                .load(file.getFileUrl())
                .placeholder(R.drawable.img) // Optional: Set a placeholder image
                .into(holder.fileImage);
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView fileName;
        ImageView fileImage;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.file_name);
            fileImage = itemView.findViewById(R.id.file_image);
        }
    }
}
