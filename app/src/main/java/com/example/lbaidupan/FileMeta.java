package com.example.lbaidupan;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class FileMeta {
    private int category;
    @SerializedName("date_taken")
    private long dateTaken;
    private String dlink;
    private String filename;
    @SerializedName("fs_id")
    private long fsId;
    private String md5;
    private long size;
    private String path;
}
