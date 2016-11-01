package com.android.launcher3.freezeapp;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.launcher3.AppInfo;
import com.android.launcher3.R;

public class FreezeNormalAppFragment extends Fragment implements OnItemClickListener {
	private static final String TAG = "FreezeNormalAppFragment";
	private Activity mActivity;

	private OnItemClickListener mOnItemClickListener;

	private GridView mGridView;
	private AppsAdapter mAppsAdapter;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = activity;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.normal_house_fragment, null);
		mGridView = (GridView) rootView.findViewById(R.id.myGrid);
		mGridView.setVisibility(View.VISIBLE);
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
		void onItemClick(AdapterView<?> parent, View itemView, AppInfo info);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View itemView, int position,
			long id) {
		if (mOnItemClickListener != null) {
			mOnItemClickListener.onItemClick(parent, itemView,
					(AppInfo) mAppsAdapter.getItem(position));
		}

	}
	
	public void changeData(ArrayList<AppInfo> apps) {
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
			if (apps != null && mApps == null) {
				mApps = apps;
			}
			notifyDataSetChanged();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.normal_house_item,
						null);
				viewHolder.icon = (ImageView) convertView
						.findViewById(R.id.app_icon);
				viewHolder.text = (TextView) convertView
						.findViewById(R.id.app_name);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			AppInfo app = mApps.get(position);
//			if(app.componentName.getPackageName().equals("com.android.launcher3")){
//			}
//			Drawable drawable = new BitmapDrawable(app.iconBitmap);
			Drawable drawable = null;
			try {
				drawable = mContext.getPackageManager().getActivityIcon(app.componentName);
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
			viewHolder.icon.setImageDrawable(drawable);
			viewHolder.icon.setScaleType(ImageView.ScaleType.FIT_XY);
			viewHolder.icon.setAdjustViewBounds(true);
			viewHolder.text.setText(app.title);

			return convertView;
		}

		public final int getCount() {
			return mApps == null ? 0 : mApps.size();
		}

		public final Object getItem(int position) {
			return mApps.get(position);
		}

		public final long getItemId(int position) {
			return position;
		}
	}

	class ViewHolder {
		ImageView icon;
		TextView text;
	}
}
