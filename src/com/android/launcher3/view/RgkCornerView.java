package com.android.launcher3.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.launcher3.R;
import com.android.launcher3.utils.RgkPositionState;


/**
 * 角落里的状态View分别在正常模式，编辑模式，拖动模式下呈现不同的状态给用户
 */
public class RgkCornerView extends FrameLayout {

	private int mHeight;

	private int mWidth;

	private ImageView mIcon;

	private int mPositionState;

	private int mIconSize;

	private int mIconOffset;

	private int mState = STATE_NORMAL;

	public static final int STATE_NORMAL = 1;

	public static final int STATE_EDIT = 2;

	public static final int STATE_DRAG = 3;

	private float mLastEventX;

	private float mLastEventY;

	private long mClickTime;

	private OnCornerClickListener mListener;

	public interface OnCornerClickListener {
		/**
		 * 点击事件
		 */
		void cornerEvent();
	}

	public RgkCornerView(Context context) {
		this(context, null);
	}

	public RgkCornerView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RgkCornerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mIcon = new ImageView(context);
		mIconSize = context.getResources().getDimensionPixelSize(
				R.dimen.corner_icon_size);
		mIconOffset = context.getResources().getDimensionPixelSize(
				R.dimen.corner_icon_offset);
		LayoutParams params = new LayoutParams(mIconSize, mIconSize);
		setState(STATE_NORMAL);
		addView(mIcon, params);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mWidth = getMeasuredWidth();
		mHeight = getMeasuredHeight();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		// mBackgroung.layout(0, 0, mWidth, mHeight);
		mIcon.layout(0, 0, mWidth, mHeight);
		if (isLeft()) {
			mIcon.layout(mIconOffset, mHeight - mIconSize - mIconOffset,
					mIconSize + mIconOffset, mHeight - mIconOffset);
		} else if (isRight()) {
			mIcon.layout(mWidth - mIconSize - mIconOffset, mHeight - mIconSize
					- mIconOffset, mWidth - mIconOffset, mHeight - mIconOffset);
		}
	}

	public void setPositionState(int state) {
		mPositionState = state;
		// mBackgroung.setPositionState(state);
		invalidate();
	}

	public void setOnCornerListener(OnCornerClickListener listener) {
		mListener = listener;
	}

	public boolean isLeft() {
		return mPositionState == RgkPositionState.POSITION_STATE_LEFT;
	}

	public boolean isRight() {
		return mPositionState == RgkPositionState.POSITION_STATE_RIGHT;
	}

	public int getState() {
		return mState;
	}

	public void setState(int state) {
		mState = state;
		if (mState == STATE_NORMAL) {
			mIcon.setImageDrawable(getContext().getResources().getDrawable(
					R.drawable.close));
		} else if (mState == STATE_EDIT) {
			// mIcon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_arrow_back));
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastEventX = event.getX();
			mLastEventY = event.getY();
			mClickTime = System.currentTimeMillis();
			break;
		case MotionEvent.ACTION_MOVE:

			break;
		case MotionEvent.ACTION_UP:
			float newX = event.getX();
			float newY = event.getY();
			long clicktime = System.currentTimeMillis();
			if (Math.abs(mLastEventX - newX) < 10
					&& Math.abs(mLastEventY - newY) < 10) {
				long time = Math.abs(mClickTime - clicktime);
				// 长按不消失
				if (time < 300) {
					mListener.cornerEvent();
				}
			}
			break;
		}
		return true;
	}
}
