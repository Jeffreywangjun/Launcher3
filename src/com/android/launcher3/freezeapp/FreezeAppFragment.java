package com.android.launcher3.freezeapp;

import java.util.ArrayList;
import java.util.List;

import com.android.launcher3.AppInfo;
import com.android.launcher3.R;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

public class FreezeAppFragment extends Fragment implements
		OnItemClickListener{
	private static final String TAG = "TAG";

	private final static int EXTRA_ITEM_COUNT = 1;
	private final static int ITEM_TYPE_COMMON = 0;
	private final static int ITEM_TYPE_EXTRA = 1;

	private GridView mGridView;
	private AppsAdapter mAppsAdapter;

	private Activity mActivity;

    private OnItemClickListener mOnItemClickListener;
    private ComponentName mComponentName;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.freeze_house_fragment, null);
		mGridView = (GridView) rootView.findViewById(R.id.myGrid);
		mGridView.setOnItemClickListener(this);
		mAppsAdapter = new AppsAdapter(mActivity);
		mGridView.setAdapter(mAppsAdapter);

		return rootView;
	}

	public GridView getGridView() {
		return mGridView;
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		mOnItemClickListener = listener;
	}

	interface OnItemClickListener {
		void onItemClick(AdapterView<?> parent, View itemView, int position,
						 AppInfo info);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View itemView, int position,
			long id) {
		if (mOnItemClickListener != null) {
			if (position <= EXTRA_ITEM_COUNT - 1) {
				mOnItemClickListener.onItemClick(parent, itemView, position,
						null);
			} else {
				mOnItemClickListener.onItemClick(parent, itemView, position,
						(AppInfo) mAppsAdapter.getItem(position));
			}
		}

	}

	private LayerDrawable getStaticDrawable(Drawable appIcon) {
		Drawable[] drawables = new Drawable[2];
		drawables[0] = appIcon;
		drawables[1] = getResources().getDrawable(R.drawable.icon_ice);
		LayerDrawable ld = new LayerDrawable(drawables);
		return ld;
	}

	public void changeData(ArrayList<AppInfo> apps) {
		Log.d(TAG, "changeData");
		mAppsAdapter.changeData(apps);
	}

	public class AppsAdapter extends BaseAdapter {
		private Context mContext;
		private LayoutInflater mInflater;
		private ArrayList<AppInfo> mApps;

		public AppsAdapter(Context context) {
			mContext = context;
			mInflater = LayoutInflater.from(mContext);
		}

		public void changeData(ArrayList<AppInfo> apps) {
			if (apps != null) {
                mApps = apps;
			}
			notifyDataSetChanged();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (position == 0) {
				View v = mInflater.inflate(R.layout.fp_freeze_add_app_item, null);
				return v;
			}
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.freeze_house_item,
						null);
				viewHolder.icon = (ImageView) convertView
						.findViewById(R.id.app_icon);
				viewHolder.text = (TextView) convertView
						.findViewById(R.id.app_name);
				viewHolder.over = (ImageView) convertView
						.findViewById(R.id.ice_over);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			AppInfo app = mApps.get(position - EXTRA_ITEM_COUNT);
			Drawable drawable = app.applicationInfo.loadIcon(mContext.getPackageManager());
			viewHolder.icon.setImageDrawable(drawable);
			viewHolder.text.setText(app.title);

			return convertView;
		}

		@Override
		public int getItemViewType(int position) {
			if (position == 0) {
				return ITEM_TYPE_EXTRA;
			} else {
				return ITEM_TYPE_COMMON;
			}
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		public final int getCount() {
			return (mApps == null || mApps.size() == 0 ? 0 : mApps.size())
					+ EXTRA_ITEM_COUNT;
		}

		public final Object getItem(int position) {
			return mApps.get(position - EXTRA_ITEM_COUNT);
		}

		public final long getItemId(int position) {
			return position;
		}
	}

	class ViewHolder {
		ImageView icon;
		TextView text;
		ImageView over;
	}
}
