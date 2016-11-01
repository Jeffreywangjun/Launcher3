package com.android.launcher3.freezeapp;

import com.android.launcher3.AppInfo;

import java.util.ArrayList;

public interface IFreezeModel {
	ArrayList<AppInfo> getFreezeApps();
	ArrayList<AppInfo> getNormalApps();
	void deleteFreezeApp( AppInfo info);
	void addFreezeApp( AppInfo info);
	
}
