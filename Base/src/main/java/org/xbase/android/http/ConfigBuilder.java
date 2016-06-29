package org.xbase.android.http;

import com.android.volley.VolleyLog;

/**
 * @author Ge Liang
 *
 */
public class ConfigBuilder{

	public boolean  Debug = true;
	public ConfigBuilder log(boolean islogable){
		VolleyLog.DEBUG = islogable;
		return this;
	}
}