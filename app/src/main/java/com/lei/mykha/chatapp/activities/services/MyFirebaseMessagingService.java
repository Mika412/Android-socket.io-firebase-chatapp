package com.lei.mykha.chatapp.activities.services;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.lei.mykha.chatapp.activities.activities.MainTabbedActivity;
import com.lei.mykha.chatapp.activities.activities.MessageActivity;
import com.lei.mykha.chatapp.activities.data.DatabaseContract;
import com.lei.mykha.chatapp.activities.utilities.NotificationUtils;

import org.json.JSONObject;

/**
 * Created by mykha on 8/12/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingService";
    private NotificationUtils notificationUtils;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0 && FirebaseAuth.getInstance().getCurrentUser() != null) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());
            try {
                JSONObject json = new JSONObject(remoteMessage.getData());

                String type = json.getString("type");
                switch(type){
                    case "message":
                        handleDataMessage(json);
                        break;
                    case "newconversation":
                        handleNewConversation(json);
                        break;
                    case "friendaccept":
                        handleFriendAccept(json);
                        break;
                    case "friendrequest":
                        handleFriendRequest(json);
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void handleDataMessage(JSONObject json) {
        try {
            String message_id = json.getString("messageUID");
            String conversationId = json.getString("conversationUID");
            String user = json.getString("user");
            String name = "";
            if(!user.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
               name = json.getString("name");
            String message = json.getString("message");
            String created_at = json.getString("created_at");

            ContentValues conversationValues = new ContentValues();
            conversationValues.put(com.lei.mykha.chatapp.activities.data.DatabaseContract.Messages.COLUMN_MESSAGE_ID, message_id);
            conversationValues.put(com.lei.mykha.chatapp.activities.data.DatabaseContract.Messages.COLUMN_CONVERSATION_ID, conversationId);
            conversationValues.put(com.lei.mykha.chatapp.activities.data.DatabaseContract.Messages.COLUMN_SENDER_ID, user);
            conversationValues.put(com.lei.mykha.chatapp.activities.data.DatabaseContract.Messages.COLUMN_MESSAGE, message);
            conversationValues.put(com.lei.mykha.chatapp.activities.data.DatabaseContract.Messages.COLUMN_CREATED_AT, created_at);

            Uri insertContentUri = com.lei.mykha.chatapp.activities.data.DatabaseContract.Messages.CONTENT_URI;
            if (conversationValues != null) {
                ContentResolver contentResolver = getApplicationContext().getContentResolver();
                contentResolver.insert(
                        insertContentUri,
                        conversationValues);

                if(!user.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                        // app is in foreground, broadcast the push message
                        Intent pushNotification = new Intent(NotificationUtils.PUSH_NOTIFICATION);
                        pushNotification.putExtra("message", message);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                        // play notification sound
                        NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
                        notificationUtils.playNotificationSound();
                    } else {
                        // app is in background, show the notification in notification tray
                        Intent resultIntent = new Intent(getApplicationContext(), MessageActivity.class);
                        resultIntent.putExtra("conversationId", conversationId);
                        resultIntent.putExtra("userId", user);

                        showNotificationMessage(getApplicationContext(), "Message from " + name, message, created_at, resultIntent);
                    }
                }
            }
        }catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    private void handleNewConversation(JSONObject json) {
        try {
                String conversationId = json.getString("conversationUID");
                String user = json.getString("user");

                ContentValues conversationValues = new ContentValues();
                conversationValues.put(com.lei.mykha.chatapp.activities.data.DatabaseContract.Conversations.COLUMN_UID, conversationId);
                conversationValues.put(com.lei.mykha.chatapp.activities.data.DatabaseContract.Conversations.COLUMN_USER_ID, user);
                Uri insertContentUri = com.lei.mykha.chatapp.activities.data.DatabaseContract.Conversations.CONTENT_URI;
                if (conversationValues != null) {
                    ContentResolver contentResolver = getApplicationContext().getContentResolver();
                    contentResolver.insert(
                            insertContentUri,
                            conversationValues);
                }
                handleDataMessage(json);
        }catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }


    private void handleFriendRequest(JSONObject json) {
        try {
            String by = json.getString("by");
            if(by.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                return;
            String to = json.getString("to");
            String name = json.getString("name");

            ContentValues conversationValues = new ContentValues();
            conversationValues.put(com.lei.mykha.chatapp.activities.data.DatabaseContract.Friends.COLUMN_UID, by);
            conversationValues.put(com.lei.mykha.chatapp.activities.data.DatabaseContract.Friends.COLUMN_NAME, name);
            conversationValues.put(com.lei.mykha.chatapp.activities.data.DatabaseContract.Friends.COLUMN_TYPE, "pending");

            Uri insertContentUri = com.lei.mykha.chatapp.activities.data.DatabaseContract.Friends.CONTENT_URI;
            if (conversationValues != null) {
                ContentResolver contentResolver = getApplicationContext().getContentResolver();
                contentResolver.insert(
                        insertContentUri,
                        conversationValues);

                if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                    // play notification sound
                    NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
                    notificationUtils.playNotificationSound();
                } else {
                    // app is in background, show the notification in notification tray
                    Intent resultIntent = new Intent(getApplicationContext(), MainTabbedActivity.class);
                    showNotificationMessage(getApplicationContext(), "Friend request", name + " sent a friend request.", "", resultIntent);
                }
            }
        }catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    private void handleFriendAccept(JSONObject json) {
        Log.e(TAG, json.toString());
        try {
            String by = json.getString("by");
            String to = json.getString("to");
            String name = "";
            ContentValues userValues = new ContentValues();
            if(!by.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                name = json.getString("name");
                userValues.put(DatabaseContract.Friends.COLUMN_NAME, name);
                userValues.put(DatabaseContract.Friends.COLUMN_UID, by);
            }else{
                userValues.put(DatabaseContract.Friends.COLUMN_UID, to);
            }
            userValues.put(DatabaseContract.Friends.COLUMN_TYPE, "friend");

            Uri insertContentUri = DatabaseContract.Friends.CONTENT_URI;
            if (userValues != null) {
                ContentResolver contentResolver = getApplicationContext().getContentResolver();
                if(by.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    contentResolver.update(
                            insertContentUri,
                            userValues,
                            "uid=?",
                            new String[]{to});
                }else {
                    contentResolver.insert(
                            insertContentUri,
                            userValues);

                    if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                        // play notification sound
                        NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
                        notificationUtils.playNotificationSound();
                    } else {
                        // app is in background, show the notification in notification tray
                        Intent resultIntent = new Intent(getApplicationContext(), MainTabbedActivity.class);
                        showNotificationMessage(getApplicationContext(), "Friend accept", name + " accepted your friend request.", "", resultIntent);
                    }
                }
            }
        }catch (Exception e) {
            Log.e(TAG, "Exceptiont: " + e.getMessage());
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
        notificationUtils.playNotificationSound();
    }

}