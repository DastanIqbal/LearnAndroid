package com.function.product.cm.util;

import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Environment;
import com.function.product.cm.bean.PhoneFileItem;
import com.function.product.cm.impl.callback.ICacheSizeView;
import com.function.product.cm.impl.callback.IFileView;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class FileUtil {
    private static final String TAG = "TAG";
    private static FileUtil mInstance;
    private ArrayList<PhoneFileItem> itemList = new ArrayList();
    private long mCacheSize;
    private int mCurrentFileProgress;
    private IFileView mFileView;
    private File mSDFile = Environment.getExternalStorageDirectory();
    private ScanFileTask scanFileTask;

    class ScanFileTask extends AsyncTask<Void, String, ArrayList<PhoneFileItem>> {
        ScanFileTask() {
        }

        protected void onPreExecute() {
            super.onPreExecute();
            FileUtil.this.itemList.clear();
            FileUtil.this.mCacheSize = 0;
            FileUtil.this.mCurrentFileProgress = 0;
            FileUtil.this.mFileView.onScanStarted();
        }

        protected ArrayList<PhoneFileItem> doInBackground(Void... params) {
            return getFolderList(FileUtil.this.mSDFile);
        }

        private ArrayList<PhoneFileItem> getFolderList(File mSDFile) {
            try {
                if (mSDFile.exists() && mSDFile.isDirectory()) {
                    for (File f : mSDFile.listFiles()) {
                        if (f.isDirectory()) {
                            getFolderList(f);
                        } else if (isCancelled()) {
                            return FileUtil.this.itemList;
                        } else {
                            publishProgress(new String[]{f.getAbsolutePath()});
                            Thread.sleep(100);
                            if (!f.getName().equals("") && (f.getName().endsWith(".jpg") || f.getName().endsWith(".jpeg") || f.getName().endsWith(".png") || f.getName().endsWith(".gif") || f.getName().endsWith(".mp3") || f.getName().endsWith(".mp4") || f.getName().endsWith(".txt") || f.getName().endsWith(".log") || f.getName().endsWith(".ad") || f.getName().endsWith(".apk") || f.getName().endsWith(".zip") || f.getName().endsWith(".cache"))) {
                                FileUtil.this.mCacheSize = FileUtil.this.mCacheSize + f.length();
                                PhoneFileItem fileItem = new PhoneFileItem();
                                fileItem.setFileName(f.getName());
                                fileItem.setFilePath(f.getAbsolutePath());
                                fileItem.setFileSize(f.length());
                                fileItem.setCheck(true);
                                FileUtil.this.itemList.add(fileItem);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return FileUtil.this.itemList;
        }

        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (!isCancelled()) {
                FileUtil.this.mFileView.onScanProgressUpdated(FileUtil.this.mCurrentFileProgress = FileUtil.this.mCurrentFileProgress + 1, 1000, FileUtil.this.mCacheSize, values[0]);
            }
        }

        protected void onCancelled(ArrayList<PhoneFileItem> phoneFileItems) {
            super.onCancelled(phoneFileItems);
            FileUtil.this.mFileView.onScanCompleted(phoneFileItems, FileUtil.this.mCacheSize);
        }

        protected void onPostExecute(ArrayList<PhoneFileItem> phoneFileItems) {
            super.onPostExecute(phoneFileItems);
            FileUtil.this.mFileView.onScanCompleted(phoneFileItems, FileUtil.this.mCacheSize);
        }
    }

    private FileUtil() {
    }

    public static FileUtil getInstance() {
        if (mInstance == null) {
            mInstance = new FileUtil();
        }
        return mInstance;
    }

    public void scanPhoneFile(IFileView fileView) {
        this.mFileView = fileView;
        this.scanFileTask = new ScanFileTask();
        this.scanFileTask.execute(new Void[0]);
    }

    public void cancelScanTask() {
        if (this.scanFileTask != null && !this.scanFileTask.isCancelled() && this.scanFileTask.getStatus() == Status.RUNNING) {
            this.scanFileTask.cancel(true);
            this.scanFileTask = null;
        }
    }

    public void cleanFileCacheSize(ArrayList<PhoneFileItem> fileList, final ICacheSizeView cacheSizeView) {
        new AsyncTask<ArrayList<PhoneFileItem>, Integer, Long>() {
            protected void onPreExecute() {
                super.onPreExecute();
                cacheSizeView.onCleanStarted();
            }

            protected Long doInBackground(ArrayList<PhoneFileItem>... params) {
                long cleanCacheSize = 0;
                Iterator it = params[0].iterator();
                while (it.hasNext()) {
                    PhoneFileItem fileItem = (PhoneFileItem) it.next();
                    cleanCacheSize += fileItem.getFileSize();
                    new File(fileItem.getFilePath()).delete();
                }
                return Long.valueOf(cleanCacheSize);
            }

            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                cacheSizeView.onCleanProgressUpdate(values[0].intValue());
            }

            protected void onPostExecute(Long aLong) {
                super.onPostExecute(aLong);
                cacheSizeView.onCleanCompleted(aLong.longValue());
            }
        }.execute(new ArrayList[]{fileList});
    }
}
