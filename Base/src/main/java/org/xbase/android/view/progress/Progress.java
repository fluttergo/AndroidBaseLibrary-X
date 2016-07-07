package org.xbase.android.view.progress;

import android.view.View;
import android.view.animation.AnimationUtils;

public class Progress {
	public static void show(View root,int resID){
		View findViewById = root.findViewById(resID);
		findViewById.setVisibility(View.VISIBLE);
		findViewById.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
			}
		});
		findViewById.startAnimation(AnimationUtils.loadAnimation(root.getContext(), android.R.anim.fade_in));
	}
	public static void hide(View root,int resID){
		View findViewById = root.findViewById(resID);
		findViewById.startAnimation(AnimationUtils.loadAnimation(root.getContext(), android.R.anim.fade_out));
		findViewById.setVisibility(View.GONE);
	}
}
