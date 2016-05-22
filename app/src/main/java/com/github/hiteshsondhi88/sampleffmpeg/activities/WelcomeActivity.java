package com.github.hiteshsondhi88.sampleffmpeg.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.github.hiteshsondhi88.sampleffmpeg.R;


public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {

    private Button loginButton;
    private TextView singUp;
    private TextView info;
    private LoginButton mLoginButton;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_welcome);
        info = (TextView)findViewById(R.id.info);
        mLoginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton = (Button) findViewById(R.id.button_login);
        singUp = (TextView) findViewById(R.id.signup_text);
        loginButton.setOnClickListener(this);
        singUp.setOnClickListener(this);
        mLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                info.setText(
                        "User ID: "
                                + loginResult.getAccessToken().getUserId()
                                + "\n" +
                                "Auth Token: "
                                + loginResult.getAccessToken().getToken()
                );

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
}
