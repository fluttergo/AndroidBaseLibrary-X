package org.xbase.android.http;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

public abstract class AjaxCallBack implements Listener<String>, ErrorListener {

    private boolean progress = true;
    private int     rate     = 1000 * 1; // 每秒

    @Override
    public void onResponse(String arg0) {
        onSuccess(arg0);
    }

    @Override
    public void onErrorResponse(VolleyError arg0) {
        onFailure(arg0, 0, arg0.getMessage());

    }

    public boolean isProgress() {
        return progress;
    }

    public int getRate() {
        return rate;
    }

    /**
     * 设置进度,而且只有设置了这个了以后，onLoading才能有效。
     * 
     * @param progress
     *            是否启用进度显示
     * @param rate
     *            进度更新频率
     */
    public AjaxCallBack progress(boolean progress, int rate) {
        this.progress = progress;
        this.rate = rate;
        return this;
    }

    public void onStart() {
    };

    /**
     * onLoading方法有效progress
     * 
     * @param count
     * @param current
     */
    public void onLoading(long count, long current) {
    };

    public void onSuccess(String t) {
    };

    public void onFailure(Throwable t, int errorNo, String strMsg) {
    };
}
