package com.nsqre.insquare.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.nsqre.insquare.User.InSquareProfile;

/**
 * Runs at the opening of the app. It checks if the user is already logged in to show the proper activity
 */
public class SplashActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks
{
    private static final String TAG = "SplashActivity";
    private Intent nextScreen;
    private GoogleApiClient gApiClient;
    private OptionalPendingResult<GoogleSignInResult> googleSignedIn;
    private boolean isFacebookInit = false;
    private boolean isGoogleConnected = false;
    private Thread facebookLoginThread;
    private Thread googleLoginThread;
    private OptionalPendingResult<GoogleSignInResult> opr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InSquareProfile.getInstance(getApplicationContext());

        initGoogle();

        FacebookSdk.sdkInitialize(getApplicationContext(),
                new FacebookSdk.InitializeCallback() {
                    @Override
                    public void onInitialized() {
                        facebookLoginThread = new Thread()
                        {
                            @Override
                            public void run() {
                                try {
                                    sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                boolean google = opr.isDone();

                                if((google || isFacebookSignedIn()) && InSquareProfile.hasLoginData())
                                {
                                    nextScreen = new Intent(SplashActivity.this, BottomNavActivity.class);
                                }else
                                {
                                    nextScreen = new Intent(SplashActivity.this, LoginActivity.class);
                                }

                                startActivity(nextScreen);
                            }
                        };
                        facebookLoginThread.start();
                    }
                });
    }

    private void initGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        gApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        gApiClient.connect();
        opr = Auth.GoogleSignInApi.silentSignIn(gApiClient);
    }

    private boolean isFacebookSignedIn() {
        AccessToken at = AccessToken.getCurrentAccessToken();
        boolean loggedIn = at != null;
        return loggedIn;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: the connection with Google failed!");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
