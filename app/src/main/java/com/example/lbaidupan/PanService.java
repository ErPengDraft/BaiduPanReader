package com.example.lbaidupan;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PanService {

    // https://pan.baidu.com/rest/2.0/xpan/file?method=list
    // path=/Learn/Books
    @GET("rest/2.0/xpan/file?method=list")
    Call<BaseListBean<PanFile>> fileList(@Query("access_token") String access_token,
                                 @Query("dir") String dir);
}
