package org.xbase.android.sample.activity;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.internal.LinkedTreeMap;

import org.xbase.android.BaseActivity;
import org.xbase.android.adapter.JsonArrayAdapter;
import org.xbase.android.adapter.ViewHolder;
import org.xbase.android.http.AjaxCallBack;
import org.xbase.android.http.XHttp;
import org.xbase.android.sample.App;
import org.xbase.android.sample.R;

import org.xbase.android.sample.activity.dummy.DummyContent;
import org.xbase.android.sample.fragment.GoodsListragment;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link KindsDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class KindsListActivity extends BaseActivity {

    private static final String TAG = "KindsListActivity";
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private View recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kinds_list);


        XHttp.get(App.HOST + "/goodstype", new AjaxCallBack() {
            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                setupRecyclerView((ListView) recyclerView,t);

            }
        });
        recyclerView = findViewById(R.id.kinds_list);
        assert recyclerView != null;
        if (findViewById(R.id.kinds_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    private void setupRecyclerView(@NonNull ListView recyclerView,String t) {

        JsonArrayAdapter adapter = new JsonArrayAdapter(this,t,R.layout.kinds_list_content,new String[]{ "name" },new int[]{ R.id.content}){
            int selectPosition;
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {

                View rootView = super.getView(position, convertView, parent);

                final View root = rootView.findViewById(R.id.kind);
                final TextView content = (TextView)rootView.findViewById(R.id.content);
                if (selectPosition != position) {
                    root.setBackgroundColor(0x00000000);
                    content.setTextColor(getApplication().getResources().getColor(R.color.colorTextDefault));
                    Log.d(TAG,"unselcet"+position);
                } else {
                    root.setBackgroundColor(getApplication().getResources().getColor(R.color.colorBgSelect));
                    content.setTextColor(getApplication().getResources().getColor(R.color.colorPrimary));
                    Log.d(TAG,"selcet"+selectPosition);
                }
                root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle arguments = new Bundle();
                        arguments.putString(GoodsListragment.KEY_ITEM_ID, String.valueOf(getItemId(position)));
                        GoodsListragment fragment = new GoodsListragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.kinds_detail_container, fragment)
                                .commit();
                        selectPosition = position;
                        notifyDataSetChanged();
                    }
                });
                return rootView;
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
