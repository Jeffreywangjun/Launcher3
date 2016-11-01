/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.util.Log;

import com.android.launcher3.compat.LauncherActivityInfoCompat;
import com.android.launcher3.compat.UserHandleCompat;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.util.ComponentKey;
import com.pinyinsearch.model.PinyinSearchUnit;
import com.pinyinsearch.util.PinyinUtil;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

/**
 * Represents an app in AllAppsView.
 */
public class AppInfo extends ItemInfo {
    private static final String TAG = "Launcher3.AppInfo";

    /**
     * The intent used to start the application.
     */
    public Intent intent;

    /**
     * A bitmap version of the application icon.
     */
    public Bitmap iconBitmap;

    public ApplicationInfo applicationInfo;//A : taoqi
    /**
     * Indicates whether we're using a low res icon
     */
    boolean usingLowResIcon;

    /**
     * The time at which the app was first installed.
     */
    long firstInstallTime;

    public ComponentName componentName;

    static final int DOWNLOADED_FLAG = 1;
    static final int UPDATED_SYSTEM_APP_FLAG = 2;

    int flags = 0;

    public AppInfo() { //modify by zhaopenglin for t9
        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_SHORTCUT;
    }

//add by zhaopenglin for t9 start
    public Bitmap getIconBitmap() {
        return iconBitmap;
    }
    public String getTitle(){
        return title.toString();
    }

    public enum SearchByType{
        SearchByNull,SearchByLabel,
    }
    private PinyinSearchUnit mLabelPinyinSearchUnit;// save the mLabel converted to Pinyin characters.
    private SearchByType mSearchByType; // Used to save the type of search
    private StringBuffer mMatchKeywords;// Used to save the type of Match Keywords.(label)
    private int mMatchStartIndex;       //the match start  position of mMatchKeywords in original string(label).
    private int mMatchLength;           //the match length of mMatchKeywords in original string(name or phoneNumber).

    public String getPackageName() {
        String packageName = "";
        if (intent != null) {
            packageName = intent.getPackage();
            if (packageName == null && intent.getComponent() != null) {
                packageName = intent.getComponent().getPackageName();
            }
        }
        return packageName;
    }
//add by zhaopenglin for t9 end
    public Intent getIntent() {
        return intent;
    }

    protected Intent getRestoredIntent() {
        return null;
    }

    /**
     * Must not hold the Context.
     */
    public AppInfo(Context context, LauncherActivityInfoCompat info, UserHandleCompat user,
            IconCache iconCache) {
        this.componentName = info.getComponentName();
        this.container = ItemInfo.NO_ID;

        flags = initFlags(info);
        firstInstallTime = info.getFirstInstallTime();
        iconCache.getTitleAndIcon(this, info, false /* useLowResIcon */);
        intent = makeLaunchIntent(context, info, user);
        this.user = user;
//add by zhaopenglin for t9 start
        setLabelPinyinSearchUnit(new PinyinSearchUnit());
        setSearchByType(SearchByType.SearchByNull);
        setMatchKeywords(new StringBuffer());
        getMatchKeywords().delete(0, getMatchKeywords().length());
        setMatchStartIndex(-1);
        setMatchLength(0);
//add by zhaopenglin for t9 end
    }

    public static int initFlags(LauncherActivityInfoCompat info) {
        int appFlags = info.getApplicationInfo().flags;
        int flags = 0;
        if ((appFlags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
            flags |= DOWNLOADED_FLAG;

            if ((appFlags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                flags |= UPDATED_SYSTEM_APP_FLAG;
            }
        }
        return flags;
    }

    public AppInfo(AppInfo info) {
        super(info);
        componentName = info.componentName;
        title = Utilities.trim(info.title);
        intent = new Intent(info.intent);
        flags = info.flags;
        firstInstallTime = info.firstInstallTime;
        iconBitmap = info.iconBitmap;
    }

    @Override
    public String toString() {
        return "ApplicationInfo(title=" + title + " id=" + this.id
                + " type=" + this.itemType + " container=" + this.container
                + " screen=" + screenId + " cellX=" + cellX + " cellY=" + cellY
                + " spanX=" + spanX + " spanY=" + spanY + " dropPos=" + Arrays.toString(dropPos)
                + " user=" + user + ")";
    }

    /**
     * Helper method used for debugging.
     */
    public static void dumpApplicationInfoList(String tag, String label, ArrayList<AppInfo> list) {
        Log.d(tag, label + " size=" + list.size());
        for (AppInfo info: list) {
            Log.d(tag, "   title=\"" + info.title + "\" iconBitmap=" + info.iconBitmap 
                    + " firstInstallTime=" + info.firstInstallTime
                    + " componentName=" + info.componentName.getPackageName());
        }
    }

    public ShortcutInfo makeShortcut() {
        return new ShortcutInfo(this);
    }

    public ComponentKey toComponentKey() {
        return new ComponentKey(componentName, user);
    }

    public static Intent makeLaunchIntent(Context context, LauncherActivityInfoCompat info,
            UserHandleCompat user) {
        long serialNumber = UserManagerCompat.getInstance(context).getSerialNumberForUser(user);
        return new Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(info.getComponentName())
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            .putExtra(EXTRA_PROFILE, serialNumber);
    }
//add by zhaopenglin for t9 start
    public static Comparator<AppInfo> mSearchComparator = new Comparator<AppInfo>() {

        @Override
        public int compare(AppInfo lhs, AppInfo rhs) {
            int compareMatchStartIndex=(lhs.mMatchStartIndex-rhs.mMatchStartIndex);
            int compareMatchLength=rhs.mMatchLength-lhs.mMatchLength;

            return ((0!=compareMatchStartIndex)?(compareMatchStartIndex):((0!=compareMatchLength)?(compareMatchLength):(lhs.getTitle().length()-rhs.getTitle().length())));
        }
    };


    public PinyinSearchUnit getLabelPinyinSearchUnit() {
        return mLabelPinyinSearchUnit;
    }

    public void setLabelPinyinSearchUnit(PinyinSearchUnit labelPinyinSearchUnit) {
        mLabelPinyinSearchUnit = labelPinyinSearchUnit;
    }

    public SearchByType getSearchByType() {
        return mSearchByType;
    }

    public void setSearchByType(SearchByType searchByType) {
        mSearchByType = searchByType;
    }

    public StringBuffer getMatchKeywords() {
        return mMatchKeywords;
    }

    public void setMatchKeywords(StringBuffer matchKeywords) {
        mMatchKeywords = matchKeywords;
    }

    public void setMatchKeywords(String matchKeywords) {
        mMatchKeywords.delete(0, mMatchKeywords.length());
        mMatchKeywords.append(matchKeywords);
    }

    public void clearMatchKeywords() {
        mMatchKeywords.delete(0, mMatchKeywords.length());
    }

    public void setMatchStartIndex(int matchStartIndex) {
        mMatchStartIndex = matchStartIndex;
    }

    public void setMatchLength(int matchLength) {
        mMatchLength = matchLength;
    }
//add by zhaopenglin for t9 end
}
