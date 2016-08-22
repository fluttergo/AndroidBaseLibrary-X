package org.xbase.android.sample.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.xbase.android.BaseFragment;
import org.xbase.android.adapter.JsonArrayAdapter;
import org.xbase.android.http.AjaxCallBack;
import org.xbase.android.http.XHttp;
import org.xbase.android.sample.App;
import org.xbase.android.sample.R;

import java.util.Map;

public class GoodsTypeFragment extends BaseFragment {

    private static final String TAG = "KindsListActivity";
    private View recyclerView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_kinds_list, null);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        XHttp.get(App.HOST + "/goodstype", new AjaxCallBack() {
            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                setupRecyclerView((ListView) recyclerView,t);

            }
        });
        recyclerView = findViewById(R.id.kinds_list);

    }
    private void setupRecyclerView(ListView recyclerView, String t) {
        JsonArrayAdapter adapter = new JsonArrayAdapter(getActivity(),t,R.layout.kinds_list_content,new String[]{ "name" },new int[]{ R.id.content}){
            int selectPosition =-1;
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {

                View rootView = super.getView(position, convertView, parent);

                final View root = rootView.findViewById(R.id.kind);
                final TextView content = (TextView)rootView.findViewById(R.id.content);
                if (selectPosition<0){//select at init
                    onSelectItem(0);
                    root.setBackgroundColor(getActivity().getResources().getColor(R.color.colorBgSelect));
                    content.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
                }else if  (selectPosition != position) {
                    root.setBackgroundColor(0x00000000);
                    content.setTextColor(getActivity().getResources().getColor(R.color.colorTextDefault));
                } else {
                    root.setBackgroundColor(getActivity().getResources().getColor(R.color.colorBgSelect));
                    content.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
                }
                root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onSelectItem(position);
                        notifyDataSetChanged();
                    }
                });
                return rootView;
            }

            public void onSelectItem(int position) {
                Bundle arguments = new Bundle();
                arguments.putString(GoodsListragment.KEY_ITEM_ID, String.valueOf(getItemId(position)));
                GoodsListragment fragment = new GoodsListragment();
                fragment.setArguments(arguments);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.kinds_detail_container, fragment)
                        .commit();
                selectPosition = position;
            }

            @Override
            public long getItemId(int position) {
                Map<String, String> hashmap = mData.get(position);
                return Long.parseLong(hashmap.get("id"));
            }
        };
        recyclerView.setAdapter(adapter);
    }

}
