package com.example.lbaidupan;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class PanFile {
    @SerializedName("fs_id")
    private long fsId;

    @SerializedName("server_filename")
    private String serverFilename;

    private String md5;
    private int isdir; /* 是否目录，0 文件、1 目录 */
    private String path;
    // private boolean isDownload;
    private int process = -1;

    public boolean isDir() {
        return isdir == 1;
    }

    public String getProcessStr() {
        if (isDir()) {
            return "";
        }

        if (process == -1) {
            return "网盘文件";
        } else if (process == 101) {
            return "本地";
        } else if (0 <= process && process <= 100) {
            return process + "%";
        }
        return "";
    }

}
