package org.xbase.android.sample.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.xbase.android.BaseFragment;
import org.xbase.android.BaseListFragment;
import org.xbase.android.adapter.JsonArrayAdapter;
import org.xbase.android.http.AjaxCallBack;
import org.xbase.android.http.XHttp;
import org.xbase.android.sample.R;

import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import cn.bingoogolapple.refreshlayout.BGARefreshViewHolder;

public class GoodsListragment extends BaseListFragment {
    private ListView listView;
    JsonArrayAdapter jsonArrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.listview_ptr, null);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = (ListView) view.findViewById(android.R.id.list);

        onRefresh();
    }

    @Override
    protected void onRefresh() {
        XHttp.get("http://shuiguorili.com:8080/goods?type=0&pagesize=10&pageindex=" + 1, new AjaxCallBack() {
            @Override
            public void onSuccess(String t) {

                super.onSuccess(t);
                jsonArrayAdapter = new JsonArrayAdapter(getActivity(), t, R.layout.item_listview_t_i, new String[]{"name"}, new int[]{R.id.tv_name});
                listView.setAdapter(jsonArrayAdapter);
                // 在这里加载最新数据
                // 加载完毕后在UI线程结束下拉刷新
                endRefreshing();
            }
        });
    }

    @Override
    protected void onLoadMore(int pageIndex) {
        XHttp.get("http://shuiguorili.com:8080/goods?type=0&pagesize=10&pageindex=" + (1+pageIndex), new AjaxCallBack() {
                    @Override
                    public void onSuccess(final String t) {
                        super.onSuccess(t);
                        endLoadingMore();
                        try {
                            if (new JSONArray(t).length() > 0) {
                                jsonArrayAdapter.append(t);
                                jsonArrayAdapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // 加载完毕后在UI线程结束加载更多
                    }
                }

        );
    }

    @Override
    protected int getBGARefreshLayoutID() {
        return R.id.rl_modulename_refresh;
    }


}
