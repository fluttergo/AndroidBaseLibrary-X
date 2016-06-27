package org.xbase.android.image;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.nostra13.universalimageloader.core.imageaware.ViewAware;

public class BackgroundViewAware extends ViewAware {

	public BackgroundViewAware(View view, boolean checkActualViewSize) {
		super(view, checkActualViewSize);
	}

	@Override
	protected void setImageBitmapInto(Bitmap bitmap, View view) {
		AvoidRecycledBitmapDrawable rbd = new AvoidRecycledBitmapDrawable(
				bitmap);
		view.setBackgroundDrawable(rbd);
	}

	@Override
	protected void setImageDrawableInto(Drawable drawable, View view) {
		view.setBackgroundDrawable(drawable);
	}

}
