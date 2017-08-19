package com.lei.mykha.chatapp.activities.services;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.lei.mykha.chatapp.activities.socketiochat.AppSocketListener;
import com.lei.mykha.chatapp.activities.socketioservice.SocketEventConstants;

/**
 * Created by mykha on 8/12/2017.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "MyFirebaseIDService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG, "Refreshed token: " + refreshedToken);

        sendRegistrationToServer(refreshedToken);
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        if(FirebaseAuth.getInstance().getCurrentUser() != null)
            AppSocketListener.getInstance().emit(SocketEventConstants.updateToken, FirebaseAuth.getInstance().getCurrentUser().getUid(), token);
    }
}