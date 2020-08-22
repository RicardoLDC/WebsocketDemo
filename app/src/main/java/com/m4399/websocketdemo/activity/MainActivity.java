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
import com.m4399.websocketdemo.utils.JSONUtils;
import com.tencent.smtt.utils.ByteUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.ByteString;

import static com.m4399.websocketdemo.utils.JSONUtils.parseJSONObjectFromString;

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

    private LocalSocket localSocket;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initView();
    }

    private boolean isFirstTime = true;

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
                    String name = "webview_devtools_remote_" + Process.myPid();

                    ServerSocket mServerSocket = new ServerSocket(PORT_NUMBER);

                    while (true)
                    {
                        Socket socket = mServerSocket.accept();

                        new Thread()
                        {
                            @Override
                            public void run()
                            {
                                while (!socket.isClosed())
                                {
                                    try
                                    {
                                        Log.d("Ricardo", "tcp 建立链接");

                                        byte[] bytes = new byte[1024 * 64];

                                        int size = socket.getInputStream()
                                                         .read(bytes);

                                        Log.d("Ricardo", "size = " + size);

                                        byte[] result = ByteUtils.subByte(bytes, 0, size);

                                        Log.d("Ricardo", "读取内容 ：" + new String(result));

                                        localSocket = new LocalSocket();

                                        localSocket.connect(new LocalSocketAddress(name));

                                        Log.d("Ricardo", "localsocket 建立LocalSocket链接");

                                        localSocket.getOutputStream()
                                                   .write(result);

                                        Log.d("Ricardo", "localsocket 建立ws request str:" + new String(result));
//                                        connectToLocalSocket(socket);

                                    }
                                    catch (IOException e)
                                    {
                                        Log.d("Ricardo", Log.getStackTraceString(e));

                                        e.printStackTrace();
                                    }

                                    new Thread()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            while (true)
                                            {
                                                try
                                                {
                                                    byte[] buffer = new byte[1024 * 1024];

                                                    int bytesRead;

                                                    while (localSocket != null && (bytesRead = localSocket.getInputStream().read(buffer)) != -1)
                                                    {
                                                        Log.d("Ricardo",
                                                              "localsocket 建立ws response filestr:" + new String(
                                                                      ByteUtils.subByte(buffer, 0,
                                                                                        bytesRead)));

                                                        socket.getOutputStream()
                                                              .write(buffer, 0, bytesRead);
                                                    }

                                                    if (isFirstTime)
                                                    {
                                                        isFirstTime = false;

                                                        socket.getOutputStream()
                                                              .close();

                                                        socket.close();

                                                        localSocket.close();

                                                        break;
                                                    }

                                                }
                                                catch (Exception e)
                                                {
                                                    Log.d("Ricardo", Log.getStackTraceString(e));

                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }.start();
                                }
                            }
                        }.start();
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

    private void connectToLocalSocket(Socket socket)
    {
        String name = "webview_devtools_remote_" + Process.myPid();

        try
        {
            Log.d("Ricardo", "tcp 建立链接");

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                                                                             StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();

            String line;

            while ((line = reader.readLine()) != null)
            {
                sb.append(line);
            }

            Log.d("Ricardo", "content = " + sb.toString());

            byte[] result = sb.toString().getBytes(StandardCharsets.UTF_8);

            Log.d("Ricardo", "读取内容 ：" + new String(result));

            localSocket = new LocalSocket();

            localSocket.connect(new LocalSocketAddress(name));

            Log.d("Ricardo", "localsocket 建立LocalSocket链接");

            localSocket.getOutputStream()
                       .write(result);

            Log.d("Ricardo", "localsocket 建立ws request str:" + new String(result));
        }
        catch (IOException e)
        {
            Log.d("Ricardo", Log.getStackTraceString(e));
            e.printStackTrace();
        }
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
                                                              .readTimeout(30, TimeUnit.SECONDS)
                                                              .writeTimeout(30, TimeUnit.SECONDS)
                                                              .callTimeout(30, TimeUnit.SECONDS)
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
                                String webSocketDebuggerUrl;

                                String jsonStr = debugInfoStr.substring(debugInfoStr.indexOf('{'), debugInfoStr.lastIndexOf('}') + 1);

                                JSONObject object = parseJSONObjectFromString(jsonStr);

                                webSocketDebuggerUrl = JSONUtils.getString("webSocketDebuggerUrl", object);

                                log(webSocketDebuggerUrl);

                                mWebSocketHandler = new WebSocketHandler(webSocketDebuggerUrl);

                                mWebSocketHandler.setSocketIOCallBack(new WebSocketCallBack()
                                {
                                    @Override
                                    public void onOpen()
                                    {
                                        log("连接成功");

                                        try
                                        {
                                            JSONObject object = new JSONObject();

                                            object.put("type", 1);

                                            object.put("message", "123456789");

                                            mWebSocketHandler.sendMessage(object.toString());
                                        }
                                        catch (JSONException e)
                                        {
                                            Log.d("Ricardo", Log.getStackTraceString(e));

                                            e.printStackTrace();
                                        }

//                                        mWebSocketHandler.sendMessage("123456789\n");

//                                        Log.d("Ricardo", "发送数据 ：" + "123456789");

//                                        while (true)
//                                        {
//                                            mWebSocketHandler.sendMessage("i");
//
//                                            try
//                                            {
//                                                Thread.sleep(2000);
//                                            }
//                                            catch (Exception e)
//                                            {
//                                                LogUtils.logd("connectToWebViewWebSocket() exception = " + e.toString());
//                                                e.printStackTrace();
//                                            }
//                                        }
                                    }

                                    @Override
                                    public void onMessage(String msg)
                                    {
                                        //接收数据后传递到mWebSocketReceiveHandler中的websocket中
                                        //由mWebSocketReceiveHandler转发到游戏方
                                        log("onMessage");

                                        JSONObject obj = JSONUtils.parseJSONObjectFromString(msg);

                                        //"code" = -1 => 接收远端数据，发送给mWebSocketReceiveHandler
                                        if (JSONUtils.getInt("code", object) == -1)
                                        {
                                            mWebSocketReceiveHandler.getWebSocket().send(msg);
                                        }
                                        //"code" != -1 => 从mWebSocketReceiveHandler接收数据， 发送给远端
                                        else
                                        {

                                        }
                                    }

                                    @Override
                                    public void onMessage(ByteString msg)
                                    {
                                        log("onMessage");
                                    }

                                    @Override
                                    public void onClose()
                                    {
                                        log("onClose");
                                    }

                                    @Override
                                    public void onConnectError(Throwable t)
                                    {
                                        log("连接发生异常");
                                        log("onFailure throwable = " + t.toString());
                                    }
                                });

                                mWebSocketHandler.connect();

//                                mWebSocketReceiveHandler = new WebSocketHandler("ws://echo.websocket.org");
//
//                                mWebSocketReceiveHandler.setSocketIOCallBack(new WebSocketCallBack()
//                                {
//                                    @Override
//                                    public void onOpen()
//                                    {
//                                        //连接成功后返回
//                                    }
//
//                                    @Override
//                                    public void onMessage(String msg)
//                                    {
//                                        //双向数据传递， 包装协议进行区分
//
//                                        //todo 协议地址尚未确定
//
////                                        log("onMessage : " + msg);
//
//                                        mWebSocketHandler.getWebSocket().send(msg);
//                                    }
//
//                                    @Override
//                                    public void onMessage(ByteString msg)
//                                    {
//
//                                    }
//
//                                    @Override
//                                    public void onClose()
//                                    {
//                                        log("onClose");
//                                    }
//
//                                    @Override
//                                    public void onConnectError(Throwable t)
//                                    {
//
//                                    }
//                                });
//
//                                mWebSocketReceiveHandler.connect();
                            }
                        }
                    });
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
