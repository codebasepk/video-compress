package com.github.hiteshsondhi88.sampleffmpeg.activities;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.github.hiteshsondhi88.sampleffmpeg.DaggerDependencyModule;
import com.github.hiteshsondhi88.sampleffmpeg.R;
import com.github.hiteshsondhi88.sampleffmpeg.utils.AppGlobals;
import com.github.hiteshsondhi88.sampleffmpeg.utils.Helpers;
import com.github.lzyzsd.circleprogress.DonutProgress;

import java.io.File;
import java.util.Arrays;

import javax.inject.Inject;

import butterknife.ButterKnife;
import dagger.ObjectGraph;

public class CompressActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = CompressActivity.class.getSimpleName();

    private DonutProgress progressDialog;
    private long timeInMilliSeconds;
    private TextView processingUpdate;
    private String outputFile;
    @Inject
    FFmpeg ffmpeg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        File appFolder = new File(Helpers.getFolderPath());
        if (!appFolder.exists()) {
            appFolder.mkdirs();
        }
        String path = getIntent().getStringExtra(AppGlobals.KEY_TO_BE_PROCESSED_VIDEO_PATH);
        timeInMilliSeconds = getIntent().getLongExtra(AppGlobals.KEY_TIME_IN_MILLIS, 11L);
        Log.i("TIME", "" + timeInMilliSeconds);
        setContentView(R.layout.layout_compress_activity);
        progressDialog = (DonutProgress) findViewById(R.id.donut_progress);
        processingUpdate = (TextView) findViewById(R.id.processing);
        progressDialog.setMax(100);
        ButterKnife.inject(this);
        ObjectGraph.create(new DaggerDependencyModule(this)).inject(this);
        loadFFMpegBinary();
        initUI();
        outputFile =  appFolder + File.separator+ Helpers.getTimeStamp()+".mp4";
        Log.i("App_folder", outputFile);
        Log.i("paht", path);
        String cm = "-i " + path + " -strict experimental -vcodec libx264 -preset" +
                " ultrafast -crf 24 -acodec aac -ar 44100 -ac 2 -b:a 96k -s 640x360 -aspect " +
                "4:3 " + outputFile;
        String cmd = cm;
        String[] command = cmd.split(" ");
        if (command.length != 0) {
            execFFmpegBinary(command);
        } else {
            Toast.makeText(CompressActivity.this, getString(R.string.empty_command_toast), Toast.LENGTH_LONG).show();
        }
    }

    private void initUI() {
//        runButton.setOnClickListener(this);
        progressDialog = (DonutProgress) findViewById(R.id.donut_progress);
    }

    private void loadFFMpegBinary() {
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    showUnsupportedExceptionDialog();
                }
            });
        } catch (FFmpegNotSupportedException e) {
            showUnsupportedExceptionDialog();
        }
    }

    private void execFFmpegBinary(final String[] command) {
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {

                @Override
                public void onFailure(String s) {
                    addTextViewToLayout("FAILED with output : " + s);
                    Toast.makeText(CompressActivity.this, "There was an error please try Again",
                            Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), SelectVideo.class));
                }

                @Override
                public void onSuccess(String s) {
                    addTextViewToLayout("SUCCESS with output : " + s);
                    progressDialog.setProgress(100);
                    processingUpdate.setText("Finished");
                    finish();
                    Intent intent  = new Intent(getApplicationContext(), GiveTitleActivity.class);
                    intent.putExtra(AppGlobals.KEY_FILE_TO_UPLOAD, outputFile);
                    startActivity(intent);
                }

                @Override
                public void onProgress(String s) {
//                    Log.d(TAG, "Started command : ffmpeg " + command);
//                    addTextViewToLayout("progress : " + s);

                    if (s.contains("speed")) {
                        String result = s;
                        result = result.substring(result.indexOf("time") + 5);
                        result = result.substring(0, result.indexOf("bitrate"));
                        String[] tokens = result.split(":");
                        Log.i("SEC_MILIS", Arrays.toString(tokens));
                        String[] secMiliseconds= tokens[2].split("\\.");
                        Log.i("secMiliseconds", tokens[2]);
                        Log.i("SEC_MILIS", Arrays.toString(secMiliseconds));
                        int secondsToMs = Integer.parseInt(secMiliseconds[0].trim()) * 1000;
                        int milisecondsPart = Integer.parseInt(secMiliseconds[1].trim());
                        int minutesToMs = Integer.parseInt(tokens[1].trim()) * 60000;
                        int hoursToMs = Integer.parseInt(tokens[0].trim()) * 3600000;
                        long total = secondsToMs + minutesToMs + hoursToMs + milisecondsPart;
                        Log.i("currentProgress", String.valueOf(progressDialog.getProgress()));
                        double updateProgress = ((double)total/timeInMilliSeconds)*100;
                        Log.e("update", (int) updateProgress+ " current "+ progressDialog.getProgress());
                        if (progressDialog.getProgress() != (int) updateProgress) {
                            progressDialog.setProgress((int) updateProgress);
                        }
                        Log.e("onProgress", String.valueOf(total));
                    }
                }

                @Override
                public void onStart() {
                    Toast.makeText(CompressActivity.this, "please wait a moment", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Started command : ffmpeg " + command);
                    processingUpdate.setText("Processing");
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg " + command);
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        }
    }

    private void addTextViewToLayout(String text) {
        TextView textView = new TextView(CompressActivity.this);
        textView.setText(text);
    }

    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(CompressActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.device_not_supported))
                .setMessage(getString(R.string.device_not_supported_message))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CompressActivity.this.finish();
                    }
                })
                .create()
                .show();

    }

    @Override
    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.run_command:
//                String data  = Environment.getExternalStorageDirectory().getAbsolutePath();
//                System.out.println(data);
//                String cm = "-i "+data+"/DCIM/Camera/video.mp4 -strict experimental -vcodec libx264 -preset" +
//                        " ultrafast -crf 24 -acodec aac -ar 44100 -ac 2 -b:a 96k -s 640x360 -aspect " +
//                        "4:3 "+data+"/DCIM/Camera/test.mp4";
//                String cmd = cm;
//                String[] command = cmd.split(" ");
//                if (command.length != 0) {
//                    execFFmpegBinary(command);
//                } else {
//                    Toast.makeText(Home.this, getString(R.string.empty_command_toast), Toast.LENGTH_LONG).show();
//                }
//                break;
//        }
//    }
    }
}
