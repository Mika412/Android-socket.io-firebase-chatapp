package com.lei.mykha.chatapp.activities.fragments;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.lei.mykha.chatapp.R;
import com.lei.mykha.chatapp.activities.data.DatabaseContract;
import com.lei.mykha.chatapp.activities.recyclerview.FriendsAdapter;
import com.lei.mykha.chatapp.activities.socketiochat.AppContext;
import com.lei.mykha.chatapp.activities.socketiochat.AppSocketListener;
import com.lei.mykha.chatapp.activities.socketioservice.SocketEventConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

import io.socket.emitter.Emitter;

/**
 * Created by mykha on 8/15/2017.
 */

public class FriendsFragment extends Fragment implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor>  {
    String TAG = "FriendsFragment";

    public static final String[] MAIN_PROJECTION = {
            com.lei.mykha.chatapp.activities.data.DatabaseContract.Friends._ID,
            com.lei.mykha.chatapp.activities.data.DatabaseContract.Friends.COLUMN_UID,
            com.lei.mykha.chatapp.activities.data.DatabaseContract.Friends.COLUMN_NAME,
            DatabaseContract.Friends.COLUMN_TYPE,
    };


    private static final int ID_LOADER_FRIENDS = 44;
    private static final int ID_LOADER_PENDING = 45;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RecyclerView pendingRecyclerView;
    private RecyclerView friendsRecyclerView;
    private RecyclerView searchFriendsView;

    private TextView pendingTitle;
    private TextView friendsTitle;

    FriendsAdapter pendingAdapter;
    FriendsAdapter friendsAdapter;

    FriendsAdapter searchFriendsAdapter;


    ProgressBar progressBar;
    SearchView searchView;

    TextView friendlistEmptyText;
    TextView searchEmptyText;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        progressBar = view.findViewById(R.id.pb_friendsList);
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        searchView = view.findViewById(R.id.search_people);


        progressBar.setVisibility(View.VISIBLE);

        pendingTitle = view.findViewById(R.id.pendingHeader);
        friendsTitle = view.findViewById(R.id.friendsHeader);

        searchFriendsView = view.findViewById(R.id.rv_searchlist);
        pendingRecyclerView = view.findViewById(R.id.rv_pendinglist);
        friendsRecyclerView = view.findViewById(R.id.rv_friendlist);

        friendlistEmptyText = view.findViewById(R.id.tx_no_friends);
        searchEmptyText = view.findViewById(R.id.tx_no_search);

        pendingAdapter = new FriendsAdapter(new ArrayList<String>() {{
            add("id");
            add("uid");
            add("name");
            add("type");
        }});
        friendsAdapter = new FriendsAdapter(new ArrayList<String>() {{
            add("id");
            add("uid");
            add("name");
            add("type");
        }});
        searchFriendsAdapter = new FriendsAdapter(new ArrayList<String>() {{
            add("uid");
            add("name");
            add("type");
        }});

        pendingRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        searchFriendsView.setLayoutManager(new LinearLayoutManager(getActivity()));

        pendingRecyclerView.setAdapter(pendingAdapter);
        friendsRecyclerView.setAdapter(friendsAdapter);
        searchFriendsView.setAdapter(searchFriendsAdapter);


        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                AppSocketListener.getInstance().addOnHandler(SocketEventConstants.friendlistResponse, AppContext.getEmitterListeners().onFriendListReceive);
                AppSocketListener.getInstance().addOnHandler(SocketEventConstants.friendlistResponse, onRefreshed);
                AppSocketListener.getInstance().emit(SocketEventConstants.friendlist, FirebaseAuth.getInstance().getCurrentUser().getUid());
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){

            @Override
            public boolean onQueryTextSubmit(String query) {
                onQueryTextChange(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchFriendsAdapter.swapCursor(null);
                if(!newText.equals("")){
                    AppSocketListener.getInstance().addOnHandler(SocketEventConstants.searchPersonResponse, onPersonResult);
                    AppSocketListener.getInstance().emit(SocketEventConstants.searchPerson, FirebaseAuth.getInstance().getCurrentUser().getUid(), newText);
                    showSearchResult();
                }else{
                    showFriends();
                }
                return false;
            }
        });

        getLoaderManager().initLoader(ID_LOADER_PENDING, null, this);

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String whereClause = com.lei.mykha.chatapp.activities.data.DatabaseContract.Friends.COLUMN_TYPE;
        switch (id) {
            case ID_LOADER_PENDING:
                Uri pendingUri = com.lei.mykha.chatapp.activities.data.DatabaseContract.Friends.CONTENT_URI;
                return new CursorLoader(getActivity(),
                        pendingUri,
                        MAIN_PROJECTION,
                        whereClause  + " = 'pending'",
                        null,
                        "name ASC");
            case ID_LOADER_FRIENDS:
                Uri friendsUri = com.lei.mykha.chatapp.activities.data.DatabaseContract.Friends.CONTENT_URI;
                return new CursorLoader(getActivity(),
                        friendsUri,
                        MAIN_PROJECTION,
                        whereClause  + " = 'friend'",
                        null,
                        "name ASC");
        }
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case ID_LOADER_PENDING:
                getLoaderManager().restartLoader(ID_LOADER_FRIENDS, null, this);
                pendingAdapter.swapCursor(data);
                break;
            case ID_LOADER_FRIENDS:
                friendsAdapter.swapCursor(data);
                showFriends();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void showFriends(){
        progressBar.setVisibility(View.INVISIBLE);
        if(pendingAdapter.getItemCount() > 0) {
            pendingRecyclerView.setVisibility(View.VISIBLE);
            pendingTitle.setVisibility(View.VISIBLE);
        }else{
            pendingRecyclerView.setVisibility(View.GONE);
            pendingTitle.setVisibility(View.GONE);
        }

        if(friendsAdapter.getItemCount() > 0) {
            friendsRecyclerView.setVisibility(View.VISIBLE);
            friendsTitle.setVisibility(View.VISIBLE);
        }else{
            friendsRecyclerView.setVisibility(View.GONE);
            friendsTitle.setVisibility(View.GONE);
        }

        searchEmptyText.setVisibility(View.GONE);

        if(pendingAdapter.getItemCount() > 0 || friendsAdapter.getItemCount() > 0)
            friendlistEmptyText.setVisibility(View.GONE);
        else
            friendlistEmptyText.setVisibility(View.VISIBLE);

        searchFriendsView.setVisibility(View.GONE);
    }

    public void showSearchResult(){
        searchFriendsView.setVisibility(View.VISIBLE);
        friendlistEmptyText.setVisibility(View.GONE);

        pendingRecyclerView.setVisibility(View.GONE);
        friendsRecyclerView.setVisibility(View.GONE);
        friendsTitle.setVisibility(View.GONE);
        pendingTitle.setVisibility(View.GONE);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private Emitter.Listener onRefreshed = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getLoaderManager().restartLoader(ID_LOADER_PENDING, null, FriendsFragment.this);
                    mSwipeRefreshLayout.setRefreshing(false);
                    AppSocketListener.getInstance().off(SocketEventConstants.friendlistResponse);
                }
            });
        }
    };

    private Emitter.Listener onPersonResult = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            // here you check the value of getActivity() and break up if needed
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    String jsonString = args[0].toString();
                    String[] columns = new String[] { "uid", "name", "type" };
                    MatrixCursor matrixCursor= new MatrixCursor(columns);

                    try {
                        JSONObject jObject = new JSONObject(jsonString);
                        JSONArray jFriends = jObject.getJSONArray("userList");
                        for (int i = 0; i < jFriends.length(); i++) {
                            JSONObject jObj = jFriends.getJSONObject(i);
                            String id = jObj.getString("uid");
                            String name = jObj.getString("name");
                            String type = jObj.getString("type");
                            matrixCursor.addRow(new Object[] { id, name, type});
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    searchFriendsAdapter.swapCursor(matrixCursor);

                    if(searchFriendsAdapter.getItemCount() > 0)
                        searchEmptyText.setVisibility(View.GONE);
                    else
                        searchEmptyText.setVisibility(View.VISIBLE);

                }
            });
        }
    };
}
