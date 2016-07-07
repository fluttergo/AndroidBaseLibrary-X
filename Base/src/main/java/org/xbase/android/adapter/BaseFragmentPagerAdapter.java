package org.xbase.android.adapter;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class BaseFragmentPagerAdapter extends FragmentPagerAdapter {
	List<Fragment> list;


	public BaseFragmentPagerAdapter(FragmentManager fm,
			List<Fragment> fragmentList) {
		super(fm);
		this.list = fragmentList;
	}

	@Override
	public Fragment getItem(int position) {
		return list.get(position);
	}

	@Override
	public int getCount() {
		return list == null ? 0 : list.size();
	}


}
