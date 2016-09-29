package com.android.launcher3.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.view.animation.BounceInterpolator;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.utils.RgkPositionState;
import com.android.launcher3.utils.onChangeListener;


public class RgkLayout extends FrameLayout implements
        RgkView.OnAngleChangeListener, RgkIndicatorView.OnIndexChangedLitener,
        RgkView.OnEditModeChangeListener, RgkCornerView.OnCornerClickListener {

    private Context mContext;
    /**
     * 旋转View
     */
    private RgkView mAngleView;
    /**
     * AngleView主题
     */
    private RgkAngleViewTheme mAngleViewTheme;
    /**
     *
     */
    private int mAngleSize;
    /**
     * 底部的指示器
     */
    private RgkIndicatorView mIndicator;
    /**
     * AngleIndicatorView主题
     */
    private RgkIndicatorViewTheme mIndicatorTheme;
    /**
     * 状态view,返回,退出编辑,垃圾桶
     */
    private RgkCornerView mCornerView;

    // private LoadingView mLoading;

    private float mThemeScale;

    private int mChildHalfSize;

    private int mAngleLogoSize;

    private int mIndicatorSize;

    /**
     * 主题的小用像素，然后根据Indocator再做大小变化
     */
    private int mIndicatorThemeSize;
    /**
     * 当前的旋转状态
     */
    private int mTouchState = TOUCH_STATE_REST;
    /**
     * 停滞状态
     */
    private static final int TOUCH_STATE_REST = 0;
    /**
     * 旋转中
     */
    private static final int TOUCH_STATE_WHIRLING = 1;
    /**
     * 转向上一个
     */
    private static final int TOUCH_STATE_PRE = 2;
    /**
     * 转向下一个
     */
    private static final int TOUCH_STATE_NEXT = 3;

    private static final int TOUCH_STATE_AUTO = 4;

    private boolean isAllowAngle = true;

    private float mLastMotionX;

    private float mLastMotionY;

    private long mLastTime;

    /**
     * 允许滑动的最小距离
     */
    private int mTouchSlop;
    /**
     * 速度检测
     */
    private VelocityTracker mVelocityTracker;

    private int mMaximumVelocity, mMinmumVelocity;
    /**
     * 容器的宽高
     */
    private int mWidth;

    private int mHeight;

    private float mAngleLayoutScale;

    /**
     * 切换状态，开和关
     */
    private int mSwitchType = SWITCH_TYPE_OFF;

    public static final int SWITCH_TYPE_ON = 0;

    public static final int SWITCH_TYPE_OFF = 1;
    /**
     * AngleView 的编辑状态
     */
    private int mEditState = STATE_NORMAL;
    /**
     * 正常模式
     */
    public static final int STATE_NORMAL = 0;
    /**
     * 编辑模式
     */
    public static final int STATE_EDIT = 1;

    private ValueAnimator mAnimator;

    private boolean isAnimator = true;

    public OnOffListener mOffListener;

    private boolean isLockAction;

    private int MOVE_TYPE = TYPE_NULL;

    static final int TYPE_ROTATION = 1;

    static final int TYPE_OFF = 2;

    static final int TYPE_NULL = -1;


    public interface OnOffListener extends onChangeListener {
        /**
         * 当Angle关闭的时候回调,目的是通知SwipeLayout去dissmis
         */
        void onOff();
    }

    public OnItemDragListener mItemDragListener;

    public interface OnItemDragListener {

        /**
         * 拖拽结束时调用
         *
         * @param index 返回当前的数据索引index
         */
        void onDragEnd(int index);
    }

    public RgkLayout(Context context) {
        this(context, null);
    }

    public RgkLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RgkLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        /**
         * Item尺寸
         */
        mChildHalfSize = getResources().getDimensionPixelSize(
                R.dimen.angleitem_half_size);
        /**
         * AngleView的大小
         */
        mAngleSize = getResources().getDimensionPixelSize(
                R.dimen.angleview_size);
        /**
         * IndicatorView的大小
         */
        mIndicatorSize = getResources().getDimensionPixelSize(
                R.dimen.angleindicator_size);
        /**
         * indicator的大小用像素，因为如果是dp的话，当半径大小不一时，
         * 用三段弧线拼接出来的指示器两头对其所需要的ossfert值无法确定的计算
         * 所以用的px，然后再根据IndicatorView的大小求的的scale值来对IndicatorViewTheme来进行缩放，效果就达到了
         */
        mIndicatorThemeSize = getResources().getDimensionPixelSize(
                R.dimen.angleindicator_theme_size);
        mAngleLogoSize = getResources().getDimensionPixelSize(
                R.dimen.anglelogo_size);
        ViewConfiguration mConfig = ViewConfiguration.get(context);
        mTouchSlop = mConfig.getScaledTouchSlop();
        mMaximumVelocity = ViewConfiguration.get(context)
                .getScaledMaximumFlingVelocity();
        mMinmumVelocity = ViewConfiguration.get(context)
                .getScaledMinimumFlingVelocity();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAngleView = (RgkView) findViewById(R.id.angleview);
        mAngleViewTheme = (RgkAngleViewTheme) findViewById(R.id.angleview_theme);
        mAngleView.setOnAngleChangeListener(this);
        mAngleView.setOnAngleLongClickListener(this);

        mIndicator = (RgkIndicatorView) findViewById(R.id.indicator);
        mIndicatorTheme = (RgkIndicatorViewTheme) findViewById(R.id.indicator_theme);

        mIndicator.setOnChangeListener(this);
        mIndicator.setCurrent(0);

        mCornerView = (RgkCornerView) findViewById(R.id.corner_view);
        mCornerView.setOnCornerListener(this);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        /**
         * AngleView,IndicatorView的大小
         */
        if (mAngleView.isLeft()) {
            mAngleView.layout(0, mHeight - mAngleSize, mAngleSize, mHeight);
            mAngleViewTheme
                    .layout(0, mHeight - mAngleSize, mAngleSize, mHeight);
            mIndicator.layout(0, mHeight - mIndicatorSize, mIndicatorSize,
                    mHeight);
            mIndicatorTheme.layout(0, mHeight - mIndicatorThemeSize,
                    mIndicatorThemeSize, mHeight);

            mIndicatorTheme.setPivotX(0);
            mIndicatorTheme.setPivotY(mIndicatorThemeSize);
            mCornerView.layout(0, mHeight - mAngleLogoSize, mAngleLogoSize,
                    mHeight);

        } else if (mAngleView.isRight()) {
            mAngleView.layout(mWidth - mAngleSize, mHeight - mAngleSize,
                    mWidth, mHeight);
            mAngleViewTheme.layout(mWidth - mAngleSize, mHeight - mAngleSize,
                    mWidth, mHeight);
            mIndicator.layout(mWidth - mIndicatorSize,
                    mHeight - mIndicatorSize, mWidth, mHeight);
            mIndicatorTheme.layout(mWidth - mIndicatorThemeSize, mHeight
                    - mIndicatorThemeSize, mWidth, mHeight);

            mIndicatorTheme.setPivotX(mIndicatorThemeSize);
            mIndicatorTheme.setPivotY(mIndicatorThemeSize);
            mCornerView.layout(mWidth - mAngleLogoSize, mHeight
                    - mAngleLogoSize, mWidth, mHeight);

        }
        /**
         * 根据Indicator来缩放IndicatorTheme保证Theme的质量
         */
        float scale = (float) mIndicatorSize / (float) mIndicatorThemeSize;
        mIndicatorTheme.setScaleX(scale);
        mIndicatorTheme.setScaleY(scale);

        /**
         * 删除时的背景颜色的Scale值
         */
        mThemeScale = (float) mIndicatorSize / (float) mAngleLogoSize;
    }

    /**
     * onInterceptTouchEvent方法解析：这个方法从字面上理解也就是阶段touch事件的意思，当我们触摸到一个view的时候，事件的传递
     * 实际上是从其最顶层view开始向下传递的
     * ，执行的顺序依次是dispatchTouchEvent-onInterceptTouchEvent-onTouchEvent,
     * 先来说一下onInterceptTouchEvent，这个是拦截事件的主要方法，如果此方法返回true，则事件不在往下传递，也就是子view响应
     * 不到实践，直接会执行当前view的onTouchEvent方法，如果返回false，则会向下传递，也就是当前view的子view会响应到事件;
     * <p/>
     * <p/>
     * 有一点需要注意的是：如果在onInterceptTouchEvent方法的ACTION_DOWN事件里返回了false，
     * 这时候依然会执行当前view的ACTION_MOVE
     * 和ACTION_UP事件，这一点尤其神奇，困扰了一段时间，说来都是苦逼，反之如果反悔了true
     * ，当前view的ACTION_MOVE和ACTION_UP事件将不再执行， 事件直接往下传递，6不6？
     */

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mTouchState = TOUCH_STATE_REST;
                mLastMotionX = ev.getX();
                mLastMotionY = ev.getY();

                if (mEditState == STATE_NORMAL) {
                    if (mAngleView.isLeft()) {

                        // 记录手指按下的初始角度
                        mAngleView.downAngle(mLastMotionX, mHeight - mLastMotionY);

                    } else if (mAngleView.isRight()) {
                        // 记录手指按下的初始角度
                        mAngleView.downAngle(mWidth - mLastMotionX, mHeight
                                - mLastMotionY);
                    }

                }

                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("LUORAN478", "MotionEvent.ACTION_DOWN" + mTouchSlop);
                float newX = ev.getX();
                float newY = ev.getY();
                float diffX = newX - mLastMotionX;
                float diffY = newY - mLastMotionY;
                if (mEditState == STATE_NORMAL) {

                    // 如果满足条件的话，拦截掉事件，不往下（子view）传递，执行当前view的onTouchEvent方法,否则执行子view的dipatchTouchEvent方法
                    if ((Math.abs(diffX) > mTouchSlop || Math.abs(diffY) > mTouchSlop)
                            && isAllowAngle) {

                        return true;
                    }

                } else if (mEditState == STATE_EDIT) {
                    // if ((Math.abs(diffX) > mTouchSlop || Math.abs(diffY) >
                    // mTouchSlop) && isAllowAngle) {
                    return true;
                    // }
                }

                break;
            case MotionEvent.ACTION_UP:
                Log.d("LUORAN478", "MotionEvent.ACTION_UP");
                break;
            case MotionEvent.ACTION_CANCEL:
                mTouchState = TOUCH_STATE_REST;
                break;
        }
        Log.d("LUORAN478",
                "MotionEvent.ACTION_UP" + super.onInterceptTouchEvent(ev));
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getChildCount() <= 0) {
            return super.onTouchEvent(event);
        }

        // 初始化速度跟踪器
        initVeloCityTracker(event);

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mTouchState = TOUCH_STATE_REST;
                mLastMotionX = event.getX();
                mLastMotionY = event.getY();

                // 获取第一次点击的时间
                mLastTime = System.currentTimeMillis();
                // 拦截掉事件，准备滑动
                if (mEditState == STATE_NORMAL) {
                    if (mAngleView.isLeft()) {


                        mAngleView.downAngle(mLastMotionX, mHeight - mLastMotionY);
                        return true;
                    } else if (mAngleView.isRight()) {
                        mAngleView.downAngle(mWidth - mLastMotionX, mHeight
                                - mLastMotionY);
                        return true;
                    }
                } else if (mEditState == STATE_EDIT) {
                    Log.d("LUOMAN123", "HAHAHAH");
                    return true;
                }

                break;
            case MotionEvent.ACTION_MOVE:
                float newX = event.getX();
                float newY = event.getY();
                float diffX = newX - mLastMotionX;
                float diffY = newY - mLastMotionY;
                if ((Math.abs(diffX) > mTouchSlop || Math.abs(diffY) > mTouchSlop)
                        && isAllowAngle) {
                    mTouchState = TOUCH_STATE_WHIRLING;
                }
                /**
                 * 转动的相关代码
                 */
                if (mEditState == STATE_NORMAL) {
                    if (mTouchState == TOUCH_STATE_WHIRLING && newY < mHeight) {

                        if (mAngleView.isLeft()) {

                            /**
                             * 动作处理：先判断手指滑动的区域，一旦满足条件就设置MOVE类型，并锁定
                             */
                            if (newX > mLastMotionX && newY > mLastMotionY
                                    || newX < mLastMotionX && newY < mLastMotionY) {
                                /**
                                 * 判断区域转动
                                 */
                                if (!isLockAction) {
                                    Log.d("LUORAN1245", "isLockAction:"
                                            + isLockAction);
                                    isLockAction = true;
                                    MOVE_TYPE = TYPE_ROTATION;
                                }

                            } else if (newX < mLastMotionX && newY > mLastMotionY) {

                                /**
                                 * 判断滑动关闭区域
                                 */
                                float dis = (float) Math.sqrt(Math.pow(
                                        (newX - mLastMotionX), 2)
                                        + Math.pow((newY - mLastMotionY), 2));
                                Log.d("LUORAN888", "MOVE_TYPE == TYPE_ROTATION0");
                                if (dis > 200) {
                                    if (!isLockAction) {

                                        isLockAction = true;
                                        MOVE_TYPE = TYPE_OFF;
                                    }
                                }
                            }
                            /**
                             * 处理滑动事件
                             */
                            if (MOVE_TYPE == TYPE_ROTATION) {

                                Log.d("LUORAN88", "MOVE_TYPE == TYPE_ROTATION0");

                                mAngleView.changeAngle(newX, mHeight - newY);
                            }
                        } else if (mAngleView.isRight()) {
                            if (newX < mLastMotionX && newY > mLastMotionY
                                    || newX > mLastMotionX && newY < mLastMotionY) {
                                if (!isLockAction) {
                                    isLockAction = true;
                                    MOVE_TYPE = TYPE_ROTATION;
                                }
                            } else if (newX > mLastMotionX && newY > mLastMotionY) {
                                float dis = (float) Math.sqrt(Math.pow(
                                        (newX - mLastMotionX), 2)
                                        + Math.pow((newY - mLastMotionY), 2));
                                if (dis > mAngleSize / 2) {
                                    if (!isLockAction) {
                                        isLockAction = true;
                                        MOVE_TYPE = TYPE_OFF;
                                    }
                                }
                            }

                            if (MOVE_TYPE == TYPE_ROTATION) {

                                Log.d("LUORAN88", "MOVE_TYPE == TYPE_ROTATION1");

                                mAngleView.changeAngle(mWidth - newX, mHeight
                                        - newY);
                            }
                        }
                    }
                } else if (mEditState == STATE_EDIT) {
                    // onDrag(event.getX(), event.getY(), true);
                }

                break;
            case MotionEvent.ACTION_UP:

                Log.d("CHENCONG", "ffkljwefjlfjwefop;o");
                mTouchState = TOUCH_STATE_REST;

                // 计算滑动的初始速度
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                float vx = mVelocityTracker.getXVelocity();
                float vy = mVelocityTracker.getYVelocity();
                if (mEditState == STATE_NORMAL && MOVE_TYPE == TYPE_ROTATION) {
                    // 开始滑动
                    mAngleView.fling(vx, vy);
                }

                recyleVelocityTracker();

                float upX = event.getX();
                float upY = event.getY();
                long upTime = System.currentTimeMillis();
                if (mAngleView.isLeft()) {

                    // 这个地方判断点击的位置坐标
                    float upDistance = (float) Math.sqrt(Math.pow((upX - 0), 2)
                            + Math.pow((upY - mHeight), 2));
                    Log.d("LUORAN",
                            "mAngleView.getMeasuredHeight():"
                                    + mAngleView.getMeasuredHeight());
                    // 加这个判断是防止在外部区域滑动的时候可能会关闭view
                    if (Math.abs(upX - mLastMotionX) < 8 && Math.abs(upY - mLastMotionY) < 8 &&
                            (upTime - mLastTime) < 200 && (upDistance > mAngleView.getMeasuredHeight()))  {
                        if (mEditState == STATE_EDIT) {

                            // 退出编辑模式
                            Log.d("HHRRR", "mEditState" + mEditState);
                            setEditState(RgkLayout.STATE_NORMAL);
                        } else {
                            Log.d("HHRRRTT", "mEditState" + mEditState);
                            // 点击外部去与关闭RgkLayout
                            off();
                        }
                    }
                } else if (mAngleView.isRight()) {
                    float upDistance = (float) Math.sqrt(Math
                            .pow((upX - mWidth), 2) + Math.pow((upY - mHeight), 2));
                    if (Math.abs(upX - mLastMotionX) < 8 && Math.abs(upY - mLastMotionY) < 8 &&
                            (upTime - mLastTime) < 200 && (upDistance > mAngleView.getMeasuredHeight())) {
                        if (mEditState == STATE_EDIT) {
                            setEditState(RgkLayout.STATE_NORMAL);
                        } else {
                            Log.d("LUORAN","off()");
                            // 点击外部去与关闭RgkLayout
                            off();
                        }
                    }
                }

                if (MOVE_TYPE == TYPE_OFF && upTime - mLastTime < 400) {
                    off();
                }
                isLockAction = false;
                MOVE_TYPE = TYPE_NULL;
                break;
            case MotionEvent.ACTION_CANCEL:
                recyleVelocityTracker();
                /**
                 * 复原动作锁定和MOVE类型
                 */
                isLockAction = false;
                MOVE_TYPE = TYPE_NULL;
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * cur:索引
     * <p/>
     * p 百分比
     */
    @Override
    public void onAngleChanged(int cur, float p) {
        mIndicator.onAngleChanged2(cur, p);
        mIndicatorTheme.changeStartAngle(cur, p);
    }

    // index发生变化的时候
    @Override
    public void onIndexChanged(int index) {
        /**
         * 点击Indicator的时候自动旋转AngleView
         *
         * 0
         */
        if (mEditState == STATE_NORMAL) {
            mAngleView.setViewsIndex(index);
        }
    }

    @Override
    public void onEnterEditMode(View view) {
        /**
         * 长按之后进入编辑模式
         */
        mEditState = STATE_EDIT;
        /**
         * 角落View的状态
         */
        mCornerView.setState(RgkCornerView.STATE_EDIT);
    }

    @Override
    public void onExitEditMode() {
        mEditState = STATE_NORMAL;
        mCornerView.setState(RgkCornerView.STATE_NORMAL);
    }

    public void setEditState(final int state) {
        mEditState = state;
        mAngleView.endEditMode();
        mCornerView.setState(RgkCornerView.STATE_NORMAL);
    }

    public int getEditState() {
        return mEditState;
    }

    @Override
    public void cornerEvent() {
        if (mEditState == STATE_EDIT) {
            setEditState(RgkLayout.STATE_NORMAL);
            return;
        }
        if (mEditState == STATE_NORMAL) {
            off();
        }
    }

    /**
     * 重要方法:反转AngleLayout 反转之后根据不同情况对子控件做反转
     */
    public void setPositionLeft() {
        setPivotX(0);
        setPivotY(mContext.getResources().getDisplayMetrics().heightPixels
                - Utilities.getStatusBarHeight(mContext));
        setPositionState(RgkPositionState.POSITION_STATE_LEFT);
        /**
         * 左右两边的的角度一样，但是显示的限象不一样，通过一个公倍数12来换算成限象一样
         *
         *
         * 场景：左边区域滑动到0象限，为了保持再次从右面划出保持也是0象限
         */

        if (mAngleView.getCurrentIndex() == 0) {

            Log.d("LUORAN99",
                    "mAngleView.getCurrentIndex():"
                            + mAngleView.getCurrentIndex());
            // mAngleView.setItemstartAnimator(mAngleView.getCurrentIndex());

        } else {

            Log.d("LUORAN88", "newrotation:" + mAngleView.getCurrentIndex());
            mAngleView.setBaseAngle((12 - mAngleView.getCurrentIndex())
                    * RgkView.DEGREES_90);
            // mAngleView.setItemstartAnimator(mAngleView.getCurrentIndex());
        }
        requestLayout();
    }

    /**
     * 重要方法:反转AngleLayout 反转之后根据不同情况对子控件做反转
     */
    public void setPositionRight() {
        setPivotX(mContext.getResources().getDisplayMetrics().widthPixels);
        setPivotY(mContext.getResources().getDisplayMetrics().heightPixels
                - Utilities.getStatusBarHeight(mContext));
        setPositionState(RgkPositionState.POSITION_STATE_RIGHT);

        /**
         * 左右两百年的角度一样，但是显示的限象不一样，通过一个公倍数12来换算成限象一样
         */
        if (mAngleView.getCurrentIndex() != 0) {
            mAngleView.setBaseAngle(mAngleView.getCurrentIndex()
                    * RgkView.DEGREES_90);
        }
        requestLayout();
    }

    public void setPositionState(int state) {
        mIndicator.setPositionState(state);
        mIndicatorTheme.setPositionState(state);
        mAngleView.setPositionState(state);
        mAngleViewTheme.setPositionState(state);
        mCornerView.setPositionState(state);

    }

    public int getPositionState() {
        return mAngleView.getPositionState();
    }

    public void setOnOffListener(OnOffListener listener) {
        mOffListener = listener;
    }

    public void setOnDragItemListener(OnItemDragListener listener) {
        mItemDragListener = listener;
    }

    /**
     * 初始化VelocityTracker
     *
     * @param event
     */
    private void initVeloCityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    /**
     * 回收VelocityTracker
     */
    private void recyleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    /**
     * 放大和缩小的状态
     *
     * @param scale
     */
    public void setAngleLayoutScale(float scale) {
        mAngleLayoutScale = scale;
        setScaleX(mAngleLayoutScale);
        setScaleY(mAngleLayoutScale);
    }

    /**
     * 自动切换菜单
     */
    public void switchAngleLayout() {
        if (mAngleLayoutScale < 0.3) {
            off(mAngleLayoutScale);
        } else if (mAngleLayoutScale >= 0.3) {
            Log.d("LUORAN11", "switchAngleLayout");
            on(mAngleLayoutScale);
        }
    }

    /**
     * 获取当前的Scale值
     *
     * @return
     */
    public float getAngleLayoutScale() {
        return mAngleLayoutScale;
    }

    /**
     * 打开
     */
    public void on() {
        Log.d("LUORAN12", "fsdfsdfds1");
        on(mAngleLayoutScale);
    }

    // 打开动画
    public void on(float start) {
        if (isAnimator) {
            mSwitchType = SWITCH_TYPE_ON;
            Log.d("LUORAN12", "fsdfsdfds" + start);
            animator(start, 1.0f);
        }
    }

    public void off() {
        off(mAngleLayoutScale);
    }

    public void offnoAnimator() {
        mAngleLayoutScale = 0f;
        mSwitchType = SWITCH_TYPE_OFF;
    }

    /**
     * 关闭
     *
     * @param start
     */
    private void off(float start) {
        if (isAnimator) {
            mSwitchType = SWITCH_TYPE_OFF;
            animator(start, 0f);
        }
    }

    // 打开的动画
    public void animator(float start, float end) {
        isAnimator = false;
        mAnimator = ValueAnimator.ofFloat(start, end);
        mAnimator.setDuration(500);
        if (mSwitchType == SWITCH_TYPE_ON) {

            Log.d("HHRRR", "mSwitchType" + mSwitchType);
            mAnimator.setInterpolator(new BounceInterpolator());
        } else if (mSwitchType == SWITCH_TYPE_OFF) {
            Log.d("HHRRR", "mSwitchType" + mSwitchType);
            mAnimator
                    .setInterpolator(new AnticipateOvershootInterpolator(0.9f));
        }
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                mOffListener.change(v);
                setAngleLayoutScale(v);
            }
        });
        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimator = true;
                if (mSwitchType == SWITCH_TYPE_OFF) {
                    mOffListener.onOff();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimator.start();
    }

    /**
     * 返回AngleView
     *
     * @return
     */
    public RgkView getAngleView() {
        return mAngleView;
    }

    /**
     * 获取开还是关
     *
     * @return
     */
    public int getSwitchType() {
        return mSwitchType;
    }

    public void setSwitchType(int type) {
        mSwitchType = type;
    }

}
