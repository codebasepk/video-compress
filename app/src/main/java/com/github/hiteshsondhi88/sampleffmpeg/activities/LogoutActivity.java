package com.github.hiteshsondhi88.sampleffmpeg.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.github.hiteshsondhi88.sampleffmpeg.R;
import com.github.hiteshsondhi88.sampleffmpeg.utils.Helpers;

public class LogoutActivity extends AppCompatActivity implements View.OnClickListener {

    Button goToStartButton;
    Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);
        goToStartButton = (Button) findViewById(R.id.button_start);
        logoutButton = (Button) findViewById(R.id.button_logout);
        goToStartButton.setOnClickListener(this);
        logoutButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_start:
                startActivity(new Intent(getApplicationContext(), SelectVideo.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;

            case R.id.button_logout:
                SharedPreferences sharedPreferences = Helpers.getPreferenceManager();
                sharedPreferences.edit().clear().apply();
                Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
                this.finish();
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
