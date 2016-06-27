package org.xbase.android.utils;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.webkit.WebView;
import android.widget.ZoomButtonsController;

import org.xbase.android.log.Logger;

import java.lang.reflect.Method;


/**
 * <p>
 * <strong>用于相关api需要过版本兼容</strong>
 * </p>
 */
@SuppressLint("NewApi")
public class ApiVersionUtils {
    // ===========================================================
    // Constants
    // ===========================================================
    private final static Logger LOG = Logger.getLogger(ApiVersionUtils.class);

    // ===========================================================
    // Fields
    // ===========================================================

    // ===========================================================
    // Constructors

    public static void setBackground(View pView, Drawable pDrawable) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            pView.setBackgroundDrawable(pDrawable);
        } else {
            pView.setBackground(pDrawable);
        }
    }

    /**
     * 设置webView 放大缩小控制按钮不可用
     */
    public static void disableWebViewZoomController(WebView pWebView) {
        // API version 大于11的时候，SDK提供了屏蔽缩放按钮的方法
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            pWebView.getSettings().setBuiltInZoomControls(true);
            pWebView.getSettings().setDisplayZoomControls(false);
        } else {
            // 如果是11- 的版本使用JAVA中的映射的办法
            getControlls(pWebView);
        }
    }

    private static void getControlls(WebView pWebView) {
        try {
            Class webview = Class.forName("android.webkit.WebView");
            Method method = webview.getMethod("getZoomButtonsController");
            ZoomButtonsController zoomController = (ZoomButtonsController) method.invoke(pWebView);
            if (zoomController != null) {
                zoomController.setVisible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===========================================================

    // ===========================================================
    // Getter &amp; Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

}
