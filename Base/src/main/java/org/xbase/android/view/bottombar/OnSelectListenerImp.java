package org.xbase.android.view.bottombar;

import java.util.List;


import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
/**
 * 实现图片资源的切换
 * @author Administrator
 *
 */
public class OnSelectListenerImp implements BottomBar.OnItemSelectListener {
	
	@Override
	public void onSelect(List<View> items,BottomBarItem[] mFragments, int select) {
		for (int i = 0; i < items.size(); i++) {
			((ImageView) items.get(i)).setImageResource(mFragments[i].unsel);
		}
		ImageView imageView = (ImageView) items.get(select);
		imageView.startAnimation(AnimationUtils.loadAnimation(imageView.getContext(),android.R.anim.fade_in));		
		((ImageView) items.get(select)).setImageResource(mFragments[select].sel);
	}
	
	
}
