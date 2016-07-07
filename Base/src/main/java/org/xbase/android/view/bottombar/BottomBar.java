package org.xbase.android.view.bottombar;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;

/**
 * 底部导航栏
 * @author Administrator
 *
 */
public class BottomBar {
	/**
	 * 初始化界面和监听事件
	 * @param pActivity
	 * @param viewGroupResourceId 包含切换按钮的父亲布局
	 * @param mFragments
	 * @param select
	 */
	public static void initBottomBar(FragmentActivity pActivity,int fragmentContainerID,int viewGroupResourceId,final BottomBarItem[] mFragments,final OnItemSelectListener select) {
		FragmentManager fm = pActivity.getSupportFragmentManager();

		FragmentTransaction ft = fm.beginTransaction();
		for (int i = 0; i < mFragments.length; i++) {
			ft.add(fragmentContainerID, mFragments[i].f);
		}
		ft.commit();
	        
		final ArrayList<View> list =new ArrayList<View>();
		ViewGroup vg = (ViewGroup) pActivity.findViewById(viewGroupResourceId);
		View.OnClickListener cb= new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				select.onSelect(list, mFragments,Integer.parseInt(v.getTag().toString()));
			}
		};
		for (int i = 0; i < vg.getChildCount(); i++) {
			vg.getChildAt(i).setOnClickListener(cb);
			vg.getChildAt(i).setTag(""+i);
			list.add(vg.getChildAt(i));
		}
	}
	public interface OnItemSelectListener{
		public void onSelect(List<View> items, BottomBarItem[] mFragments, int select);
	}
	/**
	 * 切换显示
	 * @param pActivity
	 * @param mFragments
	 * @param select
	 */
	public static void switchFragment(FragmentActivity  pActivity ,BottomBarItem[] mFragments,int select) {
		FragmentTransaction fragmentTransaction =pActivity.getSupportFragmentManager().beginTransaction();
        for (int i = 0; i < mFragments.length; i++) {
        	fragmentTransaction .hide(mFragments[i].f);  
		}
        fragmentTransaction.show(mFragments[select].f).commit();
	}
}
