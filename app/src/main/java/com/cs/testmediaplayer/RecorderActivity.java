package com.cs.testmediaplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;

import static android.os.Environment.DIRECTORY_MOVIES;

public class RecorderActivity extends AppCompatActivity {
    private static final String TAG = "RecorderActivity";
    MediaRecorder recorder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);
        initView();
    }

    private void initView() {
        recorder= new MediaRecorder();

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

    }

    public void onRecorderStart(View view) {
        File movieFilesDir = getExternalFilesDir(DIRECTORY_MOVIES);
        String path = movieFilesDir.getAbsolutePath()+"/zyjy.mp4";
        Log.d(TAG, "onRecorderStart: "+path);
        recorder.setOutputFile(path);
        recorder.setVideoSize(getScreenWidth(),getScreenHeight());
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recorder.start();   // Recording is now started

    }

    public void onRecorderStop(View view) {
        recorder.stop();
        recorder.reset();   // You can reuse the object by going back to setAudioSource() step
    }

    private int getScreenWidth() {
        return ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
    }

    private int getScreenHeight() {
        return ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getHeight();
    }
}