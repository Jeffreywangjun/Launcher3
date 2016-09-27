package com.android.launcher3.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import com.android.launcher3.R;
import com.android.launcher3.utils.RgkPositionState;
import com.android.launcher3.utils.onChangeListener;


/**
 * 作为触发屏幕触摸事件的View
 */
public class RgkTouchView extends PositionStateView {

    private Paint mPaint;

    private Rect mRect = new Rect();

    public int mTouchSlop;

    private int mDisplayWidth;

    private int mDisplayHeight;

    private int mRectLeft;

    private int mRectTop;

    private int mRectRight;

    private int mRectBottom;

    private int mWidth;

    private int mHeight;

    private float mLastX;

    private float mLastY;

    private int mTouchState = 2;

    public static final int TOUCH_STATE_REST = 0;

    public static final int TOUCH_STATE_SLIDE = 1;

    public static final int VELOCITY_2500 = 2500;

    public int mAngleSize;

    private WindowManager.LayoutParams mParams;

    private WindowManager mManager;

    private OnEdgeSlidingListener mListener;

    /**
     * 速度检测
     */
    private VelocityTracker mVelocityTracker;

    private int mMaximumVelocity, mMinmumVelocity;

    public interface OnEdgeSlidingListener extends onChangeListener {
        /**
         * 打开
         */
        void openLeft();

        void openRight();

        /**
         * true速度满足自动打开 false速度不满足根据抬手时的状态来判断是否打开
         *
         * @param view
         * @param flag
         */
        void cancel(View view, boolean flag);

    }

    public RgkTouchView(Context context) {
        this(context, null);

    }

    public RgkTouchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public RgkTouchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.d("LUORAN", "CatchView");
        // 这句很重要，不加这句的话不走onDraw方法（不要问为什么，我也不知道）
        setWillNotDraw(false);

        mPaint = new Paint();
        mPaint.setColor(Color.TRANSPARENT);
        // 获取屏幕的宽高
        mDisplayWidth = context.getResources().getDisplayMetrics().widthPixels;
        mDisplayHeight = context.getResources().getDisplayMetrics().heightPixels;
        ViewConfiguration configuration = ViewConfiguration.get(context);
        // 允许滑动的最小距离
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = ViewConfiguration.get(context)
                .getScaledMaximumFlingVelocity();
        mMinmumVelocity = ViewConfiguration.get(context)
                .getScaledMinimumFlingVelocity();
        mAngleSize = getResources().getDimensionPixelSize(
                R.dimen.angleview_size);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d("LUORAN", "onMeasure");
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.d("LUORAN", "onDraw");
        canvas.drawRect(mRect, mPaint);
    }

    // 可滑动区域的touch事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        initVeloCityTracker(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastX = event.getX();
                mLastY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float newx = event.getX();
                float newy = event.getY();
                if (mPositionState == RgkPositionState.POSITION_STATE_LEFT) {
                    if (newx - mLastX > mTouchSlop
                            && Math.abs(newy - mLastY) > mTouchSlop
                            && mTouchState != TOUCH_STATE_SLIDE) {
                        mTouchState = TOUCH_STATE_SLIDE;

                        // 在这里打开catchView（left）
                        mListener.openLeft();

                    }
                } else if (mPositionState == RgkPositionState.POSITION_STATE_RIGHT) {
                    if (Math.abs(newx - mLastX) > mTouchSlop
                            && Math.abs(newy - mLastY) > mTouchSlop
                            && mTouchState != TOUCH_STATE_SLIDE) {
                        mTouchState = TOUCH_STATE_SLIDE;

                        // 在这里打开catchView（right）
                        mListener.openRight();

                    }
                }
                if (mTouchState == TOUCH_STATE_SLIDE) {

                    float p = (float) Math.sqrt(Math.pow((newx - mLastX), 2)
                            + Math.pow((newy - mLastY), 2));

                    // 在手指滑动的过程中不断传递一个值用于改变swipeLayout的背景和angleLayout的放大与缩小的状态
                    mListener.change(p < mAngleSize ? p / mAngleSize
                            : ((p - mAngleSize) / 2) / mAngleSize + 1);
                }
                break;
            case MotionEvent.ACTION_UP:
                mTouchState = TOUCH_STATE_REST;
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);

                // 获取滑动的速度(x方向和y方向)，如果速度满足，直接打开
                float vx = mVelocityTracker.getXVelocity();
                float vy = mVelocityTracker.getYVelocity();

                // 这个地方就是判断滑动catchview的速度
                if (vx > VELOCITY_2500 || vy < -VELOCITY_2500
                        || vx < -VELOCITY_2500) {
                    // 如果滑动的角度不对，则收回catchView
                    mListener.cancel(this, true);
                    // new RgkLayout(getContext()).startAnimation();
                } else {
                    mListener.cancel(this, false);
                    // new RgkLayout(getContext()).startAnimation();
                }
                recyleVelocityTracker();
                break;
            case MotionEvent.ACTION_CANCEL:
                mListener.cancel(this, false);
                recyleVelocityTracker();
                break;
        }
        return true;
    }

    /**
     * 设置颜色
     *
     * @param color
     */
    public void setColor(int color) {
        mPaint.setColor(color);
        invalidate();
    }

    /**
     * 初始化
     *
     * @param x
     * @param y
     */
    private void initManager(int x, int y) {
        mParams = new WindowManager.LayoutParams();
        mManager = (WindowManager) getContext().getSystemService(
                getContext().WINDOW_SERVICE);
        mParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        mParams.format = PixelFormat.RGBA_8888;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        if (mPositionState == RgkPositionState.POSITION_STATE_LEFT) {
            mParams.x = 0;
        } else if (mPositionState == RgkPositionState.POSITION_STATE_RIGHT) {
            mParams.x = x;
        }

        mParams.y = y;
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mManager.addView(this, mParams);
       /* if (mManager != null) {
            mManager.removeView(this);
        }*/
    }


    public void setState(int state, int left, int top, int width, int height) {

        Log.d("LUORAN", "setState");
        mPositionState = state;
        initManager(mDisplayWidth, mDisplayHeight);
        initRect(left, top, width, height);
    }

    public int getState() {
        return mPositionState;
    }

    /**
     * 初始化范围
     */
    private void initRect(int left, int top, int width, int height) {
        mRectLeft = left;
        mRectTop = top;
        mRectRight = mWidth = width;
        mRectBottom = mHeight = height;
        mRect.set(mRectLeft, mRectTop, mRectRight, mRectBottom);
        invalidate();
    }

    public boolean isManager() {
        return mManager != null;
    }

    public void show() {
        if (isManager()) {
            if (this.getParent() == null) {
                mManager.addView(this, mParams);
            }
        }
    }

    public void updata() {
        if (isManager()) {
            if (this.getParent() != null) {
                mManager.updateViewLayout(this, mParams);
            }
        }
    }

    public void dismiss() {
        if (isManager()) {
            if (this.getParent() != null) {
                mManager.removeView(this);
            }
        }
    }

    public void setOnEdgeSlidingListener(OnEdgeSlidingListener listener) {
        mListener = listener;
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
}
