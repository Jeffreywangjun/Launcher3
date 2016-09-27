package com.android.launcher3.view;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.launcher3.R;
import com.android.launcher3.utils.RgkItemAppsInfo;


public class AppsIndexView extends LinearLayout {

	private TextView mKey;

	private GridLayout mAppsGridLayout;

	private RgkSateLiteFavoriteDialog mSwipeEditFavoriteDialog;

	private int mSize;

	public AppsIndexView(Context context) {
		this(context, null);
	}

	public AppsIndexView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AppsIndexView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mSize = getResources().getDimensionPixelSize(R.dimen.angleitem_size);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mKey = (TextView) findViewById(R.id.apps_index_item_key);
		mAppsGridLayout = new GridLayout(getContext());
		mAppsGridLayout.setColumnCount(3);
		addView(mAppsGridLayout);
	}

	// 设置key(A B C D)
	public void setKeyString(String key) {
		mKey.setText(key);
	}

	public void setDialogListener(
			RgkSateLiteFavoriteDialog swipeEditFavoriteDialog) {
		mSwipeEditFavoriteDialog = swipeEditFavoriteDialog;
	}

	// 填充内容
	public void setContent(ArrayList<RgkItemAppsInfo> infos,
			ArrayList<RgkItemAppsInfo> headerlist) {
		mAppsGridLayout.removeAllViews();
		GridLayoutItemView itemview;
		for (int i = 0; i < infos.size(); i++) {
			// 加载子布局
			itemview = (GridLayoutItemView) LayoutInflater.from(getContext())
					.inflate(R.layout.rgk_grid_item_layout, null);
			// 如果header里面的数据包含的应用则直接设置为true
			if (headerlist != null) {
				if (containApp(headerlist, infos.get(i))) {
					itemview.setChecked(true);

				} else {
					itemview.setChecked(false);
				}
			}
			itemview.setItemIcon(infos.get(i).mIconBitmap);
			itemview.setTag(infos.get(i));
			itemview.setTitle(infos.get(i).mTitle.toString());

			// GridLayoutItemView的点击事件
			itemview.setOnClickListener(mSwipeEditFavoriteDialog);

			// 填充all apps数据
			mAppsGridLayout.addView(itemview, mAppsGridLayout.getChildCount(),
					new LayoutParams(mSize, mSize));
		}
	}

	public boolean containApp(ArrayList<RgkItemAppsInfo> applist,
			RgkItemAppsInfo app) {
		for (int i = 0; i < applist.size(); i++) {

			if (applist.get(i).mIntent.getComponent().getClassName()
					.equals(app.mIntent.getComponent().getClassName())
					&& applist.get(i).mIntent
							.getComponent()
							.getPackageName()
							.equals(app.mIntent.getComponent().getPackageName())) {

				return true;
			}
		}
		return false;
	}

}
