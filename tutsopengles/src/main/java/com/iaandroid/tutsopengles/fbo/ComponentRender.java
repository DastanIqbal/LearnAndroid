package com.iaandroid.tutsopengles.fbo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.opengl.GLES20;

import com.dastanapps.mediasdk.opengles.gpu.fbo.Component;
import com.dastanapps.mediasdk.opengles.gpu.fbo.IBitmapCache;
import com.dastanapps.mediasdk.opengles.gpu.fbo.Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


/**
 * 贴纸组件渲染器
 *
 * @author like
 * @date 2018-01-09
 */
public class ComponentRender {

    private Context mContext;
    private Component mComponent;
    private IBitmapCache mBitmapCache;

    private int mTexture;

    // 上一帧序号
    private int mLastIndex = -1;

    private long mStartTime = -1;

    // 渲染顶点坐标
    private FloatBuffer mRenderVertices;


    public ComponentRender(Context context, Component component) {
        mContext = context;
        mComponent = component;

        // 4个顶点，每个顶点由x，y两个float变量组成，每个float占4字节，总共32字节
        mRenderVertices = ByteBuffer.allocateDirect(32)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
    }

    /**
     * 设置图片缓存
     *
     * @param bitmapCache
     */
    public void setBitmapCache(IBitmapCache bitmapCache) {
        mBitmapCache = bitmapCache;
    }


    /**
     * 绘制组件
     * <p>
     * 在GL线程调用
     *
     * @param textureHandle      纹理指针
     * @param positionHandle     渲染顶点坐标指针
     * @param textureCoordHandle 纹理顶点坐标指针
     * @param textureVertices    纹理顶点坐标
     */
    public void onDraw(int textureHandle, int positionHandle, int textureCoordHandle, FloatBuffer textureVertices) {
        mRenderVertices.position(0);
        textureVertices.position(0);

        if (mStartTime == -1) {
            mStartTime = System.currentTimeMillis();
        }
        long currentTime = System.currentTimeMillis();
        long position = (currentTime - mStartTime) % mComponent.duration;
        // 如mComponent.duration=3000，mComponent.length=60，position=1000，则currentIndex=20
        int currentIndex = Math.round((mComponent.length - 1) * 1.0f / mComponent.duration * position);

        String path = mComponent.resources.get(currentIndex);
        Bitmap bitmap = mBitmapCache.get(path);
        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = Utils.loadBitmap(mContext, path, mComponent.width, mComponent.height);
            if (bitmap != null && !bitmap.isRecycled()) {
//                // 按照mComponent.width和mComponent.height尺寸对图片进行缩放
//                if (bitmap.getWidth() != mComponent.width || bitmap.getHeight() != mComponent.height) {
//                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, mComponent.width, mComponent.height, true);
//                    if (scaledBitmap != bitmap) {
//                        bitmap.recycle();
//                    }
//                    bitmap = scaledBitmap;
//                }
                mBitmapCache.put(path, bitmap);
            } else {
                return;
            }
        }

        // 当前帧不变时，不重新绑定Bitmap，直接渲染已绑定的纹理
        if (mLastIndex != currentIndex) {
            if (mTexture != 0) {
                int[] tex = new int[1];
                tex[0] = mTexture;
                GLES20.glDeleteTextures(1, tex, 0);
                mTexture = 0;
            }
            mTexture = Utils.bindBitmap(bitmap);
        }

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture);
        GLES20.glUniform1i(textureHandle, 2);

        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, mRenderVertices);
        GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, textureVertices);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        mLastIndex = currentIndex;
    }

    /**
     * 销毁组件资源
     * <p>
     * 在GL线程调用
     */
    public void destroy() {
        if (mTexture != 0) {
            int[] tex = new int[1];
            tex[0] = mTexture;
            GLES20.glDeleteTextures(1, tex, 0);
            mTexture = 0;
        }
    }

    private PointF getRotateVertices(PointF point, PointF anchorPoint, double angle) {
        return new PointF(
                (float) ((point.x - anchorPoint.x) * Math.cos(angle) -
                        (point.y - anchorPoint.y) * Math.sin(angle) + anchorPoint.x),
                (float) ((point.x - anchorPoint.x) * Math.sin(angle) +
                        (point.y - anchorPoint.y) * Math.cos(angle) + anchorPoint.y));
    }

    private PointF transVerticesToOpenGL(PointF point, float width, float height) {
        return new PointF((point.x - width / 2) / (width / 2),
                (point.y - height / 2) / (height / 2));
    }

    private float distanceOf(PointF p0, PointF p1) {
        return (float) Math.sqrt((p0.x - p1.x) * (p0.x - p1.x) + (p0.y - p1.y) * (p0.y - p1.y));
    }
}
