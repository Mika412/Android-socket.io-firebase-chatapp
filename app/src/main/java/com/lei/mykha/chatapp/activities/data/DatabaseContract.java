package com.lei.mykha.chatapp.activities.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the movies database.
 */
public final class DatabaseContract {

    static final String CONTENT_AUTHORITY = "com.lei.mykha.chatapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    static final String PATH_FRIENDLIST = "friends";
    static final String PATH_CONVERSATIONS = "conversations";
    static final String PATH_MESSAGES = "messages";

    public static final class Friends implements BaseColumns {

        public static final String COLUMN_UID = "uid";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_TYPE = "type";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FRIENDLIST).build();

        public static final String TABLE_NAME = "friends";

    }


    public static final class Conversations implements BaseColumns {

        public static final String COLUMN_UID = "uid";
        public static final String COLUMN_USER_ID = "user_id";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CONVERSATIONS).build();
        public static final String TABLE_NAME = "conversations";

        public static Uri buildConversationSearch(String uid) {
            return CONTENT_URI.buildUpon().appendPath("'" + uid + "'").build();
        }
    }

    public static final class Messages implements BaseColumns {

        public static final String COLUMN_MESSAGE_ID = "message_id";
        public static final String COLUMN_CONVERSATION_ID = "conversation_id";
        public static final String COLUMN_SENDER_ID = "user1_sender_id";
        public static final String COLUMN_MESSAGE = "message";
        public static final String COLUMN_CREATED_AT = "last_updated";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MESSAGES).build();
        public static final String TABLE_NAME = "messages";

    }

}
