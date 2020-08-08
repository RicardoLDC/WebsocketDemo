package com.m4399.websocketdemo;

/**
 * Project Name: WebsocketDemo
 * File Name:    Application.java
 * ClassName:    Application
 *
 * Description: TODO.
 *
 * @author LDC
 * @date 2020年08月04日 9:39
 *
 * Copyright (c) 2020年, 4399 Network CO.ltd. All Rights Reserved.
 */
public class Application extends android.app.Application
{
    private static Application mInstance;

    public static Application getInstance()
    {
        synchronized (Application.class)
        {
            if (mInstance == null)
            {
                mInstance = new Application();
            }
        }

        return mInstance;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    public String getClassName()
    {
        return getClass().getName();
    }
}
