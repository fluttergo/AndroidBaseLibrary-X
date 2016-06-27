package org.xbase.android.image;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

public class AvoidRecycledBitmapDrawable extends BitmapDrawable {

	/**
	 * Create drawable from a bitmap, setting initial target density based on
	 * the display metrics of the resources.
	 */
	public AvoidRecycledBitmapDrawable(Bitmap bitmap) {
		super(bitmap);
	}

	@Override
	public void draw(Canvas canvas) {
		if (this.getBitmap() != null && !this.getBitmap().isRecycled()) {
			super.draw(canvas);
		}
	}

}
