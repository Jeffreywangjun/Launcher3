package com.android.launcher3.utils;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.android.launcher3.R;


public class RgkSateLiteWindowManager {

	private WindowManager mManager;

	private WindowManager.LayoutParams mParams;

	public RgkSateLiteWindowManager(int x, int y, Context context) {

		View view = LayoutInflater.from(context).inflate(R.layout.rgk_sate_layout,
				null);
		mParams = new WindowManager.LayoutParams();
		mManager = (WindowManager) context
				.getSystemService(context.WINDOW_SERVICE);
		// mParams.type = WindowManager.LayoutParams.TYPE_PHONE;
		mParams.type = WindowManager.LayoutParams.TYPE_TOAST;
		mParams.format = PixelFormat.RGBA_8888;
		mParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_FULLSCREEN;
		mParams.gravity = Gravity.LEFT | Gravity.TOP;
		mParams.x = x;
		mParams.y = y;
		mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
		mParams.height = WindowManager.LayoutParams.MATCH_PARENT;
		//mManager.addView(view, mParams);
	}

	public boolean isManager() {
		return mManager != null;
	}

	// ��ʾview
	public void show(View view) {
		if (isManager()) {
			if (view.getParent() == null) {
				mManager.addView(view, mParams);
			}
		}
	}

	public boolean hasView(View view) {
		if (isManager()) {
			return view.getParent() != null;
		}
		return false;
	}
	public void hide(View view) {
		if (isManager()) {
			if (view.getParent() != null) {
				mManager.removeView(view);
			}
		}
	}
	public void setParams(int x, int y) {
		mParams.x = x;
		mParams.y = y;
	}

}
