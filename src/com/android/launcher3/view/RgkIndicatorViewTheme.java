package com.android.launcher3.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;

import com.android.launcher3.R;
import com.android.launcher3.utils.RgkPositionState;


/*
 *
 * 默认蓝色的指示器
 */
public class RgkIndicatorViewTheme extends PositionStateView {

    private Paint mBoundPaint;

    private Paint mPaint;

    private int mColor;

    private int mWidth;

    private int mHeight;

    private RectF mInneRectF = new RectF();

    private float mInnerSize;

    private RectF mMindRectF = new RectF();

    private float mMidSize;

    private RectF mOuterRecF = new RectF();

    private float mOuterSize;

    private float mUtilAngle = 90f / 8f;

    private float mLeftStartAngle = mUtilAngle;

    private float mRightStartAngle = mUtilAngle * 5 + DEGREES_90;

    public static final int DEGREES_90 = 90;

    public RgkIndicatorViewTheme(Context context) {
        this(context, null);
    }

    public RgkIndicatorViewTheme(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RgkIndicatorViewTheme(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        mColor = getResources().getColor(R.color.indicator_color);
        mBoundPaint = new Paint();
    
        mBoundPaint.setAntiAlias(true);
        mBoundPaint.setColor(mColor);
        mBoundPaint.setStyle(Paint.Style.STROKE);
        mBoundPaint.setStrokeWidth(20);
        mBoundPaint.setStrokeCap(Paint.Cap.ROUND);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mColor);
        mPaint.setStyle(Paint.Style.STROKE);
        //mPaint.setAlpha(180);
        mWidth = getResources().getDimensionPixelSize(R.dimen.angleindicator_theme_size);
        mHeight = getResources().getDimensionPixelSize(R.dimen.angleindicator_theme_size);

        mInnerSize = getResources().getDimensionPixelSize(R.dimen.angleindicator_theme_innersize);
        mOuterSize = getResources().getDimensionPixelSize(R.dimen.angleindicator_theme_outersize);

        mMidSize = mInnerSize + (mOuterSize - mInnerSize) / 2;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mPositionState == RgkPositionState.POSITION_STATE_LEFT) {

            mInneRectF.left = -mInnerSize;
            mInneRectF.top = mHeight - mInnerSize;
            mInneRectF.right = mInnerSize;
            mInneRectF.bottom = mHeight + mInnerSize;

            mOuterRecF.left = -mOuterSize;
            mOuterRecF.top = mHeight - mOuterSize;
            mOuterRecF.right = mOuterSize;
            mOuterRecF.bottom = mHeight + mOuterSize;

            mMindRectF.left = -mMidSize;
            mMindRectF.top = mHeight - mMidSize;
            mMindRectF.right = mMidSize;
            mMindRectF.bottom = mHeight + mMidSize;
            mPaint.setStrokeWidth(mOuterSize - mInnerSize);
        } else if (mPositionState == RgkPositionState.POSITION_STATE_RIGHT) {
            mInneRectF.left = mWidth - mInnerSize;
            mInneRectF.top = mHeight - mInnerSize;
            mInneRectF.right = mWidth + mInnerSize;
            mInneRectF.bottom = mHeight + mInnerSize;

            mOuterRecF.left = mWidth - mOuterSize;
            mOuterRecF.top = mHeight - mOuterSize;
            mOuterRecF.right = mWidth + mOuterSize;
            mOuterRecF.bottom = mHeight + mOuterSize;

            mMindRectF.left = mWidth - mMidSize; 
            mMindRectF.top = mHeight - mMidSize;
            mMindRectF.right = mWidth + mMidSize;
            mMindRectF.bottom = mHeight + mMidSize;
            mPaint.setStrokeWidth(mOuterSize - mInnerSize);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        
        
        /**
         * drawArc参数
         * 1：圆弧所在的椭圆对象
         * 2：圆弧的起始角度
         * 3：圆弧的角度
         * 4：是否显示半径连线
         */
        if (mPositionState == RgkPositionState.POSITION_STATE_LEFT) {
            canvas.drawArc(mInneRectF, 270 + mLeftStartAngle + 6f, mUtilAngle * 1 - 0.8f, false, mBoundPaint);

            canvas.drawArc(mOuterRecF, 270 + mLeftStartAngle + 3.5f, mUtilAngle * 2 - 7f, false, mBoundPaint);
            canvas.drawArc(mMindRectF, 270 + mLeftStartAngle, mUtilAngle * 2, false, mPaint);
            //canvas.drawRoundRect(5.1f,5.1f,5.1f,5.1f,2.45f,2.45f,mPaint);
        } else if (mPositionState == RgkPositionState.POSITION_STATE_RIGHT) {
            canvas.drawArc(mInneRectF, 90 + mRightStartAngle + 6f, mUtilAngle * 1 - 0.8f, false, mBoundPaint);
            canvas.drawArc(mOuterRecF, 90 + mRightStartAngle + 3.5f, mUtilAngle * 2 - 7f, false, mBoundPaint);
            canvas.drawArc(mMindRectF, 90 + mRightStartAngle, mUtilAngle * 2, false, mPaint);

            //canvas.drawR
        }
    }

    @Override
    public void setPositionState(int state) {
        super.setPositionState(state);
    }
    //旋转的时候切换指示器的角度
    
    public void changeStartAngle(int cur, float pre) {

        Log.d("LUORAN77","changeStartAngle");

 

        if (mPositionState == RgkPositionState.POSITION_STATE_LEFT) {
            if (cur == 0) {
                mLeftStartAngle = 5 * mUtilAngle + 28 * mUtilAngle * (1 - pre);

            } else if (cur == 1) {
                mLeftStartAngle = mUtilAngle + 2 * mUtilAngle * (1 - pre);

            } else if (cur == 2) {
                mLeftStartAngle = 3 * mUtilAngle + 2 * mUtilAngle * (1 - pre);

            }
        } else if (mPositionState == RgkPositionState.POSITION_STATE_RIGHT) {
            if (cur == 0) {
                mRightStartAngle = DEGREES_90 + 3 * mUtilAngle + 2 * mUtilAngle * (1 - pre);

            } else if (cur == 1) {
                if (pre < 0.5) {
                    mRightStartAngle = DEGREES_90 - mUtilAngle + 2 * mUtilAngle * (1 - pre) - DEGREES_90 * 2 * pre;
                } else {
                    mRightStartAngle = DEGREES_90 + 5 * mUtilAngle + 2 * mUtilAngle * (1 - pre) + DEGREES_90 * 2 * (1 - pre);
                }

            } else if (cur == 2) {
                mRightStartAngle = DEGREES_90 + mUtilAngle + 2 * mUtilAngle * (1 - pre);

            }
        }
        invalidate();
    }

}
