package com.lei.mykha.chatapp.activities.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.lei.mykha.chatapp.R;
import com.lei.mykha.chatapp.activities.activities.MessageActivity;
import com.lei.mykha.chatapp.activities.data.DatabaseContract;
import com.lei.mykha.chatapp.activities.recyclerview.ConversationAdapter;
import com.lei.mykha.chatapp.activities.socketiochat.AppContext;
import com.lei.mykha.chatapp.activities.socketiochat.AppSocketListener;
import com.lei.mykha.chatapp.activities.socketioservice.SocketEventConstants;

import java.util.ArrayList;

import io.socket.emitter.Emitter;

/**
 * Created by mykha on 8/15/2017.
 */

public class ConversationsFragment extends Fragment implements ConversationAdapter.ListItemClickListener, LoaderManager.LoaderCallbacks<Cursor>   {

    private static final String TAG = "ConversationsFragment";

    private static final int ID_LOADER = 11;

    ProgressBar progressBar;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private CoordinatorLayout coordinatorLayout;

    ConversationAdapter mAdapter;

    private FirebaseAuth auth;

    TextView conversationsEmptyText;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversations, container, false);


        auth = FirebaseAuth.getInstance();

        coordinatorLayout = view.findViewById(R.id
                .coordinatorLayout);

        progressBar = view.findViewById(R.id.pb_conversationList);
        progressBar.setVisibility(View.VISIBLE);

        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                doUpdates();
            }
        });

        recyclerView = view.findViewById(R.id.rv_conversations);

        conversationsEmptyText = view.findViewById(R.id.tx_no_conversations);

        mAdapter = new ConversationAdapter(new ArrayList<String>() {{
            add("uid");
            add("user");
            add("name");
        }}, this);

        recyclerView.setAdapter(mAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        getLoaderManager().initLoader(ID_LOADER, null, this);

        return view;
    }


    void doUpdates(){
        AppSocketListener.getInstance().addOnHandler(SocketEventConstants.conversationsResponse, AppContext.getEmitterListeners().onConversationsListReceive);
        AppSocketListener.getInstance().addOnHandler(SocketEventConstants.conversationsResponse, onRefreshed);
        AppSocketListener.getInstance().emit(SocketEventConstants.conversations, auth.getCurrentUser().getUid());
    }

    public void showPosters(){
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        conversationsEmptyText.setVisibility(View.GONE);
    }
    public void showNone(){
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        conversationsEmptyText.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(String clickedConversation, String userId) {
        Intent conversationIntent = new Intent(getActivity(), MessageActivity.class);
        conversationIntent.putExtra("conversationId", clickedConversation);
        conversationIntent.putExtra("userId", userId);
        startActivity(conversationIntent);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri insertContentUri = com.lei.mykha.chatapp.activities.data.DatabaseContract.Conversations.CONTENT_URI;
        return new CursorLoader(getActivity(),
                insertContentUri,
                new String[]{
                        DatabaseContract.Conversations.TABLE_NAME + "." + DatabaseContract.Conversations.COLUMN_UID,
                        DatabaseContract.Conversations.COLUMN_USER_ID,
                        DatabaseContract.Friends.COLUMN_NAME,
                },
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        if (data != null && data.getCount() != 0)
            showPosters();
        else
            showNone();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private Emitter.Listener onRefreshed = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getLoaderManager().restartLoader(ID_LOADER, null, ConversationsFragment.this);
                    mSwipeRefreshLayout.setRefreshing(false);

                    //AppSocketListener.getInstance().off(SocketEventConstants.conversationsResponse);
                }
            });
        }
    };
}
