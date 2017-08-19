package com.lei.mykha.chatapp.activities.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

public class DatabaseProvider extends ContentProvider {
    String TAG = this.getClass().getSimpleName();


    static final int FRIENDLIST = 100;
    static final int FRIENDLIST_BY_TYPE = 102;
    static final int USER_NAME = 103;
    static final int CONVERSATION = 200;
    static final int CONVERSATION_BY_ID = 201;
    static final int MESSAGES = 300;
    static final int MESSAGES_BY_ID = 301;

    private static final UriMatcher URI_MATCHER = buildUriMatcher();
    private static final String FAILED_TO_INSERT_ROW_INTO = "Failed to insert row into ";
    private DatabaseDbHelper dbHelper;

    static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DatabaseContract.CONTENT_AUTHORITY;


        uriMatcher.addURI(authority, DatabaseContract.PATH_FRIENDLIST, FRIENDLIST);
        uriMatcher.addURI(authority, DatabaseContract.PATH_FRIENDLIST + "/*", FRIENDLIST_BY_TYPE);

        uriMatcher.addURI(authority, DatabaseContract.PATH_CONVERSATIONS, CONVERSATION);
        uriMatcher.addURI(authority, DatabaseContract.PATH_CONVERSATIONS + "/*", CONVERSATION_BY_ID);

        uriMatcher.addURI(authority, DatabaseContract.PATH_MESSAGES, MESSAGES);
        uriMatcher.addURI(authority, DatabaseContract.PATH_MESSAGES + "/#", MESSAGES_BY_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        int match = URI_MATCHER.match(uri);
        Cursor cursor;
        switch (match) {
            case FRIENDLIST:
                cursor = dbHelper.getReadableDatabase().query(
                        DatabaseContract.Friends.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );

                break;
            case FRIENDLIST_BY_TYPE:
                String queryBy = uri.getLastPathSegment();

                cursor = dbHelper.getReadableDatabase().query(
                        DatabaseContract.Friends.TABLE_NAME,
                        projection,
                        selection + " = " + queryBy,
                        null,
                        null,
                        null,
                        sortOrder
                );

                break;
            case USER_NAME:
                String queryUID = uri.getLastPathSegment();
                cursor = dbHelper.getReadableDatabase().query(
                        DatabaseContract.Friends.TABLE_NAME,
                        projection,
                        selection + " = '" + queryUID+"'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            case CONVERSATION:
                cursor = dbHelper.getReadableDatabase().query(
                        DatabaseContract.Conversations.TABLE_NAME + " INNER JOIN " + DatabaseContract.Friends.TABLE_NAME + " ON conversations.user_id=friends.uid",
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case CONVERSATION_BY_ID:
                String _query = uri.getLastPathSegment();
                cursor = dbHelper.getReadableDatabase().query(
                        DatabaseContract.Conversations.TABLE_NAME,
                        projection,
                        selection + " = " + _query,
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            case MESSAGES:
                cursor = dbHelper.getReadableDatabase().query(
                        DatabaseContract.Messages.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );

                break;
            default:
                return null;
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }



    @Override
    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = URI_MATCHER.match(uri);
        Uri returnUri;
        long id;
        switch (match) {
            case FRIENDLIST:
                id = db.insertWithOnConflict(DatabaseContract.Friends.TABLE_NAME, null,
                        values, SQLiteDatabase.CONFLICT_REPLACE);
                if (id > 0) {
                    returnUri = DatabaseContract.Friends.CONTENT_URI;
                } else {
                    throw new android.database.SQLException(FAILED_TO_INSERT_ROW_INTO + uri);
                }
                break;
            case CONVERSATION:
                id = db.insertWithOnConflict(DatabaseContract.Conversations.TABLE_NAME, null,
                        values, SQLiteDatabase.CONFLICT_REPLACE);
                if (id > 0) {
                    returnUri = DatabaseContract.Conversations.CONTENT_URI;
                } else {
                    throw new android.database.SQLException(FAILED_TO_INSERT_ROW_INTO + uri);
                }
                break;
            case MESSAGES:
                id = db.insertWithOnConflict(DatabaseContract.Messages.TABLE_NAME, null,
                        values, SQLiteDatabase.CONFLICT_REPLACE);
                if (id > 0) {
                    returnUri = DatabaseContract.Conversations.CONTENT_URI;
                } else {
                    throw new android.database.SQLException(FAILED_TO_INSERT_ROW_INTO + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = URI_MATCHER.match(uri);
        int rowsUpdated = 0;
        switch (match) {
            case FRIENDLIST:
                rowsUpdated = db.update(DatabaseContract.Friends.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = URI_MATCHER.match(uri);
        int rowsDeleted;
        switch (match) {
            case FRIENDLIST:
                rowsDeleted = db.delete(DatabaseContract.Friends.TABLE_NAME, selection, selectionArgs);
                break;
            case CONVERSATION:
                rowsDeleted = db.delete(DatabaseContract.Conversations.TABLE_NAME, selection, selectionArgs);
                break;
            case MESSAGES:
                rowsDeleted = db.delete(DatabaseContract.Messages.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public void shutdown() {
        dbHelper.close();
        super.shutdown();
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = URI_MATCHER.match(uri);

        int returnCount = 0;
        switch (match) {
            case FRIENDLIST:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long id = db.insertWithOnConflict(DatabaseContract.Friends.TABLE_NAME,
                                null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            case CONVERSATION:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long id = db.insertWithOnConflict(DatabaseContract.Conversations.TABLE_NAME,
                                null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            case MESSAGES:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long id = db.insertWithOnConflict(DatabaseContract.Messages.TABLE_NAME,
                                null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            default:
                return super.bulkInsert(uri, values);
        }
        return returnCount;
    }
}
