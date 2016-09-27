package com.android.launcher3.utils;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.android.launcher3.LauncherProvider;
import com.android.launcher3.Utilities;

import java.util.HashMap;

public class RgkItemAppsInfo extends ItemInfo {
    private LauncherProvider.DatabaseHelper mDatabaseHelper;
    static final String TAG = "ItemApplication";
    /**
     * app的Intent
     */
    public Intent mIntent;
    /**
     * bitmap
     */
    public Bitmap mIconBitmap;
    /**
     * app的ComonentName
     */
    public ComponentName mComponentName;

    int flags = 0;

    static final int DOWNLOADED_FLAG = 1;

    static final int UPDATED_SYSTEM_APP_FLAG = 2;

    public RgkItemAppsInfo() {
        mType = Utilities.BaseColumns.ITEM_TYPE_APPLICATION;
    }

    public RgkItemAppsInfo(RgkItemAppsInfo appinfo) {
        super(appinfo);
        mIntent = appinfo.mIntent;
        mIconBitmap = appinfo.mIconBitmap;
        mComponentName = appinfo.mComponentName;
    }

    public RgkItemAppsInfo(Resources resources, PackageManager manager,
                           ResolveInfo info, RgkAppIconCache iconcache,
                           HashMap<Object, CharSequence> lable) {
        String packageName = info.activityInfo.applicationInfo.packageName;
        mComponentName = new ComponentName(packageName, info.activityInfo.name);
        setActivity(mComponentName, Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        iconcache.getTitleAndIcon(this, info, lable);
    }

    private void setActivity(ComponentName clazzName, int flag) {
        mIntent = new Intent(Intent.ACTION_MAIN);
        mIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mIntent.setComponent(clazzName);
        mIntent.setFlags(flag);
        mType = Utilities.BaseColumns.ITEM_TYPE_APPLICATION;
    }

    public int delete(Context context) {
        ContentResolver resolver = context.getContentResolver();
        return resolver.delete(Utilities.Favorites.CONTENT_URI,
                Utilities.BaseColumns.ITEM_INTENT + "=?",
                new String[]{mIntent.toUri(0)});
    }

    public int deleteAll(Context context) {
        ContentResolver resolver = context.getContentResolver();
        return resolver
                .delete(Utilities.Favorites.CONTENT_URI,
                        Utilities.BaseColumns.ITEM_TYPE + "=?",
                        new String[]{String
                                .valueOf(Utilities.BaseColumns.ITEM_TYPE_APPLICATION)});
    }

    // 组装contentvalues
    public ContentValues assembleContentValues(Context context, int index,
                                               Intent intent, PackageManager packageManager) {
        intent.setComponent(mIntent.getComponent());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        try {
            ApplicationInfo appinfo = packageManager.getApplicationInfo(mIntent
                    .getComponent().getPackageName(), 0);
            Drawable drawable = appinfo.loadIcon(packageManager);
            BitmapDrawable bd = (BitmapDrawable) drawable;
            ContentValues values = new ContentValues();
            values.put(Utilities.BaseColumns.ITEM_TITLE, mTitle.toString());
            values.put(Utilities.BaseColumns.ITEM_INTENT, intent.toUri(0));
            values.put(Utilities.BaseColumns.ITEM_INDEX, index);
            values.put(Utilities.BaseColumns.ITEM_TYPE,
                    Utilities.BaseColumns.ITEM_TYPE_APPLICATION);
            values.put(Utilities.BaseColumns.ICON_TYPE,
                    Utilities.BaseColumns.ICON_TYPE_BITMAP);
            values.put(Utilities.BaseColumns.ICON_BITMAP,
                    flattenBitmap(bd.getBitmap()));
            return values;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void rgkInsert(Context context, ContentValues values[]) {
        mDatabaseHelper = new LauncherProvider().new DatabaseHelper(context);
        SQLiteDatabase rgkSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
        for (int i = 0; i < values.length; i++) {
            rgkSQLiteDatabase.insert(LauncherProvider.SATELITE_TABLE_FAVORITES, null, values[i]);
        }
    }

}
