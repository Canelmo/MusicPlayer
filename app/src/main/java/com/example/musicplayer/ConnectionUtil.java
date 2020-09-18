package com.example.musicplayer;

public class ConnectionUtil { public final static int ON = 1;
    public final static int OFF = 0;
    public final static int port = 8888;

    //messages
    public final static String CLIENT_START = "start";
    public final static String CLIENT_STOP = "stop";
    public final static String CLIENT_ALIVE = "alive";
    public final static String CLIENT_EXIT = "exit";
    public final static String SERVER_PING = "ping";
    public final static String SERVER_STOP = "stop";
}
