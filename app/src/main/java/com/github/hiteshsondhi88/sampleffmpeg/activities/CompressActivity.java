package com.github.hiteshsondhi88.sampleffmpeg.activities;


import android.app.AlertDialog;
import android.content.DialogInterface;
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

import javax.inject.Inject;

import butterknife.ButterKnife;
import dagger.ObjectGraph;

public class CompressActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = CompressActivity.class.getSimpleName();

    private DonutProgress progressDialog;
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
        setContentView(R.layout.layout_compress_activity);
        ButterKnife.inject(this);
        ObjectGraph.create(new DaggerDependencyModule(this)).inject(this);
        loadFFMpegBinary();
        initUI();
        Log.i("App_folder", String.valueOf(appFolder));
        Log.i("paht", path);
        String cm = "-i " + path + " -strict experimental -vcodec libx264 -preset" +
                " ultrafast -crf 24 -acodec aac -ar 44100 -ac 2 -b:a 96k -s 640x360 -aspect " +
                "4:3 " + appFolder +File.separator+ "test.mp4";
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
                }

                @Override
                public void onSuccess(String s) {
                    addTextViewToLayout("SUCCESS with output : " + s);
                }

                @Override
                public void onProgress(String s) {
                    Log.d(TAG, "Started command : ffmpeg " + command);
                    addTextViewToLayout("progress : " + s);
                    Log.i("TAG", s);
                }

                @Override
                public void onStart() {
                    Log.d(TAG, "Started command : ffmpeg " + command);
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
