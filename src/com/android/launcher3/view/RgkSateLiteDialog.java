package com.android.launcher3.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.launcher3.R;
import com.android.launcher3.utils.OnDialogListener;


public abstract class RgkSateLiteDialog extends RelativeLayout {

    public TextView mDialogTitle;

    public Button mPositiveBtn;

    public Button mNegativeBtn;

    public LinearLayout mContentLayout;

    public int mSize;

    public String mTitleFormat;
    
    public OnDialogListener mOnDialogListener;

    public RgkSateLiteDialog(Context context) {
        this(context, null);
    }

    public RgkSateLiteDialog(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RgkSateLiteDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mSize = getResources().getDimensionPixelSize(R.dimen.angleitem_size);
        mTitleFormat = getResources().getString(R.string.rgk_edit_header_title);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDialogTitle = (TextView) findViewById(R.id.swipe_edit_header_title);
        mContentLayout = (LinearLayout) findViewById(R.id.swipe_edit_content);
        //rgksGridView=(GridView)findViewById(R.id.swipe_edit_content_view);
        mPositiveBtn = (Button) findViewById(R.id.swipe_edit_footer_ok);
        mNegativeBtn = (Button) findViewById(R.id.swipe_edit_footer_cancel);
        mContentLayout.addView(createContentView());
    }

    public void setOnDialogListener(OnDialogListener listener) {
        mOnDialogListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    public abstract View createContentView();
}
