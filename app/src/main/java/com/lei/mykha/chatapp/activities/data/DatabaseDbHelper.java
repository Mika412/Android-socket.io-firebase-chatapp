package com.lei.mykha.chatapp.activities.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Manages a local database for movies.
 */
public class DatabaseDbHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "leiapp.db";
    private static final int DATABASE_SCHEMA_VERSION = 1;
    private static final String SQL_DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";

    public DatabaseDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_SCHEMA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_USERS_TABLE =
                "CREATE TABLE " + DatabaseContract.Friends.TABLE_NAME + " (" +
                        DatabaseContract.Friends._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        DatabaseContract.Friends.COLUMN_UID + " TEXT, " +
                        DatabaseContract.Friends.COLUMN_NAME + " TEXT, " +
                        DatabaseContract.Friends.COLUMN_TYPE + " TEXT" +
                        " );";

        final String SQL_CREATE_CONVERSATIONS_TABLE =
                "CREATE TABLE " + DatabaseContract.Conversations.TABLE_NAME + " (" +
                        DatabaseContract.Conversations._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        DatabaseContract.Conversations.COLUMN_UID + " TEXT, " +
                        DatabaseContract.Conversations.COLUMN_USER_ID + " TEXT " +
                        " );";

        final String SQL_CREATE_MESSAGES_TABLE =
                "CREATE TABLE " + DatabaseContract.Messages.TABLE_NAME + " (" +
                        DatabaseContract.Messages._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        DatabaseContract.Messages.COLUMN_MESSAGE_ID + " TEXT, " +
                        DatabaseContract.Messages.COLUMN_CONVERSATION_ID + " TEXT, " +
                        DatabaseContract.Messages.COLUMN_SENDER_ID + " TEXT, " +
                        DatabaseContract.Messages.COLUMN_MESSAGE + " STRING, " +
                        DatabaseContract.Messages.COLUMN_CREATED_AT + " STRING" +
                        " );";
        db.execSQL(SQL_CREATE_USERS_TABLE);
        db.execSQL(SQL_CREATE_CONVERSATIONS_TABLE);
        db.execSQL(SQL_CREATE_MESSAGES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_TABLE_IF_EXISTS + com.lei.mykha.chatapp.activities.data.DatabaseContract.Friends.TABLE_NAME);
        db.execSQL(SQL_DROP_TABLE_IF_EXISTS + com.lei.mykha.chatapp.activities.data.DatabaseContract.Conversations.TABLE_NAME);
        db.execSQL(SQL_DROP_TABLE_IF_EXISTS + com.lei.mykha.chatapp.activities.data.DatabaseContract.Messages.TABLE_NAME);
        onCreate(db);
    }

    public void resetDatabase() {
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL(SQL_DROP_TABLE_IF_EXISTS + com.lei.mykha.chatapp.activities.data.DatabaseContract.Friends.TABLE_NAME);
        db.execSQL(SQL_DROP_TABLE_IF_EXISTS + com.lei.mykha.chatapp.activities.data.DatabaseContract.Conversations.TABLE_NAME);
        db.execSQL(SQL_DROP_TABLE_IF_EXISTS + com.lei.mykha.chatapp.activities.data.DatabaseContract.Messages.TABLE_NAME);
        onCreate(db);
        db.close();
    }
}
