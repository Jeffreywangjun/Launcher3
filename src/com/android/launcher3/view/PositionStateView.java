package com.android.launcher3.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.android.launcher3.utils.RgkPositionState;


public class PositionStateView extends ViewGroup {
	public int mPositionState = RgkPositionState.POSITION_STATE_LEFT;

	public PositionStateView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public PositionStateView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public PositionStateView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public void setPositionState(int state) {
		this.mPositionState = state;
		invalidate();
	}

	// �жϻ��������Ƿ�����Ļ���
	public boolean isLeft() {

		return mPositionState == RgkPositionState.POSITION_STATE_LEFT;
	}

	// �жϻ��������Ƿ�����Ļ�ұ�
	public boolean isRight() {
		return mPositionState == RgkPositionState.POSITION_STATE_RIGHT;
	}

	@Override
	protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
		// TODO Auto-generated method stub

	}

	public int getPositionState() {
		return mPositionState;
	}
}
