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


/*
    path	string	否	查询共享目录文件时需要，格式: /<share>uk-fsid，uk对应共享目录创建者ID，fsid对应共享目录的fsid
    fsids	json array	是	fsid数组，数组中元素类型为uint64，大小上限100
    thumb	int	否	是否需要缩略图地址，0 否、1 是，默认0
    dlink	int	否	是否需要文件下载地址dlink，0 否、1 是，默认0
    extra	int	否	图片是否需要拍摄时间、原图分辨率等其他信息，0 否、1 是，默认0
            */

    // https://pan.baidu.com/rest/2.0/xpan/multimedia?method=filemetas

    @GET(value = "rest/2.0/xpan/multimedia?method=filemetas&dlink=1")
    Call<BaseListBean<FileMeta>> filemetas(@Query("access_token") String access_token,
                                         @Query(value="fsids", encoded = true) String fsids);
}
