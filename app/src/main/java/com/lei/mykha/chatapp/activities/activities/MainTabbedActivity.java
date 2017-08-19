package com.lei.mykha.chatapp.activities.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.lei.mykha.chatapp.R;
import com.lei.mykha.chatapp.activities.data.DatabaseDbHelper;
import com.lei.mykha.chatapp.activities.fragments.ConversationsFragment;
import com.lei.mykha.chatapp.activities.fragments.FriendsFragment;
import com.lei.mykha.chatapp.activities.fragments.SectionPageAdapter;
import com.lei.mykha.chatapp.activities.socketiochat.AppContext;
import com.lei.mykha.chatapp.activities.socketiochat.AppSocketListener;
import com.lei.mykha.chatapp.activities.socketioservice.SocketEventConstants;
import com.lei.mykha.chatapp.activities.utilities.JsonUtils;

import java.util.Calendar;

public class MainTabbedActivity extends AppCompatActivity {

    private static final String TAG = "MainTabbedActivity";

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tabbed);
        Log.d(TAG, "onCreate: Starting.");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        toolbar.setTitle(getResources().getString(R.string.MainTitle));
        setSupportActionBar(toolbar);
        //Update firebase token for FCM
        AppSocketListener.getInstance().emit(SocketEventConstants.updateToken, FirebaseAuth.getInstance().getCurrentUser().getUid(), FirebaseInstanceId.getInstance().getToken());

        AppSocketListener.getInstance().addOnHandler(SocketEventConstants.friendlistResponse, AppContext.getEmitterListeners().onFriendListReceive);
        AppSocketListener.getInstance().emit(SocketEventConstants.friendlist, FirebaseAuth.getInstance().getCurrentUser().getUid());

        //AppSocketListener.getInstance().addOnHandler(SocketEventConstants.conversationsResponce, onRefreshed);
        AppSocketListener.getInstance().addOnHandler(SocketEventConstants.conversationsResponse, AppContext.getEmitterListeners().onConversationsListReceive);
        AppSocketListener.getInstance().emit(SocketEventConstants.conversations, FirebaseAuth.getInstance().getCurrentUser().getUid());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_logout:
                DatabaseDbHelper databaseDbHelper = new DatabaseDbHelper(getApplication());
                databaseDbHelper.resetDatabase();

                //Update firebase token for FCM
                AppSocketListener.getInstance().emit(SocketEventConstants.deleteToken, FirebaseAuth.getInstance().getCurrentUser().getUid(), FirebaseInstanceId.getInstance().getToken());

                FirebaseAuth.getInstance().signOut();

                Intent i = new Intent(MainTabbedActivity.this,
                        LoginActivity.class);
                startActivity(i);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionPageAdapter adapter = new SectionPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new ConversationsFragment(), getResources().getString(R.string.ConversationsFragmentTitle));
        adapter.addFragment(new FriendsFragment(), getResources().getString(R.string.FriendsFragmentTitle));
        viewPager.setAdapter(adapter);
    }


}
