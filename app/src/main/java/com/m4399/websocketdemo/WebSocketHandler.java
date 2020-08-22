package com.m4399.websocketdemo;

import android.util.Log;

import com.m4399.websocketdemo.constants.ConnectStatus;
import com.m4399.websocketdemo.interfaces.WebSocketCallBack;

import java.util.concurrent.TimeUnit;

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
 * Description: WebSocket工具类.
 *
 * @author LDC
 * @date 2020年08月03日 15:04
 *
 * Copyright (c) 2020年, 4399 Network CO.ltd. All Rights Reserved.
 */
public class WebSocketHandler
{
    private String wsUrl;

    private WebSocket mWebSocket;

    private ConnectStatus status;

    private WebSocketCallBack mSocketIOCallBack;

    private OkHttpClient client = new OkHttpClient.Builder()
                                                  .readTimeout(3, TimeUnit.SECONDS)
                                                  .writeTimeout(3, TimeUnit.SECONDS)
                                                  .connectTimeout(3, TimeUnit.SECONDS)
                                                  .build();

    public WebSocketHandler(String wsUrl)
    {
        this.wsUrl = wsUrl;
    }

//    private static WebSocketHandler INST;
//
//    public static WebSocketHandler getInstance(String url)
//    {
//        if (INST == null)
//        {
//            synchronized (WebSocketHandler.class)
//            {
//                INST = new WebSocketHandler(url);
//            }
//        }
//
//        return INST;
//    }

    public WebSocket getWebSocket()
    {
        return mWebSocket;
    }

    public void sendMessage(String message)
    {
        mWebSocket.send(message);
    }

    public void sendMessage(byte... data)
    {
        ByteString bs = ByteString.of(data);

        mWebSocket.send(bs);
    }

    public void connect()
    {
        //构造request对象
        Request request = new Request.Builder().url(wsUrl).build();

        CustomWebsocketListener socketListener = new CustomWebsocketListener();

        mWebSocket = client.newWebSocket(request, socketListener);

        client.dispatcher().executorService().shutdown();

        status = ConnectStatus.Connecting;
    }

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

    private class CustomWebsocketListener extends WebSocketListener
    {
        @Override
        public void onOpen(WebSocket webSocket, Response response)
        {
            super.onOpen(webSocket, response);

            mWebSocket = webSocket;

            mWebSocket.send("welcome");

            if (mSocketIOCallBack != null)
            {
                mSocketIOCallBack.onOpen();
            }
            else
            {
                log("mSocketIOCallBack = null");
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, String text)
        {
            super.onMessage(webSocket, text);

            log("onMessage");

            log("onMessage text = " + text);

            if (mSocketIOCallBack != null)
            {
                mSocketIOCallBack.onMessage(text);
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes)
        {
            super.onMessage(webSocket, bytes);

            if (mSocketIOCallBack != null)
            {
                mSocketIOCallBack.onMessage(bytes);
            }
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason)
        {
            super.onClosing(webSocket, code, reason);

            log("onClosing");

            log("onClosing code = " + code + " , reason = " + reason);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason)
        {
            super.onClosed(webSocket, code, reason);

            log("onOpen");

            if (mSocketIOCallBack != null)
            {
                mSocketIOCallBack.onClose();
            }
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response)
        {
            super.onFailure(webSocket, t, response);

            log("WebSocketHandler onFailure = " + t.toString());

            if (mSocketIOCallBack != null)
            {
                mSocketIOCallBack.onConnectError(t);
            }
            else
            {
                log("mSocketIOCallBack = null");
            }
        }
    }
}
