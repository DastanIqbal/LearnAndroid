package processing.async;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

public class Renderer extends Thread implements TextureView.SurfaceTextureListener {
    private String TAG = Renderer.class.getSimpleName();
    private Object mLock = new Object();        // guards mSurfaceTexture, mDone
    private SurfaceTexture mSurfaceTexture;
    private boolean mDone;

    private int mWidth;     // from SurfaceTexture
    private int mHeight;

    public Renderer() {
        super("TextureViewCanvas Renderer");
    }

    @Override
    public void run() {
        while (true) {
            SurfaceTexture surfaceTexture = null;

            // Latch the SurfaceTexture when it becomes available.  We have to wait for
            // the TextureView to create it.
            synchronized (mLock) {
                while (!mDone && (surfaceTexture = mSurfaceTexture) == null) {
                    try {
                        mLock.wait();
                    } catch (InterruptedException ie) {
                        throw new RuntimeException(ie);     // not expected
                    }
                }
                if (mDone) {
                    break;
                }
            }
            Log.d(TAG, "Got surfaceTexture=" + surfaceTexture);

            // Render frames until we're told to stop or the SurfaceTexture is destroyed.
            doAnimation();
        }

        Log.d(TAG, "Renderer thread exiting");
    }

    /**
     * Draws updates as fast as the system will allow.
     * <p>
     * In 4.4, with the synchronous buffer queue queue, the frame rate will be limited.
     * In previous (and future) releases, with the async queue, many of the frames we
     * render may be dropped.
     * <p>
     * The correct thing to do here is use Choreographer to schedule frame updates off
     * of vsync, but that's not nearly as much fun.
     */
    private void doAnimation() {
        final int BLOCK_WIDTH = 80;
        final int BLOCK_SPEED = 2;
        int clearColor = 0;
        int xpos = -BLOCK_WIDTH / 2;
        int xdir = BLOCK_SPEED;

        // Create a Surface for the SurfaceTexture.
        Surface surface = null;
        synchronized (mLock) {
            SurfaceTexture surfaceTexture = mSurfaceTexture;
            if (surfaceTexture == null) {
                Log.d(TAG, "ST null on entry");
                return;
            }
            surface = new Surface(surfaceTexture);
        }

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);

        boolean partial = false;
        while (true) {
            Rect dirty = null;
            if (partial) {
                // Set a dirty rect to confirm that the feature is working.  It's
                // possible for lockCanvas() to expand the dirty rect if for some
                // reason the system doesn't have access to the previous buffer.
                dirty = new Rect(0, mHeight * 3 / 8, mWidth, mHeight * 5 / 8);
            }
            Canvas canvas = surface.lockCanvas(dirty);
            if (canvas == null) {
                Log.d(TAG, "lockCanvas() failed");
                break;
            }
            try {
                // just curious
                if (canvas.getWidth() != mWidth || canvas.getHeight() != mHeight) {
                    Log.d(TAG, "WEIRD: width/height mismatch");
                }

                // Draw the entire window.  If the dirty rect is set we should actually
                // just be drawing into the area covered by it -- the system lets us draw
                // whatever we want, then overwrites the areas outside the dirty rect with
                // the previous contents.  So we've got a lot of overdraw here.
                canvas.drawRGB(clearColor, clearColor, clearColor);
                canvas.drawRect(xpos, mHeight / 4, xpos + BLOCK_WIDTH, mHeight * 3 / 4, paint);
            } finally {
                // Publish the frame.  If we overrun the consumer, frames will be dropped,
                // so on a sufficiently fast device the animation will run at faster than
                // the display refresh rate.
                //
                // If the SurfaceTexture has been destroyed, this will throw an exception.
                try {
                    surface.unlockCanvasAndPost(canvas);
                } catch (IllegalArgumentException iae) {
                    Log.d(TAG, "unlockCanvasAndPost failed: " + iae.getMessage());
                    break;
                }
            }

            // Advance state
            clearColor += 4;
            if (clearColor > 255) {
                clearColor = 0;
                partial = !partial;
            }
            xpos += xdir;
            if (xpos <= -BLOCK_WIDTH / 2 || xpos >= mWidth - BLOCK_WIDTH / 2) {
                Log.d(TAG, "change direction");
                xdir = -xdir;
            }
        }

        surface.release();
    }

    /**
     * Tells the thread to stop running.
     */
    public void halt() {
        synchronized (mLock) {
            mDone = true;
            mLock.notify();
        }
    }

    @Override   // will be called on UI thread
    public void onSurfaceTextureAvailable(SurfaceTexture st, int width, int height) {
        Log.d(TAG, "onSurfaceTextureAvailable(" + width + "x" + height + ")");
        mWidth = width;
        mHeight = height;
        synchronized (mLock) {
            mSurfaceTexture = st;
            mLock.notify();
        }
    }

    @Override   // will be called on UI thread
    public void onSurfaceTextureSizeChanged(SurfaceTexture st, int width, int height) {
        Log.d(TAG, "onSurfaceTextureSizeChanged(" + width + "x" + height + ")");
        mWidth = width;
        mHeight = height;
    }

    @Override   // will be called on UI thread
    public boolean onSurfaceTextureDestroyed(SurfaceTexture st) {
        Log.d(TAG, "onSurfaceTextureDestroyed");

        synchronized (mLock) {
            mSurfaceTexture = null;
        }
        return true;
    }

    @Override   // will be called on UI thread
    public void onSurfaceTextureUpdated(SurfaceTexture st) {
        //Log.d(TAG, "onSurfaceTextureUpdated");
    }
}