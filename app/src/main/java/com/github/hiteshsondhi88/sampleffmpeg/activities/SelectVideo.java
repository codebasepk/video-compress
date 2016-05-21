package com.github.hiteshsondhi88.sampleffmpeg.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.hiteshsondhi88.sampleffmpeg.R;
import com.github.hiteshsondhi88.sampleffmpeg.utils.AppGlobals;
import com.github.hiteshsondhi88.sampleffmpeg.utils.Helpers;


public class SelectVideo extends Activity implements View.OnClickListener {

    private Button selectVideo;
    private Button hamburger;
    private final int PICK_VIDEO = 0;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static String videoPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Helpers.isUserLoggedIn()) {
            startActivity(new Intent(getApplicationContext(), WelcomeActivity.class));
        }
        setContentView(R.layout.layout_select_video);
        hamburger = (Button) findViewById(R.id.hamburger);
        selectVideo = (Button) findViewById(R.id.select_video_button);
        selectVideo.setOnClickListener(this);
        hamburger.setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!Helpers.isUserLoggedIn()) {
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.select_video_button:
//                if (ContextCompat.checkSelfPermission(SelectVideo.this,
//                        Manifest.permission.READ_EXTERNAL_STORAGE)
//                        != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions(SelectVideo.this,
//                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
//                } else {
                    openPictures();
//                }
                break;
            case R.id.hamburger:
                startActivity(new Intent(getApplicationContext(), LogoutActivity.class));
                break;
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Log.i("TAG", "Permission granted");
//                    openPictures();
//                } else {
//                    Toast.makeText(SelectVideo.this.getApplicationContext(), "Permission denied!"
//                            , Toast.LENGTH_LONG).show();
//                }
//                break;
//        }
//    }

    private void openPictures() {
        if (Build.VERSION.SDK_INT < 19) {
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Videos"), PICK_VIDEO);
        } else {
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Videos"), PICK_VIDEO);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO && resultCode == RESULT_OK
                && null != data) {
            Log.i("TAG", "if part");
            // Get the video from data
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            if (data.getData() != null) {

                Uri mImageUri = data.getData();
                videoPath = getImagePath(mImageUri);
                Intent intent = new Intent(getApplicationContext(), DisplayVideoActivity.class);
                intent.putExtra(AppGlobals.KEY_VIDEO_PATH, videoPath);
                startActivity(intent);

            }
//            else {
//                if (data.getClipData() != null) {
//                    Log.i("TAG", "else part");
//                    ClipData mClipData = data.getClipData();
//                    for (int i = 0; i < mClipData.getItemCount(); i++) {
//
//                        ClipData.Item item = mClipData.getItemAt(i);
//                        Uri uri = item.getUri();
//                        videoPath = getImagePath(uri);
//                        // Get the cursor
//                    }
//                }
//            }
        } else {
            Toast.makeText(SelectVideo.this, "You haven't picked any video",
                    Toast.LENGTH_LONG).show();
        }
        Log.i("TAG", videoPath);
    }

    public String getImagePath(Uri uri) {
        Cursor cursor = getApplicationContext().getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getApplicationContext().getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Video.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
        cursor.close();

        return path;
    }
}
