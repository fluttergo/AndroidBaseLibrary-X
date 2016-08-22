package org.xbase.android.sample.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.android.volley.toolbox.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.xbase.android.BaseFragment;
import org.xbase.android.BaseListFragment;
import org.xbase.android.adapter.JsonArrayAdapter;
import org.xbase.android.adapter.JsonArrayImageListAdapter;
import org.xbase.android.http.AjaxCallBack;
import org.xbase.android.http.XHttp;
import org.xbase.android.image.ImageUtils;
import org.xbase.android.sample.App;
import org.xbase.android.sample.R;

import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import cn.bingoogolapple.refreshlayout.BGARefreshViewHolder;

public class GoodsListragment extends BaseListFragment {
    public static final String KEY_ITEM_ID = "GOODSID" ;
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
        XHttp.get(App.HOST+"/goods?type="+ getGoodsTypeID() +"&pagesize=10&pageindex=" + 1, new AjaxCallBack() {
            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                jsonArrayAdapter = new JsonArrayImageListAdapter(getActivity(), t, R.layout.item_listview_t_i, new String[]{"name","img"}, new int[]{R.id.tv_name,R.id.iv_image}){
                    @Override
                    public void setViewImage(ImageView v, String value) {
                        ImageUtils.getInstance().showImage(App.HOST+value,v);
                    }

                };
                listView.setAdapter(jsonArrayAdapter);
                endRefreshing();
            }
        });
    }

    private String getGoodsTypeID() {
        return getArguments()==null?"0":getArguments().getString(KEY_ITEM_ID);
    }

    @Override
    protected void onLoadMore(int pageIndex) {
        XHttp.get(App.HOST+"/goods?type="+ getGoodsTypeID() +"&pagesize=10&pageindex=" + (1+pageIndex), new AjaxCallBack() {
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
                    }
                }

        );
    }

    @Override
    protected int getBGARefreshLayoutID() {
        return R.id.rl_modulename_refresh;
    }


}
