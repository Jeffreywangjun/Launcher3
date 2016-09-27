package com.android.launcher3.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

import com.android.launcher3.R;


public class GridLayoutItemView extends RgkItemLayout {

    private CheckBox mCheckBox;

    public GridLayoutItemView(Context context) {
        this(context, null);
    }

    public GridLayoutItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GridLayoutItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCheckBox = (CheckBox) findViewById(R.id.appindex_item_check);
    }

    public void setChecked(boolean check) {
        mCheckBox.setChecked(check);
    }

    public CheckBox getCheckBox() {
        return mCheckBox;
    }
}
