package com.github.hiteshsondhi88.sampleffmpeg.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.hiteshsondhi88.sampleffmpeg.R;
import com.github.hiteshsondhi88.sampleffmpeg.utils.AppGlobals;


public class UploadActivity extends AppCompatActivity {

    private String fileToUpload;
    private String videoName;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_upload_activity);
        fileToUpload = getIntent().getStringExtra(AppGlobals.KEY_FILE_TO_UPLOAD);
        videoName = getIntent().getStringExtra(AppGlobals.KEY_FILE_NAME);
    }
}
