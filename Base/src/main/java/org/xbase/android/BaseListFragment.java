package org.xbase.android;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import cn.bingoogolapple.refreshlayout.BGARefreshViewHolder;

/**
 * Created by Administrator on 2016/7/8.
 */
public abstract class BaseListFragment extends BaseFragment implements BGARefreshLayout.BGARefreshLayoutDelegate {

    private BGARefreshLayout mRefreshLayout;
    protected int mPageIndex = 0;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initRefreshLayout();
        onBGARefreshLayoutBeginRefreshing(null);
    }

    protected int  getBGARefreshLayoutID() {
        return android.R.id.content;
    }

    private void initRefreshLayout() {

        mRefreshLayout = (BGARefreshLayout) mRootView.findViewById(getBGARefreshLayoutID());
        if(mRefreshLayout == null){
            new RuntimeException("mRefreshLayout is null ,Override int getBGARefreshLayoutID() method").printStackTrace();
            return;
        }
        // 为BGARefreshLayout设置代理
        mRefreshLayout.setDelegate(this);
        // 设置下拉刷新和上拉加载更多的风格     参数1：应用程序上下文，参数2：是否具有上拉加载更多功能
        BGARefreshViewHolder refreshViewHolder = new BGANormalRefreshViewHolder(getActivity(), true);
        // 设置下拉刷新和上拉加载更多的风格
        mRefreshLayout.setRefreshViewHolder(refreshViewHolder);

        mRefreshLayout.setIsShowLoadingMoreView(true);

        refreshViewHolder.setLoadingMoreText("加载更多");
        // 设置整个加载更多控件的背景颜色资源id
        refreshViewHolder.setLoadMoreBackgroundColorRes(android.R.color.black);
        // 设置整个加载更多控件的背景drawable资源id
//        refreshViewHolder.setLoadMoreBackgroundDrawableRes(loadMoreBackgroundDrawableRes);
        // 设置下拉刷新控件的背景颜色资源id
        refreshViewHolder.setRefreshViewBackgroundColorRes(android.R.color.holo_green_light);
        // 设置下拉刷新控件的背景drawable资源id
//        refreshViewHolder.setRefreshViewBackgroundDrawableRes(refreshViewBackgroundDrawableRes);
        // 设置自定义头部视图（也可以不用设置）     参数1：自定义头部视图（例如广告位）， 参数2：上拉加载更多是否可用
//        mRefreshLayout.setCustomHeaderView(mBanner, false);


    }

    /**
     * 设置View取消下拉刷新的精度条
     */
    public void endRefreshing() {
        if(mRefreshLayout!=null){
            mRefreshLayout.endRefreshing();
        }
    }

    /**
     * 设置View取消加载更多的进度条,要在append数据前调用. (比如 adapter.notifyDataSetChanged())之前
     */
    public void endLoadingMore() {
        mRefreshLayout.endLoadingMore();
    }

    protected abstract void onRefresh();
    protected abstract void onLoadMore(int pageIndex);
    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        mPageIndex = 0;
        onRefresh();
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        // 在这里加载更多数据，或者更具产品需求实现上拉刷新也可以
        mPageIndex++;
        onLoadMore(mPageIndex);
        return true;
    }
}
