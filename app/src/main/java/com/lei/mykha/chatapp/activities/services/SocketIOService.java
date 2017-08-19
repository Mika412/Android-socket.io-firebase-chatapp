package com.lei.mykha.chatapp.activities.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.lei.mykha.chatapp.activities.Constants;
import com.lei.mykha.chatapp.activities.socketioservice.SocketEventConstants;
import com.lei.mykha.chatapp.activities.socketioservice.SocketListener;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketIOService extends Service {
    private SocketListener socketListener;
    private Boolean appConnectedToService;
    private Socket mSocket;
    private boolean serviceBinded = false;
    private final LocalBinder mBinder = new LocalBinder();

    public void setAppConnectedToService(Boolean appConnectedToService) {
        this.appConnectedToService = appConnectedToService;
    }

    public void setSocketListener(SocketListener socketListener) {
        this.socketListener = socketListener;
    }

    public class LocalBinder extends Binder{
       public SocketIOService getService(){
            return SocketIOService.this;
        }
    }

    public void setServiceBinded(boolean serviceBinded) {
        this.serviceBinded = serviceBinded;
    }

    @Override
    public IBinder onBind(Intent intent) {
       return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeSocket();
        addSocketHandlers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeSocketSession();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return serviceBinded;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void initializeSocket() {
        try{
            IO.Options options = new IO.Options();
            mSocket = IO.socket(Constants.CHAT_SERVER_URL,options);
        }
        catch (Exception e){
            Log.e("Error", "Exception in socket creation");
            throw new RuntimeException(e);
        }
    }

    private void closeSocketSession(){
        mSocket.disconnect();
        mSocket.off();
    }
    private void addSocketHandlers(){

        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Intent intent = new Intent(SocketEventConstants.socketConnection);
                intent.putExtra("connectionStatus", true);
                broadcastEvent(intent);
            }
        });

        mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Intent intent = new Intent(SocketEventConstants.socketConnection);
                intent.putExtra("connectionStatus", false);
                broadcastEvent(intent);
            }
        });


        mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Intent intent = new Intent(SocketEventConstants.connectionFailure);
                broadcastEvent(intent);
            }
        });

        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Intent intent = new Intent(SocketEventConstants.connectionFailure);
                broadcastEvent(intent);
            }
        });
        mSocket.connect();
    }

    public void emit(String event,Object[] args,Ack ack){
        mSocket.emit(event, args, ack);
    }
    public void emit (String event,Object... args) {
        try {
            mSocket.emit(event, args,null);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void addOnHandler(String event,Emitter.Listener listener){
            mSocket.on(event, listener);
    }

    public void connect(){
        mSocket.connect();
    }

    public void disconnect(){
        mSocket.disconnect();
    }

    public void restartSocket(){
        mSocket.off();
        mSocket.disconnect();
        addSocketHandlers();
    }

    public void off(String event){
        mSocket.off(event);
    }

    private void broadcastEvent(Intent intent){
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public boolean isSocketConnected(){
        if (mSocket == null){
            return false;
        }
        return mSocket.connected();
    }



}
