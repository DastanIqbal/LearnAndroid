package com.function.product.cm.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.IPackageStatsObserver.Stub;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.RemoteException;
import android.util.Log;
import com.function.product.cm.bean.PhoneAppItem;
import com.function.product.cm.impl.callback.IAppInfosView;
import com.function.product.cm.impl.callback.ICacheSizeView;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import kotlin.jvm.internal.LongCompanionObject;

public class PackageUtil {
    private static PackageUtil mInstance;
    private Method appInfosCacheSizeMethod;
    private Method cleanCacheSizeMethod;
    private ArrayList<PhoneAppItem> itemList = new ArrayList();
    private IAppInfosView mAppInfosView;
    private long mCacheSize;
    private PackageManager packageManager;
    private ScanAppInfosAsyncTask scanAppInfosTask;

    class ScanAppInfosAsyncTask extends AsyncTask<Void, String, ArrayList<PhoneAppItem>> {
        ScanAppInfosAsyncTask() {
        }

        protected void onPreExecute() {
            super.onPreExecute();
            PackageUtil.this.itemList.clear();
            PackageUtil.this.mCacheSize = 0;
            PackageUtil.this.mAppInfosView.onScanStarted();
        }

        protected ArrayList<PhoneAppItem> doInBackground(Void... params) {
            Iterator it = ((ArrayList) PackageUtil.this.packageManager.getInstalledApplications(128)).iterator();
            while (it.hasNext()) {
                ApplicationInfo info = (ApplicationInfo) it.next();
                if (isCancelled()) {
                    return PackageUtil.this.itemList;
                }
                final PhoneAppItem appItem = new PhoneAppItem();
                appItem.setAppIcon(info.loadIcon(PackageUtil.this.packageManager));
                appItem.setAppName(info.loadLabel(PackageUtil.this.packageManager).toString());
                appItem.setCheck(true);
                publishProgress(new String[]{info.dataDir});
                try {
                    PackageUtil.this.appInfosCacheSizeMethod.invoke(PackageUtil.this.packageManager, new Object[]{info.packageName, new Stub() {
                        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                            PackageUtil.this.mCacheSize = PackageUtil.this.mCacheSize + pStats.cacheSize;
                            appItem.setCacheSize(pStats.cacheSize);
                        }
                    }});
                } catch (Exception e) {
                    e.printStackTrace();
                }
                PackageUtil.this.itemList.add(appItem);
            }
            return PackageUtil.this.itemList;
        }

        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (!isCancelled()) {
                PackageUtil.this.mAppInfosView.onScanProgressUpdated(PackageUtil.this.mCacheSize, values[0]);
            }
        }

        protected void onPostExecute(ArrayList<PhoneAppItem> items) {
            super.onPostExecute(items);
            PackageUtil.this.mAppInfosView.onScanCompleted(items, PackageUtil.this.mCacheSize);
        }

        protected void onCancelled(ArrayList<PhoneAppItem> phoneAppItems) {
            super.onCancelled(phoneAppItems);
            PackageUtil.this.mAppInfosView.onScanCompleted(phoneAppItems, PackageUtil.this.mCacheSize);
        }
    }

    private PackageUtil(Context context) {
        try {
            this.packageManager = context.getPackageManager();
            this.appInfosCacheSizeMethod = this.packageManager.getClass().getMethod("getPackageSizeInfo", new Class[]{String.class, IPackageStatsObserver.class});
            this.cleanCacheSizeMethod = this.packageManager.getClass().getMethod("freeStorageAndNotify", new Class[]{Long.TYPE, IPackageDataObserver.class});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PackageUtil getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PackageUtil(context);
        }
        return mInstance;
    }

    public void getAllAppInfos(IAppInfosView appInfosView) {
        this.mAppInfosView = appInfosView;
        this.scanAppInfosTask = new ScanAppInfosAsyncTask();
        this.scanAppInfosTask.execute(new Void[0]);
    }

    public void cancelScanTask() {
        if (this.scanAppInfosTask != null && !this.scanAppInfosTask.isCancelled() && this.scanAppInfosTask.getStatus() == Status.RUNNING) {
            this.scanAppInfosTask.cancel(true);
            this.scanAppInfosTask = null;
        }
    }

    public void cleanAllAppInfosCacheSize(final long scanCacheSize, final ICacheSizeView cacheSizeView) {
        new AsyncTask<Void, Integer, Long>() {
            protected void onPreExecute() {
                super.onPreExecute();
                cacheSizeView.onCleanStarted();
            }

            protected Long doInBackground(Void... params) {
                try {
                    PackageUtil.this.cleanCacheSizeMethod.invoke(PackageUtil.this.packageManager, new Object[]{Long.valueOf(LongCompanionObject.MAX_VALUE), new IPackageDataObserver.Stub() {
                        public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {
                            Log.e("TAG", "packageName => " + packageName + ",succeeded => " + succeeded);
                        }
                    }});
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Long.valueOf(scanCacheSize);
            }

            protected void onPostExecute(Long cacheSize) {
                super.onPostExecute(cacheSize);
                cacheSizeView.onCleanCompleted(cacheSize.longValue());
            }
        }.execute(new Void[0]);
    }
}
