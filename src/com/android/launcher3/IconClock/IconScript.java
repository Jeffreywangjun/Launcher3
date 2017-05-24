package com.android.launcher3.IconClock;
import com.android.launcher3.FastBitmapDrawable;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Color;
import android.view.View;
import android.util.Log;
/**
 * 
 * @author huachun.li
 * @date 2017-02-22 
 */

// Dynamic_clock_icon   ---this file
public class IconScript extends Drawable {
	
	private static final String TAG = "IconScript";

	 public boolean isRuning = false;  
    public FastBitmapDrawable mFastBitmapDrawable = null;  
    protected Paint mPaint = new Paint();  
      
    public IconScript(){  
        mPaint.setAntiAlias(true);   
        mPaint.setFilterBitmap(true);  
    }  
      
    public void draw(Canvas canvas){  

		 Log.d("lihuachun","IconScript:draw before");
        if(mFastBitmapDrawable != null){  
            canvas.drawBitmap(mFastBitmapDrawable.getBitmap(), null, getBounds(),mPaint);//»­µ×Í¼  
        }  

		Log.d("lihuachun","IconScript:draw after");
    }  
      

    public void run(View view){  
        isRuning = true;  
    }  
      

    public void onStop(){  
        isRuning = false;  
    }  
      

    public void onPause(){  
        isRuning = false;  
    }  
      

    public void onResume(){  
        isRuning = true;  
    }  

    @Override  
    public int getOpacity() {  
        // TODO Auto-generated method stub  
        return 0;  
    }  
  
    @Override  
    public void setAlpha(int arg0) {  
        // TODO Auto-generated method stub  
          
    }  
  
    @Override  
    public void setColorFilter(ColorFilter arg0) {  
        // TODO Auto-generated method stub  
          
    }  
      
    @Override  
    public int getIntrinsicWidth() {  
        int width = getBounds().width();  
        if (width == 0) {  
            width = mFastBitmapDrawable.getBitmap().getWidth();  
        }  
        return width;  
    }  
  
    @Override  
    public int getIntrinsicHeight() {  
        int height = getBounds().height();  
        if (height == 0) {  
            height = mFastBitmapDrawable.getBitmap().getHeight();  
        }  
        return height;  
    }  
  
    @Override  
    public int getMinimumWidth() {  
        return getBounds().width();  
    }  
  
    @Override  
    public int getMinimumHeight() {  
        return getBounds().height();  
    }  
      
    @Override  
    public void setFilterBitmap(boolean filterBitmap) {  
        mPaint.setFilterBitmap(filterBitmap);  
        mPaint.setAntiAlias(filterBitmap);  
    }  
      
    public void setFastBitmapDrawable(FastBitmapDrawable drawable){  
        mFastBitmapDrawable = drawable;  
    }  
      
    public FastBitmapDrawable getFastBitmapDrawable(){  
        return mFastBitmapDrawable;  
    }  

	
}
