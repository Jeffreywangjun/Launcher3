package com.android.launcher3.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;


public class ItemInfo {

    /**
     * item类型
     */
    int mType;
    /**
     * Item的名字
     */
    public CharSequence mTitle;

    /**
     * Item的顺序索引
     */
    int mIndex;


    public ItemInfo() {

    }

    ItemInfo(ItemInfo info) { 
        mType = info.mType;
        mTitle = info.mTitle;
        mIndex = info.mIndex;
    }

    private static String getPackageName(Intent intent) {
        if (intent != null) {
            String packageName = intent.getPackage();
            if (packageName == null && intent.getComponent() != null) {
                return intent.getComponent().getPackageName();
            }
            if (packageName != null) {
                return packageName;
            }
        }
        return "";
    }

    /**
     * 把图片保存到sqlite
     *
     * @param bitmap
     * @return
     */
    public static byte[] flattenBitmap(Bitmap bitmap) {
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
