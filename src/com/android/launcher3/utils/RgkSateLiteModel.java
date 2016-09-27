package com.android.launcher3.utils;

import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherProvider;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;


public class RgkSateLiteModel extends BroadcastReceiver {
    private LauncherProvider.DatabaseHelper rgkDataBasehelper;
    /**
     * 加载default_workspace
     */
    public LoadTask mLoadTask;
    /**
     * 应用Application注册LauncherModel
     */
    private RgkApplication mApplication;
    /**
     * 所有的app数据
     */
    private RgkAppsList mAllAppsList;
    /**
     * 所有的Tools数据
     */
    private RgkToolsList mAllToolsList;

    /**
     * 图片缓存
     */
    private RgkAppIconCache mIconCache;

    private WeakReference<Callback> mCallback;

    private Bitmap mDefaultIcon;

    private static final int NUM_BUTTONS = 8;

    private static final int MAX_RECENT_TASKS = NUM_BUTTONS * 2;
    private Intent recentAppIntent;
    private List<Intent> rgkRecentApps = new ArrayList<Intent>();
    private boolean c;

    public RgkSateLiteModel() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        rgkDataBasehelper = new LauncherProvider().new DatabaseHelper(context);
        String action = intent.getAction();
        if (Utilities.RECENT_ACTION.equals(action)) {
            String extra = intent.getExtras().getString("intent");
            try {
                //对Intent进行反序列化
                recentAppIntent = Intent.parseUri(extra, 0);
                SQLiteDatabase sqLiteDatabase = rgkDataBasehelper.getWritableDatabase();
                //如果有，删除后再添加
                if (isExistsApp(context, recentAppIntent.toUri(0))) {
                    sqLiteDatabase.delete(LauncherSettings.Favorites.SATELITE_RECENT_TABLE_NAME, "item_intent=?", new String[]{recentAppIntent.toUri(0)});
                }
                ContentValues contentValues = new ContentValues();
                contentValues.put(Utilities.BaseColumns.ITEM_INTENT, recentAppIntent.toUri(0));
                sqLiteDatabase.insert(LauncherSettings.Favorites.SATELITE_RECENT_TABLE_NAME, null, contentValues);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    //判断数据库中是否已经有相关应用
    public boolean isExistsApp(Context context, String intentAction) {
        rgkDataBasehelper = new LauncherProvider().new DatabaseHelper(context);
        SQLiteDatabase sqLiteDatabase = rgkDataBasehelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(LauncherSettings.Favorites.SATELITE_RECENT_TABLE_NAME, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String intentStr = cursor.getString(cursor.getColumnIndex(Utilities.BaseColumns.ITEM_INTENT));
            if (intentStr.equals(intentAction)) {
                return true;
            }
        }
        cursor.close();
        return false;
    }
    // 创建接口用于回调

    public interface Callback {
        // 绑定所有的app
        void bindAllApps(ArrayList<RgkItemAppsInfo> appslist);

        // 绑定常用应用
        void bindFavorites(ArrayList<RgkItemAppsInfo> appslist);

        // 绑定快捷开关
        void bindSwitch(ArrayList<RgkItemToolsInfo> switchlist);

        // 绑定结束的时候回调
        void bindFinish();
    }

    public RgkSateLiteModel(RgkApplication app, RgkAppIconCache iconCache) {
        mApplication = app;
        mAllAppsList = new RgkAppsList();
        mAllToolsList = new RgkToolsList(app);
        mIconCache = iconCache;
    }

    public void initCallBack(Callback callback) {
        mCallback = new WeakReference<>(callback);
    }

    // 开始加载数据
    public void startLoadTask() {
        mLoadTask = new LoadTask(mApplication);
        Thread thread = new Thread(mLoadTask);
        thread.start();
    }

    public RgkAppsList getAllAppsList() {
        return mAllAppsList;
    }

    public RgkToolsList getAllToolsList() {
        return mAllToolsList;
    }

    public Bitmap getFallbackIcon() {
        return Bitmap.createBitmap(mDefaultIcon);
    }

    // 从数据库中读取图片
    public Bitmap getIconFromCursor(Cursor c, int iconIndex, Context context) {
        @SuppressWarnings("all")
        // suppress dead code warning
        final boolean debug = false;
        byte[] data = c.getBlob(iconIndex);
        try {
            return BitmapFactory.decodeByteArray(data, 0, data.length);

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 点击加号弹出对话框内容
     *
     * @param context
     * @return
     */
    public ArrayList<RgkItemAppsInfo> loadFavorite(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver
                .query(Utilities.Favorites.CONTENT_URI,
                        null,
                        Utilities.BaseColumns.ITEM_TYPE + "=?",
                        new String[]{String
                                .valueOf(Utilities.BaseColumns.ITEM_TYPE_APPLICATION)},
                        null);
        ArrayList<RgkItemAppsInfo> favorites = new ArrayList<>();
        while (cursor.moveToNext()) {
            int type = cursor.getInt(cursor
                    .getColumnIndex(Utilities.BaseColumns.ITEM_TYPE));
            String title = cursor.getString(cursor
                    .getColumnIndex(Utilities.BaseColumns.ITEM_TITLE));
            String intentStr = cursor.getString(cursor
                    .getColumnIndex(Utilities.BaseColumns.ITEM_INTENT));
            int iconType = cursor.getInt(cursor
                    .getColumnIndex(Utilities.BaseColumns.ICON_TYPE));
            int iconIndex = cursor
                    .getColumnIndex(Utilities.BaseColumns.ICON_BITMAP);
            Intent intent = null;
            Bitmap icon = null;
            // 对intent进行反序列化
            try {
                intent = Intent.parseUri(intentStr, 0);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            RgkItemAppsInfo application = new RgkItemAppsInfo();
            application.mType = type;
            application.mTitle = title;
            application.mIntent = intent;

            switch (iconType) {

                case Utilities.BaseColumns.ICON_TYPE_BITMAP:
                    icon = getIconFromCursor(cursor, iconIndex, context);
                    break;
                default:
                    break;
            }
            application.mIconBitmap = icon;

            favorites.add(application);
        }
        cursor.close();
        return favorites;
    }

    // 点击加号弹出对话框内容
    public ArrayList<RgkItemToolsInfo> loadTools(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Utilities.Favorites.CONTENT_URI,
                null, Utilities.BaseColumns.ITEM_TYPE + "=?",
                new String[]{String
                        .valueOf(Utilities.BaseColumns.ITEM_TYPE_SWITCH)},
                null);
        ArrayList<RgkItemToolsInfo> switches = new ArrayList<>();
        while (cursor.moveToNext()) {
            RgkItemToolsInfo application = new RgkItemToolsInfo();
            application.mType = cursor.getInt(cursor
                    .getColumnIndex(Utilities.BaseColumns.ITEM_TYPE));
            application.mTitle = cursor.getString(cursor
                    .getColumnIndex(Utilities.BaseColumns.ITEM_TITLE));
            application.mAction = cursor.getString(cursor
                    .getColumnIndex(Utilities.BaseColumns.ITEM_ACTION));
            application.mTitle = cursor.getString(cursor
                    .getColumnIndex(Utilities.BaseColumns.ITEM_TITLE));
            switches.add(application);
        }
        cursor.close();
        return switches;
    }

    //加载最近应用
    public List<Intent> loadRecentTask(Context context) {
        List<Intent> rgkRecentAppIntent = new ArrayList<>();
        rgkDataBasehelper = new LauncherProvider().new DatabaseHelper(context);
        SQLiteDatabase sqLiteDatabase = rgkDataBasehelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(LauncherSettings.Favorites.SATELITE_RECENT_TABLE_NAME, null, null, null, null, null, null);
        if (rgkRecentAppIntent.size() != 0) {
            rgkRecentAppIntent.clear();
        }
        while (cursor.moveToNext()) {
            String rgkIntent = cursor.getString(cursor.getColumnIndex(Utilities.BaseColumns.ITEM_INTENT));
            try {
                if (rgkIntent != null) {
                    rgkRecentAppIntent.add(Intent.parseUri(rgkIntent, 0));
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

        }
        return rgkRecentAppIntent;
    }

    public static ComponentName getComponentNameFromResolveInfo(ResolveInfo info) {
        if (info.activityInfo != null) {
            return new ComponentName(info.activityInfo.packageName,
                    info.activityInfo.name);
        } else {
            return new ComponentName(info.serviceInfo.packageName,
                    info.serviceInfo.name);
        }

    }

    // 开启异步加载内容
    private class LoadTask implements Runnable {

        private Context mContext;

        private HashMap<Object, CharSequence> mLabelCache;

        public LoadTask(Context context) {
            mContext = context;
            mLabelCache = new HashMap<>();
        }

        @Override
        public void run() {
            // 解析数据存入数据库
            loadDefaultWorkspace();
            bindFavorites();
            bindSwitch();
            bindFinish();
            bindAllApps();
        }

        // 解析xml文件
        private void loadDefaultWorkspace() {
            mApplication.getProvider().loadDefaultFavoritesIfNecessary(
                    R.xml.default_workspace);
        }

        /**
         * 加载设备上的app数据
         */
        private void bindAllApps() {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            PackageManager manager = mContext.getPackageManager();
            List<ResolveInfo> mInfoLists = manager.queryIntentActivities(
                    mainIntent, 0);
            if (mAllAppsList.data.size() > 0) {
                mAllAppsList.data.clear();
            }
            for (int i = 0; i < mInfoLists.size(); i++) {
                mAllAppsList.data.add(new RgkItemAppsInfo(mContext
                        .getResources(), manager, mInfoLists.get(i),
                        mIconCache, mLabelCache));
            }

        }

        /**
         * 从表中读出数据传到Service(回调)
         */
        public void bindFavorites() {
            ContentResolver resolver = mContext.getContentResolver();
            Cursor cursor = resolver
                    .query(Utilities.Favorites.CONTENT_URI,
                            null,
                            Utilities.BaseColumns.ITEM_TYPE + "=?",
                            new String[]{String
                                    .valueOf(Utilities.BaseColumns.ITEM_TYPE_APPLICATION)},
                            null);
            ArrayList<RgkItemAppsInfo> favorites = new ArrayList<>();
            Intent intent = null;
            Bitmap icon = null;
            while (cursor.moveToNext()) {
                int type = cursor.getInt(cursor
                        .getColumnIndex(Utilities.BaseColumns.ITEM_TYPE));
                String title = cursor.getString(cursor
                        .getColumnIndex(Utilities.BaseColumns.ITEM_TITLE));
                String intentStr = cursor.getString(cursor
                        .getColumnIndex(Utilities.BaseColumns.ITEM_INTENT));
                int iconType = cursor.getInt(cursor
                        .getColumnIndex(Utilities.BaseColumns.ICON_TYPE));

                // 获得列标
                int iconIndex = cursor
                        .getColumnIndex(Utilities.BaseColumns.ICON_BITMAP);
                try {
                    intent = Intent.parseUri(intentStr, 0);
                } catch (URISyntaxException e) {

                }
                RgkItemAppsInfo application = new RgkItemAppsInfo();
                application.mType = type;
                application.mTitle = title;
                application.mIntent = intent;
                switch (iconType) {

                    case Utilities.BaseColumns.ICON_TYPE_BITMAP:
                        // 从数据库中读取图片
                        icon = getIconFromCursor(cursor, iconIndex, mContext);
                        break;
                    default:
                        break;
                }
                application.mIconBitmap = icon;
                favorites.add(application);
            }
            cursor.close();
            mCallback.get().bindFavorites(favorites);
        }

        /**
         * 查询并绑定开关数据
         */
        private void bindSwitch() {
            ContentResolver resolver = mContext.getContentResolver();
            Cursor cursor = resolver
                    .query(Utilities.Favorites.CONTENT_URI,
                            null,
                            Utilities.BaseColumns.ITEM_TYPE + "=?",
                            new String[]{String
                                    .valueOf(Utilities.BaseColumns.ITEM_TYPE_SWITCH)},
                            null);
            ArrayList<RgkItemToolsInfo> switches = new ArrayList<>();
            Log.d("LUORAN78", "cursor.getCount():" + cursor.getCount());
            while (cursor.moveToNext()) {
                RgkItemToolsInfo application = new RgkItemToolsInfo();
                application.mType = cursor.getInt(cursor
                        .getColumnIndex(Utilities.BaseColumns.ITEM_TYPE));
                application.mTitle = cursor.getString(cursor
                        .getColumnIndex(Utilities.BaseColumns.ITEM_TITLE));
                application.mAction = cursor.getString(cursor
                        .getColumnIndex(Utilities.BaseColumns.ITEM_ACTION));
                switches.add(application);
            }
            cursor.close();
            mCallback.get().bindSwitch(switches);
        }

        private void bindFinish() {
            mCallback.get().bindFinish();
        }

    }

}
