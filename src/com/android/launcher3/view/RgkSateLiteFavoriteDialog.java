package com.android.launcher3.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.android.launcher3.R;
import com.android.launcher3.utils.Pinyin;
import com.android.launcher3.utils.RgkItemAppsInfo;


public class RgkSateLiteFavoriteDialog extends RgkSateLiteDialog implements
		View.OnClickListener {

	private GridLayout mHeaderGridLayout;

	private ListView mListView;
	/**
	 * 用来给数据进行排序的集合
	 */
	private ArrayList<String> mKeys;

	private ArrayList<RgkItemAppsInfo> mList;
	/**
	 * 用来装ListView的数据
	 */
	private HashMap<String, ArrayList<RgkItemAppsInfo>> mDataList;

	private KeyAdapter mAdapter;

	private ArrayList<RgkItemAppsInfo> mApplist;
	/**
	 * 用来显示Header的数据
	 */
	private ArrayList<RgkItemAppsInfo> mHeaderDataList;
	/**
	 * 用来关闭时做比较的另一个数据集合
	 */
	private ArrayList<RgkItemAppsInfo> mFixedDataList;

	public RgkSateLiteFavoriteDialog(Context context) {
		this(context, null);
	}

	public RgkSateLiteFavoriteDialog(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RgkSateLiteFavoriteDialog(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mHeaderGridLayout = new GridLayout(context);
		mHeaderGridLayout.setColumnCount(4);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mPositiveBtn.setOnClickListener(this);
		mNegativeBtn.setOnClickListener(this);
		mDialogTitle.setText(String.format(mTitleFormat, "1", "9"));
	}

	@Override
	public View createContentView() {
		mListView = new ListView(getContext());
		mListView.addHeaderView(mHeaderGridLayout);
		return mListView;
	}

	/**
	 * 按照首字符过滤分类Apps的数据并设置给ListView的设
	 * 
	 * @param datalist
	 *            apps数据
	 */
	public void setData(ArrayList<RgkItemAppsInfo> datalist) {
		mKeys = new ArrayList<>();
		mDataList = new HashMap<>();
		mList = new ArrayList<>();

		// en cn
		// 获得手机当前的语言环境
		String language = Locale.getDefault().getLanguage();

		// 不区分大小写英文单词 一个或多个
		Pattern pattern = Pattern.compile("^[A-Za-z]+$");
		for (int i = 0; i < datalist.size(); i++) {
			RgkItemAppsInfo app = datalist.get(i);
			CharSequence title = app.mTitle;
			if (!TextUtils.isEmpty(title)) {

				// toUpperCase() 所有的英文单词转换为大写字母
				char keyChar[] = title.toString().trim().toUpperCase()
						.substring(0, 1).toCharArray();
				char key = keyChar[0];
				// 如果是数字的话
				if (Character.isDigit(key)) {

					contains(String.valueOf(key), app);
				} else if (pattern.matcher(String.valueOf(key)).matches()) {
					contains(String.valueOf(key), app);
				} else if (Locale.ENGLISH.getLanguage().equalsIgnoreCase(
						language)) {
					contains(String.valueOf(key), app);

				} else if (Locale.CHINA.getLanguage()
						.equalsIgnoreCase(language)) {
					ArrayList<Pinyin.Token> tokens = Pinyin.getInstance().get(
							app.mTitle.toString());
					if (tokens != null && tokens.size() > 0) {
						Pinyin.Token token = tokens.get(0);
						if (Pinyin.Token.PINYIN == token.type
								&& !TextUtils.isEmpty(token.target)) {
							String pinyin = token.target.trim().toUpperCase()
									.substring(0, 1).charAt(0)
									+ "";

							contains(String.valueOf(pinyin), app);
						} else {
						}
					} else {

					}
				}

			}
			Collections.sort(mKeys);

			mAdapter = new KeyAdapter(getContext(), mKeys);
			mListView.setAdapter(mAdapter);
		}
	}

	private void contains(String key, RgkItemAppsInfo app) {
		if (!mKeys.contains(key)) {
			mKeys.add(key);
			mApplist = new ArrayList<>();
			// Log.d("HHHHH", "mApplist" + mApplist.size());
			mApplist.add(app);
		} else {
			mApplist = mDataList.get(key);
			mApplist.add(app);
		}
		// 组装数据
		mDataList.put(key, mApplist);
	}

	/**
	 * Header的数据
	 * 
	 * @param appslist
	 */
	public void setHeaderData(ArrayList<RgkItemAppsInfo> appslist) {
		mHeaderDataList = new ArrayList<>();
		mFixedDataList = new ArrayList<>();

		mHeaderDataList.addAll(appslist);
		// 原始数据
		mFixedDataList.addAll(appslist);
		refreshHeader();
	}

	/**
	 * 刷新ListView Header 填充header
	 */
	public void refreshHeader() {
		mHeaderGridLayout.removeAllViews();
		if (mHeaderDataList != null && mHeaderDataList.size() > 0) {
			for (int i = 0; i < mHeaderDataList.size(); i++) {
				final GridLayoutItemView itemview = (GridLayoutItemView) LayoutInflater
						.from(getContext()).inflate(
								R.layout.rgk_grid_item_layout, null);
				itemview.setItemIcon(mHeaderDataList.get(i).mIconBitmap);
				itemview.setTag(mHeaderDataList.get(i));
				itemview.getCheckBox().setVisibility(GONE);
				itemview.setTitle(mHeaderDataList.get(i).mTitle.toString());

				// dialog的header的点击事件
				itemview.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						hideAnimation(v);
					}
				});
				mHeaderGridLayout.addView(itemview,
						new LinearLayout.LayoutParams(mSize, mSize));
			}
		}
		mDialogTitle.setText(String.format(mTitleFormat,
				String.valueOf(mHeaderDataList.size()), "9"));
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * 隐藏动画
	 * 
	 * @param view
	 */
	private void hideAnimation(final View view) {
		view.setPivotX(view.getWidth() / 2);
		view.setPivotY(view.getHeight() / 2);
		final RgkItemAppsInfo itemapp = (RgkItemAppsInfo) view.getTag();
		ValueAnimator valueAnimator = ValueAnimator.ofFloat(1.0f, 2.0f, 0f);
		AnimatorSet animator = new AnimatorSet();
		ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(view,
				"rotation", 0f, 360f);
		animator.play(valueAnimator).with(rotationAnimator);
		valueAnimator.setDuration(1000);
		valueAnimator
				.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						float values = (float) animation.getAnimatedValue();
						view.setScaleX(values);
						view.setScaleY(values);
					}
				});
		valueAnimator.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				mHeaderDataList.remove(itemapp);
				refreshHeader();
			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		});
		animator.start();
	}

	public ArrayList<RgkItemAppsInfo> getNewDataList() {
		return mHeaderDataList;
	}

	public ArrayList<RgkItemAppsInfo> getOldDataList() {
		return mFixedDataList;
	}

	// 点击事件
	@Override
	public void onClick(View v) {
		if (v == mPositiveBtn) {
			mOnDialogListener.onPositive(this);
		} else if (v == mNegativeBtn) {
			mOnDialogListener.onNegative(this);
		} else if (v instanceof GridLayoutItemView) {
			RgkItemAppsInfo itemapp = (RgkItemAppsInfo) v.getTag();
			GridLayoutItemView itemview = (GridLayoutItemView) v;
			if (itemview.getCheckBox().isChecked() == true) {
				// delete
				int index = findAppIndexInHeader(itemapp);
				if (index > -1) {
					mHeaderDataList.remove(index);
					refreshHeader();
				}

			} else {
				if (mHeaderDataList.size() < 9) {
					mHeaderDataList.add(itemapp);
					refreshHeader();
				} else {
					Toast.makeText(
							getContext(),
							getResources().getString(R.string.favorite_up_to_9),
							Toast.LENGTH_SHORT).show();
				}

			}
		}
	}

	/**
	 * 从Header中找到当前的item，返回索引Index
	 * 
	 * @param app
	 *            当前的传入的item
	 * @return 返回找到的index，找不到返回－1
	 */
	public int findAppIndexInHeader(RgkItemAppsInfo app) {
		for (int i = 0; i < mHeaderDataList.size(); i++) {
			if (mHeaderDataList.get(i).mIntent.getComponent().getClassName()
					.equals(app.mIntent.getComponent().getClassName())
					&& mHeaderDataList.get(i).mIntent
							.getComponent()
							.getPackageName()
							.equals(app.mIntent.getComponent().getPackageName())) {
				return i;
			}
		}
		return -1;
	}

	public boolean compare() {
		return compare(getContext(), getOldDataList(), getNewDataList());
	}

	// 判断两个集合的数据是否已经发生了变化
	public boolean compare(Context context, ArrayList<RgkItemAppsInfo> oldlist,
			ArrayList<RgkItemAppsInfo> newlist) {
		/**
		 * 长度相等的时候经一步比较，负责直接更新
		 */
		if (newlist.size() == oldlist.size()) {
			boolean bool = false;
			for (int i = 0; i < newlist.size(); i++) {
				if (!newlist.get(i).mIntent
						.getComponent()
						.getClassName()
						.equals(oldlist.get(i).mIntent.getComponent()
								.getClassName())) {
					bool = true;
				}
			}
			if (bool) {
				deletedListAll(context);
				addList(context, newlist);
				return true;
			}
		} else {
			// 替换
			deletedListAll(context);
			addList(context, newlist);
			return true;
		}
		return false;
	}

	/**
	 * 删除一个ItemApp List
	 * 
	 * @param context
	 * @param oldlist
	 *            需要删除的list数据
	 */
	public void deleteList(Context context, ArrayList<RgkItemAppsInfo> oldlist) {
		for (int i = 0; i < oldlist.size(); i++) {
			oldlist.get(i).delete(context);
		}
	}

	public void deletedListAll(Context context) {
		new RgkItemAppsInfo().deleteAll(context);
	}

	/**
	 * 存入app数据到数据库
	 * 
	 * @param context
	 * @param newlist
	 */
	public void addList(Context context, ArrayList<RgkItemAppsInfo> newlist) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		PackageManager packageManager = context.getPackageManager();
		ContentValues contentValues[] = new ContentValues[newlist.size()];
		for (int i = 0; i < newlist.size(); i++) {
			// 组装ContentValue
			contentValues[i] = newlist.get(i).assembleContentValues(context, i,
					intent, packageManager);
		}
		new RgkItemAppsInfo().rgkInsert(context, contentValues);
	}

	/**
	 * 带keylist的Adapter
	 */
	class KeyAdapter extends BaseAdapter {

		private Context mContext;

		private ArrayList<String> mKeys;

		private HashMap<String, ArrayList<RgkItemAppsInfo>> mLists;

		public KeyAdapter(Context context, ArrayList<String> keys) {
			mContext = context;
			mKeys = keys;
		}

		@Override
		public int getCount() {
			return mKeys.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		// 引入AppsIndexView
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.rgk_index_view_layout, null);
			}
			((AppsIndexView) convertView).setKeyString(mKeys.get(position)
					.toString());
			// 设置回调监听
			((AppsIndexView) convertView)
					.setDialogListener(RgkSateLiteFavoriteDialog.this);
			((AppsIndexView) convertView).setContent(
					mDataList.get(mKeys.get(position)), mHeaderDataList);

			return convertView;
		}
	}
}
