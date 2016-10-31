package com.android.launcher3.music;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.android.launcher3.R;
public class Xuanzhuan {
	Context context;
	Drawable drawable2;
	BitmapDrawable bitmapDrawable2;
	Bitmap bitmap2;
	Drawable drawable22;
	BitmapDrawable bitmapDrawable22;
	Bitmap bitmap22;

	public Xuanzhuan(Context context) {
		this.context = context;
		drawable2 = context.getResources().getDrawable(
				R.drawable.icon_panel_progress_thumb2);
		bitmapDrawable2 = (BitmapDrawable) drawable2;
		bitmap2 = bitmapDrawable2.getBitmap();
		drawable22 = context.getResources().getDrawable(
				R.drawable.icon_panel_progress_barleft);
		bitmapDrawable22 = (BitmapDrawable) drawable22;
		bitmap22 = bitmapDrawable22.getBitmap();
	}

	public Bitmap rotate(int degrees) {
		if (degrees != 0 && bitmap2 != null) {
			Matrix m = new Matrix();
			m.setRotate(degrees, bitmap2.getWidth() / 2,
					bitmap2.getHeight() / 2);
			try {
				bitmap2 = Bitmap.createBitmap(bitmap2, 0, 0,
						bitmap2.getWidth(), bitmap2.getHeight(), m, true);
				// WidgetBitmap.setBitmap(Bitmap.createBitmap(b, 0, 0,
				// b.getWidth(), b.getHeight(), m, true));
			} catch (OutOfMemoryError ex) {
				ex.printStackTrace();
			}
		}
		return bitmap2;
	}

	public Bitmap rotate2(int degrees) {
		if (degrees != 0 && bitmap22 != null) {
			Matrix m = new Matrix();
			m.setRotate(degrees, bitmap22.getWidth() / 2,
					bitmap22.getHeight() / 2);
			try {
				bitmap22 = Bitmap.createBitmap(bitmap22, 0, 0,
						bitmap22.getWidth(), bitmap22.getHeight(), m, true);
				// WidgetBitmap.setBitmap2(Bitmap.createBitmap(b, 0, 0,
				// b.getWidth(), b.getHeight(), m, true));
			} catch (OutOfMemoryError ex) {
				ex.printStackTrace();
			}
		}
		return bitmap22;
	}
}
