package com.android.launcher3.music;

import android.annotation.SuppressLint;

public class Tool {
	@SuppressLint("DefaultLocale")
	public String toTime(int time) {
		time /= 1000;		
		int minute = time / 60;
		minute %= 60;
		int second = time % 60;
		return String.format("%02d:%02d", minute, second);
	}
}
