package org.xbase.android.http;

import java.util.HashMap;
import java.util.Map;

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
		String filePath = getCacheKey();
		if(VolleyUtil.getQueue().getCache() instanceof DiskBasedCache){
			filePath = ((DiskBasedCache)(VolleyUtil.getQueue().getCache())).getFileForKey(getCacheKey()).getAbsolutePath();
		}
		if (isAliveForver) {
			parseCacheHeaders.ttl=Long.MAX_VALUE;
			parseCacheHeaders.softTtl=Long.MAX_VALUE;
		}
		return Response.success(filePath,
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
