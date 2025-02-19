package com.abc.campushub;

public class FileModel {
    private String fileName;
    private String fileUrl;

    public FileModel(String fileName, String fileUrl) {
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }
}
