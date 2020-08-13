package com.m4399.websocketdemo;

import android.util.Log;

import com.m4399.websocketdemo.constants.ConnectStatus;
import com.m4399.websocketdemo.interfaces.WebSocketCallBack;

import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Project Name: WebsocketDemo
 * File Name:    WebSocketHandler.java
 * ClassName:    WebSocketHandler
 *
 * Description: TODO.
 *
 * @author LDC
 * @date 2020年08月03日 15:04
 *
 * Copyright (c) 2020年, 4399 Network CO.ltd. All Rights Reserved.
 */
public class WebSocketHandler extends WebSocketListener
{
    private static final String TAG = "WebSocketHandler";

    private String wsUrl;

    private WebSocket webSocket;

    private ConnectStatus status;

    private OkHttpClient client = new OkHttpClient.Builder()
                                                  .readTimeout(10, TimeUnit.SECONDS)
                                                  .writeTimeout(10, TimeUnit.SECONDS)
                                                  .connectTimeout(10, TimeUnit.SECONDS)
                                                  .pingInterval(40, TimeUnit.SECONDS)
                                                  .build();

    private WebSocketHandler(String wsUrl)
    {
        this.wsUrl = wsUrl;
    }

    private static WebSocketHandler INST;

    public static WebSocketHandler getInstance(String url)
    {
        if (INST == null)
        {
            synchronized (WebSocketHandler.class)
            {
                INST = new WebSocketHandler(url);
            }
        }

        return INST;
    }

    public WebSocket getWebSocket()
    {
        return webSocket;
    }

    public void connect()
    {
        //构造request对象
        Request request = new Request.Builder().url(wsUrl).build();

        client.newWebSocket(request, WebSocketHandler.this);

        client.dispatcher().executorService().shutdown();

        status = ConnectStatus.Connecting;
    }

    public void close()
    {
        if (webSocket != null)
        {
            webSocket.close(1000, null);
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response)
    {
//        super.onOpen(webSocket, response);
        log("WebSocket onOpen");

        webSocket.send("hello world");
        webSocket.send("welcome");
        webSocket.close(1000, "再见");
        this.status = ConnectStatus.Open;
        if (mSocketIOCallBack != null)
        {
            mSocketIOCallBack.onOpen();
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text)
    {
//        super.onMessage(webSocket, text);
        log("WebSocket onMessage: " + text);
        if (mSocketIOCallBack != null)
        {
            mSocketIOCallBack.onMessage(text);
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes)
    {
        super.onMessage(webSocket, bytes);
        log("WebSocket bytes = " + bytes);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason)
    {
        super.onClosing(webSocket, code, reason);
//        close();
        this.status = ConnectStatus.Closing;
        log("WebSocket onClosing");
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason)
    {
        super.onClosed(webSocket, code, reason);
        log("WebSocket onClosed");
        this.status = ConnectStatus.Closed;
        if (mSocketIOCallBack != null)
        {
            mSocketIOCallBack.onClose();
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response)
    {
        super.onFailure(webSocket, t, response);
        log("WebSocket onFailure: " + t.toString());
        t.printStackTrace();
        this.status = ConnectStatus.Canceled;
        if (mSocketIOCallBack != null)
        {
            mSocketIOCallBack.onConnectError(t);
        }
    }

    private WebSocketCallBack mSocketIOCallBack;

    public void setSocketIOCallBack(WebSocketCallBack callBack)
    {
        mSocketIOCallBack = callBack;
    }

    public void removeSocketIOCallBack()
    {
        mSocketIOCallBack = null;
    }

    private void log(String text)
    {
        Log.d("Ricardo", text);
    }
}
