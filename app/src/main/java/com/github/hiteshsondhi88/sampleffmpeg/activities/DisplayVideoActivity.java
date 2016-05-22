package com.github.hiteshsondhi88.sampleffmpeg.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.VideoView;

import com.github.hiteshsondhi88.sampleffmpeg.R;
import com.github.hiteshsondhi88.sampleffmpeg.utils.AppGlobals;


public class DisplayVideoActivity extends AppCompatActivity implements View.OnClickListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private String path = "";
    private LinearLayout layout;
    private VideoView videoView;
    private Button cancelButton;
    private Button chooseButton;
    private ImageView playPauseButton;
    private long timeInmillisec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_video_display);
        cancelButton = (Button) findViewById(R.id.cancel_button);
        chooseButton = (Button) findViewById(R.id.choose_button);
        playPauseButton = (ImageView) findViewById(R.id.play_pause_button);
        playPauseButton.setVisibility(View.INVISIBLE);
        cancelButton.setOnClickListener(this);
        chooseButton.setOnClickListener(this);
        playPauseButton.setOnClickListener(this);
        videoView = (VideoView) findViewById(R.id.video_view);
        path = getIntent().getStringExtra(AppGlobals.KEY_VIDEO_PATH);
        if (path.trim().isEmpty()) {
            finish();
        }
        showFrames();
        videoView.setVideoPath(path);
        videoView.setOnPreparedListener(this);
        videoView.setOnCompletionListener(this);
        videoView.seekTo(100);
        new LoadThumbnailTask().execute();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel_button:
                finish();
                break;
            case R.id.choose_button:
                Intent intent = new Intent(getApplicationContext(), CompressActivity.class);
                intent.putExtra(AppGlobals.KEY_TO_BE_PROCESSED_VIDEO_PATH, path);
                intent.putExtra(AppGlobals.KEY_TIME_IN_MILLIS, timeInmillisec);
                startActivity(intent);
                break;
            case R.id.play_pause_button:
                if (videoView.isPlaying()) {
                    videoView.pause();
                    playPauseButton.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    videoView.start();
                    playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
                }
                break;
        }

    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        playPauseButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        playPauseButton.setImageResource(android.R.drawable.ic_media_play);
    }

    class LoadThumbnailTask extends AsyncTask<String, Bitmap, Bitmap> {

        private int val = 0;

        @Override
        protected Bitmap doInBackground(String... strings) {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(path);
            String time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            timeInmillisec = Long.parseLong(time);
            long duration = timeInmillisec / 1000;
            long hours = duration / 3600;
            long minutes = (duration - hours * 3600) / 60;
            long seconds = duration - (hours * 3600 + minutes * 60);
            Log.i("Time", "time in milliseconds:"+ timeInmillisec + " duration:" + duration+
                    " hours:"+ hours+ " minutes" + minutes+ " seconds:" + seconds+ " ");
            for (int value = 0; value < seconds; value++) {
                Log.i("LOG", " "+ value);
                publishProgress(mediaMetadataRetriever.getFrameAtTime(value));
                val = value;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Bitmap... values) {
            super.onProgressUpdate(values);
            ImageView imageView = new ImageView(DisplayVideoActivity.this);
            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(100,100);
            imageView.setLayoutParams(parms);
            imageView.setPadding(2, 2, 2, 2);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageBitmap(values[0]);
            layout.addView(imageView);
        }
    }

    private void showFrames() {
        layout = (LinearLayout) findViewById(R.id.linear);
    }
}
