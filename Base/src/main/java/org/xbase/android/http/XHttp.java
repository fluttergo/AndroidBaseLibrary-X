package org.xbase.android.http;

import java.util.Map;

import android.app.Application;
import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.StringUtil;
import com.android.volley.VolleyUtil;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;
/**
 * Http base on Volley
 * @author Ge Liang
 */
public class XHttp {
	public static Application app;
	private static ConfigBuilder defaultConfig =new ConfigBuilder();
	public static ConfigBuilder getConfigBuilder(){
		return defaultConfig;
	}
	public static void get(String url, AjaxCallBack callback) {
		VolleyUtil.getQueue(app)
				.add(new StringRequest(url, callback, callback));
	}
	public static void download(String url, AjaxCallBack callback) {
		VolleyUtil.getQueue(app)
				.add(new FileRequest(url, callback, callback));
	}

	public static void downloadAndForverCache(String url, AjaxCallBack callback) {
		final FileRequest request = new FileRequest(url, callback, callback);
		request.isAliveForver = true;
		VolleyUtil.getQueue(app).add(request);
	}

	
    /**
     * @param dstURL
     * @param pars
     * @param callBack
     */
    public void post(Context context, String dstURL, final AjaxParams pars, final AjaxCallBack callBack) {

        StringRequest request =
            new StringRequest(Method.POST, StringUtil.preUrl(dstURL.toString().trim()), callBack, callBack) {

                // 重写getParams设置post请求的参数
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    return pars.getParamMap();
                }

            };
        // 请求加上Tag,用于取消请求
        request.setTag(context == null ? dstURL : context);
        VolleyUtil.getQueue().add(request);

    }

    /**
     * @param dstURL
     * @param pars
     * @param callBack
     */
    public void post(String dstURL, final AjaxParams pars, final AjaxCallBack callBack) {
        post(null, dstURL, pars, callBack);
    }
}

