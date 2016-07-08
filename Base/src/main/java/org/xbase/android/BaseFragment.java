package org.xbase.android;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusException;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

/**Fragment的基础类*/
public abstract class BaseFragment extends Fragment   {
	public String TAG = "BaseFragment";
	public View mRootView;

	public static Fragment creatBaseFramgent(Bundle extras,
											 Class<? extends BaseFragment> clazz) {
		BaseFragment fragment = null;
		try {
			fragment = clazz.newInstance();
			fragment.setArguments(extras);
		} catch (java.lang.InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return fragment;
	}
	public static Fragment creatBaseFramgent(Class<? extends BaseFragment> clazz) {
		BaseFragment fragment = null;
		try {
			fragment = clazz.newInstance();
		} catch (java.lang.InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return fragment;
	}
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		this.mRootView = view;
	}

	protected boolean isValueFragment() {
		if (getActivity() == null) {
			return false;
		}
		return true;
	}
	protected View findViewById(int id) {
		if (mRootView == null) {
			return null;
		}
		return mRootView.findViewById(id);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getChild()!=null) {
			EventBus.getDefault().register(getChild());
		}
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (getChild()!=null) {
			EventBus.getDefault().unregister(this);
		}
	}
	/**
	 * 获取子类实例,用于EventBus注册子类为订阅者,如果子类实现了该方法,则必须实现public void OnEvent()方法
	 * @return
	 */
	public  BaseFragment getChild(){
		return null;
	}

}
