package com.lei.mykha.chatapp.activities.socketiochat;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.lei.mykha.chatapp.activities.data.DatabaseContract;
import com.lei.mykha.chatapp.activities.socketioservice.SocketEventConstants;
import com.lei.mykha.chatapp.activities.utilities.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.emitter.Emitter;

/**
 * Created by mykha on 8/13/2017.
 */

public class EmitterListeners extends AppCompatActivity {
    String TAG  = "SocketListener";

    private Context context;

    public EmitterListeners(Context appContext) {
        this.context = appContext;
    }

    public Emitter.Listener onFriendListReceive = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jObj = (JSONObject) args[0];
                        ContentValues[] users = JsonUtils.getUserContentValuesFromJson(jObj.toString());
                        Uri insertContentUri = com.lei.mykha.chatapp.activities.data.DatabaseContract.Friends.CONTENT_URI;

                        ContentResolver usersContentResolver = context.getContentResolver();
                        usersContentResolver.delete(
                                insertContentUri,
                                null,
                                null);

                        if (users != null && users.length != 0) {
                            usersContentResolver.bulkInsert(
                                    insertContentUri,
                                    users);
                        }


                        AppSocketListener.getInstance().off(SocketEventConstants.friendlistResponse);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    public Emitter.Listener onConversationsListReceive = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jObj = (JSONObject) args[0];
                        ContentValues[] conversations = JsonUtils.getConversationContentValuesFromJson(jObj.toString());
                        Uri insertContentUri = DatabaseContract.Conversations.CONTENT_URI;
                        ContentResolver conversationsContentResolver = context.getContentResolver();
                        conversationsContentResolver.delete(
                                insertContentUri,
                                null,
                                null);
                        if (conversations != null && conversations.length != 0) {

                            conversationsContentResolver.bulkInsert(
                                    insertContentUri,
                                    conversations);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    AppSocketListener.getInstance().off(SocketEventConstants.conversationsResponse);
                }
            });
        }
    };

    public Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            String userId = "";
            String name = "";
            try {
                userId = data.getString("userId");
                name = data.getString("name");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            AppSocketListener.getInstance().off(SocketEventConstants.loginResponse);
        }
    };


    public Emitter.Listener onRegister = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            int userId = -1;
            try {
                userId = data.getInt("userId");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            AppSocketListener.getInstance().off(SocketEventConstants.registerResponse);
        }
    };


    public Emitter.Listener onMessagesListReceive = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jObj = (JSONObject) args[0];
                        ContentValues[] messages = JsonUtils.getMessagesContentValuesFromJson(jObj.toString());
                        Uri insertContentUri = com.lei.mykha.chatapp.activities.data.DatabaseContract.Messages.CONTENT_URI;

                        ContentResolver moviesContentResolver = context.getContentResolver();
                        moviesContentResolver.delete(
                                insertContentUri,
                                null,
                                null);
                        if (messages != null && messages.length != 0) {

                            moviesContentResolver.bulkInsert(
                                    insertContentUri,
                                    messages);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    AppSocketListener.getInstance().off(SocketEventConstants.queryMessagesResponse);
                }
            });
        }
    };
}
