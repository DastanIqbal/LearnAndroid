package com.flow.framework;

import android.app.Application;
import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration.Builder;
import com.flow.framework.model.AppModel;
import com.flow.framework.model.OfferResultModel;
import com.flow.framework.model.TrackUrlModel;
import com.flow.framework.model.UserModel;
import com.flow.framework.util.plog;

public class PApp extends Application {
    public static PApp mInstance;

    public void onCreate() {
        super.onCreate();
        init();
    }

    public void onTerminate() {
        super.onTerminate();
        terminate();
    }

    private void init() {
        mInstance = this;
        plog.init();
        initializeDB();
        CrashHandler.getInstance().init();
    }

    private void terminate() {
        ActiveAndroid.dispose();
    }

    protected void initializeDB() {
        Builder configurationBuilder = new Builder(this);
        configurationBuilder.setDatabaseName("flow_db");
        configurationBuilder.addModelClass(OfferResultModel.class).addModelClass(TrackUrlModel.class).addModelClass(UserModel.class).addModelClass(AppModel.class);
        ActiveAndroid.initialize(configurationBuilder.create());
    }
}
