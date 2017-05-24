
package com.android.launcher3.IconClock;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.format.Time;
import android.util.Log;
import android.view.View;

import com.android.launcher3.R;

// Dynamic_clock_icon   ---this file
public class ClockScript extends IconScript {
    Rect mRect = null;
    Context mcontext = null;
    /**
     * Ð§ï¿½ï¿½Õ¹Ê¾Ä¿ï¿½ï¿½View
     */
    private View mView;
    /**
     * Í¨ÖªÏµÍ³ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½Í¼ï¿½Ö³ï¿½
     */
    private ClockThread mClockThread = null;
    /**
     * ï¿½ï¿½Ç°ï¿½Ç·ï¿½ï¿½ï¿½Ê¾ï¿½ï¿½ï¿½ï¿½Ä»ï¿½ï¿½
     */
    private boolean mIsShowInScreen = false;
    private Rect bounds;

    public ClockScript() {
        super();
    }

    public ClockScript(Context mcontext) {
        this.mcontext = mcontext;
    }

    public void run(View view) {


        mView = view;
        mRect = bounds;
        if (mClockThread == null) {
            mClockThread = new ClockThread();
            mClockThread.start();
        }
    }

    @Override
    public void onPause() {
        mClockThread.pauseRun();
        super.onPause();
    }

    @Override
    public void onResume() {
        mClockThread.resumeRun();
        super.onResume();
    }

    @Override
    public void onStop() {
        mClockThread.stopRun();
        super.onStop();
    }


    @Override
    public void draw(Canvas canvas) {


        Log.d("lihuachun", "ClockScript:draw before");

        mIsShowInScreen = true;

        drawIndicator(canvas, mRect.centerX() + 3, mRect.centerY() + 63, mPaint);


        if (mClockThread.wait) {
            mClockThread.resumeRun();
        }
        Log.d("lihuachun", "ClockScript:draw after");
    }

    /**
     * @param canvas
     * @param centerX
     * @param centerY
     * @param p
     */
    private void drawIndicator(Canvas canvas, int centerX, int centerY, Paint p) {

        Log.d("lihuachun", "ClockScript:drawIndicator before");
        Time t = new Time();
        t.setToNow();
        p.setStrokeWidth(2);
        p.setColor(Color.BLACK);
        canvas.drawBitmap(BitmapFactory.decodeResource(mcontext.getResources(), R.drawable.android_clock_dial), -55, 7, p);
        //Ê±Õë
        canvas.drawLine(centerX, centerY,
                (int) (centerX + (mRect.width() / 2 - 8) * Math.cos((t.hour + (float) t.minute / 60) * (Math.PI / 6) - Math.PI / 2)),
                (int) (centerY + (mRect.width() / 2 - 8) * Math.sin((t.hour + (float) t.minute / 60) * (Math.PI / 6) - Math.PI / 2)), p);
        canvas.drawLine(centerX, centerY,
                (int) (centerX + (mRect.width() / 2 - 23) * Math.cos((t.hour + (float) t.minute / 60) * (Math.PI / 6) + Math.PI / 2)),
                (int) (centerY + (mRect.width() / 2 - 23) * Math.sin((t.hour + (float) t.minute / 60) * (Math.PI / 6) + Math.PI / 2)), p);
        p.setColor(Color.GRAY);
        //·ÖÕë  
        canvas.drawLine(centerX, centerY,
                (int) (centerX + (mRect.width() / 2 - 8) * Math.cos(t.minute * (Math.PI / 30) - Math.PI / 2)),
                (int) (centerY + (mRect.width() / 2 - 8) * Math.sin(t.minute * (Math.PI / 30) - Math.PI / 2)), p);
        canvas.drawLine(centerX, centerY,
                (int) (centerX + (mRect.width() / 2 - 32) * Math.cos(t.minute * (Math.PI / 30) + Math.PI / 2)),
                (int) (centerY + (mRect.width() / 2 - 32) * Math.sin(t.minute * (Math.PI / 30) + Math.PI / 2)), p);
        p.setColor(Color.RED);
        //ÃëÕë  
        p.setStrokeWidth(1);
        canvas.drawLine(centerX, centerY,
                (int) (centerX + (mRect.width() / 2 - 8) * Math.cos(t.second * (Math.PI / 30) - Math.PI / 2)),
                (int) (centerY + (mRect.width() / 2 - 8) * Math.sin(t.second * (Math.PI / 30) - Math.PI / 2)), p);
        p.setStrokeWidth(1);
        canvas.drawLine(centerX, centerY,
                (int) (centerX + (mRect.width() / 2 - 34) * Math.cos(t.second * (Math.PI / 30) + Math.PI / 2)),
                (int) (centerY + (mRect.width() / 2 - 34) * Math.sin(t.second * (Math.PI / 30) + Math.PI / 2)), p);
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
        }

        ;

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
//  Log.d("lihuachun","ClockScript:ClockThread running after");
    }

}  