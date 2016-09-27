package com.android.launcher3.utils;

import java.lang.ref.WeakReference;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;

import com.android.launcher3.LauncherProvider;
import com.android.launcher3.service.RgkSateLiteService;


public class RgkApplication extends Application {

    private RgkSateLiteModel mModel;

    private RgkAppIconCache mIconCache;

    private WeakReference<LauncherProvider> mSwipeProvider;

    @Override
    public void onCreate() {
        super.onCreate();
        mIconCache = new RgkAppIconCache(this);
        mModel = new RgkSateLiteModel(this, mIconCache);

    }

    public RgkSateLiteModel setLauncher(RgkSateLiteService service) {
        mModel.initCallBack(service);
        return mModel;
    }

    public void setProvider(LauncherProvider provider) {
        mSwipeProvider = new WeakReference<>(provider);
    }

    public LauncherProvider getProvider() {
        return mSwipeProvider.get();
    }

}
