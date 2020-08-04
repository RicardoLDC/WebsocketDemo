package com.m4399.websocketdemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

/**
 * Project Name: WebsocketDemo
 * File Name:    LocalListenService.java
 * ClassName:    LocalListenService
 *
 * Description: 监听webview设置的端口号， 通过WebSocket来进行数据的传输.
 *
 * @author LDC
 * @date 2020年08月03日 15:23
 *
 * Copyright (c) 2020年, 4399 Network CO.ltd. All Rights Reserved.
 */
public class LocalListenService extends Service
{
    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
