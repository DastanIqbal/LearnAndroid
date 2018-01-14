package com.function.product.cm.bean;

import android.graphics.drawable.Drawable;
import java.io.Serializable;

public class PhoneFileItem implements Serializable {
    private Drawable fileIcon;
    private String fileName;
    private String filePath;
    private long fileSize;
    private boolean isCheck;

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Drawable getFileIcon() {
        return this.fileIcon;
    }

    public void setFileIcon(Drawable fileIcon) {
        this.fileIcon = fileIcon;
    }

    public long getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isCheck() {
        return this.isCheck;
    }

    public void setCheck(boolean check) {
        this.isCheck = check;
    }
}
