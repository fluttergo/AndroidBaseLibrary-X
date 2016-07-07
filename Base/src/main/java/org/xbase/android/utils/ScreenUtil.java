package org.xbase.android.utils;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class ScreenUtil {
	private static float height = 0;
	private static float width = 0;
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static float getSceenHight(Context context) {
		if (height!=0) {
			return height;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			Point outZise = new Point();
			((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(outZise);
			height = outZise.y;
		}else{
			DisplayMetrics displaymetrics = new DisplayMetrics();
			((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
			.getDefaultDisplay().getMetrics(displaymetrics);
			height  = displaymetrics.heightPixels;
		}
		return height;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static float getScreenWidth(Context context) {
		if (width!=0) {
			return width;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			Point outZise = new Point();
			((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(outZise);
			width = outZise.x;
		}else{
			DisplayMetrics displaymetrics = new DisplayMetrics();
			((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
			.getDefaultDisplay().getMetrics(displaymetrics);
			width  = displaymetrics.widthPixels;
		}
		return width;
	}

}
