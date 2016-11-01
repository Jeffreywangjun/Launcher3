package com.android.launcher3.freezeapp;

import com.android.launcher3.AppInfo;

import java.util.ArrayList;

public class FreezePresenter {
	IFreezeView view;
	IFreezeModel model;
	
	public FreezePresenter(IFreezeView view, IFreezeModel model) {
		this.view = view;
		this.model = model;
	}
	
	public ArrayList<AppInfo> getFreezeApps() {
		return model.getFreezeApps();
	}
	
	public ArrayList<AppInfo> getNormalApps() {
		return model.getNormalApps();
	}
	
	public void deleteFreezeApp(AppInfo mAppInfo) {
		model.deleteFreezeApp(mAppInfo);
		view.deleteFreezeApp(mAppInfo);
	}

	public void addFreezeApp(AppInfo mAppInfo) {
		model.addFreezeApp(mAppInfo);
		view.addFreezeApp(mAppInfo);
	}
}
