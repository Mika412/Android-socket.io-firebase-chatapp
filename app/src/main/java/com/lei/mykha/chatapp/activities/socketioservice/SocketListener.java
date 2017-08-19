package com.lei.mykha.chatapp.activities.socketioservice;

/**
 * Created by mykha on 01/06/2017.
 */

public interface SocketListener {
    void onSocketConnected();
    void onSocketDisconnected();
    void onNewMessageReceived(String username, String message);
}
