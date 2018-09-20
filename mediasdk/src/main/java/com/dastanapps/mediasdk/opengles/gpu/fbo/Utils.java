package com.dastanapps.mediasdk.opengles.gpu.fbo;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

public class Utils {

    public static Bitmap GetFromAssets(Context context, String name) {
        Bitmap img = null;
        //get asset manager
        AssetManager assetManager = context.getAssets();
        InputStream istr;
        try {
            //open image to input stream
            istr = assetManager.open(name);
            //decode input stream
            img = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }

    static public FloatBuffer CreateVertexArray(float[] coord) {
        FloatBuffer fb = ByteBuffer.allocateDirect(coord.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        fb.put(coord).position(0);
        return fb;
    }

    public static int LoadTexture(GLSurfaceView view, int imgResID) {
        Log.d("Utils", "Loadtexture");
        Bitmap img = null;
        int textures[] = new int[1];
        try {
            img = BitmapFactory.decodeResource(view.getResources(), imgResID);
            GLES20.glGenTextures(1, textures, 0);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);

            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, img, 0);
            Log.d("LoadTexture", "Loaded texture" + ":H:" + img.getHeight() + ":W:" + img.getWidth());
        } catch (Exception e) {
            Log.d("LoadTexture", e.toString() + ":" + e.getMessage() + ":" + e.getLocalizedMessage());
        }
        img.recycle();
        return textures[0];
    }

    static public int LoadTexture(GLSurfaceView view, String name) {
        Log.d("Utils", "Loadtexture");
        int textures[] = new int[1];
        Bitmap img = GetFromAssets(view.getContext(), name);
        try {
            GLES20.glGenTextures(1, textures, 0);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, img, 0);
            Log.d("LoadTexture", "Loaded texture" + ":H:" + img.getHeight() + ":W:" + img.getWidth());
        } catch (Exception e) {
            Log.d("LoadTexture", e.toString() + ":" + e.getMessage() + ":" + e.getLocalizedMessage());
        }
        img.recycle();
        return textures[0];
    }

    public static int LoadShader(String strSource, int iType) {
        Log.d("Utils", "LoadShader");
        int[] compiled = new int[1];
        int iShader = GLES20.glCreateShader(iType);
        GLES20.glShaderSource(iShader, strSource);
        GLES20.glCompileShader(iShader);
        GLES20.glGetShaderiv(iShader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.d("Load Shader Failed", "Compilation\n" + GLES20.glGetShaderInfoLog(iShader));
            return 0;
        }
        return iShader;
    }

    public static int LoadProgram(String strVSource, String strFSource) {
        Log.d("Utils", "LoadProgram");
        int iVShader;
        int iFShader;
        int iProgId;
        int[] link = new int[1];
        iVShader = LoadShader(strVSource, GLES20.GL_VERTEX_SHADER);
        if (iVShader == 0) {
            Log.d("Load Program", "Vertex Shader Failed");
            return 0;
        }
        iFShader = LoadShader(strFSource, GLES20.GL_FRAGMENT_SHADER);
        if (iFShader == 0) {
            Log.d("Load Program", "Fragment Shader Failed");
            return 0;
        }

        iProgId = GLES20.glCreateProgram();

        GLES20.glAttachShader(iProgId, iVShader);
        GLES20.glAttachShader(iProgId, iFShader);

        GLES20.glLinkProgram(iProgId);

        GLES20.glGetProgramiv(iProgId, GLES20.GL_LINK_STATUS, link, 0);
        if (link[0] <= 0) {
            Log.d("Load Program", "Linking Failed");
            return 0;
        }
        GLES20.glDeleteShader(iVShader);
        GLES20.glDeleteShader(iFShader);
        return iProgId;
    }

    public static float rnd(float min, float max) {
        float fRandNum = (float) Math.random();
        return min + (max - min) * fRandNum;
    }

    public static int LoadProgram(Context ctx, String strVertShader, String strFragShader) {
        String strVShader = "";
        String strFShader = "";
        try {
            AssetManager assetManager = ctx.getAssets();
            InputStream is = assetManager.open(strVertShader);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            try {
                String line = br.readLine();
                while (line != null) {
                    strVShader += line + "\n";
                    line = br.readLine();
                }
            } catch (IOException e) {
                strVShader = "";
                e.printStackTrace();
            }
            Log.d("VSHADER", strVShader);
            is = assetManager.open(strFragShader);
            br = new BufferedReader(new InputStreamReader(is));


            try {
                String line = br.readLine();
                while (line != null) {
                    strFShader += line;
                    line = br.readLine();
                }
            } catch (IOException e) {
                strFShader = "";
                e.printStackTrace();
            }
            Log.d("FSHADER", strFShader);
        } catch (Exception e) {
            Log.d("LoadTexture", e.toString() + ":" + e.getMessage() + ":" + e.getLocalizedMessage());
        }
        return LoadProgram(strVShader, strFShader);
    }

    public static int bindBitmap(Bitmap bitmap) {
        int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_REPEAT);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_REPEAT);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        return tex[0];
    }

    public static List<String> convert(Context context, String dir) {
        String names[] = null;
        try {
            names = context.getAssets().list(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> paths = new ArrayList<>();
        if (names != null) {
            for (String name : names) {
                paths.add(dir + "/" + name);
            }
        }

        // 根据图片文件名进行排序
        Collections.sort(paths, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                // 最后一个下划线_之后及小数点.之前的数字
                String[] sp1 = o1.split("_");
                String[] sp2 = o2.split("_");
                String num1 = sp1[sp1.length - 1];
                if (num1.contains(".")) {
                    num1 = num1.substring(0, num1.indexOf("."));
                }
                if (!TextUtils.isDigitsOnly(num1)) {
                    num1 = num1.substring(num1.length() - 2);
                }
                String num2 = sp2[sp2.length - 1];
                if (num2.contains(".")) {
                    num2 = num2.substring(0, num2.indexOf("."));
                }
                if (!TextUtils.isDigitsOnly(num2)) {
                    num2 = num2.substring(num2.length() - 2);
                }
                return parseInt(num1) - parseInt(num2);
            }
        });
        return paths;
    }

    public static void convert(Context context, Component component) {
        String names[] = null;
        try {
            names = context.getAssets().list(component.src);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> paths = new ArrayList<>();
        if (names != null) {
            for (String name : names) {
                paths.add(component.src + "/" + name);
            }
        }

        component.length = paths.size();

        // 根据图片文件名进行排序
        Collections.sort(paths, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                // 最后一个下划线_之后及小数点.之前的数字
                String[] sp1 = o1.split("_");
                String[] sp2 = o2.split("_");
                String num1 = sp1[sp1.length - 1];
                if (num1.contains(".")) {
                    num1 = num1.substring(0, num1.indexOf("."));
                }
                if (!TextUtils.isDigitsOnly(num1)) {
                    num1 = num1.substring(num1.length() - 2);
                }
                String num2 = sp2[sp2.length - 1];
                if (num2.contains(".")) {
                    num2 = num2.substring(0, num2.indexOf("."));
                }
                if (!TextUtils.isDigitsOnly(num2)) {
                    num2 = num2.substring(num2.length() - 2);
                }
                return parseInt(num1) - parseInt(num2);
            }
        });
        component.resources = paths;
    }

    public static int parseInt(String val) {
        if (TextUtils.isEmpty(val)) return 0;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static Bitmap loadBitmap(Context context, String path, int width, int height) {
        InputStream in = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            in = context.getAssets().open(path);
            if (in != null) {
                BitmapFactory.decodeStream(in, null, options);
            }
            int outWidth = options.outWidth;
            int outHeight = options.outHeight;
            int sampleSize = 1;
            while (outWidth / (sampleSize * 2) > width || outHeight / (sampleSize * 2) > height) {
                sampleSize *= 2;
            }
            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;
            in = context.getAssets().open(path);
            return BitmapFactory.decodeStream(in, null, options);
        } catch (IOException | OutOfMemoryError e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
