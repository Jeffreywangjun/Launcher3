package com.android.launcher3.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

import com.android.launcher3.LauncherProvider;
import com.android.launcher3.Utilities;

public class RgkItemToolsInfo extends ItemInfo {
    private LauncherProvider.DatabaseHelper mDatabaseHelper;
    /**
     * 快捷开关的action
     */
    public String mAction;

    public Bitmap mDefaultIcon;

    public boolean isChecked;

    public RgkItemToolsInfo() {
        mType = Utilities.BaseColumns.ITEM_TYPE_SWITCH;
    }

    public RgkItemToolsInfo(RgkItemToolsInfo switchitem) {
        super(switchitem);
        mAction = switchitem.mAction;
        mDefaultIcon = switchitem.mDefaultIcon;
    }

    public int delete(Context context) {
        ContentResolver resolver = context.getContentResolver();
        return resolver.delete(Utilities.Favorites.CONTENT_URI,
                Utilities.BaseColumns.ITEM_ACTION + "=?",
                new String[]{mAction});
    }

    public int deletedAll(Context context) {
        ContentResolver resolver = context.getContentResolver();
        return resolver.delete(Utilities.Favorites.CONTENT_URI,
                Utilities.BaseColumns.ITEM_TYPE + "=?", new String[]{String
                        .valueOf(Utilities.BaseColumns.ITEM_TYPE_SWITCH)});
    }

    public void insert(Context context, int index) {
        ContentResolver resolver = context.getContentResolver();
        resolver.insert(Utilities.Favorites.CONTENT_URI,
                assembleContentValues(context, index));
    }

    // 组装数据
    public ContentValues assembleContentValues(Context context, int index) {
        ContentValues values = new ContentValues();
        values.put(Utilities.BaseColumns.ITEM_TITLE, mTitle.toString());
        values.put(Utilities.BaseColumns.ITEM_INDEX, index);
        values.put(Utilities.BaseColumns.ITEM_TYPE,
                Utilities.BaseColumns.ITEM_TYPE_SWITCH);
        values.put(Utilities.BaseColumns.ITEM_ACTION, mAction);
        return values;
    }

    // 插入数据
    public void bulkInsert(Context context, ContentValues values[]) {
        mDatabaseHelper=new LauncherProvider().new DatabaseHelper(context);
        SQLiteDatabase rgkSQLiteDatabase=mDatabaseHelper.getWritableDatabase();
        for(int i=0;i<values.length;i++){
            rgkSQLiteDatabase.insert(LauncherProvider.SATELITE_TABLE_FAVORITES,null,values[i]);
        }
    }
}
