package com.android.launcher3.shortcut;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;

import com.android.launcher3.R;
import com.android.launcher3.utils.RgkItemTools;


public class RgkBluetooth extends RgkItemTools {

	BluetoothAdapter adapter = null;

	private volatile static RgkBluetooth mInstance;

	private RgkBluetooth() {
		adapter = BluetoothAdapter.getDefaultAdapter();
	}

	public static RgkBluetooth getInstance() {
		if (mInstance == null) {
			synchronized (RgkBluetooth.class) {
				if (mInstance == null) {
					mInstance = new RgkBluetooth();
				}
			}
		}
		return mInstance;
	}

	public void changeState() {
		if (available()) {
			if (adapter.getState() == BluetoothAdapter.STATE_OFF) {
				adapter.enable();
			} else if (adapter.getState() == BluetoothAdapter.STATE_ON) {
				adapter.disable();
			}
		}
	}

	public boolean getState() {
		// 判断蓝牙是否可用
		if (available()) {
			return adapter.isEnabled();
		}
		return false;
	}

	public boolean available() {
		return adapter != null;
	}

	@Override
	public BitmapDrawable getDrawableState(Context context) {
		if (available()) {
			switch (adapter.getState()) {
			case BluetoothAdapter.STATE_OFF:
				return (BitmapDrawable) context.getResources().getDrawable(
						R.drawable.ic_bluetooth_off);
			case BluetoothAdapter.STATE_ON:
				return (BitmapDrawable) context.getResources().getDrawable(
						R.drawable.ic_bluetooth_on);
			}
		}
		return (BitmapDrawable) context.getResources().getDrawable(
				R.drawable.ic_bluetooth_off);
	}

	@Override
	public String getTitleState(Context context) {
		return null;
	}
}
