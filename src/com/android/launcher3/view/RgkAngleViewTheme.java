package com.android.launcher3.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.OvershootInterpolator;

import com.android.launcher3.R;


public class RgkAngleViewTheme extends PositionStateView {

	private Paint mPaint;

	private int mColor;

	private int mWidth;

	private int mHeight;

	private int mInnerSize;

	private int mDistance;

	public RgkAngleViewTheme(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public RgkAngleViewTheme(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public RgkAngleViewTheme(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub

		setWillNotDraw(false);

		mInnerSize = getResources().getDimensionPixelSize(
				R.dimen.angleindicator_size);
		mDistance = getResources().getDimensionPixelSize(
				R.dimen.angleview_indicatorview_distance);
		mColor = getResources().getColor(R.color.angleview_arc_background);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(mColor);
		mPaint.setAlpha(180);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		Log.d("LUORAN11", "isLeft()");
		mWidth = getMeasuredWidth();
		mHeight = getMeasuredHeight();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		if (isLeft()) {

			Log.d("LUORAN11", "isLeft()");
			canvas.drawCircle(0, mHeight, mHeight, mPaint);
			// ���û��ʵķ�ʽ
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));

			canvas.drawCircle(0, mHeight, mInnerSize + mHeight / 3, mPaint);
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
			canvas.drawCircle(0, mHeight, mInnerSize + 220, mPaint);
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
			canvas.drawCircle(0, mHeight, mInnerSize + 50, mPaint);

			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
			canvas.drawCircle(0, mHeight, mInnerSize + 47, mPaint);
			/*
			 * mPaint.setXfermode(new
			 * PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
			 * canvas.drawCircle(0, mHeight, mInnerSize, mPaint);
			 */
			// mPaint.setXfermode(new
			// PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
			// canvas.drawCircle(0, mHeight, mInnerSize + mDistance, mPaint);
			// canvas.drawCircle(0, mHeight, mInnerSize, mPaint);

			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
			canvas.drawCircle(0, mHeight, mInnerSize - 180, mPaint);
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
			canvas.drawCircle(0, mHeight, mInnerSize - 183, mPaint);
			mPaint.setXfermode(null);
		} else if (isRight()) {
			Log.d("LUORAN11", "isRight()");
			canvas.drawCircle(mWidth, mHeight, mHeight, mPaint);
			// ���û��ʵķ�ʽ
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));

			canvas.drawCircle(mWidth, mHeight, mInnerSize + mHeight / 3, mPaint);
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
			canvas.drawCircle(mWidth, mHeight, mInnerSize + 220, mPaint);
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
			canvas.drawCircle(mWidth, mHeight, mInnerSize + 50, mPaint);

			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
			canvas.drawCircle(mWidth, mHeight, mInnerSize + 47, mPaint);
			/*
			 * mPaint.setXfermode(new
			 * PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
			 * canvas.drawCircle(0, mHeight, mInnerSize, mPaint);
			 */
			// mPaint.setXfermode(new
			// PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
			// canvas.drawCircle(0, mHeight, mInnerSize + mDistance, mPaint);
			// canvas.drawCircle(0, mHeight, mInnerSize, mPaint);

			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
			canvas.drawCircle(mWidth, mHeight, mInnerSize - 180, mPaint);
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
			canvas.drawCircle(mWidth, mHeight, mInnerSize - 183, mPaint);
			mPaint.setXfermode(null);
		}
	}

}
