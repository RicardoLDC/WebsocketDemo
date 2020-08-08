package com.m4399.websocketdemo.activity;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.m4399.websocketdemo.Application;
import com.m4399.websocketdemo.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
{
////    private static final String HOST = "ws://echo.websocket.org";

    private static final String APPLICATION_CLASS_NAME = Application.getInstance().getClassName();

    private static final String HOST = "localhost";

    private static final int PORT_NUMBER = 8888;

    private FrameLayout mFlContainer;

    private WebView mWebView;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initView();
    }

    public static String _pattern = "yyyy-MM-dd HH:mm:ss SSS";

    public static SimpleDateFormat format = new SimpleDateFormat(_pattern);

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

        WebChromeClient webChromeClient = new WebChromeClient();

        mWebView.setWebChromeClient(webChromeClient);

        mWebView.loadUrl("https://blog.csdn.net/weixin_34274029/article/details/87961352");

        new Thread()
        {
            @Override
            public void run()
            {
                Log.d("Ricardo", "run");

                try
                {
                    /**
                     * adb forward tcp:9222 localabstract:chrome_devtools_remote
                     * 通过localSocket实现上述adb命令
                     * webview_devtools_remote_webview进程id
                     *
                     */
                    LocalSocket localSocket = new LocalSocket();

                    Log.d("Ricardo", android.os.Process.myPid() + "");

                    localSocket.connect(new LocalSocketAddress("webview_devtools_remote_" + android.os.Process.myPid()));

//                    ServerSocket server = new ServerSocket(PORT_NUMBER);

                    Socket socket = new Socket();

                    socket.setReuseAddress(true);

                    socket.bind(new InetSocketAddress(PORT_NUMBER));

                    socket.connect(new InetSocketAddress(PORT_NUMBER));

                    InputStream inputStream = socket.getInputStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    String temp;

                    Log.d("Ricardo", "inputStream output: ----------------------------");

                    while ((temp = reader.readLine()) != null)
                    {
                        Log.d("Ricardo", temp);
                    }

                    Log.d("Ricardo", "建立链接");

                    Request request = new Request.Builder().url("http://" + HOST + ":" + PORT_NUMBER + "/json").build();

                    OkHttpClient okHttpClient = new OkHttpClient();

                    okHttpClient.newCall(request).enqueue(new Callback()
                    {
                        @Override
                        public void onFailure(Call call, IOException e)
                        {
                            Log.d("Ricardo", "onFailure: " + e.toString());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException
                        {
                            Log.d("Ricardo", "onResponse: " + response.toString());
                        }
                    });

//                    while (true)
//                    {
//                        try
//                        {
//                            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//                            Log.d("Ricardo", format.format(new Date()) + "\nClient:" + br.readLine() + "\n");
//
//                            System.out.println();
//
//                            Thread.sleep(1000);
//
//                        }
//                        catch (Exception e)
//                        {
//                            e.printStackTrace();
//                        }
//
//                    }

                }
                catch (Exception e)
                {
                    Log.d("Ricardo", "exception : " + e.toString());

                    e.printStackTrace();
                }
            }
        }.start();


//        WebSocketHandler socketHandler = WebSocketHandler.getInstance("http:" + HOST + ":" + PORT_NUMBER);

//        WebSocketHandler socketHandler = WebSocketHandler.getInstance("ws://echo.websocket.org");
//
//        socketHandler.connect();

        mFlContainer.addView(mWebView);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

//        SettingProxy.revertBackProxy(mWebView, APPLICATION_CLASS_NAME);
    }
}
