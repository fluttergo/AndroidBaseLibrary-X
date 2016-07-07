package org.xbase.android.http;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache.Entry;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyUtil;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpHeaderParser;
/**
 * 文件下载,默认返回下载URL
 * @author Ge Liang
 *
 */
public class FileRequest extends Request<String> {

	private final Listener<String> mListener;

	public FileRequest(int method, String url, Listener<String> listener,
			ErrorListener errorListener) {
		super(method, url, errorListener);
		mListener = listener;
		setShouldCache(true);
	}
	public  boolean isAliveForver = false;
	public FileRequest(String url, Listener<String> listener,
			ErrorListener errorListener) {
		this(Method.GET, url, listener, errorListener);
	}

	@Override
	protected Response<String> parseNetworkResponse(NetworkResponse response) {
		final Entry parseCacheHeaders = HttpHeaderParser.parseCacheHeaders(response);
		String noCacheHeadFilePath ="";
		if(VolleyUtil.getQueue().getCache() instanceof DiskBasedCache){
			final File cacheFile = ((DiskBasedCache)(VolleyUtil.getQueue().getCache())).getFileForKey(getCacheKey());
			 noCacheHeadFilePath = cacheFile.getAbsolutePath()+"_NoCacheHeader";
			 if (XHttp.getConfigBuilder().Debug) {
				Log.d("FileRquest", "clip the cache Head From File:"+noCacheHeadFilePath);
			}
			if (!new File(noCacheHeadFilePath).exists()) {
				try {
					BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(noCacheHeadFilePath));
					fos.write(response.data);
					fos.flush();
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		if (isAliveForver) {
			parseCacheHeaders.ttl=Long.MAX_VALUE;
			parseCacheHeaders.softTtl=Long.MAX_VALUE;
		}
		return Response.success(noCacheHeadFilePath,
				parseCacheHeaders);
	}

	@Override
	protected void deliverResponse(String response) {
		mListener.onResponse(response);
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		Map<String, String> headerMap = new HashMap<String, String>();
		headerMap.put("User-Agent", "android-open-project-analysis/1.0");
		return headerMap;
	}
}
