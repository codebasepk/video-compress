package com.github.hiteshsondhi88.sampleffmpeg.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.hiteshsondhi88.sampleffmpeg.R;
import com.github.hiteshsondhi88.sampleffmpeg.utils.AppGlobals;


public class GiveTitleActivity extends AppCompatActivity implements View.OnClickListener {

    private Button continueButton;
    private EditText videoTitle;
    private String fileToUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_give_title_activity);
        fileToUpload = getIntent().getStringExtra(AppGlobals.KEY_FILE_TO_UPLOAD);
        continueButton = (Button) findViewById(R.id.button_continue);
        videoTitle = (EditText) findViewById(R.id.editText);
        continueButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_continue:
                String title = videoTitle.getText().toString();
                if (title.trim().isEmpty()) {
                    Toast.makeText(GiveTitleActivity.this, "please give name to this video",
                            Toast.LENGTH_SHORT).show();
                    return;
                } else if (!title.trim().isEmpty()) {
                    Intent intent = new Intent(getApplicationContext(), UploadActivity.class);
                    intent.putExtra(AppGlobals.KEY_FILE_TO_UPLOAD, fileToUpload);
                    intent.putExtra(AppGlobals.KEY_FILE_NAME, title);
                    startActivity(intent);
                }

                break;
        }
    }
}
