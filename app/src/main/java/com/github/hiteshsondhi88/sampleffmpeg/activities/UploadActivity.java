package com.github.hiteshsondhi88.sampleffmpeg.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.hiteshsondhi88.sampleffmpeg.R;
import com.github.hiteshsondhi88.sampleffmpeg.utils.AppGlobals;
import com.github.hiteshsondhi88.sampleffmpeg.utils.Helpers;
import com.github.hiteshsondhi88.sampleffmpeg.utils.WebServiceHelper;
import com.github.lzyzsd.circleprogress.DonutProgress;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class UploadActivity extends AppCompatActivity {

    private String fileToUpload;
    private String videoName;
    private static DonutProgress donutProgress;
    private static UploadActivity sInstance;
    private String boundary;
    private static final String CRLF = "\r\n";
    private static final String CHARSET = "UTF-8";
    private StringBuilder responseBuilder;
    private long requestLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_upload_activity);
        sInstance = this;
        donutProgress = (DonutProgress) findViewById(R.id.upload_progress);
        fileToUpload = getIntent().getStringExtra(AppGlobals.KEY_FILE_TO_UPLOAD);
        videoName = getIntent().getStringExtra(AppGlobals.KEY_FILE_NAME);
        if (videoName != null && fileToUpload != null) {
            new UploadFileTask().execute();
        }
    }

    class UploadFileTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection.setFollowRedirects(false);
            responseBuilder = new StringBuilder();
            HttpURLConnection connection = null;
            File file = new File(fileToUpload);
            String fileName = file.getName();
            try {
                connection = (HttpURLConnection) new URL(AppGlobals.UPLOAD_URL).openConnection();
                connection.setRequestMethod("POST");
                boundary = "---------------------------boundary";
                String tail = "\r\n--" + boundary + "--\r\n";
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                connection.setRequestProperty(WebServiceHelper.COOKIE,
                        Helpers.getStringFromSharedPreferences(AppGlobals.FULL_TOKEN));
                connection.setDoOutput(true);

                StringBuilder metadataPart = new StringBuilder();
                addFormField(metadataPart, "action", "new_video_to_review");
                addFormField(metadataPart, "add_video_to_this_account_id",
                        Helpers.getStringFromSharedPreferences(AppGlobals.KEY_USER_ACCOUNT_ID));
                addFormField(metadataPart, "user_id",
                        Helpers.getStringFromSharedPreferences(AppGlobals.KEY_USER_ID));
                addFormField(metadataPart, "session_token",
                        Helpers.getStringFromSharedPreferences(AppGlobals.KEY_USER_TOKEN));
                addFormField(metadataPart, "title", videoName);

                String fileHeader1 = "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"vid_file\"; filename=\""
                        + fileName + "\"\r\n"
                        + "Content-Type: application/octet-stream\r\n"
                        + "Content-Transfer-Encoding: binary\r\n";
                Log.i("TAG", metadataPart.toString());

                long fileLength = file.length() + tail.length();
                String fileHeader2 = "Content-length: " + fileLength + "\r\n";
                String fileHeader = fileHeader1 + fileHeader2 + "\r\n";
                String stringData = metadataPart.toString() + fileHeader;

                requestLength = stringData.length() + fileLength;
                connection.setRequestProperty("Content-length", "" + requestLength);
                connection.setFixedLengthStreamingMode((int) requestLength);
                connection.connect();

                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.writeBytes(stringData);
                out.flush();

                int progress = 0;
                int bytesRead = 0;
                byte buf[] = new byte[1024];
                BufferedInputStream bufInput = new BufferedInputStream(new FileInputStream(file));
                while ((bytesRead = bufInput.read(buf)) != -1) {
                    // write output
                    out.write(buf, 0, bytesRead);
                    out.flush();
                    progress += bytesRead;
                    // update progress bar
                    publishProgress(progress);
                }

                // Write closing boundary and close stream
                out.writeBytes(tail);
                out.flush();
                out.close();

                // Get server response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                responseBuilder = new StringBuilder();
                while((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }

                Log.i("Response", responseBuilder.toString());

            } catch (Exception e) {
                // Exception
            } finally {
                if (connection != null) connection.disconnect();
            }
            return responseBuilder.toString();
        }

        public void addFormField(StringBuilder stringBuilder ,final String name, final String value) {
            stringBuilder.append("--").append(boundary).append(CRLF)
                    .append("Content-Disposition: form-data; name=\"").append(name)
                    .append("\"").append(CRLF)
                    .append("Content-Type: text/plain; charset=").append(CHARSET)
                    .append(CRLF).append(CRLF).append(value).append(CRLF);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.i("TAG", "" + requestLength);
            Log.i("division", "" + values[0]/(double)requestLength);
            double progress = (values[0]/(double)requestLength)*100;
            Log.i("current progress", "" + progress);
            donutProgress.setProgress((int) progress);

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            donutProgress.setProgress(100);
            String title = "";
            String message = "Video has been successfully uploaded.";
            try {
                JSONObject jsonObject = new JSONObject(s);
                title = jsonObject.getString("result");
                if (!title.equals("success")) {
                    message = jsonObject.getString("message");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UploadActivity.this);
            alertDialogBuilder.setTitle(title);
            alertDialogBuilder
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            finish();

                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }
    }

}
