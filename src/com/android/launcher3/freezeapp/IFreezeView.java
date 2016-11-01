package com.android.launcher3.freezeapp;

import com.android.launcher3.AppInfo;

public interface IFreezeView {
	void addFreezeApp(AppInfo mAppInfo);
	void deleteFreezeApp(AppInfo mAppInfo);
}
