package com.m4399.websocketdemo.activity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.m4399.websocketdemo.R;
import com.m4399.websocketdemo.WebSocketHandler;
import com.m4399.websocketdemo.interfaces.WebSocketCallBack;
import com.m4399.websocketdemo.utils.ProxyUtils;
import com.m4399.websocketdemo.utils.SettingProxy;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity
{
////    private static final String HOST = "ws://echo.websocket.org";

    private static final String HOST = "192.168.213.2";

    private static final int PORT_NUMBER = 8080;

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

//        QbSdk.forceSysWebView();

        WebSettings webSettings = mWebView.getSettings();

        mWebView.setWebContentsDebuggingEnabled(true);

        webSettings.setDomStorageEnabled(true);

        webSettings.setAllowFileAccess(true);

        webSettings.setJavaScriptEnabled(true);

        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webSettings.setDefaultTextEncodingName("utf-8");

        webSettings.setNeedInitialFocus(false);

        webSettings.setSupportMultipleWindows(true);

        WebChromeClient webChromeClient = new WebChromeClient();

        mWebView.setWebChromeClient(webChromeClient);

        mWebView.loadUrl("https://blog.csdn.net/weixin_34274029/article/details/87961352");

//        mWebView.loadUrl("ws://echo.websocket.org");

//        SettingProxy.setProxy(mWebView, HOST, PORT_NUMBER, "com.m4399.WebsocketDemo");
//
        ProxyUtils.setProxy(mWebView, HOST, PORT_NUMBER);

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
//                WebSocketHandler socketHandler = WebSocketHandler.getInstance("http:" + HOST + ":" + PORT_NUMBER);

                WebSocketHandler socketHandler = WebSocketHandler.getInstance("http://" + HOST + ":" + PORT_NUMBER);

                //                WebSocketHandler socketHandler = WebSocketHandler.getInstance("https://blog.csdn.net/weixin_34274029/article/details/87961352");

                socketHandler.setSocketIOCallBack(new WebSocketCallBack()
                {
                    @Override
                    public void onOpen()
                    {
                        Log.d("ldc", " onOpen");
                    }

                    @Override
                    public void onMessage(String msg)
                    {
                        Log.d("ldc", " onMessage " + msg);
                    }

                    @Override
                    public void onClose()
                    {
                        Log.d("ldc", " onClose");
                    }

                    @Override
                    public void onConnectError(Throwable t)
                    {
                    }
                });

                socketHandler.connect();
            }
        }).run();

        mFlContainer.addView(mWebView);
    }
}
