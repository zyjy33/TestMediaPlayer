package com.cs.testmediaplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static android.os.Environment.DIRECTORY_MOVIES;


public class MainActivity extends AppCompatActivity {
    SurfaceView mSvVideoPlayer;
    MediaRecorder recorder;
    private MediaPlayer mMediaPlayer;
    private int mPosition = 0;
    private boolean hasActiveHolder = false;
    private static final String TAG = "MainActivity";
    Surface mRecorderSurface;
    private SurfaceView displaySurfaceView;
    private HandlerThread mediaRecorderZyHandler;
    private Handler mRecorderHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSvVideoPlayer = findViewById(R.id.sv_video_player);
        displaySurfaceView = findViewById(R.id.display_player);
        playVideo();

        mRecorderSurface = MediaCodec.createPersistentInputSurface();
        mediaRecorderZyHandler = new HandlerThread("MediaRecorder_zy_Handler");
        mediaRecorderZyHandler.start();
        mRecorderHandler = new Handler(mediaRecorderZyHandler.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0) {
                    if (recorder == null)
                        recorder = new MediaRecorder();
                    Log.d(TAG, "handleMessage: had create Recorder");
                }
            }
        };
        mRecorderHandler.sendEmptyMessage(0);
    }

    /**
     * ????????????
     */
    public void playVideo() {
        if (mMediaPlayer == null) {
            //?????????MediaPlayer??????
            mMediaPlayer = new MediaPlayer();
            mSvVideoPlayer.setVisibility(View.VISIBLE);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            boolean mHardwareDecoder = false;
            // ???????????????????????????????????????
            // if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB && mHardwareDecoder) {
            mSvVideoPlayer.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            //   }
            mSvVideoPlayer.getHolder().setFixedSize(getScreenWidth(), getScreenHeight());
            mSvVideoPlayer.getHolder().setKeepScreenOn(true);//??????????????????
            mSvVideoPlayer.getHolder().addCallback(new SurFaceCallback());

        }
    }

    /**
     * ???player?????????dispay????????????SurfaceHolder?????????????????????SurfaceView??????????????????????????????????????????SurfaceView???????????????
     */
    private final class SurFaceCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (mMediaPlayer == null) {
                return;
            }
            if (!hasActiveHolder) {
                play(mPosition);
                hasActiveHolder = true;
            }
            if (mPosition > 0) {
                play(mPosition);
                mPosition = 0;
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mMediaPlayer == null) {
                return;
            }
            mMediaPlayer.setScreenOnWhilePlaying(true);
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                // mPosition = mMediaPlayer.getCurrentPosition();
            }
        }

        private void play(int position) {
            try {
                //????????????????????????????????????MediaPlayer
                //  AssetFileDescriptor fileDescriptor = getResources().openRawResourceFd(R.raw.info);
                mMediaPlayer.reset();
                //???mMediaPlayer???????????????SurfaceHolder??????????????????SurfaceView????????????
                //mMediaPlayer.setDisplay(mSvVideoPlayer.getHolder());
                mMediaPlayer.setDisplay(mSvVideoPlayer.getHolder());
                // mMediaPlayer.setDataSource(path );
                //mMediaPlayer.setDataSource("https://vjs.zencdn.net/v/oceans.mp4");
                // mMediaPlayer.setDataSource("http://172.29.163.154:8080/resoure/boon.mp4");
                //https://storage.googleapis.com/exoplayer-test-media-1/mkv/android-screens-lavf-56.36.100-aac-avc-main-1280x720.mkv
                mMediaPlayer.setDataSource("http://192.168.58.168:8080/resoure/kidd.mp4");
                // mMediaPlayer.setDataSource("https://storage.googleapis.com/exoplayer-test-media-1/mkv/android-screens-lavf-56.36.100-aac-avc-main-1280x720.mkv");
                Log.d(TAG, "setDataSource: ");

                // mMediaPlayer.stop();
                // ??????
                mMediaPlayer.prepareAsync();

                //mMediaPlayer.setOnBufferingUpdateListener(new BufferingUpdateListener());

                mMediaPlayer.setOnPreparedListener(new PreparedListener());

                mMediaPlayer.setOnCompletionListener(new CompletionListener());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ?????????????????????
     */
    private final class BufferingUpdateListener implements MediaPlayer.OnBufferingUpdateListener {

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            if (mMediaPlayer != null) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mPosition = mMediaPlayer.getCurrentPosition();
                } else {
                    mMediaPlayer.start();
                    mMediaPlayer.seekTo(mPosition);
                }
            }
            //  mMediaPlayer.stop();

        }
    }

    /**
     * ??????????????????
     * ?????????????????????????????????????????????????????????????????????????????????????????????prepare??????
     */
    private final class PreparedListener implements MediaPlayer.OnPreparedListener {
        private int position;


        @Override
        public void onPrepared(MediaPlayer mp) {
            Log.d(TAG, "onPrepared:start ");
            mMediaPlayer.start();

        }
    }

    /**
     * ?????????????????????
     */
    private final class CompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            ///   mMediaPlayer.start();
        }
    }

    @Override
    public void onDestroy() {
        //???????????????MediaPlayer???????????????C++????????????????????????????????????????????????
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                //mPosition = mMediaPlayer.getCurrentPosition();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        super.onDestroy();
    }

    private int getScreenWidth() {
        return ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
    }

    private int getScreenHeight() {
        return ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getHeight();
    }


    private void initRecorder() {


        recorder.reset();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE); //???????????? surface ?????????????????????????????? camera
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setVideoSize(848, 476);

        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setVideoEncodingBitRate(256000);
        recorder.setVideoFrameRate(20);
        recorder.setAudioSamplingRate(44000);

        File movieFilesDir = getExternalFilesDir(DIRECTORY_MOVIES);
        String path = movieFilesDir.getAbsolutePath() + "/zyjy.3gp";
        Log.d(TAG, "onRecorderStart: " + path);
        recorder.setOutputFile(path);
        // recorder.setPreviewDisplay(displaySurfaceView.getHolder().getSurface());
        recorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                Log.d(TAG, "onInfo: what = " + what + " extra = " + extra);
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    recorder.stop();
                    Log.d(TAG, "onInfo: stop");
                }
            }
        });
        try {

            recorder.prepare();
            Surface surface = recorder.getSurface();
            mMediaPlayer.setSurface(surface);
            Log.d(TAG, "initRecorder: setSurface start ");
            //   mMediaPlayer.setDisplay(null);


            Log.d(TAG, "initRecorder: setSurface end ");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void onRecorderStart(View view) {

        initRecorder();

        recorder.start();   // Recording is now started

    }

    public void onRecorderStop(View view) {

        //????????????RuntimeException:stop failed
        recorder.setOnErrorListener(null);
        Log.d(TAG, "onRecorderStop: 1");
        recorder.setOnInfoListener(null);
        Log.d(TAG, "onRecorderStop: 2");
        recorder.setPreviewDisplay(null);
        Log.d(TAG, "onRecorderStop: 3");
        mMediaPlayer.setSurface(mSvVideoPlayer.getHolder().getSurface());
        // recorder.setInputSurface(null);
        Log.d(TAG, "onRecorderStop: 4");

        recorder.stop();
        Log.d(TAG, "onRecorderStop: 5");
        recorder.reset();   // You can reuse the object by going back to setAudioSource() step
        Log.d(TAG, "onRecorderStop: 6");


    }


}
