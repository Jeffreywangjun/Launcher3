package com.android.launcher3.freezeapp;

import java.util.ArrayList;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.android.launcher3.AppInfo;

public class FreezeAppsLoader extends AsyncTaskLoader<ArrayList<AppInfo>> {
	private static final String TAG = "FreezeAppsLoader";
	
	private Context mContext;

	public static final int FLAG_FREEZED_APPS = 1;
	public static final int FLAG_NORMAL_APPS = 2;

	private int mFlag;
	private FreezePresenter presenter;

	public FreezeAppsLoader(Context context, int flag, FreezePresenter presenter) {
		super(context);
		if (flag != FLAG_FREEZED_APPS && flag != FLAG_NORMAL_APPS) {
			throw new IllegalArgumentException("Invalid flag: " + flag);
		}
		mFlag = flag;
		this.presenter = presenter;
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		forceLoad();
	}

	@Override
	public ArrayList<AppInfo> loadInBackground() {
		
		switch (mFlag) {
		case FLAG_FREEZED_APPS:
			Log.d(TAG, "FLAG_FREEZED_APPS");
			synchronized (TAG) {
				return presenter.getFreezeApps();
			}
			
		case FLAG_NORMAL_APPS:
			Log.d(TAG, "FLAG_NORMAL_APPS");
			synchronized (TAG) {
				return presenter.getNormalApps();
			}
		}

		return null;
	}

	@Override
	protected void onStopLoading() {
		super.onStopLoading();
		cancelLoad();
	}

	@Override
	protected void onReset() {
		super.onReset();
		cancelLoad();
	}
}
