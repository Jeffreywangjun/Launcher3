package com.android.launcher3.utils;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;


public abstract class RgkItemTools {
    /**
     * 根据状态获取icon
     *
     * @param context
     * @return
     */
    public abstract BitmapDrawable getDrawableState(Context context);

    /**
     * 根据状态获取title
     *
     * @param context
     * @return
     */
    public abstract String getTitleState(Context context);
}
