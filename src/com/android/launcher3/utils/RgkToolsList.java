package com.android.launcher3.utils;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

import com.android.launcher3.R;


public class RgkToolsList {

	private static final String TAG = "RgkToolsList";

	public ArrayList<RgkItemToolsInfo> mSwipeDataList;

	public String mSwipeActionArray[];

	public String mSwipeTitleArray[];

	public RgkToolsList(Context context) {
		mSwipeDataList = new ArrayList<>();
		mSwipeActionArray = context.getResources().getStringArray(
				R.array.rgk_tools_action_array);
		mSwipeTitleArray = context.getResources().getStringArray(
				R.array.rgk_tools_title_array);
		for (int i = 0; i < mSwipeActionArray.length; i++) {
			RgkItemToolsInfo itemswitch = new RgkItemToolsInfo();
			itemswitch.mTitle = mSwipeTitleArray[i];
			itemswitch.mAction = mSwipeActionArray[i];
			mSwipeDataList.add(itemswitch);
		}
		Log.d(TAG,"RgkToolsList.size():"+mSwipeDataList.size());
	}
}
