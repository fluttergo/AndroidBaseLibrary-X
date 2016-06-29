package org.xbase.android.http;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.google.gson.Gson;

import android.opengl.GLSurfaceView;

/**
 * <p>
 * <strong>泛化的http 请求回调</strong>
 * </p>
 * 
 * @author Ge Liang
 * @Create at 2016-6-29 上午11:52:24
 * @Version 1.0
 * @see Gson,volley
 * 
 */
public abstract class AbstractUnformatCallback<T> extends AjaxCallBack{
	Type mType;
	public final int ERR_EXCEPTION = -1;
	private static GLSurfaceView mGLSurfaceView;

	public AbstractUnformatCallback() {
		super();
	}
	/**
	 * 绑定GLSurfaceView，用于GL线程切换
	 * @param pGLSurfaceView
	 */
	public static void attachGLThread(GLSurfaceView pGLSurfaceView) {
		AbstractUnformatCallback.mGLSurfaceView = pGLSurfaceView;
	}
	/**
	 * 解绑GLSurfaceView
	 * @param pGLSurfaceView
	 */
	public static void detachGLThread() {
		AbstractUnformatCallback.mGLSurfaceView = null;
	}
	/**
	 * 设置泛型的类型信息
	 * 
	 * @deprecated 无需此步骤,在运行时获取
	 * @param type
	 */
	@Deprecated
	public void setClass(Type type) {
		this.mType = type;
	}

	/**
	 * 去掉一些格式化的外壳
	 * 
	 * @param result
	 * @return 真正的内容
	 * @throws Exception
	 */
	protected String unFormatContent(String result) throws Exception {
		return result;
	}

	/**
	 * http 成功的响应,
	 * 
	 * @param orgResult
	 *            原始结果
	 * @param result
	 *            ,result is null 如果T是String.class 经过解析的结果
	 */
	public abstract void onSuccess(String orgResult, T result);

	/**
	 * http 请求 失败,也可能是结果无法解析
	 * 
	 * @param errCode
	 * @param errMsg
	 */
	public abstract void onFail(int errCode, String errMsg);

	protected boolean onSuccessInGLThread(final Type type, final String result) {
		if (AbstractUnformatCallback.mGLSurfaceView != null) {
			AbstractUnformatCallback.mGLSurfaceView.queueEvent(new Runnable() {

				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					try {
						if (result == null || type == String.class) {

							onSuccess(unFormatContent(result), null);

						} else {
							onSuccess(result, (T) new Gson().fromJson(
									unFormatContent(result), type));
						}
					} catch (Exception e) {
						onFailureInGLThread(e);
						e.printStackTrace();
					}
				}

			});
			return true;
		}
		return false;
	}
	public void onFailureInGLThread(final Exception e) {
		if (AbstractUnformatCallback.mGLSurfaceView != null) {
			AbstractUnformatCallback.mGLSurfaceView.queueEvent(new Runnable() {

				@Override
				public void run() {
					onFail(ERR_EXCEPTION, e.getMessage());
				}

			});
		} else {
			onFail(ERR_EXCEPTION, e.getMessage());
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void onSuccess(final String result) {
		try {
			// 运行时获取泛型类型信息
			Type superclass = getClass().getGenericSuperclass();
			if (superclass instanceof Class) {
				throw new RuntimeException("Missing type parameter.");
			}
			ParameterizedType parameterized = (ParameterizedType) superclass;
			final Type type = parameterized.getActualTypeArguments()[0];

			if (!onSuccessInGLThread(type,result)) {
				if (result == null || type == String.class) {
					onSuccess(unFormatContent(result), null);
				} else {
					onSuccess(result, (T) new Gson().fromJson(
							unFormatContent(result), type));
				}
			}

		} catch (final Exception e) {
			e.printStackTrace();
			onFail(ERR_EXCEPTION, e.getMessage());
		}
	}

	@Override
	public void onFailure(Throwable t, int errorNo, String strMsg) {
		onFail(errorNo, strMsg);
	}
}
