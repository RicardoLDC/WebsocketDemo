package com.m4399.websocketdemo.interfaces;

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

    void onClose();

    void onConnectError(Throwable t);
}