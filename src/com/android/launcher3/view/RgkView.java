package com.android.launcher3.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.utils.RgkItemAppsInfo;
import com.android.launcher3.utils.RgkItemToolsInfo;
import com.android.launcher3.utils.RgkToolsBean;


public class RgkView extends PositionStateView {

    // 旋转的基数角度

    private float mBaseAngle = 0;

    // 跟随手指转动的是的变量

    private float mChangeAngle = 0;

    // 按下时的角度

    private double mDownAngle;

    // 转轴PivotX,PivotY

    private int mPivotX = 0;

    private int mPivotY = 0;

    // 控件的宽高

    private int mHeight;

    private int mWidth;

    // 用于计算单击时间的X&Y

    private float mMotionX;

    private float mMotionY;

    // 单击事件的第一个时间

    private long mClickTime1;

    // 用来区分点击事件的类型

    private int mClickType = -1;

    private static final int TYPE_CLICK = 0;

    private static final int TYPE_DELCLICK = 1;

    private static final int TYPE_ADDCLICK = 2;

    private static final int TYPE_RECENT = 0;

    private static final int TYPE_TOOLS = 1;

    private static final int TYPE_FAVORITE = 2;
    // AngleView点击时候找到的ItemView
    RgkItemLayout mTargetItem;

    // 用于点击或者长按的时候计算得到的当前点击的Item的四个坐标

    private float mDownLeft;

    private float mDownTop;

    private float mDownRight;

    private float mDownBottom;

    // 顺时针/逆时针

    private int ANGLE_STATE = ANGLE_STATE_REST;

    public static final int ANGLE_STATE_REST = 0;

    public static final int ANGLE_STATE_ALONG = 1;

    public static final int ANGLE_STATE_INVERSE = 2;

    public static final int DEGREES_360 = 360;

    public static final int DEGREES_1080 = DEGREES_360 * 3;

    private int mChildHalfSize;

    public int mInnerRadius;

    public int mOuterRadius;

    public int mDeleteBtnSize;
    /**
     * 单位度数
     */
    public static final int DEGREES_90 = 90;
    /**
     * 判定范围
     */
    public static final int DEGREES_OFFSET = 15;
    /**
     * 判定Fling动作的速度范围
     */
    private static final int ALLOW_FLING = 2500;

    private static final int COUNT_4 = 4;

    private static final int COUNT_3 = 3;

    private static final int COUNT_12 = COUNT_4 * COUNT_3;

    private static final String TAG = "RgkView";

    private int mCurrentIndex;

    private ValueAnimator mAngleAnimator;

    private Map<Integer, ArrayList<RgkItemLayout>> mMap = new HashMap<>();

    private ArrayList<RgkItemLayout> mRecentAppList = new ArrayList<>();

    private ArrayList<RgkItemLayout> mSwitchList = new ArrayList<>();

    private ArrayList<RgkItemLayout> mFavoriteAppList = new ArrayList<>();

    /**
     * 删除前临时保存数据
     */
    private ArrayList<RgkItemLayout> mDelPre = new ArrayList<>();

    /**
     * 删除后临时保存数据
     */
    private ArrayList<Coordinate> mDelNext = new ArrayList<>();

    /**
     * 交换前
     */

    // 判断移除item是否已经结束
    private boolean isRemoveFinish = true;

    private OnAngleChangeListener mAngleListener;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    /**
     * 长按处理
     */
    private LongClickRunable mLongRunable = new LongClickRunable();
    /**
     * 震动
     */
    private Vibrator mVibrator;

    public interface OnAngleChangeListener {

        /**
         * 角度发生变化时传递当前的所显示的数据索引值&当前的百分比 用于改变Indicator的选中状态，百分比则用来渲染过渡效果
         *
         * @param cur 正在显示的是数据index
         * @param p   百分比
         */
        void onAngleChanged(int cur, float p);

    }

    OnClickListener mOnClickListener;

    public interface OnClickListener {
        /**
         * 本身的OnClick无效，所以自己根据在onTouch中定义一个Onclick
         */
        void onAngleClick(View view);

        /**
         * 位于布局左上角的删除按钮，点击的时候直接在控件AngleView内部处理了数据
         *
         * @param view
         */
        void onDeleteClick(View view);

        /**
         * 在每一个扇区最后有一个添加按钮，扇区满的时候这个按钮没有显示出来， 在扇区显示的时候，点击添加按钮，吧参数回调到SwipLayout
         * 然后添加新的数据进来
         */
        void onAddClick(int index);
    }

    OnEditModeChangeListener mOnEditModeChangeListener;

    public interface OnEditModeChangeListener {
        /**
         * 进入编辑模式
         *
         * @param view
         */
        void onEnterEditMode(View view);

        /**
         * 退出编辑模式
         */
        void onExitEditMode();

    }

    /**
     * 临时坐标信息
     */
    class Coordinate {

        public float x;

        public float y;

        public Coordinate(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public RgkView(Context context) {
        this(context, null);
    }

    public RgkView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RgkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mChildHalfSize = getResources().getDimensionPixelSize(
                R.dimen.angleitem_half_size);
        mInnerRadius = getResources().getDimensionPixelSize(
                R.dimen.angleview_inner_radius);
        mOuterRadius = getResources().getDimensionPixelSize(
                R.dimen.angleview_outer_radius);
        mDeleteBtnSize = getResources().getDimensionPixelSize(
                R.dimen.angleview_item_delete_size);

        mMap.put(TYPE_RECENT, mRecentAppList);
        mMap.put(TYPE_TOOLS, mSwitchList);
        mMap.put(TYPE_FAVORITE, mFavoriteAppList);

    }

    /**
     * 刷新AngleView数据
     */
    public void refresh() {
        removeAllViews();

        for (Map.Entry<Integer, ArrayList<RgkItemLayout>> entrySet : mMap
                .entrySet()) {
            ArrayList<RgkItemLayout> views = entrySet.getValue();
            for (View view : views) {
                if (view.getParent() == null) {
                    addView(view);
                }
            }

        }
        // 重新确定各个view的位置,重新调用onMeasure方法和onLayout
        requestLayout();
    }

    // 加载app数据到map集合里面
    public void putItemApplications(ArrayList<RgkItemAppsInfo> itemlist) {
        if (mMap.get(TYPE_FAVORITE).size() > 0) {
            mMap.get(TYPE_FAVORITE).clear();
        }
        AngleItemStartUp itemview;
        for (RgkItemAppsInfo appitem : itemlist) {
            itemview = (AngleItemStartUp) LayoutInflater.from(getContext())
                    .inflate(R.layout.angle_item_startup, null);
            itemview.setTitle(appitem.mTitle.toString());
            itemview.setItemIcon(appitem.mIconBitmap);
            itemview.setTag(appitem);
            mMap.get(TYPE_FAVORITE).add(itemview);
        }
        /**
         * 添加额外的add按钮
         */
        RgkAddItemInfo targetItem = (RgkAddItemInfo) LayoutInflater.from(
                getContext()).inflate(R.layout.angle_item_addto, null);
        mMap.get(TYPE_FAVORITE).add(targetItem);
    }

    /**
     * 从Tag中拿出所有的Apps数据
     *
     * @return
     */
    public ArrayList<RgkItemAppsInfo> getItemApplications() {
        if (getViewsIndex() == 2) {
            ArrayList<RgkItemLayout> views = getData();
            if (views != null) {
                ArrayList<RgkItemAppsInfo> itemApplications = new ArrayList<>();
                for (int i = 0; i < views.size() - 1; i++) {
                    itemApplications.add((RgkItemAppsInfo) views.get(i)
                            .getTag());
                }
                return itemApplications;
            }
        }
        return null;
    }

    // 初始化快捷方式的icon和title并加载tools数据到map集合里面
    public void putItemQuickSwitch(ArrayList<RgkItemToolsInfo> itemlist) {
        if (mMap.get(TYPE_TOOLS).size() > 0) {
            mMap.get(TYPE_TOOLS).clear();
        }

        AngleItemStartUp itemview;
        for (RgkItemToolsInfo appitem : itemlist) {
            itemview = (AngleItemStartUp) LayoutInflater.from(getContext())
                    .inflate(R.layout.angle_item_startup, null);
            itemview.setTitle(appitem.mTitle.toString());

            // 加载tools的icon
            RgkToolsBean.getInstance()
                    .initView(getContext(), itemview, appitem);

            // 设置tag值，防止图标乱序
            itemview.setTag(appitem);
            mMap.get(TYPE_TOOLS).add(itemview);
        }
        /**
         * 添加额外的add按钮
         */
        RgkAddItemInfo targetItem = (RgkAddItemInfo) LayoutInflater.from(
                getContext()).inflate(R.layout.angle_item_addto, null);
        mMap.get(TYPE_TOOLS).add(targetItem);
    }

    /**
     * 从Tag中拿出所有的Tools数据
     *
     * @return
     */
    public ArrayList<RgkItemToolsInfo> getToolsArrayList() {
        if (getViewsIndex() == 1) {
            ArrayList<RgkItemLayout> views = getData();
            if (views != null) {
                ArrayList<RgkItemToolsInfo> toolsArrayList = new ArrayList<>();
                for (int i = 0; i < views.size() - 1; i++) {
                    toolsArrayList
                            .add((RgkItemToolsInfo) views.get(i).getTag());
                }
                return toolsArrayList;
            }
        }
        return null;
    }

    // 当系统属性发生变化的时候，刷新当前显示的数据
    public void refreshToolsView() {
        int index = getViewsIndex();
        for (int i = 0; i < mMap.get(index).size(); i++) {
            RgkItemLayout itemview = mMap.get(index).get(i);
            if (itemview instanceof AngleItemStartUp
                    && itemview.getTag() instanceof RgkItemToolsInfo) {
                RgkItemToolsInfo item = (RgkItemToolsInfo) itemview.getTag();
                RgkToolsBean.getInstance().initView(getContext(), itemview,
                        item);
            }
        }
    }

    /**
     * 设置RecentTask数据
     *
     * @param activityIntentList
     */
    public void putRecentTask(Context context,
                              List<Intent> activityIntentList) {
        if (mRecentAppList != null) {
            mRecentAppList.clear();
        }
        List<Intent> retentApps = new ArrayList<>();
        for (int i = 0; i < activityIntentList.size(); i++) {
            retentApps.add(0, activityIntentList.get(i));
        }
        for (int i = 0; i < retentApps.size(); i++) {
            Intent intent = retentApps.get(i);
            ActivityInfo activityInfo = intent.resolveActivityInfo(context.getPackageManager(), 0);
            String title = activityInfo.loadLabel(context.getPackageManager()).toString();
            Drawable icon = activityInfo.loadIcon(context.getPackageManager());
            BitmapDrawable bd = (BitmapDrawable) icon;
            if (title != null && title.length() > 0 && icon != null) {
                AngleItemStartUp itemview = (AngleItemStartUp) LayoutInflater
                        .from(getContext()).inflate(
                                R.layout.angle_item_startup, null);
                itemview.setTag(retentApps.get(i));
                AngleItemStartUp.RecentTag recenttag = new AngleItemStartUp.RecentTag();
                recenttag.info = activityInfo;
                recenttag.intent = intent;
                itemview.mRecentTag = recenttag;
                itemview.setTitle(title);
                itemview.setItemIcon(bd.getBitmap());
                if (!isAppExists(itemview)) {
                    mRecentAppList.add(itemview);
                }
            }
        }
        refresh();
    }

    private boolean isAppExists(RgkItemLayout itemLayout) {
        for (int i = 0; i < mRecentAppList.size(); i++) {
            if (mRecentAppList.get(i).getTitle().equals(itemLayout.getTitle())) {
                return true;
            }

        }
        return false;
    }

    public boolean contains(
            List<RecentTaskInfo> activityInfoList,
            RgkItemAppsInfo app) {
        for (int i = 0; i < activityInfoList.size(); i++) {
            RecentTaskInfo info = activityInfoList.get(i);
            Intent intent = new Intent(info.baseIntent);
            if (info.origActivity != null) {
                intent.setComponent(info.origActivity);
            }
            if (intent.getComponent().getPackageName()
                    .equals(app.mIntent.getComponent().getPackageName())
                    && intent.getComponent().getClassName()
                    .equals(app.mIntent.getComponent().getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mPivotX = getMeasuredWidth();
        mPivotY = getMeasuredHeight();
        mHeight = getMeasuredHeight();
        mWidth = getMeasuredWidth();
        // setBaseAngle(90);
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).measure(mChildHalfSize * 2, mChildHalfSize * 2);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 0 11

        int index = (int) ((mBaseAngle) / DEGREES_90);

        if (isLeft()) {

            itemLayout(index);

        } else if (isRight()) {

            itemLayout2(index);
        }
    }

    /**
     * view left mBaseAngle 90 180 270....990
     *
     * @param index
     */
    private void itemLayout(int index) {

        // getPreViewsIndex(getViewsIndex(index)) 0 1 2
        mCurrentIndex = getRealIndex(index);
        Log.d(TAG, "getPreViewsIndex:" + getPreViewsIndex(getViewsIndex(index)));
        Log.d(TAG, "getViewsIndex:" + getViewsIndex(index));
        Log.d(TAG, "getNextViewsIndex:"
                + getNextViewsIndex(getViewsIndex(index)));
        Log.d(TAG, "mBaseAngle:" + mBaseAngle);

        itemLayout(mMap.get(getPreViewsIndex(getViewsIndex(index))),
                getPreQuaIndex(getQuaIndex(index)));

        itemLayout(mMap.get(getViewsIndex(index)), getQuaIndex(index));

        itemLayout(mMap.get(getNextViewsIndex(getViewsIndex(index))),
                getNextQuaIndex(getQuaIndex(index)));
    }

    /**
     * view right
     *
     * @param index
     */
    private void itemLayout2(int index) {
        mCurrentIndex = index;
        itemLayout(mMap.get(getPreViewsIndex(getViewsIndex2(index))),
                getPreQuaIndex(getQuaIndex2(index)));
        itemLayout(mMap.get(getViewsIndex2(index)), getQuaIndex2(index));
        itemLayout(mMap.get(getNextViewsIndex(getViewsIndex2(index))),
                getNextQuaIndex(getQuaIndex2(index)));
    }

    /**
     * 把一组数据指定布局到一个限象
     *
     * @param views 需要布局的数据组
     * @param qua   限象
     */
    private void itemLayout(ArrayList<RgkItemLayout> views, int qua) {
        if (views != null) {
            for (int index = 0; index < views.size(); index++) {
                /**
                 * 计算坐标
                 */
                Coordinate coordinate = coordinate(views, index, qua);
                /**
                 * 矫正子view 旋转一定的角度,以保证旋转至第0限象时方向是正的
                 */
                if (isLeft()) {
                    views.get(index).setRotation(DEGREES_90 * qua);
                } else if (isRight()) {
                    views.get(index).setRotation(-DEGREES_90 * qua);
                }
                /**
                 * 指定位置
                 */
                if (index < 9) {
                    views.get(index).setParentX(coordinate.x);
                    views.get(index).setParentY(coordinate.y);
                    views.get(index).layout(
                            (int) (coordinate.x - mChildHalfSize),
                            (int) (coordinate.y - mChildHalfSize),
                            (int) (coordinate.x + mChildHalfSize),
                            (int) (coordinate.y + mChildHalfSize));
                }
            }
        }

    }

    /**
     * 根据所有控件的索引Index计算坐标
     *
     * @param views 控件集合
     * @param index 遍历控件时的索引
     * @param qua   限象值
     * @return 返回一个包含坐标(x, y)
     */
    public Coordinate coordinate(ArrayList<RgkItemLayout> views, int index,
                                 int qua) {
        int size = 0;
        /**
         * group可认为是跟随环数而变化的一个值，用来计算index非0时的子控件的角度增长 角度增为只有一个子控件的时候：90/1=45；
         * index非0的时候：(group＋0.5)*newdegree(按照当前环中子控件的总数平分90的值)
         */
        int group = 0;
        /**
         * 半径变化，环数增加，半径增加
         */
        int radius = 0;
        if (views.size() <= COUNT_4) {
            size = views.size();
            group = index;
            radius = mInnerRadius;

            Log.d("LUORAN123", "size:" + size + "  group:" + group + " radius:"
                    + radius);
        } else if (views.size() <= 9) {
            if (index < COUNT_4) {
                /**
                 * 总数大于4时内环正好是4
                 */
                size = COUNT_4;
                group = index;
                radius = mInnerRadius;
            } else if (index < 9) {
                /**
                 * 总数大于4时外环 size＝总数－4 group＝views(index)-4
                 */
                size = views.size() - COUNT_4;
                group = index - COUNT_4;
                radius = mOuterRadius;
            }
        } else {
            if (index < COUNT_4) {
                /**
                 * 总数大于4时内环正好是4
                 */
                size = COUNT_4;
                group = index;
                radius = mInnerRadius;
            } else if (index < 9) {
                /**
                 * 总数大于4时外环 size＝总数－4 group＝views(index)-4
                 */
                size = COUNT_4 + 1;
                group = index - COUNT_4;
                radius = mOuterRadius;
            }
        }
        /**
         * 按照views(index)所在的当前环的个数平分90度
         */
        float degree = (float) DEGREES_90 / (float) (size);
        /**
         * 得出一个新的递增的角度，用来后面按照三角函数计算子控的位置
         */
        float newdegree;
        if (index == 0) {
            newdegree = degree / 2;
        } else {
            newdegree = (int) ((group + 0.5) * degree);
        }
        /**
         * 1.按照限象使用不同的三角函数计算所得x,y坐标 2.子控件根据不同的呃限象旋转位置满足在第0限象的正常显示效果
         * 3.当整个控件的容器反转之后，为保证显示效果，要做一定的反转
         */
        float x = 0l;
        float y = 0l;
        if (isLeft()) {
            if (qua == 0) {
                x = (float) Math.sin(Math.toRadians(newdegree)) * radius;
                y = (float) (mHeight - Math.cos(Math.toRadians(newdegree))
                        * radius);
                Log.d("GAOLI", "x:" + x + "  y:" + y);
            } else if (qua == 1) {
                x = (float) Math.cos(Math.toRadians(newdegree)) * radius;
                y = (float) (mHeight + Math.sin(Math.toRadians(newdegree))
                        * radius);
            } else if (qua == 2) {
                x = (float) -Math.sin(Math.toRadians(newdegree)) * radius;
                y = (float) (mHeight + Math.cos(Math.toRadians(newdegree))
                        * radius);
            } else if (qua == 3) {
                x = (float) -Math.cos(Math.toRadians(newdegree)) * radius;
                y = (float) (mHeight - Math.sin(Math.toRadians(newdegree))
                        * radius);
            }
        } else if (isRight()) {
            if (qua == 0) {
                x = (float) (mWidth - Math.sin(Math.toRadians(newdegree))
                        * radius);
                y = (float) (mHeight - Math.cos(Math.toRadians(newdegree))
                        * radius);
            } else if (qua == 1) {
                x = (float) (mWidth - Math.cos(Math.toRadians(newdegree))
                        * radius);
                y = (float) (mHeight + Math.sin(Math.toRadians(newdegree))
                        * radius);
            } else if (qua == 2) {
                x = (float) (mWidth + Math.sin(Math.toRadians(newdegree))
                        * radius);
                y = (float) (mHeight + Math.cos(Math.toRadians(newdegree))
                        * radius);
            } else if (qua == 3) {
                x = (float) (mWidth + Math.cos(Math.toRadians(newdegree))
                        * radius);
                y = (float) (mHeight - Math.sin(Math.toRadians(newdegree))
                        * radius);
            }
        }

        return new Coordinate(x, y);
    }

    /**
     * 计算当前的数据的坐标不考虑限象
     *
     * @param views 当前的视图
     * @param index 索引
     * @return 返回(x, y)坐标
     */
    public Coordinate coordinate2(ArrayList<RgkItemLayout> views, int index) {
        /**
         * size按照当前views的总数，以4为区分，分别计算出<4,=4,超出4的部分剪掉4即从1，2，3重新开始计数
         */
        int size = 0;
        /**
         * group可认为是跟随环数而变化的一个值，用来计算index非0时的子控件的角度增长 角度增为只有一个子控件的时候：90/1=45；
         * index非0的时候：(group＋0.5)*newdegree(按照当前环中子控件的总数平分90的值)
         */
        int group = 0;
        /**
         * 半径变化，环数增加，半径增加
         */
        int radius = 0;
        if (views.size() <= COUNT_4) {
            size = views.size();
            group = index;
            radius = mInnerRadius;
        } else if (views.size() <= 9) {
            if (index < COUNT_4) {
                /**
                 * 总数大于4时内环正好是4
                 */
                size = COUNT_4;
                group = index;
                radius = mInnerRadius;
            } else if (index < 9) {
                /**
                 * 总数大于4时外环 size＝总数－4 group＝views(index)-4
                 */
                size = views.size() - COUNT_4;
                group = index - COUNT_4;
                radius = mOuterRadius;
            }
        } else {
            if (index < COUNT_4) {
                /**
                 * 总数大于4时内环正好是4
                 */
                size = COUNT_4;
                group = index;
                radius = mInnerRadius;
            } else if (index < 9) {
                /**
                 * 总数大于4时外环 size＝总数－4 group＝views(index)-4
                 */
                size = COUNT_4 + 1;
                group = index - COUNT_4;
                radius = mOuterRadius;
            } else if (index >= 9) {
                size = COUNT_4 + 1;
                group = 8 - COUNT_4;
                radius = mOuterRadius;
            }
        }
        /**
         * 按照views(index)所在的当前环的个数平分90度
         */
        float degree = (float) DEGREES_90 / (float) (size);
        /**
         * 得出一个新的递增的角度，用来后面按照三角函数计算子控的位置
         */
        float newdegree;
        if (index == 0) {
            newdegree = degree / 2;
        } else {
            newdegree = (int) ((group + 0.5) * degree);
        }
        /**
         * 1.按照限象使用不同的三角函数计算所得x,y坐标 2.子控件根据不同的呃限象旋转位置满足在第0限象的正常显示效果
         * 3.当整个控件的容器反转之后，为保证显示效果，要做一定的反转
         */
        float x = 0l;
        float y = 0l;
        if (isLeft()) {
            // 角度转换为弧度
            x = (float) Math.sin(Math.toRadians(newdegree)) * radius;
            y = (float) (mHeight - Math.cos(Math.toRadians(newdegree)) * radius);
        } else if (isRight()) {
            x = (float) (mWidth - Math.sin(Math.toRadians(newdegree)) * radius);
            y = (float) (mHeight - Math.cos(Math.toRadians(newdegree)) * radius);
        }
        return new Coordinate(x, y);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        if (isLeft()) {
            canvas.rotate(mBaseAngle + mChangeAngle, 0, mPivotY);
        } else if (isRight()) {
            canvas.rotate(mBaseAngle + mChangeAngle, mPivotX, mPivotY);
        }
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        Log.d("LUOMANLUO", "dhdhkhkk");
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mMotionX = event.getX();
                mMotionY = event.getY();
                ArrayList<RgkItemLayout> views = getData();
                /**
                 * isRemoveFinish移除动画一处之前不允许再拖动
                 */
                if (views != null && isRemoveFinish) {
                    for (int index = 0; index < views.size(); index++) {

                        Coordinate coordinate = coordinate2(views, index);

                        mDownLeft = (coordinate.x - mChildHalfSize);
                        mDownTop = (coordinate.y - mChildHalfSize);
                        mDownRight = (coordinate.x + mChildHalfSize);
                        mDownBottom = (coordinate.y + mChildHalfSize);

                        if (mMotionX > mDownLeft && mMotionX < mDownRight
                                && mMotionY > mDownTop && mMotionY < mDownBottom) {

                            mClickTime1 = System.currentTimeMillis();
                            /**
                             * 找到当前点击的那个item
                             * 判断isDeletedAnimCompleted的目的是，连续快速点击删除按钮的时候，
                             * 上一个刚点击正在执行删除的时候再点击的话会错误的判断删除的是后点击的Item
                             */
                            mTargetItem = views.get(index);
                            mTargetItem.setIndex(index);

                            if (mTargetItem instanceof AngleItemStartUp) {
                                if (((AngleItemStartUp) mTargetItem).getDelBtn()
                                        .getVisibility() == View.GONE) {

                                    mClickType = TYPE_CLICK;

                                } else if (mMotionX > mDownLeft
                                        && mMotionX < (mDownLeft + mDeleteBtnSize)
                                        && mMotionY > mDownTop
                                        && mMotionY < (mDownTop + mDeleteBtnSize)
                                        && ((AngleItemStartUp) mTargetItem)
                                        .getDelBtn().getVisibility() == View.VISIBLE) {
                                    mClickType = TYPE_DELCLICK;

                                } else if (((AngleItemStartUp) mTargetItem)
                                        .getDelBtn().getVisibility() == View.VISIBLE) {

                                }
                                // 长按item进入编辑模式
                                if (getViewsIndex() != 0
                                        && ((AngleItemStartUp) mTargetItem)
                                        .getDelBtn().getVisibility() == View.GONE) {

                                    handler.postDelayed(mLongRunable, 600);
                                }
                            } else if (mTargetItem instanceof RgkAddItemInfo) {
                                mClickType = TYPE_ADDCLICK;
                            }
                            // 不能去除，否则click事件不能执行
                            return true;
                        }
                    }
                }

                break;
            case MotionEvent.ACTION_MOVE:
                float movenewx = event.getX();
                float movenewy = event.getY();

                // 如果发生了滑动，则注销点击事件，不注销的话依然会相应长点击事件

                if (Math.abs(movenewx - mMotionX) > 10
                        || Math.abs(movenewy - mMotionY) > 10) {
                    handler.removeCallbacks(mLongRunable);
                }

                break;
            case MotionEvent.ACTION_UP:
                /**
                 * 这里在拖拽取消是执行，即拖拽还没有发生时抬起了手指，因为一旦拖拽发生，
                 * 触摸事件就被AngleLayout拦截下来了，AngleView就接受不到触摸事件了，
                 * So这里叫做取消拖拽并不是结束拖拽，结束拖拽要在AngleLayout里执行
                 */
                if (mTargetItem != null) {
                    if ((mTargetItem).getVisibility() == View.GONE) {
                        // mOnEditModeChangeListener.onCancelDrag();
                    }
                }

                float clicknewx = event.getX();
                float clicknewy = event.getY();
                long clicktime = System.currentTimeMillis();
                if (Math.abs(mMotionX - clicknewx) < 10
                        && Math.abs(mMotionY - clicknewy) < 10) {
                    long time = Math.abs(mClickTime1 - clicktime);
                    if (time < 300) {
                        if (mClickType == TYPE_CLICK) {
                            /**
                             * 正常的点击事件
                             */
                            shake(mTargetItem);
                            postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (mOnClickListener != null) {
                                        mOnClickListener.onAngleClick(mTargetItem);

                                    }
                                }
                            }, 140);

                        } else if (mClickType == TYPE_ADDCLICK) {
                            /**
                             * 点击最后一个AddBtn时的点击事件
                             */
                            shake(mTargetItem);
                            postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mOnClickListener.onAddClick(getViewsIndex());
                                }
                            }, 140);
                        }
                        handler.removeCallbacks(mLongRunable);
                    }
                }
                mClickType = -1;
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mClickType == TYPE_DELCLICK) {
                    /**
                     * 删除时直接掉用删掉当前的数据，回调接口暂时没有用
                     */
                    if (mOnClickListener != null) {
                        mOnClickListener.onDeleteClick(mTargetItem);
                    }
                }
                mClickType = -1;
                break;
        }
        Log.d("YIYA", "super.onTouchEvent(event)" + super.onTouchEvent(event));
        return super.onTouchEvent(event);

    }

    /**
     * 用来锁定复原动画，在动画结束前，不允许动画再次启动
     */
    private boolean isRestoreFinish = true;

    public boolean isRestoreFinish() {
        return isRestoreFinish;
    }

    public void setIsRestoreFinish(boolean isResore) {
        isRestoreFinish = isResore;
    }

    /**
     * 移除动画
     */
    public void removeItem() {
        if (isRemoveFinish) {
            isRemoveFinish = false;
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(1.0f, 0f);
            valueAnimator.setDuration(150);
            valueAnimator
                    .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float values = (float) animation.getAnimatedValue();
                            mTargetItem.setScaleX(values);
                            mTargetItem.setScaleY(values);
                            requestLayout();
                        }
                    });
            valueAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    // getData().remove(mTargetItem);
                    mDelPre.clear();
                    mDelPre.addAll(getData());
                    mDelPre.remove(mTargetItem);

                    ArrayList<RgkItemLayout> views = mDelPre;
                    if (views != null) {
                        mDelNext.clear();
                        for (int index = 0; index < views.size(); index++) {
                            /**
                             * size按照当前views的总数，以4为区分，分别计算出<4,=4,超出4的部分剪掉4即从1，2，
                             * 3重新开始计数
                             */
                            // 删除item以后，更新新坐标
                            mDelNext.add(coordinate(views, index, getQuaIndex()));
                        }
                    }
                    transAnimator(mDelNext, mDelPre);

                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            valueAnimator.start();
        }
    }

    /**
     * 移除item之后的过渡动画 从原始坐标移动到新坐标
     *
     * @param resource   移除控件之后计算产生的新的item坐标
     * @param targetView 原始坐标
     */
    public void transAnimator(final ArrayList<Coordinate> resource,
                              final ArrayList<RgkItemLayout> targetView) {
        ValueAnimator translation = ValueAnimator.ofFloat(0f, 1f);
        translation.setDuration(250);
        translation
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float values = (float) animation.getAnimatedValue();
                        for (int i = 0; i < targetView.size(); i++) {
                            float x = (float) (resource.get(i).x - targetView
                                    .get(i).getParentX()) * values;
                            float y = (float) (resource.get(i).y - targetView
                                    .get(i).getParentY()) * values;
                            targetView.get(i).setTranslationX(x);
                            targetView.get(i).setTranslationY(y);
                            requestLayout();
                        }
                    }
                });
        translation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                for (int i = 0; i < targetView.size(); i++) {
                    targetView.get(i).setTranslationX(0);
                    targetView.get(i).setTranslationY(0);
                }
                getData().remove(mTargetItem);
                /**
                 * 判断最后一个Item，也就是只剩加号的时候退出编辑状态
                 */
                if (getData().size() == 1) {
                    mOnEditModeChangeListener.onExitEditMode();
                }

                isRemoveFinish = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        translation.start();
    }

    public void setOnAngleChangeListener(OnAngleChangeListener listener) {
        mAngleListener = listener;
    }

    public void setOnAngleClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }

    public void setOnAngleLongClickListener(OnEditModeChangeListener listener) {
        mOnEditModeChangeListener = listener;
    }

    /**
     * 手指按下时的初始角度
     */
    public void downAngle(float x, float y) {
        mDownAngle = Math.toDegrees(Math.atan(x / y));
    }

    /**
     * 手指滑动时 动态的计算一个变化量diff，在根据条件合理的计算 (滑动的时候不断地调用这个方法)
     *
     * @param x
     * @param y
     */
    public void changeAngle(float x, float y) {
        double diffAngle;
        double angle;
        // 计算当前的角度
        angle = Math.toDegrees(Math.atan(x / y));
        // 计算角度的变化量
        diffAngle = angle - mDownAngle;

        if (diffAngle > 0) {
            // 顺时针
            ANGLE_STATE = ANGLE_STATE_ALONG;
        } else {
            // 逆时针
            ANGLE_STATE = ANGLE_STATE_INVERSE;
        }
        if (isLeft()) {
            changeAngle(diffAngle);
        } else if (isRight()) {

            changeAngle(-diffAngle);
        }
    }

    /**
     * 改变mChangeAngle的值 mChangeAngle 角度的变化量
     *
     * @param rotation
     */
    private void changeAngle(double rotation) {
        mChangeAngle = (float) rotation;
        angleChange();
    }

    private void angleChange() {
        // 转动的时候回传当前限象index
        mAngleListener.onAngleChanged(
                getViewsIndex((int) (getAngleValues() / DEGREES_90)),
                ((getAngleValues() % DEGREES_90) / DEGREES_90));
        invalidate();
    }

    /**
     * 松开手指后根据x,y方向上的速度来判定是留在当前限象还是旋转到上一个或者下一个限象
     *
     * @param vx
     * @param vy
     */
    public void fling(float vx, float vy) {
        if (isLeft()) {
            if (vy > ALLOW_FLING || vx > ALLOW_FLING) {
                flingForward();
            } else if (vx < -ALLOW_FLING || vy < -ALLOW_FLING) {
                flingCurrnet();
            } else {
                upAngle();
            }
        } else if (isRight()) {
            if (vy > ALLOW_FLING || vx < -ALLOW_FLING) {
                flingCurrnet();
            } else if (vy < -ALLOW_FLING || vx > ALLOW_FLING) {
                flingForward();
            } else {
                upAngle();
            }
        }

    }

    /**
     * 松手后根据当前已经旋转的角，POSITION，顺逆时针来判定是留在当前限象还是旋转到上一个或者下一个限象
     */
    private void upAngle() {
        if (ANGLE_STATE == ANGLE_STATE_ALONG) {
            forward();
        } else if (ANGLE_STATE == ANGLE_STATE_INVERSE) {
            reverse();
        }
    }

    /**
     * 顺时针旋转
     */
    private void forward() {

        float diff = 0;
        if (isLeft()) {
            diff = getAngleValues() % DEGREES_90;
            if (diff > 0 && diff < DEGREES_OFFSET) {
                flingCurrnet();
            } else if (diff > DEGREES_OFFSET && diff < DEGREES_90) {
                /**
                 * 转到下一个
                 */
                flingForward();
            }
        } else if (isRight()) {
            diff = DEGREES_90 - getAngleValues() % DEGREES_90;
            if (diff > 0 && diff < DEGREES_OFFSET) {
                flingForward();
            } else if (diff > DEGREES_OFFSET && diff < DEGREES_90) {
                /**
                 * 转到下一个
                 */
                flingCurrnet();
            }
        }
    }

    /**
     * 逆时针旋转
     */
    private void reverse() {
        float diff = 0;
        if (isLeft()) {
            diff = (DEGREES_1080 - getAngleValues()) % DEGREES_90;
            if (diff > 0 && diff < DEGREES_OFFSET) {
                flingForward();
            } else if (diff > DEGREES_OFFSET && diff < DEGREES_90) {
                /**
                 * 转到下一个
                 */
                flingCurrnet();
            }
        } else if (isRight()) {
            diff = getAngleValues() % DEGREES_90;
            if (diff > 0 && diff < DEGREES_OFFSET) {
                flingCurrnet();
            } else if (diff > DEGREES_OFFSET && diff < DEGREES_90) {
                flingForward();
            }
        }

    }

    // 顺时针转动90度 0-1
    private void flingForward() {
        autoRotation(getAngleValues(),
                ((int) (getAngleValues() / DEGREES_90) + 1) * DEGREES_90);
    }

    // 回到当前的位置
    private void flingCurrnet() {
        autoRotation(getAngleValues(), ((int) (getAngleValues() / DEGREES_90))
                * DEGREES_90);
    }

    // 逆时针旋转90度
    private void flingReveser() {
        autoRotation(getAngleValues(),
                ((int) (getAngleValues() / DEGREES_90) - 1) * DEGREES_90);
    }

    // 根据旋转和点击的情况来判断view索要落在的位置
    public void setViewsIndex(int cur) {
        if (isLeft()) {
            int index = getViewsIndex((int) (getAngleValues() / DEGREES_90));
            if (index == 0) {
                if (cur == 1) {
                    flingReveser();
                } else if (cur == 2) {
                    flingForward();
                }
            } else if (index == 1) {
                if (cur == 0) {
                    flingForward();
                } else if (cur == 2) {
                    flingReveser();
                }
            } else if (index == 2) {
                if (cur == 0) {
                    flingReveser();
                } else if (cur == 1) {
                    flingForward();
                }
            }
        } else if (isRight()) {
            int index = getViewsIndex2((int) (getAngleValues() / DEGREES_90));
            if (index == 0) {
                if (cur == 1) {
                    flingForward();
                } else if (cur == 2) {
                    flingReveser();
                }
            } else if (index == 1) {
                if (cur == 2) {
                    flingForward();
                } else if (cur == 0) {
                    flingReveser();
                }
            } else if (index == 2) {
                if (cur == 0) {
                    flingForward();
                } else if (cur == 1) {
                    flingReveser();
                }
            }
        }

    }

    // 滑动的过程
    private void autoRotation(float start, float end) {
        mChangeAngle = 0;
        mAngleAnimator = ValueAnimator.ofFloat(start, end);
        mAngleAnimator.setDuration(500);
        //mAngleAnimator.setInterpolator(new OvershootInterpolator());
        mAngleAnimator
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (float) animation.getAnimatedValue();
                        mBaseAngle = value;
                        mBaseAngle = mBaseAngle % DEGREES_1080;
                        angleChange();
                    }
                });
        mAngleAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                int mIndex = ((int) ((mBaseAngle) / DEGREES_90));
                if (isLeft()) {

                    Log.d(TAG, "autoWhirling isLeft()");
                    itemLayout(mIndex);

                } else if (isRight()) {
                    itemLayout2(mIndex);
                }

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAngleAnimator.start();

    }

    public void setPositionState(int position) {
        super.setPositionState(position);
    }

    /**
     * 转动时候随时可以获取到的的转动角度
     *
     * @return
     */
    public float getAngleValues() {
        float newrotation = (mBaseAngle + mChangeAngle);

        Log.d("LUORAN56789", "newrotation:" + newrotation);
        return newrotation < 0 ? DEGREES_1080 + (newrotation) : (newrotation);
    }

    public float getBaseAngle() {
        return mBaseAngle;
    }

    public void setBaseAngle(float angle) {
        mBaseAngle = angle;
        angleChange();
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    /**
     * 根据当前的index获取当前显示限象index 比如11->1,10->2,9->3,8->0
     *
     * @param index 转动结束后根据BaseAngle的值除以90得出的范围0-11 3,4的最小公倍数的是12
     * @return
     */
    public int getQuaIndex(int index) {
        return index == 0 ? 0 : (COUNT_12 - index) % COUNT_4;
    }

    /**
     * 获取真实的限象Index
     *
     * @param index
     * @return
     */
    public int getRealIndex(int index) {
        return index == 0 ? 0 : (COUNT_12 - index);
    }

    /**
     * STATE=RIGHT时
     *
     * @param index
     * @return
     */
    public int getQuaIndex2(int index) {
        return index < 0 ? COUNT_4 + index : index % COUNT_4;
    }

    /**
     * 获取当前index的上一个index
     *
     * @param index 传入的是getIndex()的返回值
     * @return 得到上一个index
     */
    public int getPreQuaIndex(int index) {
        return index == 0 ? COUNT_3 : (index - 1);
    }

    /**
     * 获取当前index的下一个index
     *
     * @param index 传入的是getIndex()的返回值
     * @return 得到下一个index
     */
    private int getNextQuaIndex(int index) {
        return index == COUNT_3 ? 0 : (index + 1);
    }

    // 第一次打开时候index默认为0，然后 1 2 就这样 一直循环
    private int getViewsIndex(int index) {
        return (COUNT_12 - index) % COUNT_3;
    }

    /**
     * getViewsIndex(int index)重载方法
     *
     * @return 直接返回当前显示数据的Index索引值
     */
    public int getViewsIndex() {
        if (isLeft()) {
            return getViewsIndex(((int) ((mBaseAngle) / DEGREES_90)));
        } else {
            return getViewsIndex2(((int) ((mBaseAngle) / DEGREES_90)));
        }
    }

    public int getQuaIndex() {
        int index = (int) ((mBaseAngle) / DEGREES_90);
        if (isLeft()) {
            return getQuaIndex(index);
        } else {
            return getQuaIndex2(index);
        }
    }

    /**
     * 根据当前的显示数据的Index来从HashMap中的取出数据
     *
     * @return 当前显示在第0限象位置的数据
     */
    public ArrayList<RgkItemLayout> getData() {
        return mMap.get(getViewsIndex());
    }

    public void putData(ArrayList<RgkItemLayout> arrayList) {
        mMap.put(getViewsIndex(), arrayList);
        refresh();
    }

    /**
     * mPositionState=Right
     *
     * @param index 根据BaseAngle求出的实际限象数
     * @return 转换为右侧的实际Index
     */
    private int getViewsIndex2(int index) {
        return index < 0 ? COUNT_3 + index : index % COUNT_3;
    }

    /**
     * 上一个数据索引
     *
     * @param index 传入的是getViews()的返回值
     * @return
     */
    public int getPreViewsIndex(int index) {
        return index == 0 ? 2 : (index - 1);
    }

    /**
     * 下一个数据索引
     *
     * @param index 传入的是getViews()的返回值
     * @return
     */
    public int getNextViewsIndex(int index) {
        return index == 2 ? 0 : (index + 1);
    }

    /**
     * 长按item后，显示删除按钮
     */
    public void startEditMode() {

        int index = getViewsIndex();

        for (int i = 0; i < mMap.get(index).size(); i++) {
            RgkItemLayout item = mMap.get(index).get(i);
            if (item instanceof AngleItemStartUp) {
                ((AngleItemStartUp) item).showDelBtn();
            }
        }
    }

    public void showAnimator(final View view) {
        Log.d("LUORAN99", "hideAnimator");
        ValueAnimator alphaAnim = ValueAnimator.ofFloat(0f, 1f);
        alphaAnim.setDuration(800);
        alphaAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float a = (float) animation.getAnimatedValue();
                view.setAlpha(a);
            }
        });
        alphaAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // view.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        alphaAnim.start();
    }

    /**
     * 退出当前编辑模式，遍历当前的Views集合，隐藏编辑按钮
     */
    public void endEditMode() {
        int index = getViewsIndex();
        for (int i = 0; i < mMap.get(index).size(); i++) {
            RgkItemLayout item = mMap.get(index).get(i);
            if (item instanceof AngleItemStartUp) {
                ((AngleItemStartUp) item).hideDelBtn();
            }
        }

    }

    public float getChildHalfSize() {
        return mChildHalfSize;
    }

    public RgkItemLayout getTargetItem() {
        return mTargetItem;
    }

    /**
     * 抖一下
     *
     * @param view
     */
    public void shake(final View view) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1.0f, 0.85f);
        valueAnimator.setRepeatCount(1);
        valueAnimator.setDuration(60);
        valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float scale = (float) animation.getAnimatedValue();
                        view.setScaleX(scale);
                        view.setScaleY(scale);
                        requestLayout();
                    }
                });
        valueAnimator.start();
    }

    public class LongClickRunable implements Runnable {

        public LongClickRunable() {
            // TODO Auto-generated constructor stub
        }

        @Override
        public void run() {

            if (mOnEditModeChangeListener != null) {
                if (getAngleValues() % DEGREES_90 == 0 && getViewsIndex() != 0) {
                    Log.d("HHRR", "LongClickRunable" + getAngleValues());
                    mVibrator = (Vibrator) getContext().getSystemService(
                            Context.VIBRATOR_SERVICE);
                    long[] pattern = {0, 35};
                    mVibrator.vibrate(pattern, -1);
                    mOnEditModeChangeListener.onEnterEditMode(mTargetItem);
                    startEditMode();
                }
            } else {
                throw new IllegalArgumentException(
                        "AngleView.OnClickListener is null(AngleView的Click监听接口对象为空)");
            }
        }
    }

}
