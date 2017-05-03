package com.dastanapps.mediaeffectswithopengles.record;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.dastanapps.mediaeffectswithopengles.R;
import com.dastanapps.mediaeffectswithopengles.gl.VideoTextureRenderer;

import java.io.IOException;

/**
 * Orgianl Tutorial: https://www.virag.si/2014/03/rendering-video-with-opengl-on-android/
 */

public class RecordSurfaceActivity extends Activity implements TextureView.SurfaceTextureListener {
    private static final String LOG_TAG = "SurfaceTest";

    private TextureView surface;
    private MediaPlayer player;
    private VideoTextureRenderer renderer;

    private int surfaceWidth;
    private int surfaceHeight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        surface = (TextureView) findViewById(R.id.surface);
        surface.setSurfaceTextureListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (surface.isAvailable())
            startPlaying();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null)
            player.release();
        if (renderer != null)
            renderer.onPause();
    }

    private void startPlaying() {
        renderer = new VideoTextureRenderer(this, surface.getSurfaceTexture(), surfaceWidth, surfaceHeight);
        player = new MediaPlayer();

        try {
            AssetFileDescriptor afd = getAssets().openFd("big_buck_bunny.mp4");
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.setSurface(new Surface(renderer.getVideoTexture()));
            player.setLooping(true);
            player.prepare();
            renderer.setVideoSize(player.getVideoWidth(), player.getVideoHeight());
            player.start();

            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    adjustAspectRatio(surface, player.getVideoWidth(), player.getVideoHeight());
                }
            });

        } catch (IOException e) {
            throw new RuntimeException("Could not open input video!");
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        surfaceWidth = width;
        surfaceHeight = height;
        startPlaying();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public static void adjustAspectRatio(TextureView vidTxv, int videoWidth, int videoHeight) {
        int viewWidth = vidTxv.getWidth();
        int viewHeight = vidTxv.getHeight();
        double aspectRatio = (double) videoHeight / videoWidth;

        int newWidth, newHeight;
        if (viewHeight > (int) (viewWidth * aspectRatio)) {
            // limited by narrow width; restrict height
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {
            // limited by short height; restrict width
            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }
        int xoff = (viewWidth - newWidth) / 2;
        int yoff = (viewHeight - newHeight) / 2;
        Log.d("GL", "video=" + videoWidth + "x" + videoHeight +
                " view=" + viewWidth + "x" + viewHeight +
                " newView=" + newWidth + "x" + newHeight +
                " off=" + xoff + "," + yoff);

        Matrix txform = new Matrix();
        vidTxv.getTransform(txform);
        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
        txform.postTranslate(xoff, yoff);
        vidTxv.setTransform(txform);
    }
}
