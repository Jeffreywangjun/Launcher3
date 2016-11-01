package com.android.launcher3.freezeapp;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Build;
import android.os.Parcel;
import android.os.UserHandle;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.compat.UserHandleCompat;

public class FreezePackageManagerAdapter {
    private Context mContext;
    private PackageManager mPackageManager;
    private static FreezePackageManagerAdapter mPackageManagerAdapter;
    public static boolean isDelete = false;

    public static FreezePackageManagerAdapter getInstance(Context context) {
        if (mPackageManagerAdapter == null) {
            mPackageManagerAdapter = new FreezePackageManagerAdapter(context);
        }
        return mPackageManagerAdapter;
    }

    private FreezePackageManagerAdapter(Context context) {
        mContext = context;
        mPackageManager = mContext.getPackageManager();
    }

    public ApplicationInfo getApplicationInfo(String packageName,
                                              int flags) {
        try {
            return mPackageManager.getApplicationInfo(packageName, flags);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setApplicationEnable(String packageName) {
        isDelete = false;
        Launcher.mModel.onPackageRemoved(packageName, UserHandleCompat.fromUser(new UserHandle(Parcel.obtain())));
        Launcher.mModel.onPackageAdded(packageName, UserHandleCompat.fromUser(new UserHandle(Parcel.obtain())));
        final ContentResolver cr = mContext.getContentResolver();
        Cursor cs = cr.query(LauncherSettings.Favorites.CONTENT_URI, null, "container=1", null, null);
        int columnInde = cs.getColumnIndex("container");
        while (cs.moveToNext()) {
            String intent = cs.getString(columnInde - 1);
            if (intent.contains(packageName)) {
                cr.delete(LauncherSettings.Favorites.CONTENT_URI, "intent='" + intent + "'", null);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setApplicationDisable(String packageName) {
        isDelete = true;
        Launcher.mModel.onPackageRemoved(packageName, UserHandleCompat.fromUser(new UserHandle(Parcel.obtain())));
    }
}
