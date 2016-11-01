package com.android.launcher3.freezeapp;

import java.net.URISyntaxException;
import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.android.launcher3.AllAppsList;
import com.android.launcher3.AppInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.ShortcutInfo;

public class FreezeModelImpl implements IFreezeModel {
    private static final String TAG = "RgkFp.FreezeModelImpl";

    private static final String URI = "content://"
            + FreezeProvider.AUTHORITIES + "/"
            + FreezeProvider.TABLE_FREEZE_APPS;

    private Context mContext;

    private ArrayList<AppInfo> freezeAppInfoList;

    public FreezeModelImpl(Context context) {
        this.mContext = context;
        freezeAppInfoList = new ArrayList<AppInfo>();
    }

    @Override
    public ArrayList<AppInfo> getFreezeApps() {
        FreezePackageManagerAdapter mAdapter = FreezePackageManagerAdapter
                .getInstance(mContext);
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri uri = Uri.parse(URI);
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        Log.i(TAG, "size0 = " + freezeAppInfoList.size());
        freezeAppInfoList.clear();
        AppInfo appInfo;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int cursorIndex = cursor
                        .getColumnIndex(FreezeProvider.FREEZE_PACKAGE_NAME);
                String packageName = cursor.getString(cursorIndex);
                String intent = cursor.getString(cursorIndex+1);
                ApplicationInfo applicationInfo = mAdapter.getApplicationInfo(
                        packageName, 0);
                if (applicationInfo != null) {
                    appInfo = new AppInfo();
                    appInfo.applicationInfo = applicationInfo;
                    appInfo.title = applicationInfo.loadLabel(mContext.getPackageManager());
                    try {
                        appInfo.intent = Intent.parseUri(intent,0);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    freezeAppInfoList.add(appInfo);
                }
            }
            cursor.close();
        }
        Log.i(TAG,"size1 = "+freezeAppInfoList.size());
        return freezeAppInfoList;
    }

    @Override
    public void deleteFreezeApp(AppInfo appInfo) {
        FreezePackageManagerAdapter mAdapter = FreezePackageManagerAdapter
                .getInstance(mContext);
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri uri = Uri.parse(URI);
        contentResolver.delete(uri, FreezeProvider.FREEZE_PACKAGE_NAME + "=?",
                new String[]{appInfo.applicationInfo.packageName});

        mAdapter.setApplicationEnable(appInfo.applicationInfo.packageName);
        freezeAppInfoList.remove(appInfo);
    }

    @Override
    public void addFreezeApp(AppInfo appInfo) {
        FreezePackageManagerAdapter mAdapter = FreezePackageManagerAdapter
                .getInstance(mContext);
        ContentResolver contentResolver = mContext.getContentResolver();
        String packageName = null;
        if(appInfo.componentName != null){
            packageName = appInfo.componentName.getPackageName();
        }else if(appInfo.applicationInfo != null){
            packageName = appInfo.applicationInfo.packageName;
        }
        Uri uri = Uri.parse(URI);
        ContentValues values = new ContentValues();
        values.put(FreezeProvider.FREEZE_PACKAGE_NAME, "" + packageName);
        Log.i("mApps","" + appInfo.intent);
        values.put("intent", "" + appInfo.intent);
        contentResolver.insert(uri, values);

        String intent = ""+appInfo.getIntent();
        if(intent.length()>5){
            Launcher.freeze.add(new ShortcutInfo(appInfo));
        }

        mAdapter.setApplicationDisable(packageName);

        appInfo.applicationInfo = mAdapter.getApplicationInfo(
                packageName, 0);
        freezeAppInfoList.add(appInfo);
    }

    @Override
    public ArrayList<AppInfo> getNormalApps() {
        AllAppsList appsList = Launcher.mModel.mBgAllAppsList;
        return appsList.data;
    }
}
