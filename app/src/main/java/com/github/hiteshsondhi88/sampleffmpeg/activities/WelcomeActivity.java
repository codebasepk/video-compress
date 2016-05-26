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
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.github.hiteshsondhi88.sampleffmpeg.R;
import com.github.hiteshsondhi88.sampleffmpeg.utils.AppGlobals;
import com.github.hiteshsondhi88.sampleffmpeg.utils.Helpers;
import com.github.hiteshsondhi88.sampleffmpeg.utils.WebServiceHelper;

import org.json.JSONException;
import org.json.JSONObject;

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

        mLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                String userId = loginResult.getAccessToken().getUserId();
                getToken = loginResult.getAccessToken().getToken();
                new FbLoginTask(getToken).execute();
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
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

    private class FbLoginTask extends AsyncTask<Void, Void, String> {

        private boolean internetAvailable = false;

        public FbLoginTask(String token) {
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
                    System.out.println(output);
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
            try {
                JSONObject jsonObject = new JSONObject(aString);
                if (jsonObject.getString("result").equals("fail")) {
                    Helpers.alertDialog(WelcomeActivity.this, "Error", jsonObject.getString("message"));
                    return;
                } else if (jsonObject.getString("result").equals("success")) {
                    String public_user_id = jsonObject.getString("public_user_id");
                    String userId = jsonObject.getString("user_id");
                    String accountId = jsonObject.getString("account_id");
                    String token = jsonObject.getString("session_token");
                    // saveing values
                    Helpers.saveDataToSharedPreferences(AppGlobals.KEY_USER_PUBLIC_ID, public_user_id);
                    Helpers.saveDataToSharedPreferences(AppGlobals.KEY_USER_ID, userId);
                    Helpers.saveDataToSharedPreferences(AppGlobals.KEY_USER_ACCOUNT_ID, accountId);
                    Helpers.saveDataToSharedPreferences(AppGlobals.KEY_USER_TOKEN, token);
                    Helpers.saveUserLogin(true);
                    startActivity(new Intent(getApplicationContext(), SelectVideo.class));
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
