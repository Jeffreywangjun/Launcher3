package com.android.launcher3.shortcut;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.util.Log;

import com.android.launcher3.R;
import com.android.launcher3.utils.RgkItemTools;


public class RgkAudio extends RgkItemTools {

	private static RgkAudio mInstance;

	private AudioManager mAudioManager;

	int mRingerVolume;

	private RgkAudio(Context context) {
		mAudioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		mRingerVolume = mAudioManager
				.getStreamVolume(AudioManager.STREAM_SYSTEM);
	}

	// 单例模式获取实例
	public static RgkAudio getInstance(Context context) {
		if (mInstance == null) {
			synchronized (RgkAudio.class) {
				if (mInstance == null) {
					mInstance = new RgkAudio(context);
				}
			}
		}
		return mInstance;
	}

	// 修改当前状态
	public void changeState() {
		Log.d("LUOMAN", "changeState():" + getState());
		switch (getState()) {
		case AudioManager.RINGER_MODE_SILENT:
			mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
			break;
		case AudioManager.RINGER_MODE_NORMAL:
			mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			break;
		case AudioManager.RINGER_MODE_VIBRATE:
			mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			break;
		}
	}

	// 获得当前铃声状�?
	public int getState() {
		return mAudioManager.getRingerMode();
	}

	// 根据图表改变icon
	@Override
	public BitmapDrawable getDrawableState(Context context) {
		switch (getState()) {
		case AudioManager.RINGER_MODE_VIBRATE:
			return (BitmapDrawable) context.getResources().getDrawable(
					R.drawable.ic_ringer_vibrate);

		case AudioManager.RINGER_MODE_NORMAL:
			return (BitmapDrawable) context.getResources().getDrawable(
					R.drawable.ic_ringer_normal);
		case AudioManager.RINGER_MODE_SILENT:
			return (BitmapDrawable) context.getResources().getDrawable(
					R.drawable.ic_ringer_silent);
		}
		return (BitmapDrawable) context.getResources().getDrawable(
				R.drawable.ic_ringer_normal);
	}

	// 根据状�?获取
	@Override
	public String getTitleState(Context context) {
		switch (getState()) {
		case AudioManager.RINGER_MODE_VIBRATE:
			return context.getResources().getString(R.string.scene_mode_0);

		case AudioManager.RINGER_MODE_NORMAL:
			return context.getResources().getString(R.string.scene_mode_2);

		case AudioManager.RINGER_MODE_SILENT:
			return context.getResources().getString(R.string.scene_mode_1);
		}
		return "";
	}
}
