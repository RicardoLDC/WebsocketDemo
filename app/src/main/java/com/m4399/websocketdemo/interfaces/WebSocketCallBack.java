package com.m4399.websocketdemo.interfaces;

import okio.ByteString;

/**
 * Project Name: WebsocketDemo
 * File Name:    WebSocketCallBack.java
 * ClassName:    WebSocketCallBack
 *
 * Description: TODO.
 *
 * @author LDC
 * @date 2020年08月03日 17:28
 *
 * Copyright (c) 2020年, 4399 Network CO.ltd. All Rights Reserved.
 */

public interface WebSocketCallBack
{
    void onOpen();

    void onMessage(String msg);

    void onMessage(ByteString msg);

    void onClose();

    void onConnectError(Throwable t);
}