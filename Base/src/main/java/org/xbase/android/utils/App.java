package org.xbase.android.utils;


import org.xbase.android.http.XHttp;

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
	}

	public static Application getInstace() {
		return instance;
	}
}
