package com.android.launcher3.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.ActivityManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.utils.OnDialogListener;
import com.android.launcher3.utils.RgkApplication;
import com.android.launcher3.utils.RgkItemAppsInfo;
import com.android.launcher3.utils.RgkItemToolsInfo;
import com.android.launcher3.utils.RgkPositionState;
import com.android.launcher3.utils.RgkSateLiteModel;
import com.android.launcher3.utils.RgkToolsBean;
import com.android.launcher3.view.AngleItemStartUp;
import com.android.launcher3.view.RgkLayout;
import com.android.launcher3.view.RgkParentLayout;
import com.android.launcher3.view.RgkTouchView;
import com.android.launcher3.view.RgkView;


public class RgkSateLiteService extends Service implements
        RgkTouchView.OnEdgeSlidingListener, RgkSateLiteModel.Callback,
        RgkView.OnClickListener, RgkLayout.OnItemDragListener, OnDialogListener {
    private Launcher mLauncher;
    RgkApplication mSwipeApplication;
    /**
     * LauncherModel广播接收机负责加载和更新数据
     */
    RgkSateLiteModel mLauncherModel;

    /**
     * 屏幕底部负责捕获手势的试图
     */
    private RgkTouchView mCatchViewLeft0;

    private RgkTouchView mCatchViewLeft1;

    private RgkTouchView mCatchViewRight0;

    private RgkTouchView mCatchViewRight1;

    private int mCatchViewHeight;

    private int mCatchViewWidth;

    private int mCatchViewBroadSize;

    private RgkParentLayout mSwipeLayout;

    private long lastClickTime = 0;

    private ChangedReceiver mReceiver;

    private MobileContentObserver mObserver;
    private MobileKeyEventReceiver mobileKeyEventReceiver;

    private Handler mHandler = new Handler();
    IBinder mBinder = new ServiceBind();

    public class ServiceBind extends Binder {

        public RgkSateLiteService getService() {
            return RgkSateLiteService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mCatchViewWidth = getResources().getDimensionPixelSize(
                R.dimen.catch_view_width);
        mCatchViewHeight = getResources().getDimensionPixelSize(
                R.dimen.catch_view_height);
        mCatchViewBroadSize = getResources().getDimensionPixelSize(
                R.dimen.catch_view_broad_size_base);
        mSwipeApplication = (RgkApplication) getApplication();

        // 初始化LauncherModel
        mLauncherModel = mSwipeApplication.setLauncher(this);
        float pre = 0.5f;
        mCatchViewLeft0 = new RgkTouchView(getBaseContext());
        mCatchViewLeft0.setOnEdgeSlidingListener(this);

        mCatchViewLeft1 = new RgkTouchView(getBaseContext());
        mCatchViewLeft1.setOnEdgeSlidingListener(this);

        mCatchViewRight0 = new RgkTouchView(getBaseContext());
        mCatchViewRight0.setOnEdgeSlidingListener(this);

        mCatchViewRight1 = new RgkTouchView(getBaseContext());
        mCatchViewRight1.setOnEdgeSlidingListener(this);

        setCatchView(pre);

        initCacthView(0);
        // 加载RgkParentLayout
        mSwipeLayout = (RgkParentLayout) LayoutInflater.from(getBaseContext())
                .inflate(R.layout.rgk_layout, null);
        /**
         * AngleView的单击监听
         */
        mSwipeLayout.getAngleLayout().getAngleView()
                .setOnAngleClickListener(this);
        /**
         * 设置FavoriteAppEditLayout关闭时的监听
         */

        mSwipeLayout.getEditFavoriteLayout().setOnDialogListener(this);

        mSwipeLayout.getEditToolsLayout().setOnDialogListener(this);

        /**
         * AngleLayout的Item拖拽事件
         */
        mSwipeLayout.getAngleLayout().setOnDragItemListener(this);
       /* if (getApplicationContext() instanceof Launcher){
            mLauncher = (Launcher) getApplicationContext();
        }
        mLauncher.setRecentApp(mLauncherModel);*/
        mReceiver = new ChangedReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mObserver = new MobileContentObserver(getApplicationContext(), mHandler);
        // 监听系统属性的变化
        getContentResolver()
                .registerContentObserver(
                        Settings.System
                                .getUriFor(Settings.System.ACCELEROMETER_ROTATION),
                        false, mObserver);
        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
                false, mObserver);
        getContentResolver()
                .registerContentObserver(
                        Settings.System
                                .getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE),
                        false, mObserver);
        //注册广播
        mobileKeyEventReceiver = new MobileKeyEventReceiver();
        final IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mobileKeyEventReceiver, homeFilter);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("RGKAPPLICATION", "onStartCommand");
        // (LUORAN)加载快捷开关和常用应用
        mLauncherModel.startLoadTask();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCatchViewLeft0.dismiss();
        mCatchViewLeft1.dismiss();

        mCatchViewRight0.dismiss();
        mCatchViewRight1.dismiss();

        unregisterReceiver(mReceiver);
        getContentResolver().unregisterContentObserver(mObserver);

    }

    /**
     * 设置CatchView 0是两边都有 1是左边 2是右边
     *
     * @param area
     */
    public void initCacthView(int area) {
        if (area == 0) {
            mCatchViewLeft0.show();
            mCatchViewLeft1.show();
            mCatchViewRight0.show();
            mCatchViewRight1.show();

        } else if (area == 1) {
            mCatchViewLeft0.show();
            mCatchViewLeft1.show();

        } else if (area == 2) {
            mCatchViewRight0.show();
            mCatchViewRight1.show();

        }
    }

    public void setCatchView(float pre) {
        mCatchViewLeft0.setState(RgkPositionState.POSITION_STATE_LEFT, 0, 0,
                (int) (mCatchViewBroadSize + mCatchViewBroadSize * pre),
                (int) (mCatchViewHeight + (mCatchViewHeight * pre)));
        mCatchViewLeft0.updata();
        mCatchViewLeft1.setState(RgkPositionState.POSITION_STATE_LEFT, 0, 0,
                (int) (mCatchViewWidth + (mCatchViewWidth * pre)),
                (int) (mCatchViewBroadSize + mCatchViewBroadSize * pre));
        mCatchViewLeft1.updata();

        mCatchViewRight0.setState(RgkPositionState.POSITION_STATE_RIGHT, 0, 0,
                (int) (mCatchViewBroadSize + mCatchViewBroadSize * pre),
                (int) (mCatchViewHeight + (mCatchViewHeight * pre)));
        mCatchViewRight0.updata();
        mCatchViewRight1.setState(RgkPositionState.POSITION_STATE_RIGHT, 0, 0,
                (int) (mCatchViewWidth + (mCatchViewWidth * pre)),
                (int) (mCatchViewBroadSize + mCatchViewBroadSize * pre));
        mCatchViewRight1.updata();

    }

    @Override
    public void openLeft() {

        mSwipeLayout.switchLeft();

    }

    @Override
    public void openRight() {

        Log.d("LUORAN10", "openRight()");
        mSwipeLayout.switchRight();

    }

    // 不断改变background的alpha值和view的缩放程度
    @Override
    public void change(float scale) {
        if (mSwipeLayout.hasView()) {
            if (mSwipeLayout.isSwipeOff()) {
                Log.d("LUORAN", "change");
                mSwipeLayout.getAngleLayout().setAngleLayoutScale(scale);
                mSwipeLayout.setSwipeBackgroundViewAlpha(scale);
            }
        }
    }

    @Override
    public void cancel(View view, boolean flag) {

        if (mSwipeLayout.isSwipeOff()) {
            int state = ((RgkTouchView) view).getState();
            if (state == RgkPositionState.POSITION_STATE_LEFT) {
                mSwipeLayout.switchLeft();
            } else if (state == RgkPositionState.POSITION_STATE_RIGHT) {
                mSwipeLayout.switchRight();
            }
            /**
             * flag==true 速度满足时自动打开 flag==flase 根据当前的sacle判断是否打开
             */
            if (flag) {

                Log.d("LUORAN12", "mSwipeLayoutkhlkhlhlkhnlknk");
                mSwipeLayout.getAngleLayout().on();
            } else {

                Log.d("LUORAN11",
                        "mSwipeLayout.getAngleLayout().switchAngleLayout()");
                mSwipeLayout.getAngleLayout().switchAngleLayout();
            }
            /**
             * 设置RecentTask数据，如果新开机之后没有数据，就拿AllApps的数据做一个补充
             */
            putRecentApps();
        }

    }

    //加载最近应用
    public void putRecentApps() {
        mSwipeLayout
                .getAngleLayout()
                .getAngleView()
                .putRecentTask(getApplicationContext(), mLauncherModel.loadRecentTask(this));
    }

    // 接口回调传递数据
    @Override
    public void bindAllApps(ArrayList<RgkItemAppsInfo> appslist) {
        // mSwipeLayout.getSwipeEditLayout().setData(appslist);
    }

    @Override
    public void bindFavorites(ArrayList<RgkItemAppsInfo> appslist) {
        mSwipeLayout.getAngleLayout().getAngleView()
                .putItemApplications(appslist);
        // mSwipeLayout.getSwipeEditLayout().setHeaderData(appslist);
    }

    @Override
    public void bindSwitch(ArrayList<RgkItemToolsInfo> switchlist) {
        Log.d("LUORAN78", "switchlist" + switchlist.size());
        mSwipeLayout.getAngleLayout().getAngleView()
                .putItemQuickSwitch(switchlist);
    }

    @Override
    public void bindFinish() {
        mSwipeLayout.getAngleLayout().getAngleView().refresh();
    }

    // item的点击事件
    @Override
    public void onAngleClick(View view) {
        Object object = view.getTag();
        AngleItemStartUp itemview = (AngleItemStartUp) view;
        if (object instanceof Intent) {
            AngleItemStartUp.RecentTag recent = itemview.mRecentTag;
            Intent intent;
            ComponentName component = recent.intent.getComponent();
            String packageName = component.getPackageName();
            PackageManager packageManager = this.getPackageManager();
            intent = packageManager.getLaunchIntentForPackage(packageName);
            if (null != intent) {
                startActivity(intent);
                mSwipeLayout.dismissAnimator();
            }
        } else if (object instanceof RgkItemAppsInfo) {
            RgkItemAppsInfo itemapp = (RgkItemAppsInfo) view.getTag();
            startActivity(itemapp.mIntent);

            mSwipeLayout.dismissAnimator();
        } else if (object instanceof RgkItemToolsInfo) {
            if (safeClick()) {
                RgkItemToolsInfo itemswitch = (RgkItemToolsInfo) view.getTag();
                RgkToolsBean.getInstance().toolsClick(this, itemview,
                        itemswitch, mSwipeLayout);
                /**
                 * 缺一不可，否则影响刷新界面
                 */
                mSwipeLayout.getAngleLayout().getAngleView().refreshToolsView();
                mSwipeLayout.getAngleLayout().getAngleView().requestLayout();
            }
        }
    }

    // 这个方法是防止连续点击事件
    public boolean safeClick() {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentTime - lastClickTime > 600) {
            lastClickTime = currentTime;
            return true;
        }
        return false;
    }

    @Override
    public void onDeleteClick(View view) {
        Object tag = view.getTag();
        Log.d("LUORAN11", "onDeleteClick");
        if (tag instanceof RgkItemAppsInfo) {

            Log.d("LUORAN11", "onDeleteClick");
            /**
             * 删除操作，删除成功之后会返回1，失败后返回-1
             */
            int index = ((RgkItemAppsInfo) tag).delete(this);
            if (index > 0) {
                /**
                 * 删除成功后更新界面
                 */
                mSwipeLayout.getAngleLayout().getAngleView().removeItem();
            }
        } else if (tag instanceof RgkItemToolsInfo) {
            int index = ((RgkItemToolsInfo) tag).delete(this);
            if (index > 0) {
                mSwipeLayout.getAngleLayout().getAngleView().removeItem();
            }
        }
    }

    @Override
    public void onAddClick(int index) {
        switch (index) {
            case 2:
                /**
                 * 点击AngleView的加号，打开编辑Favorite的窗口
                 */

                mSwipeLayout.setEditFavoritetVisiable();
                mSwipeLayout.getEditFavoriteLayout().setData(
                        mLauncherModel.getAllAppsList().data);
                mSwipeLayout.getEditFavoriteLayout().setHeaderData(
                        mLauncherModel.loadFavorite(this));

                break;
            case 1:
                /**
                 * 点击AngleView的加号，打开编辑Tools的窗口
                 */

                mSwipeLayout.setEditToolsVisiable();
                mSwipeLayout.getEditToolsLayout().setGridData(
                        mLauncherModel.getAllToolsList().mSwipeDataList);
                mSwipeLayout.getEditToolsLayout().setSelectedData(
                        mLauncherModel.loadTools(this));

                break;
        }
    }

    // 监听一些属性的变化
    class ChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                // wifi
                if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                    mSwipeLayout.getAngleLayout().getAngleView()
                            .refreshToolsView();
                    mSwipeLayout.getAngleLayout().getAngleView()
                            .requestLayout();
                    // 蓝牙
                } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    mSwipeLayout.getAngleLayout().getAngleView()
                            .refreshToolsView();
                    mSwipeLayout.getAngleLayout().getAngleView()
                            .requestLayout();
                    // 静音 震动 铃声
                } else if (action
                        .equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
                    mSwipeLayout.getAngleLayout().getAngleView()
                            .refreshToolsView();
                    mSwipeLayout.getAngleLayout().getAngleView()
                            .requestLayout();
                }
            }
        }
    }

    // 当数据库内容发生变化时候，更新一下数据
    class MobileContentObserver extends ContentObserver {

        private Context mContext;

        public MobileContentObserver(Context context, Handler handler) {
            super(handler);
            mContext = context;
        }

        public void onChange(boolean paramBoolean) {
            super.onChange(paramBoolean);
            mSwipeLayout.getAngleLayout().getAngleView().refreshToolsView();
            mSwipeLayout.getAngleLayout().getAngleView().requestLayout();
        }
    }

    @Override
    public void onDragEnd(int index) {
        // TODO Auto-generated method stub

    }

    // 对话框的确定按钮
    @Override
    public void onPositive(View view) {
        // TODO Auto-generated method stub
        if (view == mSwipeLayout.getEditFavoriteLayout()) {
            mSwipeLayout.setEditFavoriteGone();
            // 判断dialog操作以后，数据有没有发生变化
            boolean refresh = mSwipeLayout.getEditFavoriteLayout().compare();
            /**
             * refresh是在compare中对比并且更新数据之后返回的，true表示数据已经跟新过了，回来之后需要刷新
             * false表示不需要更新
             */
            if (refresh) {
                /**
                 * 重新读取数据,更新数据库
                 */
                mSwipeLayout.getAngleLayout().getAngleView()
                        .putItemApplications(mLauncherModel.loadFavorite(this));
                mSwipeLayout.getAngleLayout().getAngleView().refresh();
                /**
                 * 刷新过数据之后将编辑状态设置为STATE_NORMAL 即退出编辑状态
                 */
                mSwipeLayout.getAngleLayout().setEditState(
                        RgkLayout.STATE_NORMAL);
            }
        } else if (view == mSwipeLayout.getEditToolsLayout()) {
            mSwipeLayout.setEditToolsGone();
            boolean refresh = mSwipeLayout.getEditToolsLayout().compare();
            if (refresh) {
                mSwipeLayout.getAngleLayout().getAngleView()
                        .putItemQuickSwitch(mLauncherModel.loadTools(this));
                mSwipeLayout.getAngleLayout().getAngleView().refresh();
                /**
                 * 刷新过数据之后将编辑状态设置为STATE_NORMAL 即退出编辑状态
                 */
                mSwipeLayout.getAngleLayout().setEditState(
                        RgkLayout.STATE_NORMAL);
            }
        }
    }

    // 对话框的取消按钮
    @Override
    public void onNegative(View view) {
        // TODO Auto-generated method stub
        if (view == mSwipeLayout.getEditFavoriteLayout()) {
            mSwipeLayout.setEditFavoriteGone();
        } else if (view == mSwipeLayout.getEditToolsLayout()) {
            mSwipeLayout.setEditToolsGone();
        }
    }

    //按键监听
    class MobileKeyEventReceiver extends BroadcastReceiver {

        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";

        private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";

        private static final String SYSTEM_DIALOG_REASON_HOME = "homekey";

        private static final String SYSTEM_DIALOG_REASON_LOCK = "lock";

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (SYSTEM_DIALOG_REASON_HOME.equals(reason)) {
                    mSwipeLayout.getAngleLayout().off();
                } else if (SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
                    mSwipeLayout.getAngleLayout().off();
                } else if (SYSTEM_DIALOG_REASON_LOCK.equals(reason)) {
                    mSwipeLayout.getAngleLayout().off();
                }
            }
        }
    }


}
