package com.android.launcher3.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.app.WallpaperManager;
import android.graphics.drawable.Drawable;

import com.android.launcher3.R;
import com.android.launcher3.utils.RgkSateLiteWindowManager;

//滑动弹出区域

public class RgkParentLayout extends RelativeLayout implements
        RgkLayout.OnOffListener {

    private RgkSateLiteWindowManager mManager;

    private RgkLayout mAngleLayout;

    // private LinearLayout mBgLayout;

    private RgkSateLiteFavoriteDialog mFavoriteLayout;

    private RgkSateLiteToolsDialog mToolsLayout;

    // private SwipeToast mSwipeToast;

    private int mWidth;

    private int mHeight;

    static int mAnimatorDur = 250;
    private WallpaperManager wallpaperManager;

    public RgkParentLayout(Context context) {
        this(context, null);
    }

    public RgkParentLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RgkParentLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        wallpaperManager = (WallpaperManager) context
                .getSystemService(Context.WALLPAPER_SERVICE);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        mFavoriteLayout = (RgkSateLiteFavoriteDialog) LayoutInflater.from(
                context).inflate(R.layout.rgk_dialog_layout, null);
        addView(mFavoriteLayout, params);

        mToolsLayout = (RgkSateLiteToolsDialog) LayoutInflater.from(context)
                .inflate(R.layout.rgk_tools_layout, null);
        addView(mToolsLayout, params);

    }

    // 加载xml文件时候调用(LayoutInflater.from(context).inflate(int layoutId,false))
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAngleLayout = (RgkLayout) findViewById(R.id.anglelayout);
        mAngleLayout.setOnOffListener(this);
        //mBgLayout = (LinearLayout) findViewById(R.id.swipe_bg_layout);
        mManager = new RgkSateLiteWindowManager(0, 0, getContext());

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    /**
     * 向左切换
     */
    public void switchLeft() {
        show();

        // 控制弹出起始位置
        if (isSwipeOff()) {
            mAngleLayout.setPositionLeft();
        }
    }

    /**
     * 向右切换
     */
    public void switchRight() {
        show();
        if (isSwipeOff()) {
            mAngleLayout.setPositionRight();
        }
    }

    public boolean hasView() {
        return mManager.hasView(this);
    }

    /**
     * 是否关闭
     *
     * @return
     */
    public boolean isSwipeOff() {
        return mAngleLayout.getSwitchType() == RgkLayout.SWITCH_TYPE_OFF;
    }

    public void show() {
        mManager.show(this);
        View backgroundView = this.findViewById(R.id.blurred_view);
        // int backgroundResourceID = getArguments().getInt(KEY_BG_RESOURCE_ID,
        // 0);
        Drawable drawable = wallpaperManager.getDrawable();
        if (drawable != null) {
            backgroundView.setBackground(drawable);
        }
        BlurringView blurringView = (BlurringView) this
                .findViewById(R.id.blurring_view);
        blurringView.setBlurredView(backgroundView);
    }

    public void dismiss() {
        mManager.hide(this);
    }

    public void dismissAnimator() {
        mAngleLayout.offnoAnimator();
        dismiss();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d("LUORAN55", "RgkLayout.KEYCODE_BACK");
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                 || event.getKeyCode() == KeyEvent.KEYCODE_HOME
                && event.getAction() != KeyEvent.ACTION_UP) {
            if (mFavoriteLayout.getVisibility() == VISIBLE) {
                setEditFavoriteGone();
                return true;
            }

            if (mToolsLayout.getVisibility() == VISIBLE) {
                setEditToolsGone();
                return true;
            }

            if (mAngleLayout.getEditState() == RgkLayout.STATE_EDIT) {
                Log.d("LUORAN55", "RgkLayout.STATE_EDIT");
                mAngleLayout.setEditState(RgkLayout.STATE_NORMAL);
                return true;
            }
            if (mAngleLayout.getEditState() == RgkLayout.STATE_NORMAL) {
                Log.d("LUORAN55", "RgkLayout.STATE_NORMAL");
                mAngleLayout.off();
                return true;
            }
            return true;
        }else if(event.getKeyCode() == KeyEvent.KEYCODE_MENU){
            if (mAngleLayout.getEditState() == RgkLayout.STATE_NORMAL) {
                Log.d("LUORAN55", "RgkLayout.STATE_NORMAL");
                mAngleLayout.off();
                return true;
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * 切换AngleLayout
     */
    public void switchAngleLayout() {
        mAngleLayout.switchAngleLayout();
    }

    public void on() {

        Log.d("LUORAN12", "swipelayout");
        mAngleLayout.on();
    }

    @Override
    public void onOff() {
        dismiss();
    }

    @Override
    public void change(float alpha) {
        setSwipeBackgroundViewAlpha(alpha);
    }

    /**
     * 改变背景的透明度
     *
     * @param a
     */
    public void setSwipeBackgroundViewAlpha(float a) {
        //mBgLayout.setAlpha(((int) (a * 10) / 5f));
    }

    public RgkLayout getAngleLayout() {
        return mAngleLayout;
    }

    public RgkSateLiteFavoriteDialog getEditFavoriteLayout() {
        return mFavoriteLayout;
    }

    public RgkSateLiteToolsDialog getEditToolsLayout() {
        return mToolsLayout;
    }

    /**
     * 打开Favorite编辑Dialog
     */

    public void setEditFavoritetVisiable() {
        mFavoriteLayout.setVisibility(INVISIBLE);
        mFavoriteLayout.bringToFront();
        showAnimator(mFavoriteLayout);
    }

    /**
     * 关闭Favorite编辑Dialog
     */

    public void setEditFavoriteGone() {
        hideAnimator(mFavoriteLayout);
    }

    public void setEditToolsVisiable() {
        mToolsLayout.setVisibility(INVISIBLE);
        mToolsLayout.bringToFront();
        showAnimator(mToolsLayout);
    }

    public void setEditToolsGone() {
        hideAnimator(mToolsLayout);
    }

    /**
     * dialog打开动画
     *
     * @param view
     */
    public void showAnimator(final View view) {

        Log.d("LUORAN99", "showAnimator");
        final AnimatorSet animSet = new AnimatorSet();
        ObjectAnimator scaleAnim = ObjectAnimator.ofFloat(view, "translationX",
                -500f, 1f);
        scaleAnim.setDuration(1000);
        scaleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // float s = (float) animation.get
                // view.setPivotX(view.getHeight());
                // view.setPivotY(view.getHeight());
            }
        });

        ValueAnimator alphaAnim = ValueAnimator.ofFloat(0f, 1f);
        alphaAnim.setDuration(1000);
        alphaAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float a = (float) animation.getAnimatedValue();
                view.setAlpha(a);
            }
        });
        animSet.playTogether(scaleAnim, alphaAnim);
        post(new Runnable() {
            @Override
            public void run() {
                view.setPivotY(view.getHeight() / 2);
                view.setPivotX(view.getWidth() / 2);
                view.setVisibility(View.VISIBLE);
                animSet.start();
            }
        });
    }

    /**
     * dialog关闭动画
     *
     * @param view
     */
    public void hideAnimator(final View view) {
        Log.d("LUORAN99", "hideAnimator");
        final AnimatorSet animSet = new AnimatorSet();
        ObjectAnimator scaleAnim = ObjectAnimator.ofFloat(view, "translationX",
                1f, 500f);
        scaleAnim.setDuration(800);
        scaleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                /*
                 * float s = (float) animation.getAnimatedValue();
				 * view.setScaleX(s); view.setScaleY(s);
				 */
            }
        });

        ValueAnimator alphaAnim = ValueAnimator.ofFloat(1f, 0f);
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
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animSet.playTogether(scaleAnim, alphaAnim);
        animSet.start();
    }

	/*
     * public void removeBubble() { mFavoriteLayout.setVisibility(View.GONE); }
	 */

}
