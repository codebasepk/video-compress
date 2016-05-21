package com.github.hiteshsondhi88.sampleffmpeg.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.github.hiteshsondhi88.sampleffmpeg.R;
import com.github.hiteshsondhi88.sampleffmpeg.utils.AppGlobals;
import com.github.hiteshsondhi88.sampleffmpeg.utils.Helpers;
import com.github.hiteshsondhi88.sampleffmpeg.utils.WebServiceHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton backButton;
    private Button loginButton;
    private EditText mUser_email;
    private EditText mUser_password;

    private String mEmail;
    private String mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        backButton = (ImageButton) findViewById(R.id.back_button);
        mUser_email = (EditText) findViewById(R.id.email_et);
        mUser_password = (EditText) findViewById(R.id.password_et);
        loginButton = (Button) findViewById(R.id.button_login);

        loginButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_button:
                onBackPressed();
                break;
            case R.id.button_login:
                System.out.println(validate());
                if (!validate()) {
                    Toast.makeText(getApplicationContext(), "invalid credentials",
                            Toast.LENGTH_SHORT).show();
                } else {
                    new LoginTask(mEmail, mPassword).execute();
                }
                break;
        }
    }

    public boolean validate() {
        boolean valid = true;

        mEmail = mUser_email.getText().toString();
        mPassword = mUser_password.getText().toString();

        if (mEmail.trim().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.
                matcher(mEmail).matches()) {
            mUser_email.setError("enter a valid email address");
            valid = false;
        } else {
            mUser_email.setError(null);
        }

        if (mPassword.isEmpty() || mPassword.length() < 4 || mPassword.length() > 10) {
            mUser_password.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            mUser_password.setError(null);
        }
        return valid;
    }


    class LoginTask extends AsyncTask<Void, Void, String> {

        public String mEmail;
        private String mPassword;
        private boolean internetAvailable = false;

        public LoginTask(String email, String password) {
            super();
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            WebServiceHelper.showProgressDialog(LoginActivity.this , "LoggingIn");
        }

        @Override
        protected String doInBackground(Void... params) {
            String output = "";
            if (WebServiceHelper.isNetworkAvailable() && WebServiceHelper.isInternetWorking()) {
                internetAvailable = true;

                String data = WebServiceHelper.getLoginString(mEmail, mPassword);
                try {
                    HttpURLConnection connection = WebServiceHelper.openConnectionForUrl(data, "POST");
                    output = WebServiceHelper.readResponseData(connection);
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
                Helpers.alertDialog(LoginActivity.this, "Connection error",
                        "Check your internet connection");
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(aString);
                if(jsonObject.getString("result").equals("fail")) {
                    Helpers.alertDialog(LoginActivity.this, "Error", jsonObject.getString("message"));
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
                    startActivity(new Intent(getApplicationContext(), WelcomeActivity.class));
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}

