package com.lei.mykha.chatapp.activities.socketiochat;

import android.app.Application;
import android.content.Context;

/**
 * Created by mykha on 01/06/2017.
 */

public class AppContext extends Application {
    private static Context context;
    private static EmitterListeners emitterListeners;
    public void onCreate(){
        super.onCreate();
        AppContext.context = getApplicationContext();
        emitterListeners = new EmitterListeners(context);
        initializeSocket();
    }

    public static Context getAppContext() {
        return AppContext.context;
    }

    public void initializeSocket(){
        AppSocketListener.getInstance().initialize();
    }

    public void destroySocketListener(){
        AppSocketListener.getInstance().destroy();
    }

    public static EmitterListeners getEmitterListeners(){ return emitterListeners; }

    @Override
    public void onTerminate() {
        super.onTerminate();
        destroySocketListener();
    }
}
