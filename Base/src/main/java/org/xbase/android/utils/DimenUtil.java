package org.xbase.android.utils;


import android.util.TypedValue;

/**
 * 尺寸单位转换工具
 * 
 * @author Administrator
 * 
 */
public class DimenUtil {
	public static float sp2px(float sp) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
				App.getInstace().getResources()
						.getDisplayMetrics());
	}

	public static float dp2px(float dp) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				App.getInstace().getResources()
						.getDisplayMetrics());
	}
}
