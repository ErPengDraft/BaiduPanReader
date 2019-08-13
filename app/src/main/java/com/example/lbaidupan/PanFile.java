package com.example.lbaidupan;

import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.Getter;

@Data
public class PanFile {
    /*
    {
        "category": 6,
        "unlist": 0,
        "fs_id": 367516000038625,
        "dir_empty": 0,
        "oper_id": 3389535607,
        "server_ctime": 1541510747,
        "local_mtime": 1541510747,
        "size": 0,
        "share": 0,
        "isdir": 1,
        "path": "/baidu/test",
        "local_ctime": 1541510747,
        "server_filename": "test",
        "empty": 0,
        "server_mtime": 1541510747
    }
    */

    @SerializedName("fs_id")
    private String fsId;

    @SerializedName("server_filename")
    private String serverFilename;

    private String md5;
}
