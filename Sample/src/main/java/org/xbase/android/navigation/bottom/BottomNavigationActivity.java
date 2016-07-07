package org.xbase.android.navigation.bottom;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ImageView;

import org.xbase.android.BaseActivity;
import org.xbase.android.BaseFragment;
import org.xbase.android.sample.R;
import org.xbase.android.sample.fragment.GoodTypeFragment;
import org.xbase.android.sample.fragment.GoodsListragment;
import org.xbase.android.sample.fragment.LeaveMessageListragment;
import org.xbase.android.view.bottombar.BottomBar;
import org.xbase.android.view.bottombar.BottomBarItem;
import org.xbase.android.view.bottombar.OnSelectListenerImp;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BottomNavigationActivity extends BaseActivity {

    @BindView(R.id.container_fragment) FrameLayout doorContentsFl;
    @BindView(R.id.textView1) View textView1;
    @BindView(R.id.bottomBar) LinearLayout bottomBar;
    @BindView(R.id.bottomBarItem0) ImageView bottomBarItem0;
    @BindView(R.id.bottomBarItem1) ImageView bottomBarItem1;
    @BindView(R.id.bottomBarItem2) ImageView bottomBarItem2;
    @BindView(R.id.bottomBarItem3) ImageView bottomBarItem3;
    @BindView(R.id.bottomBarItem4) ImageView bottomBarItem4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);
        ButterKnife.bind(this);
        BottomBarItem[] mFragments = new BottomBarItem[5];
        mFragments[0] = new BottomBarItem(BaseFragment.creatBaseFramgent(GoodsListragment.class),R.drawable.home_normal,R.drawable.home_press) ;
        mFragments[1] = new BottomBarItem( BaseFragment.creatBaseFramgent(GoodTypeFragment.class),R.drawable.shopping_normal,R.drawable.shopping_press) ;
        mFragments[2] = new BottomBarItem(BaseFragment.creatBaseFramgent(LeaveMessageListragment.class),R.drawable.smlt_normal,R.drawable.smlt_press) ;
        mFragments[3] = new BottomBarItem(BaseFragment.creatBaseFramgent(GoodsListragment.class),R.drawable.shoppingcart_normal,R.drawable.shoppingcart_press) ;
        mFragments[4] = new BottomBarItem(BaseFragment.creatBaseFramgent(GoodsListragment.class),R.drawable.user_normal,R.drawable.user_press) ;

        BottomBar.switchFragment(this,mFragments,0);

        BottomBar.initBottomBar(this,R.id.container_fragment,R.id.bottomBar,mFragments,new OnSelectListenerImp() {
            @Override
            public void onSelect(List<View> items, BottomBarItem[] mFragments,
                                 int select) {
                super.onSelect(items, mFragments, select);
                BottomBar.switchFragment(BottomNavigationActivity.this,mFragments,select);
                curFragmentTag = mFragments[select].getClass().getSimpleName();
            }
        });

    }

}
