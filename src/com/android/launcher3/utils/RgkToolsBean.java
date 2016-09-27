package com.android.launcher3.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.android.launcher3.R;
import com.android.launcher3.shortcut.RgkAudio;
import com.android.launcher3.shortcut.RgkBluetooth;
import com.android.launcher3.shortcut.RgkBrightnessTools;
import com.android.launcher3.shortcut.RgkFlashLight;
import com.android.launcher3.shortcut.RgkFlightMode;
import com.android.launcher3.shortcut.RgkRotation;
import com.android.launcher3.shortcut.RgkWifi;
import com.android.launcher3.view.AngleItemStartUp;
import com.android.launcher3.view.RgkItemLayout;
import com.android.launcher3.view.RgkParentLayout;

//快捷方式的实体类
public class RgkToolsBean {

	private volatile static RgkToolsBean mInstance;

	// 单例模式获取实例
	private RgkToolsBean() {
	}

	public static RgkToolsBean getInstance() {
		if (mInstance == null) {
			synchronized (RgkToolsBean.class) {
				if (mInstance == null) {
					mInstance = new RgkToolsBean();
				}
			}
		}
		return mInstance;
	}
   
	// 初始化快捷方式的icon
	public void initView(Context context, RgkItemLayout itemview,
			RgkItemToolsInfo item) {
		if (item.mAction.equals(context.getString(R.string.rgk_flash))) {
			// 手电筒
			itemview.setItemIcon(RgkFlashLight.getInstance()
					.getDrawableState(context).getBitmap());
		} else if (item.mAction.equals(context.getString(R.string.rgk_wifi))) {
			// wifi
			itemview.setItemIcon(RgkWifi.getWifiDrawableState(context)
					.getBitmap());
		} else if (item.mAction.equals(context
				.getString(R.string.rgk_flightmode))) {
			// 飞行模式
			itemview.setItemIcon(RgkFlightMode.getDrawableState(context)
					.getBitmap());
		} else if (item.mAction.equals(context.getString(R.string.rgk_mute))) {
			// 静音 震动 响铃
			itemview.setItemIcon(RgkAudio.getInstance(context)
					.getDrawableState(context).getBitmap());
			// itemview.setTitle(SwipeAudio.getInstance(context).getTitleState(context));
		} else if (item.mAction.equals(context
				.getString(R.string.rgk_autorotation))) {
			// 旋转屏幕
			itemview.setItemIcon(RgkRotation.getDrawableState(context)
					.getBitmap());
		} else if (item.mAction.equals(context.getString(R.string.rgk_setting))) {
			// 设置
			itemview.setItemIcon(((BitmapDrawable) context.getResources()
					.getDrawable(R.drawable.ic_setting_system_advanced))
					.getBitmap());
		} else if (item.mAction.equals(context
				.getString(R.string.rgk_screenbrightness))) {
			// 屏幕的亮度
			itemview.setItemIcon(RgkBrightnessTools.getInstance(context)
					.getDrawableState(context).getBitmap());
			// itemview.setTitle(SwipeBrightness.getInstance(context).getTitleState(context));
		} else if (item.mAction.equals(context
				.getString(R.string.rgk_calculator))) {
			// 计算器
			itemview.setItemIcon(((BitmapDrawable) context.getResources()
					.getDrawable(R.drawable.ic_calculator)).getBitmap());
		} else if (item.mAction.equals(context
				.getString(R.string.rgk_bluetooth))) {
			// 蓝牙
			itemview.setItemIcon(RgkBluetooth.getInstance()
					.getDrawableState(context).getBitmap());

		}

	}

	// 快捷方式的点击事件
	public void toolsClick(Context context, AngleItemStartUp itemview,
			RgkItemToolsInfo item, RgkParentLayout mSwipeLayout) {
		if (item.mAction.equals(context.getString(R.string.rgk_flash))) {
			RgkFlashLight.getInstance().onAndOff(context);
			itemview.setItemIcon(RgkFlashLight.getInstance()
					.getDrawableState(context).getBitmap());
			if (RgkFlashLight.getInstance().isOpen()) {
				toastInfo(context,
						context.getResources().getString(R.string.flash_on));

			} else {
				toastInfo(context,
						context.getResources().getString(R.string.flash_off));

			}
		} else if (item.mAction.equals(context.getString(R.string.rgk_wifi))) {
			RgkWifi.setWifiEnable(context,
					!RgkWifi.isWifiEnable(context));
		} else if (item.mAction.equals(context
				.getString(R.string.rgk_flightmode))) {
			Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
			mSwipeLayout.dismissAnimator();
		} else if (item.mAction.equals(context.getString(R.string.rgk_mute))) {
			RgkAudio.getInstance(context).changeState();
			itemview.setItemIcon(RgkAudio.getInstance(context)
					.getDrawableState(context).getBitmap());
		} else if (item.mAction.equals(context
				.getString(R.string.rgk_autorotation))) {
			if (RgkRotation.getRotationStatus(context) == 1) {
				RgkRotation
						.setRotationStatus(context.getContentResolver(), 0);
			} else {
				RgkRotation
						.setRotationStatus(context.getContentResolver(), 1);
			}
		} else if (item.mAction.equals(context.getString(R.string.rgk_setting))) {
			Intent intent = new Intent(Settings.ACTION_SETTINGS);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
			mSwipeLayout.dismissAnimator();
		} else if (item.mAction.equals(context
				.getString(R.string.rgk_screenbrightness))) {

			RgkBrightnessTools.getInstance(context).setBrightStatus(context);

		} else if (item.mAction.equals(context
				.getString(R.string.rgk_calculator))) {
			try {
				Intent intent = new Intent();
				intent.setClassName("com.android.calculator2",
						"com.android.calculator2.Calculator");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				mSwipeLayout.dismissAnimator();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (item.mAction.equals(context
				.getString(R.string.rgk_bluetooth))) {
			RgkBluetooth.getInstance().changeState();
		}
	}

	public void toastInfo(Context context, String rgkInfo) {
		Toast.makeText(context, rgkInfo, 0).show();

	}

}
