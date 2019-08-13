package com.example.lbaidupan;

import android.view.View;

import androidx.annotation.IdRes;

import lombok.Getter;
import lombok.Singular;
import lombok.Synchronized;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitFactory {
    @Getter(lazy=true)
    private static final Retrofit retrofit = retrofit();

    private static Retrofit retrofit() {

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .build();

        return new Retrofit.Builder()
                .baseUrl("https://pan.baidu.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }

    public static <T> T create(final Class<T> service) {
        return getRetrofit().create(service);
    }

}
