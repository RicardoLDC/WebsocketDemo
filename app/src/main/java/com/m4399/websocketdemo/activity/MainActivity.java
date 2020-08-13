package com.m4399.websocketdemo.activity;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;

import com.m4399.websocketdemo.R;
import com.m4399.websocketdemo.WebSocketHandler;
import com.m4399.websocketdemo.interfaces.WebSocketCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private static final String HOST = "localhost";

    private static final int PORT_NUMBER = 8888;

    //WebView容器
    private FrameLayout mFlContainer;

    //WebView实例
    private WebView mWebView;

    //连接按钮
    private Button mBtnConnect;

    private WebSocketHandler mWebSocketHandler;

    private WebSocketHandler mWebSocketReceiveHandler;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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

        mBtnConnect = findViewById(R.id.btn_connect);

        mBtnConnect.setOnClickListener(this);

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
                    ServerSocket mServerSocket = new ServerSocket(PORT_NUMBER);

                    while (true)
                    {
                        Socket socket = mServerSocket.accept();

                        byte[] bytes = new byte[512];

                        socket.getInputStream().read(bytes);

                        Log.d("Ricardo", "tcp 建立链接");

                        String input = new String(bytes);

                        Log.d("Ricardo", "String:" + input);

                        Log.d("Ricardo", Process.myPid() + "");

                        if (input.contains("websocket"))
                        {
                            new Thread()
                            {
                                @Override
                                public void run()
                                {
                                    connectToWebViewWebsocket(bytes, socket);
                                }
                            }.start();
                        }
                        else
                        {
                            byte[] result = connectToWebViewHttp(bytes);

                            socket.getOutputStream().write(result);

                            log("localsocket read bytes:" + new String(result));

                            socket.getOutputStream().close();

                            socket.close();
                        }
                    }

                }
                catch (Exception e)
                {
                    Log.d("Ricardo", "exception : " + e.toString());

                    e.printStackTrace();
                }
            }
        }.start();

        mFlContainer.addView(mWebView);
    }

    private Request request;

    private void getDeBugInfo()
    {

        String url = "http://" + HOST + ":" + PORT_NUMBER + "/json";

        if (request == null)
        {
            request = new Request.Builder().get()
                                           .url(url)
                                           .build();
        }

        Log.d("Ricardo", "网络请求:"+url);

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();

        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor)
                                                              .build();

        okHttpClient.newCall(request)
                    .enqueue(new Callback()
                    {
                        @Override
                        public void onFailure(Call call, IOException e)
                        {
                            Log.d("Ricardo", "网络请求 onFailure: " + e.toString());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException
                        {
                            String debugInfoStr = new String(response.body().bytes());

                            Log.d("Ricardo", "网络请求 onResponse: " + debugInfoStr);

                            if (!TextUtils.isEmpty(debugInfoStr) && debugInfoStr.contains("webSocketDebuggerUrl"))
                            {
                                log("debugInfoStr != null");

                                String webSocketDebuggerUrl;

                                String jsonStr = debugInfoStr.substring(debugInfoStr.indexOf('{'), debugInfoStr.lastIndexOf('}') + 1);

                                JSONObject object = parseJSONObjectFromString(jsonStr);

                                webSocketDebuggerUrl = getString("webSocketDebuggerUrl", object);

                                log(webSocketDebuggerUrl);

                                //fixme 连接失败，onOpen()、onMessage()、onFailure()未响应
                                mWebSocketHandler = WebSocketHandler.getInstance(webSocketDebuggerUrl);

                                mWebSocketHandler.setSocketIOCallBack(new WebSocketCallBack()
                                {
                                    @Override
                                    public void onOpen()
                                    {

                                    }

                                    @Override
                                    public void onMessage(String msg)
                                    {
                                        //接收数据后传递到mWebSocketReceiveHandler中的websocket中
                                        //由mWebSocketReceiveHandler转发到游戏方
                                        log("onMessage");


                                    }

                                    @Override
                                    public void onClose()
                                    {

                                    }

                                    @Override
                                    public void onConnectError(Throwable t)
                                    {

                                    }
                                });

                                mWebSocketHandler.connect();

                                mWebSocketReceiveHandler = WebSocketHandler.getInstance(webSocketDebuggerUrl);

                                mWebSocketReceiveHandler.setSocketIOCallBack(new WebSocketCallBack()
                                {
                                    @Override
                                    public void onOpen()
                                    {

                                    }

                                    @Override
                                    public void onMessage(String msg)
                                    {
                                        //双向数据传递， 包装协议进行区分
                                    }

                                    @Override
                                    public void onClose()
                                    {

                                    }

                                    @Override
                                    public void onConnectError(Throwable t)
                                    {

                                    }
                                });

                                mWebSocketReceiveHandler.connect();
                            }
                        }
                    });
    }

    private byte[] connectToWebViewHttp(byte[] requestData)
    {
        try
        {
            String name = "webview_devtools_remote_" + android.os.Process.myPid();

            //todo 是否有优化空间，不稳定
            LocalSocket localSocket = new LocalSocket();

            localSocket.connect(new LocalSocketAddress(name));

            Log.d("Ricardo", "localsocket 建立http链接");

            localSocket.getOutputStream().write(requestData);

            localSocket.getOutputStream().flush();

            int length1 = localSocket.getInputStream().available();

            byte[] bytes1 = new byte[length1];

            localSocket.getInputStream().read(bytes1);

            localSocket.close();

            return bytes1;
        }
        catch (IOException e)
        {
            e.printStackTrace();

            log(e.toString());
        }

        return null;
    }

    private void connectToWebViewWebsocket(byte[] requestData, Socket socket)
    {
        try
        {
            String name = "webview_devtools_remote_" + android.os.Process.myPid();

            LocalSocket localSocket = new LocalSocket();

            localSocket.connect(new LocalSocketAddress(name));

            Log.d("Ricardo", "localsocket 建立ws链接");

            localSocket.getOutputStream().write(requestData);

            localSocket.getOutputStream().flush();

            while (true)
            {
                byte[] buffer = new byte[1024];

                int fileSize = 0;

                int bytesRead;

                while ((bytesRead = localSocket.getInputStream().read(buffer)) > 0)
                {
                    socket.getOutputStream().write(buffer, 0, bytesRead);

                    socket.getOutputStream().flush();

                    fileSize += bytesRead;
                }

                Log.d("Ricardo", "localsocket 建立ws response fileSize:"+fileSize);

                if (fileSize <= 0)
                {
                    socket.getOutputStream()
                          .write("111".getBytes());

                    socket.getOutputStream()
                          .flush();
                }

                try
                {
                    Thread.sleep(2000);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();

            log("connectToWebViewWebsocket exception : " + e.toString());
        }
    }

    private JSONObject parseJSONObjectFromString(String json)
    {
        JSONObject jsonObj = new JSONObject();
        if (!TextUtils.isEmpty(json))
        {
            try
            {
                jsonObj = new JSONObject(json);
            }
            catch (JSONException e)
            {
                e.printStackTrace();

                log("Exception occured when parsing the json:" + json);
            }
        }

        return jsonObj;
    }

    private String getString(String key, JSONObject object)
    {
        String value = "";

        if (object == null)
        {
            return value;
        }

        try
        {
            if (object.has(key))
            {
                value = object.getString(key);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return value;
    }

    private void log(String msg)
    {
        Log.d("Ricardo", msg);
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.btn_connect)
        {
            //连接操作
            getDeBugInfo();
        }
    }
}
