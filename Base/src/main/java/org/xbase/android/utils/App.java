package org.xbase.android.utils;


import org.xbase.android.http.XHttp;
import org.xbase.android.image.ImageUtils;

import android.app.Application;

public class App  {
	public static  boolean isdeug = false;
	private static Application instance;
	public static void init(Application app) {
		instance = app;
		// setup the hot patch
		// HotFixManager.getInstance().init(this);
		XHttp.app = app;
//		XHttp.getConfigBuilder().log(true);
		ImageUtils.getInstance().init(app);
	}

	public static Application getInstace() {
		return instance;
	}
}
