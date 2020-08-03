package com.m4399.websocketdemo;

import android.util.Log;

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

    private String wsUrl = "local://192.168.50.190";

    private WebSocket webSocket;

    private ConnectStatus status;

    private OkHttpClient client = new OkHttpClient.Builder().build();

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

    public ConnectStatus getStatus()
    {
        return status;
    }

    public void connect()
    {
        //构造request对象
        Request request = new Request.Builder().url(wsUrl)
                                               .build();

        webSocket = client.newWebSocket(request, this);
        status = ConnectStatus.Connecting;
    }

    public void reConnect()
    {
        if (webSocket != null)
        {
            webSocket = client.newWebSocket(webSocket.request(), this);
        }
    }

    public void send(String text)
    {
        if (webSocket != null)
        {
            log("send： " + text);
            webSocket.send(text);
        }
    }

    public void cancel()
    {
        if (webSocket != null)
        {
            webSocket.cancel();
        }
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
        super.onOpen(webSocket, response);
        log("onOpen");
        this.status = ConnectStatus.Open;
        if (mSocketIOCallBack != null)
        {
            mSocketIOCallBack.onOpen();
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text)
    {
        super.onMessage(webSocket, text);
        log("onMessage: " + text);
        if (mSocketIOCallBack != null)
        {
            mSocketIOCallBack.onMessage(text);
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes)
    {
        super.onMessage(webSocket, bytes);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason)
    {
        super.onClosing(webSocket, code, reason);
        this.status = ConnectStatus.Closing;
        log("onClosing");
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason)
    {
        super.onClosed(webSocket, code, reason);
        log("onClosed");
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
        log("onFailure: " + t.toString());
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
        Log.d("ldc", text);
    }
}
