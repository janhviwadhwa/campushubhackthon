package com.abc.campushub;

import java.io.File;

public class FileModel {
    private String fileName;
    private String filePath;

    public FileModel(String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    // Returns file path as a URL
    public String getFileUrl() {
        return filePath;
    }

    // Returns true if the file exists
    public boolean fileExists() {
        File file = new File(filePath);
        return file.exists();
    }
}
