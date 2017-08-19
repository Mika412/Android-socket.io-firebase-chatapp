package com.lei.mykha.chatapp.activities.utilities;

import android.content.ContentValues;
import android.text.format.DateFormat;

import com.lei.mykha.chatapp.activities.data.DatabaseContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Contains JSON based functions.
 */

public class JsonUtils {

    /** Converts a string with json data to json objects, and converts the data to an array of Movie objects. */
    public static ContentValues[] getUserContentValuesFromJson(String jsonString) throws JSONException {
        JSONObject jObject = new JSONObject(jsonString);
        JSONArray jFriends = jObject.getJSONArray("friends");
        JSONArray jPending = jObject.getJSONArray("pending");
        ContentValues[] userContentValues = new ContentValues[jFriends.length() + jPending.length()];
        for (int i = 0; i < jFriends.length(); i++) {
            JSONObject jObj = jFriends.getJSONObject(i);
            String id = jObj.getString("uid");
            String name = jObj.getString("name");
            ContentValues userValues = new ContentValues();
            userValues.put(com.lei.mykha.chatapp.activities.data.DatabaseContract.Friends.COLUMN_UID, id);
            userValues.put(DatabaseContract.Friends.COLUMN_NAME, name);
            userValues.put(com.lei.mykha.chatapp.activities.data.DatabaseContract.Friends.COLUMN_TYPE, "friend");

            userContentValues[i] = userValues ;
        }

        for (int i = 0; i < jPending.length(); i++) {
            JSONObject jObj = jPending.getJSONObject(i);
            String id = jObj.getString("uid");
            String name = jObj.getString("name");
            ContentValues userValues = new ContentValues();
            userValues.put(com.lei.mykha.chatapp.activities.data.DatabaseContract.Friends.COLUMN_UID, id);
            userValues.put(com.lei.mykha.chatapp.activities.data.DatabaseContract.Friends.COLUMN_NAME, name);
            userValues.put(com.lei.mykha.chatapp.activities.data.DatabaseContract.Friends.COLUMN_TYPE, "pending");

            userContentValues[i + jFriends.length()] = userValues ;
        }
        return userContentValues;
    }

    public static ContentValues[] getConversationContentValuesFromJson(String jsonString) throws JSONException {
        JSONObject jObject = new JSONObject(jsonString);
        JSONArray jArray = jObject.getJSONArray("conversations");
        ContentValues[] conversationsContentValues = new ContentValues[jArray.length()];
        for (int i = 0; i < jArray.length(); i++) {
            JSONObject jObj = jArray.getJSONObject(i);
            String id = jObj.getString("uid");
            String user = jObj.getString("user");;

            ContentValues conversationValues = new ContentValues();
            conversationValues.put(com.lei.mykha.chatapp.activities.data.DatabaseContract.Conversations.COLUMN_UID, id);
            conversationValues.put(com.lei.mykha.chatapp.activities.data.DatabaseContract.Conversations.COLUMN_USER_ID, user);

            conversationsContentValues[i] = conversationValues ;
        }
        return conversationsContentValues;
    }

    public static ContentValues[] getMessagesContentValuesFromJson(String jsonString) throws JSONException {
        JSONObject jObject = new JSONObject(jsonString);
        JSONArray jArray = jObject.getJSONArray("messages");
        ContentValues[] conversationsContentValues = new ContentValues[jArray.length()];
        for (int i = 0; i < jArray.length(); i++) {
            JSONObject jObj = jArray.getJSONObject(i);
            String conversationUID = jObj.getString("conversationUID");
            String message_id = jObj.getString("messageUID");
            String user = jObj.getString("user");
            String message = jObj.getString("message");;
            String created_at = jObj.getString("created_at");;

            ContentValues conversationValues = new ContentValues();
            conversationValues.put(com.lei.mykha.chatapp.activities.data.DatabaseContract.Messages.COLUMN_MESSAGE_ID, message_id);
            conversationValues.put(com.lei.mykha.chatapp.activities.data.DatabaseContract.Messages.COLUMN_CONVERSATION_ID, conversationUID);
            conversationValues.put(com.lei.mykha.chatapp.activities.data.DatabaseContract.Messages.COLUMN_SENDER_ID, user);
            conversationValues.put(com.lei.mykha.chatapp.activities.data.DatabaseContract.Messages.COLUMN_MESSAGE, message);
            conversationValues.put(com.lei.mykha.chatapp.activities.data.DatabaseContract.Messages.COLUMN_CREATED_AT, created_at);

            conversationsContentValues[i] = conversationValues ;
        }
        return conversationsContentValues;
    }


    public static String getFormattedDate(String time) {
        Long smsTimeInMilis = Long.parseLong(time);

        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(smsTimeInMilis);

        Calendar now = Calendar.getInstance();

        final String timeFormatString = "h:mm aa";
        final String dateTimeFormatString = "EEEE, MMMM d, h:mm aa";
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE) ) {
            return "Today " + DateFormat.format(timeFormatString, smsTime);
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1  ){
            return "Yesterday " + DateFormat.format(timeFormatString, smsTime);
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            return DateFormat.format(dateTimeFormatString, smsTime).toString();
        } else {
            return DateFormat.format("MMMM dd yyyy, h:mm aa", smsTime).toString();
        }
    }
}
