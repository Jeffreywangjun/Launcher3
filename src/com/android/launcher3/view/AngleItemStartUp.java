package com.android.launcher3.view;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.android.launcher3.R;


public class AngleItemStartUp extends RgkItemLayout {

    private ImageView mDelIcon;

    public RecentTag mRecentTag;

    public AngleItemStartUp(Context context) {
        super(context);
    }

    public AngleItemStartUp(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AngleItemStartUp(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDelIcon = (ImageView) findViewById(R.id.angle_item_delete);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void showDelBtn() {
        mDelIcon.setVisibility(View.VISIBLE);
    }

    public void hideDelBtn() {
        mDelIcon.setVisibility(View.GONE);
    }

    public View getDelBtn() {
        return mDelIcon;
    }


    public static class RecentTag {

        public ActivityInfo info;

        public Intent intent;

    }
}
