package com.example.lbaidupan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.BridgeWebViewClient;
import com.google.gson.Gson;

public class WebViewActivity extends AppCompatActivity {

    private BridgeWebView webView;
    private View errorImage;
    private boolean isSuccess;
    private Gson gson = new Gson();
    private String recordCallback = "recordCallback";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_view_activity);

        // 错误页
        errorImage = findViewById(R.id.error_image);
        errorImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        });

        // webView 初始化
        webView = findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);

        // 添加本地存储支持
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        webView.getSettings().setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);

        // 设置chromeClient
        webView.setWebChromeClient(new WebChromeClient());

        // 展示错误页
        BridgeWebViewClient client = new BridgeWebViewClient(webView) {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                errorImage.setVisibility(View.GONE);
                isSuccess = true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (isSuccess) {
                    errorImage.setVisibility(View.GONE);
                } else {
                    errorImage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return;
                }
                errorImage.setVisibility(View.VISIBLE);
                isSuccess = false;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                errorImage.setVisibility(View.VISIBLE);
                isSuccess = false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && url.startsWith("lbaidupan://")) {
                    try {
                        // 以下固定写法
                        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        // 防止没有安装的情况
                        e.printStackTrace();
                    }
                    return true;
                }

                return super.shouldOverrideUrlLoading(view, url);
            }
        };

        webView.setWebViewClient(client);

        // 设置userAgent
        String userAgent = webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(userAgent + " JCMobile/0.2");

        String url = getIntent().getStringExtra("url");
        webView.loadUrl(url);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            } else {
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
            if (resultCode == 1001) {
                String indexUri = data.getStringExtra("indexUri");
                webView.loadUrl(indexUri);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }


}
