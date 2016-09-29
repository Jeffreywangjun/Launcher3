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
import android.graphics.Color;
import com.android.launcher3.R;


public class RgkAngleViewTheme extends PositionStateView {

	private Paint mPaint;
	private Paint rgkPaint;
	private Paint rgksPaint;

	private int rgkCornerSize;
	private int mColor;

	private int mWidth;

	private int mHeight;

	private int mInnerSize;

	private int mDistance;
	private int rgkDistance;

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
		rgkCornerSize=getResources().getDimensionPixelSize(R.dimen.rgk_corner_size);
		mInnerSize = getResources().getDimensionPixelSize(
				R.dimen.angleindicator_size);
		mDistance = getResources().getDimensionPixelSize(
				R.dimen.angleview_indicatorview_distance);
		rgkDistance=getResources().getDimensionPixelSize(R.dimen.rgk_distance);
		mColor = Color.GRAY;
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(mColor);
		mPaint.setAlpha(280);
		rgkPaint = new Paint();
		rgkPaint.setAntiAlias(true);
		rgkPaint.setStyle(Paint.Style.FILL);
		rgkPaint.setColor(Color.GREEN);
		rgkPaint.setAlpha(150);
		rgksPaint = new Paint();
		rgksPaint.setAntiAlias(true);
		rgksPaint.setStyle(Paint.Style.FILL);
		rgksPaint.setColor(Color.DKGRAY);
		rgksPaint.setAlpha(100);
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

			canvas.drawCircle(0, mHeight, rgkDistance, mPaint);
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));

			canvas.drawCircle(0, mHeight, mInnerSize + mDistance, rgkPaint);
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
			canvas.drawCircle(0, mHeight, mDistance + rgkCornerSize, rgksPaint);
			/* canvas.drawCircle(0, mHeight, mInnerSize, mPaint); */
			mPaint.setXfermode(null);
		} else if (isRight()) {

			canvas.drawCircle(mWidth, mHeight, rgkDistance, mPaint);
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));

			canvas.drawCircle(mWidth, mHeight, mInnerSize + mDistance, rgkPaint);
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
			canvas.drawCircle(mWidth, mHeight, mDistance + rgkCornerSize, rgksPaint);
			/* canvas.drawCircle(0, mHeight, mInnerSize, mPaint); */
			mPaint.setXfermode(null);
		}
	}

}
