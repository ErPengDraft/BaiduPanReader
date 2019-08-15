package com.example.lbaidupan;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Data;

@Data
public class BaseListBean<T> {
    private int errno;
    private int guid;
    @SerializedName("guid_info")
    private String guidInfo;
    private List<T> list;
    @SerializedName("request_id")
    private long requestId;
}
