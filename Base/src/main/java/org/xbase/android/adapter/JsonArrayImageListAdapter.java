package org.xbase.android.adapter;

import android.content.Context;
import android.widget.ImageView;

import java.text.DecimalFormat;

public class JsonArrayImageListAdapter extends JsonArrayAdapter {

	public JsonArrayImageListAdapter(Context context, String jsonArrayData,
			int resource, String[] from, int[] to) {
		super(context, jsonArrayData, resource, from, to);
	}

	public JsonArrayImageListAdapter(Context context, String jsonArrayData,
			int resource, String[] from, int[] to,
			SimpleAdapter.OnItemChildViewClick mOnItemChildViewClick) {
		super(context, jsonArrayData, resource, from, to,mOnItemChildViewClick);
	}

	public JsonArrayImageListAdapter(Context context, String jsonArrayData,
			int[] itemLayouts, String[] from, int[] to) {
		super(context, jsonArrayData, itemLayouts, from, to);
	}

	public JsonArrayImageListAdapter(Context context, String jsonArrayData,
			int[] itemLayouts, String[] from, int[] to,
			SimpleAdapter.OnItemChildViewClick mOnItemChildViewClick) {
		super(context, jsonArrayData, itemLayouts, from, to,mOnItemChildViewClick);
	}

	public void setViewImage(ImageView v, String value) {
		try {
	        double a =Double.parseDouble(value);  
	        //强制转换的过程，小数点后保留3位有效数字!  
	        DecimalFormat De = new DecimalFormat("0");  
	        String b = De.format(a);
			v.setImageResource(Integer.parseInt(b));
		} catch (NumberFormatException nfe) {
			throw new RuntimeException("ImageLoader not be config");
		}
	}
}
