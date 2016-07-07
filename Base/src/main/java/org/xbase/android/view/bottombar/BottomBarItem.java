package org.xbase.android.view.bottombar;

import android.support.v4.app.Fragment;

public class BottomBarItem{
	public BottomBarItem(Fragment fragment,
			int unsel, int sel) {
		this.f = fragment;
		this.unsel = unsel;
		this.sel = sel;
	}
	public Fragment f;
	public int unsel;
	public int sel;
}