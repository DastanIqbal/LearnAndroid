package com.dastanapps.mediaeffectswithopengles.record;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;

import com.dastanapps.mediaeffectswithopengles.R;
import com.dastanapps.mediaeffectswithopengles.effects.GammaEffect;
import com.dastanapps.mediaeffectswithopengles.gl.VideoSurfaceView;


public class GLSurfaceViewPlayerActivity extends Activity {

    private static final String TAG = "SamplePlayerActivity";

    protected Resources mResources;

    private VideoSurfaceView mVideoView = null;
    private MediaPlayer mMediaPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mResources = getResources();
        mMediaPlayer = new MediaPlayer();

        try {
            // Load video file from SD Card
            // File dir = Environment
            // .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            // File file = new File(dir,
            // "sample.mp4");
            // mMediaPlayer.setDataSource(file.getAbsolutePath());
            // -----------------------------------------------------------------------
            // Load video file from Assets directory
            AssetFileDescriptor afd = getAssets().openFd("big_buck_bunny.mp4");
            mMediaPlayer.setDataSource(afd.getFileDescriptor(),
                    afd.getStartOffset(), afd.getLength());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        // Initialize VideoSurfaceView using code
        // mVideoView = new VideoSurfaceView(this);
        // setContentView(mVideoView);
        // or
        setContentView(R.layout.activity_sampleplayer);
        mVideoView = (VideoSurfaceView) findViewById(R.id.mVideoSurfaceView);
        mVideoView.init(mMediaPlayer,
                new GammaEffect(0.2f));
//      If you want to change effect then just call mVideoView.init() again
//      and then call mVideoView.onResume()
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMediaPlayer != null)
            mMediaPlayer.release();
    }
}
