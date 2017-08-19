package com.lei.mykha.chatapp.activities.activities;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.lei.mykha.chatapp.R;
import com.lei.mykha.chatapp.activities.data.DatabaseContract;
import com.lei.mykha.chatapp.activities.recyclerview.MessagesAdapter;
import com.lei.mykha.chatapp.activities.socketiochat.AppContext;
import com.lei.mykha.chatapp.activities.socketiochat.AppSocketListener;
import com.lei.mykha.chatapp.activities.socketioservice.SocketEventConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import io.socket.emitter.Emitter;

public class MessageActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>   {

    String TAG = "MessageActivity";

    private static final int ID_LOADER = 33;


    private String conversationId;
    private String userId;

    private RecyclerView mMessagesView;
    private EditText mInputMessageView;

    private MessagesAdapter mAdapter;

    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Bundle extras = getIntent().getExtras();

        conversationId = extras.getString("conversationId");
        userId = extras.getString("userId");

        auth = FirebaseAuth.getInstance();

        mMessagesView = (RecyclerView) findViewById(R.id.messages);
        mMessagesView.setLayoutManager(new LinearLayoutManager(this));

        Toolbar toolbar = (Toolbar) findViewById(R.id.message_toolbar);
        toolbar.setTitle(getUserName());

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        mInputMessageView = (EditText) findViewById(R.id.message_input);

        ImageButton sendButton = (ImageButton) findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend();
            }
        });

        if(!conversationId.equals("") && conversationId != null) {
            AppSocketListener.getInstance().addOnHandler(SocketEventConstants.queryMessagesResponse, AppContext.getEmitterListeners().onMessagesListReceive);
            AppSocketListener.getInstance().emit(SocketEventConstants.queryMessages, conversationId);
        }else{
            queryConversation();
        }
        mAdapter = new MessagesAdapter(auth.getCurrentUser().getUid(), new ArrayList<String>() {{
            add("uid");
            add("sender");
            add("message");
            add("timestamp");
        }});
        mMessagesView.setAdapter(mAdapter);


        getLoaderManager().initLoader(ID_LOADER, null, this);
    }
    public String getUserName(){
        Cursor cursor = getApplicationContext().getContentResolver().query(com.lei.mykha.chatapp.activities.data.DatabaseContract.Friends.CONTENT_URI,
                new String[]{com.lei.mykha.chatapp.activities.data.DatabaseContract.Friends.COLUMN_NAME,},
                com.lei.mykha.chatapp.activities.data.DatabaseContract.Friends.COLUMN_UID + " = '" + userId + "'",
                null,
                null);
        cursor.moveToPosition(0);
        return cursor.getString(0);
    }

    public void queryConversation(){
        Cursor cursor = getApplicationContext().getContentResolver().query(com.lei.mykha.chatapp.activities.data.DatabaseContract.Conversations.buildConversationSearch(userId),
                new String[]{DatabaseContract.Conversations.COLUMN_UID,},
                com.lei.mykha.chatapp.activities.data.DatabaseContract.Conversations.COLUMN_USER_ID,
                null,
                null);
        cursor.moveToPosition(0);
        if(cursor.getCount() > 0){
            conversationId = cursor.getString(0);
            getLoaderManager().restartLoader(ID_LOADER, null, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_messages, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void attemptSend() {

        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }
        mInputMessageView.setText("");

        if(!conversationId.equals("") && conversationId != null) {
            AppSocketListener.getInstance().emit(SocketEventConstants.sendMessage, conversationId, auth.getCurrentUser().getUid(), message);
        }else{
            AppSocketListener.getInstance().addOnHandler(SocketEventConstants.startConversationResponse, onConversationResponse);
            AppSocketListener.getInstance().emit(SocketEventConstants.startConversation, auth.getCurrentUser().getUid(), userId, message);
        }
    }

    private void scrollToBottom() {
        mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri insertContentUri = com.lei.mykha.chatapp.activities.data.DatabaseContract.Messages.CONTENT_URI;
        String whereClause = com.lei.mykha.chatapp.activities.data.DatabaseContract.Messages.COLUMN_CONVERSATION_ID;
        return new CursorLoader(this,
                insertContentUri,
                new String[]{
                        com.lei.mykha.chatapp.activities.data.DatabaseContract.Messages._ID,
                        com.lei.mykha.chatapp.activities.data.DatabaseContract.Messages.COLUMN_SENDER_ID,
                        com.lei.mykha.chatapp.activities.data.DatabaseContract.Messages.COLUMN_MESSAGE,
                        com.lei.mykha.chatapp.activities.data.DatabaseContract.Messages.COLUMN_CREATED_AT,
                },
                whereClause  + " = '" + conversationId + "'",
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        mAdapter.notifyDataSetChanged();
        scrollToBottom();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
    private Emitter.Listener onConversationResponse = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject data = (JSONObject) args[0];
                        conversationId = data.getString("conversationID");
                        getLoaderManager().restartLoader(ID_LOADER, null, MessageActivity.this);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    AppSocketListener.getInstance().off(SocketEventConstants.startConversationResponse);
                }
            });
        }
    };
}





