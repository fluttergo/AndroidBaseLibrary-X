package org.xbase.android;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.zhy.autolayout.AutoLayoutActivity;

/** activity的基础类 */

public abstract class BaseActivity extends FragmentActivity {


	private String mPageName = "";

	protected String curFragmentTag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	@Override
	public void finish() {
		super.finish();
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
	}

	public  void initViews() {
	}

	public  void initData() {
	}

	public  void addListeners() {
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (!TextUtils.isEmpty(curFragmentTag)) {
			Fragment f = getSupportFragmentManager().findFragmentByTag(curFragmentTag);
			if (f!=null) {
				f.onActivityResult(requestCode, resultCode, data);
			}
		}
	}
}
