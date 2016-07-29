package org.xbase.android.adapter;

import android.util.SparseArray;
import android.view.View;

/**
 *
 *     if (convertView == null) {
 *           convertView = inflater.inflate(R.layout.listview_item_layout, parent, false);
 *      }
 *      TextView name = Tools.ViewHolder.get(convertView, R.id.student_name);
 *      TextView age = Tools.ViewHolder.get(convertView, R.id.student_age);
 */
public  class ViewHolder {
    public static <T extends View> T get(View view, int id) {
        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
        if (viewHolder == null) {
            viewHolder = new SparseArray<View>();
            view.setTag(viewHolder);
        }
        View childView = viewHolder.get(id);
        if (childView == null) {
            childView = view.findViewById(id);
            viewHolder.put(id, childView);
        }
        return (T) childView;
    }
}
