package com.android.launcher3.freezeapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.launcher3.R;

public class IceAnimationLayout extends ViewGroup {
	private static final String TAG = "RgkFp.IAnimationLayout";

	private int mSpace;
	int[] animRes;
	
	ImageView animView;

	public IceAnimationLayout(Context context) {
		this(context, null);
	}

	public IceAnimationLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public IceAnimationLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray ta = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.IceAnimationLayout, defStyle, 0);
		int count = ta.getIndexCount();
		for (int i = 0; i < count; i++) {
			switch (ta.getIndex(i)) {
			case R.styleable.IceAnimationLayout_space:
				mSpace = ta.getDimensionPixelSize(i, 0);
				Log.d(TAG, "mSpace=" + mSpace);
				break;
			}
		}
		ta.recycle();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		Log.d(TAG, "IceAnimationLayout[onFinishInflate]");
		animView = (ImageView) getChildAt(getChildCount() -1);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		int viewWidth = right - left;
		int viewHeight = bottom - top;
		int totalChildHeight = 0;
		int childCount = getChildCount();
		int firstChildTop;
		
		int childLeft = 0;
		int childTop = 0;
		int childRight = 0;
		int childBottom = 0;
		
		if (childCount > 1) { 
			View child;
			for (int i = 0; i < childCount -1; i++) {
					child = getChildAt(i);
					totalChildHeight += child.getMeasuredHeight();
			}
			totalChildHeight += mSpace * (childCount - 2);
			firstChildTop  = (viewHeight - totalChildHeight) >> 1;
			
			childBottom = firstChildTop - mSpace;//delete the first space
			for (int i = 0; i < childCount -1; i++) {
				child = getChildAt(i);
				childLeft = (viewWidth  - child.getMeasuredWidth()) >> 1;
				childTop = childBottom + mSpace;
				childRight = childLeft + child.getMeasuredWidth();
				childBottom = childTop + child.getMeasuredHeight();
				child.layout(childLeft, childTop, childRight, childBottom);
			}
			
			//layout the anim  View
			child = getChildAt(childCount -1);
			int delta = (child.getMeasuredWidth() - getChildAt(0).getMeasuredWidth()) >> 1;
			
			childLeft = (viewWidth  - child.getMeasuredWidth()) >> 1;
			childTop = firstChildTop - delta;
			childRight = childLeft + child.getMeasuredWidth();
			childBottom = childTop + child.getMeasuredHeight();
			child.layout(childLeft, childTop, childRight, childBottom);
		} else {
			throw new IllegalArgumentException("MUST has at least two child view!");
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		if (widthMode != MeasureSpec.EXACTLY
				|| heightMode != MeasureSpec.EXACTLY) {
			throw new IllegalArgumentException("mode must MeasureSpec.EXACTLY!");
		}
		Log.d(TAG, "IceAnimationLayout[onMeasure]---widthSize=" + widthSize
				+ ", heightSize=" + heightSize);

		for (int i = 0; i < getChildCount(); i++) {
			getChildAt(i).measure(
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		}

		int width = widthSize;
		int height = heightSize;

		setMeasuredDimension(width, height);
	}
	
	public void setAnimRes(int[] animRes) {
		this.animRes = animRes;
	}
	
	AnimatorUpdateListener updateListener = new AnimatorUpdateListener() {
		int mValue = -1;
		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			int value = (int) animation.getAnimatedValue();
			Log.d(TAG, "updateListener="+value);
			if (value < animRes.length && mValue != value) {
				animView.setImageResource(animRes[value]);
				mValue = value;
			}
			if (value == animRes.length -1) {
				setVisibility(View.GONE);
			}
		}
	};
	
	public void startAnim() {
		if (animRes != null && animRes.length > 0) {
			Log.d(TAG, "startAnim:" + animRes.length);
			int v = animView.getVisibility();
			Log.d(TAG, "startAnim:" + v);
			ValueAnimator va = ValueAnimator.ofInt(0, animRes.length -1);
			va.setDuration(50 * animRes.length);
			va.addUpdateListener(updateListener);
			va.start();
		}
	}
	
	private int shakeTranslationX = 7;
	private int shakeRepeatTime = 7;
	private int shakeDuration = 50;
	
	public void shakeChild(final int childIndex) {
		if (childIndex < getChildCount() -1) {
			ObjectAnimator oa = ObjectAnimator.ofFloat(getChildAt(childIndex), 
					"translationX", shakeTranslationX);
			oa.addListener(new AnimatorListenerAdapter() {

				@Override
				public void onAnimationEnd(Animator animation) {
					getChildAt(childIndex).setTranslationX(0);
				}
			});
			oa.setRepeatMode(ObjectAnimator.REVERSE);
			oa.setRepeatCount(shakeRepeatTime);
			oa.setDuration(shakeDuration);
			oa.start();
		}
	}
	
	public void shakeChildWithAnim(final int childIndex) {
		ObjectAnimator childOA = ObjectAnimator.ofFloat(getChildAt(childIndex), 
				"translationX", shakeTranslationX);
		childOA.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				getChildAt(childIndex).setTranslationX(0);
			}
		});
		childOA.setRepeatMode(ObjectAnimator.REVERSE);
		childOA.setRepeatCount(shakeRepeatTime);
		childOA.setDuration(shakeDuration);
		
		ObjectAnimator animViewOA = ObjectAnimator.ofFloat(animView, 
				"translationX", shakeTranslationX);
		animViewOA.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				animView.setTranslationX(0);
			}
		});
		animViewOA.setRepeatMode(ObjectAnimator.REVERSE);
		animViewOA.setRepeatCount(shakeRepeatTime);
		animViewOA.setDuration(shakeDuration);
		
		AnimatorSet mSet = new AnimatorSet();
		mSet.play(childOA).with(animViewOA);
		mSet.start();
	}
}
