package com.android.launcher3.shortcut;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.android.launcher3.R;


public class RgkWifi {

	public static final int TYPE_WIFI = 1;

	public static final int TYPE_MOBILE = 2;

	public static final int TYPE_NOT_CONNECTED = 0;

	private static final String WIFI_ID = "WI-FI";

	public static boolean isWifiEnable(Context context) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		return wifiManager.isWifiEnabled();
	}

	public static void setWifiEnable(Context context, boolean enable) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(enable);
	}

	public static int getConnectivityStatus(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (null != activeNetwork) {
			if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
				return TYPE_WIFI;

			if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
				return TYPE_MOBILE;
		}
		return TYPE_NOT_CONNECTED;
	}

	public static String getWifiID(Context context) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifiManager.getConnectionInfo();
		String wifiID = info.getSSID();

		if (TextUtils.isEmpty(wifiID) || wifiID.equals("0x")
				|| wifiID.equals("<unknown ssid>")) {
			wifiID = WIFI_ID;
		}
		return wifiID;
	}

	public static BitmapDrawable getWifiDrawableState(Context context) {
		if (isWifiEnable(context)) {
			return (BitmapDrawable) context.getResources().getDrawable(
					R.drawable.ic_wifi_on);
		} else {
			return (BitmapDrawable) context.getResources().getDrawable(
					R.drawable.ic_wifi_off);
		}
	}

}
