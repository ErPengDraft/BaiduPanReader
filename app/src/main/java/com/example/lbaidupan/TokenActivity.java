package com.example.lbaidupan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.tencent.mmkv.MMKV;

import lombok.val;
import lombok.var;

public class TokenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.token_activity);

        val kv = MMKV.defaultMMKV();

        findViewById(R.id.button).setOnClickListener(v -> {
            getToken();
        });

        String token = null;
        long token_expires = 0L;

        val params = getIntent().getDataString();
        if (params != null) {
            val ACCESS_TOKEN = "access_token=";
            var start = params.indexOf(ACCESS_TOKEN) + ACCESS_TOKEN.length();
            var end = params.indexOf('&', start);
            token = params.substring(start, end);

            val EXPIRES_IN = "expires_in=";
            start = params.indexOf(EXPIRES_IN) + EXPIRES_IN.length();
            end = params.indexOf('&', start);
            token_expires = Long.valueOf(params.substring(start, end));

            kv.putString("token", token);
            kv.putLong("token_expires", System.currentTimeMillis() + token_expires * 1000);
        } else {
            token = kv.getString("token", null);
            token_expires = kv.getLong("token_expires", 0);
        }

        if (token_expires - System.currentTimeMillis() < 24 * 60 * 60 * 1000 || TextUtils.isEmpty(token)) {
            if (TextUtils.isEmpty(params)) {
                getToken();
            }
        } else {
            Intent intent = new Intent(this, PanActivity.class);
            startActivity(intent);
            finish();
        }
    }

    void getToken() {
        String url = "https://openapi.baidu.com/oauth/2.0/authorize?response_type=token&client_id=B9HoO4rtoULNLpHYom53LcMb&redirect_uri=lbaidupan://token&scope=basic,netdisk&display=popup&state=xxx";
        Intent intent = new Intent(TokenActivity.this, WebViewActivity.class);
        intent.putExtra("url", url);
        startActivity(intent);
        finish();
    }
}
