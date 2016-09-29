package com.android.launcher3.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.android.launcher3.R;
import com.android.launcher3.utils.RgkPositionState;


public class RgkIndicatorView extends PositionStateView {

    private TextPaint mPaint0 = new TextPaint();

    private TextPaint mPaint1 = new TextPaint();

    private TextPaint mPaint2 = new TextPaint();

    private Paint[] mPaintArray = new Paint[]{mPaint0, mPaint1, mPaint2};

    private String mColors[] = new String[]{"#76EE00", "#eaeaea", "#e1e1e1", "#d6d6d6", "#cac9c9",
            "#bfbebe", "#b6b5b5", "#aeadad", "#a5a4a4", "#FFFFFF"};

    private Paint mArcPaint;

    private Paint mInnerPaint;

    private int mLeftOffset;

    private int mRightOffset;

    private int OFFSET_Y;

    private int mWidth;

    private int mHeight;

    private float mLastX;

    private float mLastY;

    private int mTouchSlop;

    public static final int DEGREES_90 = 90;

    private int DEGREES_U = DEGREES_90 / 8;

    private float mLeftArcStart = START_ANGLE;

    private float mRightArcStart = DEGREES_90 + START_ANGLE * 5;

    private float mArcSweep = START_ANGLE * 2;

    public static final float START_ANGLE = 11.25f;

    private int mIndicatorWidth;

    private int mIndicatorTextSize;

    public int mRect;

    private OnIndexChangedLitener mListener;

    public interface OnIndexChangedLitener {
        /**
         * 状态选中时
         *
         * @param index
         */
        void onIndexChanged(int index);
    }

    public RgkIndicatorView(Context context) {
        this(context, null);
    }

    public RgkIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RgkIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        mLeftOffset = getResources().getDimensionPixelSize(R.dimen.angleindicator_text_paddingleft);
        mRightOffset = getResources().getDimensionPixelOffset(R.dimen.angleindicator_text_paddingright);
        mRect = getResources().getDimensionPixelSize(R.dimen.angleindicator_arc_rect);
        mIndicatorWidth = getResources().getDimensionPixelSize(R.dimen.angleindicator_arc_width);
        mIndicatorTextSize = getResources().getDimensionPixelSize(R.dimen.angleindicator_arc_textsize);
        OFFSET_Y = getResources().getDimensionPixelSize(R.dimen.angleindicator_arc_textsize_offset_y);
        mPaint0.setColor(Color.parseColor(mColors[9]));
        mPaint0.setTextSize(mIndicatorTextSize);
        mPaint0.setAntiAlias(true);

        mPaint1.setColor(Color.parseColor(mColors[9]));
        mPaint1.setTextSize(mIndicatorTextSize);
        mPaint1.setAntiAlias(true);

        mPaint2.setColor(Color.parseColor(mColors[9]));
        mPaint2.setTextSize(mIndicatorTextSize);
        mPaint2.setAntiAlias(true);

        mArcPaint = new Paint();
        //mArcPaint.setColor(Color.WHITE);
        mArcPaint.setColor(getResources().getColor(R.color.indicator_theme_purple_selector));
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStrokeWidth(mIndicatorWidth);

        mInnerPaint = new Paint();
        mInnerPaint.setColor(getResources().getColor(R.color.indicator_theme_purple));
        mInnerPaint.setStyle(Paint.Style.FILL);
        mInnerPaint.setAntiAlias(true);
        mInnerPaint.setStrokeWidth(5);

        if (mPositionState == RgkPositionState.POSITION_STATE_LEFT) {
            setRotation(-90);
        }
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int degree = DEGREES_90 / 4;
        if (mPositionState == RgkPositionState.POSITION_STATE_LEFT) {
            canvas.save();
            //Fragment fragment=new Fragment();
            canvas.rotate(degree, 0, 0);
            mPaint0.setTextAlign(Paint.Align.LEFT);
            mPaint1.setTextAlign(Paint.Align.LEFT);
            mPaint2.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(getResources().getString(R.string.recent), mLeftOffset, OFFSET_Y, mPaint0);
            canvas.rotate(degree, 0, 0);
            canvas.drawText(getResources().getString(R.string.toolbox), mLeftOffset, OFFSET_Y, mPaint1);
            canvas.rotate(degree, 0, 0);
            canvas.drawText(getResources().getString(R.string.frequent), mLeftOffset, OFFSET_Y, mPaint2);
            canvas.restore();
        } else if (mPositionState == RgkPositionState.POSITION_STATE_RIGHT) {
            canvas.save();
            canvas.rotate(-degree, mWidth, 0);
            mPaint0.setTextAlign(Paint.Align.RIGHT);
            mPaint1.setTextAlign(Paint.Align.RIGHT);
            mPaint2.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(getResources().getString(R.string.recent), mRightOffset, OFFSET_Y, mPaint0);
            canvas.rotate(-degree, mWidth, 0);
            canvas.drawText(getResources().getString(R.string.toolbox), mRightOffset, OFFSET_Y, mPaint1);
            canvas.rotate(-degree, mWidth, 0);
            canvas.drawText(getResources().getString(R.string.frequent), mRightOffset, OFFSET_Y, mPaint2);
            canvas.restore();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    /**
     * 设置当前索引的变化监听
     *
     * @param listener
     */
    public void setOnChangeListener(OnIndexChangedLitener listener) {
        mListener = listener;
    }

    /**
     * 初始化当前选中的画笔的颜色
     *
     * @param current 当前高亮的画笔颜色
     */
    public void setCurrent(int current) {
        for (int i = 0; i < mPaintArray.length; i++) {
            if (i == current) {
                mPaintArray[i].setColor(Color.parseColor(mColors[0]));
            } else {
                mPaintArray[i].setColor(Color.parseColor(mColors[9]));
            }
        }
        invalidate();
    }

    /**
     * 设置是左还是右
     *
     * @param state
     */
    public void setPositionState(int state) {
        super.setPositionState(state);
        if (state == RgkPositionState.POSITION_STATE_LEFT) {
            setRotation(-DEGREES_90);
        } else if (state == RgkPositionState.POSITION_STATE_RIGHT) {
            setRotation(DEGREES_90);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastX = event.getX();
                mLastY = event.getY();

                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                
                float newx = event.getX();
                float newy = event.getY();
                if (Math.abs(newx - mLastX) < mTouchSlop || Math.abs(newy - mLastY) < mTouchSlop) {
                    double degree = 0;
                    if (mPositionState == RgkPositionState.POSITION_STATE_LEFT) {
                        degree = Math.toDegrees(Math.atan(newy / newx));
                    } else if (mPositionState == RgkPositionState.POSITION_STATE_RIGHT) {
                        degree = Math.toDegrees(Math.atan(newy / (mWidth - newx)));
                    }
                    if (degree > 0 && degree < DEGREES_U * 3) {
                        mListener.onIndexChanged(0);
                    } else if (degree > DEGREES_U * 3 && degree < DEGREES_U * 5) {
                        mListener.onIndexChanged(1);
                    } else if (degree > DEGREES_U * 5 && degree < DEGREES_U * 8) {
                        mListener.onIndexChanged(2);
                    }
                }
                break;
        }
        return true;
    }

    /**
     * 点击时改变指示器的颜色
     * 
     *  颜色的渐变就是在这个方法中实现
     * @param cur 当前的限象值index  0 1 2
     * @param pre
     */
    public void onAngleChanged2(int cur, float pre) {
    	// index 0~9之间来变化
        int index = (int) (pre * 10);
        
        
        Log.d("LUORAN11","index:"+index);
        
        
        Log.d("LUORAN11","cur:"+cur);
        if (mPositionState == RgkPositionState.POSITION_STATE_LEFT) {
            if (cur == 0) {
                //mLeftArcStart = 5 * START_ANGLE + 28 * START_ANGLE * (1 - pre);
                mPaint0.setColor(Color.parseColor(mColors[index]));
                mPaint2.setColor(Color.parseColor(mColors[9 - index]));
            } else if (cur == 1) {
                //mLeftArcStart = START_ANGLE + 2 * START_ANGLE * (1 - pre);
                mPaint1.setColor(Color.parseColor(mColors[index]));
                mPaint0.setColor(Color.parseColor(mColors[9 - index]));
            } else if (cur == 2) {
                mLeftArcStart = 3 * START_ANGLE + 2 * START_ANGLE * (1 - pre);
                mPaint2.setColor(Color.parseColor(mColors[index]));
                mPaint1.setColor(Color.parseColor(mColors[9 - index]));
            }
        } else if (mPositionState == RgkPositionState.POSITION_STATE_RIGHT) {
            if (cur == 0) {
                //mRightArcStart = DEGREES_90 + 3 * START_ANGLE + 2 * START_ANGLE * (1 - pre);
                mPaint0.setColor(Color.parseColor(mColors[index]));
                mPaint1.setColor(Color.parseColor(mColors[9 - index]));
            } else if (cur == 1) {
                if (pre < 0.5) {
                    mRightArcStart = DEGREES_90 - START_ANGLE + 2 * START_ANGLE * (1 - pre) - DEGREES_90 * 2 * pre;
                } else {
                    mRightArcStart = DEGREES_90 + 5 * START_ANGLE + 2 * START_ANGLE * (1 - pre) + DEGREES_90 * 2 * (1 - pre);
                }
                mPaint2.setColor(Color.parseColor(mColors[index]));
                mPaint0.setColor(Color.parseColor(mColors[9 - index]));
            } else if (cur == 2) {
                mRightArcStart = DEGREES_90 + START_ANGLE + 2 * START_ANGLE * (1 - pre);
                mPaint1.setColor(Color.parseColor(mColors[index]));
                mPaint2.setColor(Color.parseColor(mColors[9 - index]));
            }
        }
        //重新绘制颜色
        invalidate();
    }
}
