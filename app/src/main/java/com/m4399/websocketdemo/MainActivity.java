package com.m4399.websocketdemo;

import android.os.Build;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity
{
//    private static final String HOST = "https://blog.csdn.net/weixin_34274029/article/details/87961352";
    private static final String HOST = "http://localhost:8888";

    private static final int PORT_NUMBER = 8888;

    private FrameLayout mFlContainer;

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initView();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initView()
    {
        mFlContainer = findViewById(R.id.fl_container);

        mWebView = new WebView(getApplicationContext());

        WebSettings webSettings = mWebView.getSettings();

        mWebView.setWebContentsDebuggingEnabled(true);

        webSettings.setDomStorageEnabled(true);

        webSettings.setAllowFileAccess(true);

        webSettings.setJavaScriptEnabled(true);

        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webSettings.setDefaultTextEncodingName("utf-8");

        webSettings.setNeedInitialFocus(false);

        webSettings.setSupportMultipleWindows(true);

        WebChromeClient webChromeClient = new WebChromeClient()
        {
            @Override
            public void onProgressChanged(WebView view, int newProgress)
            {
                super.onProgressChanged(view, newProgress);
            }
        };

        mWebView.setWebChromeClient(webChromeClient);

        mWebView.loadUrl(HOST);

        SettingProxy.setProxy(mWebView, HOST, PORT_NUMBER, "com.m4399.WebsocketDemo");

        WebSocketHandler.getInstance("http://115.182.10.20:8888").connect();

        mFlContainer.addView(mWebView);
    }
}
