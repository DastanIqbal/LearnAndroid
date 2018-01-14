package com.function.product.cm.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.util.Log;
import com.function.product.cm.bean.ProcessAppItem;
import com.function.product.cm.impl.callback.ICacheSizeView;
import com.function.product.cm.impl.callback.IProcessView;
import java.util.ArrayList;
import java.util.Iterator;

public class ProcessUtil {
    private static ProcessUtil mInstance;
    private ActivityManager activityManager;
    private ArrayList<ProcessAppItem> itemList = new ArrayList();
    private long mCacheSize;
    private IProcessView mProcessView;
    private PackageManager packageManager;
    private ScanProcessAsyncTask scanProcessTask;

    class ScanProcessAsyncTask extends AsyncTask<Void, String, ArrayList<ProcessAppItem>> {
        ScanProcessAsyncTask() {
        }

        protected void onPreExecute() {
            super.onPreExecute();
            ProcessUtil.this.itemList.clear();
            ProcessUtil.this.mCacheSize = 0;
            ProcessUtil.this.mProcessView.onScanStarted();
        }

        protected ArrayList<ProcessAppItem> doInBackground(Void... params) {
            Iterator it = ((ArrayList) ProcessUtil.this.activityManager.getRunningAppProcesses()).iterator();
            while (it.hasNext()) {
                RunningAppProcessInfo info = (RunningAppProcessInfo) it.next();
                if (isCancelled()) {
                    return ProcessUtil.this.itemList;
                }
                try {
                    ProcessAppItem processItem = new ProcessAppItem();
                    Log.e("TAG", "processName => " + ((String) ProcessUtil.this.packageManager.getApplicationInfo(info.processName, 0).loadLabel(ProcessUtil.this.packageManager)));
                    int i = info.pid;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return ProcessUtil.this.itemList;
        }

        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (!isCancelled()) {
                ProcessUtil.this.mProcessView.onScanProgressUpdated(ProcessUtil.this.mCacheSize, values[0]);
            }
        }

        protected void onPostExecute(ArrayList<ProcessAppItem> items) {
            super.onPostExecute(items);
            ProcessUtil.this.mProcessView.onScanCompleted(items, ProcessUtil.this.mCacheSize);
        }

        protected void onCancelled(ArrayList<ProcessAppItem> phoneAppItems) {
            super.onCancelled(phoneAppItems);
            ProcessUtil.this.mProcessView.onScanCompleted(phoneAppItems, ProcessUtil.this.mCacheSize);
        }
    }

    private ProcessUtil(Context context) {
        this.packageManager = context.getPackageManager();
        this.activityManager = (ActivityManager) context.getSystemService("activity");
    }

    public static ProcessUtil getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ProcessUtil(context);
        }
        return mInstance;
    }

    public void scanBackgroundProcesses(IProcessView processView) {
        this.mProcessView = processView;
        this.scanProcessTask = new ScanProcessAsyncTask();
        this.scanProcessTask.execute(new Void[0]);
    }

    public void cancelScanTask() {
        if (this.scanProcessTask != null && !this.scanProcessTask.isCancelled() && this.scanProcessTask.getStatus() == Status.RUNNING) {
            this.scanProcessTask.cancel(true);
            this.scanProcessTask = null;
        }
    }

    public void boostBackgroundProcesses(ArrayList<ProcessAppItem> processList, final ICacheSizeView cacheSizeView) {
        new AsyncTask<ArrayList<ProcessAppItem>, Integer, Long>() {
            protected void onPreExecute() {
                super.onPreExecute();
                cacheSizeView.onCleanStarted();
            }

            protected Long doInBackground(ArrayList<ProcessAppItem>... params) {
                long boostSize = 0;
                Iterator it = params[0].iterator();
                while (it.hasNext()) {
                    ProcessAppItem processItem = (ProcessAppItem) it.next();
                    boostSize += processItem.getCacheSize();
                    ProcessUtil.this.activityManager.killBackgroundProcesses(processItem.getPackageName());
                }
                return Long.valueOf(boostSize);
            }

            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                cacheSizeView.onCleanProgressUpdate(values[0].intValue());
            }

            protected void onPostExecute(Long boostSize) {
                super.onPostExecute(boostSize);
                cacheSizeView.onCleanCompleted(boostSize.longValue());
            }
        }.execute(new ArrayList[]{processList});
    }
}
