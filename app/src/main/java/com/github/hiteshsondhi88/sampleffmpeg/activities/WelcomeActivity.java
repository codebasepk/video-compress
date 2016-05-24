package com.github.hiteshsondhi88.sampleffmpeg.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.github.hiteshsondhi88.sampleffmpeg.R;
import com.github.hiteshsondhi88.sampleffmpeg.utils.Helpers;
import com.github.hiteshsondhi88.sampleffmpeg.utils.WebServiceHelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Arrays;


public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {

    private Button loginButton;
    private TextView singUp;
    private TextView info;
    private LoginButton mLoginButton;
    private CallbackManager callbackManager;
    private String getToken;
    private AccessTokenTracker accessTokenTracker;
    private AccessToken accessToken;
    private ProfileTracker profileTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_welcome);
        info = (TextView) findViewById(R.id.info);
        mLoginButton = (LoginButton) findViewById(R.id.login_button);
        mLoginButton.setReadPermissions("user_friends, email, public_profile");
        loginButton = (Button) findViewById(R.id.button_login);
        singUp = (TextView) findViewById(R.id.signup_text);
        loginButton.setOnClickListener(this);
        singUp.setOnClickListener(this);

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                System.out.println(currentProfile);

            }
        };

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken, AccessToken currentAccessToken) {
                System.out.println(currentAccessToken);

            }
        };

        accessToken = AccessToken.getCurrentAccessToken();
        System.out.println(accessToken);

        mLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String userId = loginResult.getAccessToken().getUserId();
                getToken = loginResult.getAccessToken().getToken();
                new FbLoignTask(getToken).execute();

            }

            @Override
            public void onCancel() {
                if (info != null) {
                    info.setText("Login attempt canceled.");
                }

            }

            @Override
            public void onError(FacebookException error) {
                if (info != null) {
                    info.setText("Login attempt failed.");
                }

            }
        });

        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_login:
                System.out.println("login Button");
                startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
                break;
            case R.id.signup_text:
                System.out.println("sign up Text");
                startActivity(new Intent(WelcomeActivity.this, RegisterActivity.class));
                break;
        }
    }

    private class FbLoignTask extends AsyncTask<Void, Void, String> {

        private boolean internetAvailable = false;

        public FbLoignTask(String token) {
            super();
            getToken = token;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            WebServiceHelper.showProgressDialog(WelcomeActivity.this, "LoggingIn");
        }

        @Override
        protected String doInBackground(Void... voids) {
            String output = "";
            if (WebServiceHelper.isNetworkAvailable() && WebServiceHelper.isInternetWorking()) {
                internetAvailable = true;
                String data = WebServiceHelper.getFbLoginString(getToken);
                try {
                    HttpURLConnection connection = WebServiceHelper.openConnectionForUrl(data, "POST");
                    output = WebServiceHelper.readResponseData(connection);
                    System.out.println(output + "read");
                    return output;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return output;
        }

        @Override
        protected void onPostExecute(String aString) {
            super.onPostExecute(aString);

            WebServiceHelper.dismissProgressDialog();
            if (!internetAvailable) {
                Helpers.alertDialog(WelcomeActivity.this, "Connection error",
                        "Check your internet connection");
                return;
            }
        }
    }
}
