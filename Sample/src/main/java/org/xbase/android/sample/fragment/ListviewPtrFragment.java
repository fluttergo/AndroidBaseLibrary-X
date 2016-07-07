package org.xbase.android.sample.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

import org.xbase.android.BaseFragment;
import org.xbase.android.sample.R;

public class ListviewPtrFragment extends BaseFragment {

    private FrameLayout doorRootContentFl;
    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.listview_ptr, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        doorRootContentFl = (FrameLayout) view.findViewById(R.id.door_root_content_fl);
        listView = (ListView) view.findViewById(R.id.listView);
    }

}
