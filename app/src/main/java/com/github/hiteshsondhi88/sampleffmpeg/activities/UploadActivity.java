package com.github.hiteshsondhi88.sampleffmpeg.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.hiteshsondhi88.sampleffmpeg.R;
import com.github.hiteshsondhi88.sampleffmpeg.utils.AppGlobals;
import com.github.hiteshsondhi88.sampleffmpeg.utils.Helpers;
import com.github.hiteshsondhi88.sampleffmpeg.utils.MultiPartUtility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;


public class UploadActivity extends AppCompatActivity {

    private String fileToUpload;
    private String videoName;
    private MultiPartUtility multiPartUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_upload_activity);
        fileToUpload = getIntent().getStringExtra(AppGlobals.KEY_FILE_TO_UPLOAD);
        videoName = getIntent().getStringExtra(AppGlobals.KEY_FILE_NAME);
        if (videoName != null && fileToUpload != null) {
            new UploadFileTask().execute();
        }
    }

    class UploadFileTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                multiPartUtility = new MultiPartUtility(new URL(AppGlobals.UPLOAD_URL));
                multiPartUtility.addFormField("action", "new_video_to_review");
                multiPartUtility.addFormField("account_id",
                        Helpers.getStringFromSharedPreferences(AppGlobals.KEY_USER_ACCOUNT_ID));
                multiPartUtility.addFormField("add_video_to_this_account_id",
                        Helpers.getStringFromSharedPreferences(AppGlobals.KEY_USER_ACCOUNT_ID));
                multiPartUtility.addFormField("user_id",
                        Helpers.getStringFromSharedPreferences(AppGlobals.KEY_USER_ID));
                multiPartUtility.addFormField("session_token",
                        Helpers.getStringFromSharedPreferences(AppGlobals.KEY_USER_TOKEN));
                multiPartUtility.addFormField("title", videoName);
                Log.i("TAG", "account id "+ Helpers.getStringFromSharedPreferences(AppGlobals.KEY_USER_ACCOUNT_ID) +
                "user_id " +Helpers.getStringFromSharedPreferences(AppGlobals.KEY_USER_ID) +
                                "session_token "+ Helpers.getStringFromSharedPreferences(AppGlobals.KEY_USER_TOKEN));
                multiPartUtility.addFilePart("vid_file", new File(fileToUpload));
                final byte[] bytes = multiPartUtility.finishFilesUpload();
                OutputStream os = new FileOutputStream(new File(fileToUpload));
                os.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            
        }
    }
}
