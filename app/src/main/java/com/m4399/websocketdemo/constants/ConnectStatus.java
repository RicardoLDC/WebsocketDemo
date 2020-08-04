package com.m4399.websocketdemo.constants;

/**
 * Project Name: WebsocketDemo
 * File Name:    ConnectStatus.java
 * ClassName:    ConnectStatus
 *
 * Description: TODO.
 *
 * @author LDC
 * @date 2020年08月03日 17:28
 *
 * Copyright (c) 2020年, 4399 Network CO.ltd. All Rights Reserved.
 */
public enum ConnectStatus
{
    Connecting, // the initial state of each web socket.
    Open, // the web socket has been accepted by the remote peer
    Closing, // one of the peers on the web socket has initiated a graceful shutdown
    Closed, //  the web socket has transmitted all of its messages and has received all messages from the peer
    Canceled // the web socket connection failed
}
