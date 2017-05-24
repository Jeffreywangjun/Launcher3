package com.jeffrey.dynamicclock;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.format.Time;
import android.util.Log;
import android.view.View;

public class IconScript extends Drawable {

	Rect mRect = null;
	Context mcontext = null;
	private View mView;
	private ClockThread mClockThread = null;
	private boolean mIsShowInScreen = false;
	public boolean isRuning = false;
	// public FastBitmapDrawable mFastBitmapDrawable = null;
	protected Paint mPaint = new Paint();

	public IconScript(Context mcontext, View view) {
		this.mcontext = mcontext;
		mView = view;
		mPaint.setAntiAlias(true);
		mPaint.setFilterBitmap(true);
	}

	public void run() {
		isRuning = true;

		mRect = getBounds();
		if (mClockThread == null) {
			mClockThread = new ClockThread();
			mClockThread.start();
		}
/*		Log.e("XXX", "centerX------"+ mRect.centerX());
		Log.e("XXX", "centerY------"+ mRect.centerY());
		Log.e("XXX", "width------"+ mRect.width());
		Log.e("XXX", "height------"+ mRect.height());*/
	}

	@Override
	public void draw(Canvas canvas) {
		// TODO Auto-generated method stub
		mIsShowInScreen = true;
		drawIndicator(canvas, mRect.centerX(), mRect.centerY(), mPaint);
		if (mClockThread.wait) {
			mClockThread.resumeRun();
		}
	}

	@Override
	public void setAlpha(int alpha) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setColorFilter(ColorFilter colorFilter) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getOpacity() {
		// TODO Auto-generated method stub
		return 0;
	}

	private void drawIndicator(Canvas canvas, int centerX, int centerY, Paint p) {

		Log.d("lihuachun", "ClockScript:drawIndicator before");
		Time t = new Time();
		t.setToNow();
		p.setStrokeWidth(4);
		p.setColor(Color.BLACK);
		// canvas.drawBitmap(BitmapFactory.decodeResource(mcontext.getResources(),
		// R.drawable.android_clock_dial), 55, 7, p);
		Log.e("XXX", "centerX------"+ centerX);
		Log.e("XXX", "centerY------"+ centerY);
		Log.e("XXX", "width------"+ mRect.width());
		Log.e("XXX", "height------"+ mRect.height());
		canvas.drawLine(
				centerX,
				centerY,
				(int) (centerX + (mRect.width() / 2 - 38)
						* Math.cos((t.hour + (float) t.minute / 60)
								* (Math.PI / 6) - Math.PI / 2)),
				(int) (centerY + (mRect.width() / 2 - 38)
						* Math.sin((t.hour + (float) t.minute / 60)
								* (Math.PI / 6) - Math.PI / 2)), p);
		canvas.drawLine(
				centerX,
				centerY,
				(int) (centerX + (mRect.width() / 15)
						* Math.cos((t.hour + (float) t.minute / 60)
								* (Math.PI / 6) + Math.PI / 2)),
				(int) (centerY + (mRect.width() / 15)
						* Math.sin((t.hour + (float) t.minute / 60)
								* (Math.PI / 6) + Math.PI / 2)), p);
		p.setColor(Color.GRAY);
		p.setStrokeWidth(2);
		canvas.drawLine(
				centerX,
				centerY,
				(int) (centerX + (mRect.width() / 2 - 38)
						* Math.cos(t.minute * (Math.PI / 30) - Math.PI / 2)),
				(int) (centerY + (mRect.width() / 2 - 38)
						* Math.sin(t.minute * (Math.PI / 30) - Math.PI / 2)), p);
		canvas.drawLine(
				centerX,
				centerY,
				(int) (centerX + (mRect.width() / 15)
						* Math.cos(t.minute * (Math.PI / 30) + Math.PI / 2)),
				(int) (centerY + (mRect.width() / 15)
						* Math.sin(t.minute * (Math.PI / 30) + Math.PI / 2)), p);
		p.setColor(Color.RED);
		p.setStrokeWidth(1);
		canvas.drawLine(
				centerX,
				centerY,
				(int) (centerX + (mRect.width() / 2 - 38)
						* Math.cos(t.second * (Math.PI / 30) - Math.PI / 2)),
				(int) (centerY + (mRect.width() / 2 - 38)
						* Math.sin(t.second * (Math.PI / 30) - Math.PI / 2)), p);
		p.setStrokeWidth(1);
		canvas.drawLine(
				centerX,
				centerY,
				(int) (centerX + (mRect.width() / 15)
						* Math.cos(t.second * (Math.PI / 30) + Math.PI / 2)),
				(int) (centerY + (mRect.width() / 15)
						* Math.sin(t.second * (Math.PI / 30) + Math.PI / 2)), p);
		Log.d("lihuachun", "ClockScript:drawIndicator after");
		p.setColor(Color.parseColor("#FF666666"));
		// p.setColor(Color.GRAY);
		String clock = mcontext.getResources().getString(R.string.clock);
		p.setTextSize(24);
		canvas.drawText(clock, -27, 159, p);
	}

	class ClockThread extends Thread {
		int times = 0;
		boolean running = true;

		public boolean wait = false;

		public void stopRun() {
			running = false;
			synchronized (this) {
				this.notify();
			}
		};

		public void pauseRun() {
			this.wait = true;
			synchronized (this) {
				this.notify();
			}
		}

		public void resumeRun() {
			this.wait = false;
			synchronized (this) {
				this.notify();
			}
		}

		public void run() {
			while (running) {
				Log.d("lihuachun", "ClockScript:ClockThread running before");
				synchronized (mView) {
					mView.postInvalidate();

				}
				if (!mIsShowInScreen) {
					pauseRun();
				}
				mIsShowInScreen = false;
				try {
					Thread.sleep(500);
				} catch (Exception e) {
					System.out.println(e);
				}
				synchronized (this) {
					if (wait) {
						try {
							wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	
	/*
	 * public IconScript() { super(); }
	 * 
	 * public IconScript(Context mcontext) { this.mcontext = mcontext; }
	 * 
	 * public void run(View view) { isRuning = true; mView = view; mRect =
	 * getBounds(); if(mClockThread == null){ mClockThread = new ClockThread();
	 * mClockThread.start(); } }
	 * 
	 * public void onStop() { mClockThread.stopRun(); isRuning = false; }
	 * 
	 * public void onPause() { mClockThread.pauseRun(); isRuning = false; }
	 * 
	 * public void onResume() { mClockThread.resumeRun(); isRuning = true; }
	 */

}
