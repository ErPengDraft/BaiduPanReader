package com.example.lbaidupan;

import java.util.List;

import lombok.Data;

@Data
public class BaseListBean<T> {
    private int errno;
    private int guid;
    private String guid_info;
    private List<T> list;
    private long request_id;
}
