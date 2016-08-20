/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xbase.android.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhy.autolayout.utils.AutoUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * An easy adapter to map static data to views defined in an XML file. You can
 * specify the data backing the list as an ArrayList of Maps. Each entry in the
 * ArrayList corresponds to one row in the list. The Maps contain the data for
 * each row. You also specify an XML file that defines the views used to display
 * the row, and a mapping from keys in the Map to specific views.
 * 
 * Binding data to views occurs in two phases. First, if a
 * {@link android.widget.SimpleAdapter.ViewBinder} is available,
 * {@link ViewBinder#setViewValue(View, Object, String)} is
 * invoked. If the returned value is true, binding has occurred. If the returned
 * value is false, the following views are then tried in order:
 * <ul>
 * <li>A view that implements Checkable (e.g. CheckBox). The expected bind value
 * is a boolean.
 * <li>TextView. The expected bind value is a string and
 * {@link #setViewText(TextView, String)} is invoked.
 * <li>ImageView. The expected bind value is a resource id or a string and
 * {@link #setViewImage(ImageView, int)} or
 * {@link #setViewImage(ImageView, String)} is invoked.
 * </ul>
 * If no appropriate binding can be found, an {@link IllegalStateException} is
 * thrown.
 */
public class SimpleAdapter extends BaseAdapter implements Filterable,
		OnScrollListener {
	/**
	 * background color <BR>
	 * int32 #ffffffff
	 */
	public static final String BGCOLOR = "BGCOLOR";
	protected int[] mTo;
	protected String[] mFrom;
	protected ViewBinder mViewBinder;

	protected ArrayList<HashMap<String, String>> mData;

	protected int mResource;
	private int mDropDownResource;
	protected LayoutInflater mInflater;

	private SimpleFilter mFilter;
	private ArrayList<HashMap<String, String>> mUnfilteredData;
	private int[] mItemLayouts;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The context where the View associated with this SimpleAdapter
	 *            is running
	 * @param data
	 *            A List of Maps. Each entry in the List corresponds to one row
	 *            in the list. The Maps contain the data for each row, and
	 *            should include all the entries specified in "from"
	 * @param resource
	 *            Resource identifier of a view layout that defines the views
	 *            for this list item. The layout file should include at least
	 *            those named views defined in "to"
	 * @param from
	 *            A list of column names that will be added to the Map
	 *            associated with each item.
	 * @param to
	 *            The views that should display column in the "from" parameter.
	 *            These should all be TextViews. The first N views in this list
	 *            are given the values of the first N columns in the from
	 *            parameter.
	 * @param onItemChildViewClick2
	 */
	public SimpleAdapter(Context context, ArrayList<HashMap<String, String>> data,
			int resource, String[] from, int[] to) {
		init(context, data, resource, from, to, null);
	}

	/**
	 * 子view点击监听,必须在to中设置能都点击的View Id,同时这是clickable 为 true
	 * 
	 * @param context
	 * @param data
	 * @param resource
	 * @param from
	 * @param to
	 * @param mOnItemChildViewClick
	 */
	protected void init(Context context, ArrayList<HashMap<String, String>> data,
			int resource, String[] from, int[] to,
			OnItemChildViewClick mOnItemChildViewClick) {
		this.mData = data;
		this.mResource = mDropDownResource = resource;
		this.mFrom = from;
		this.mTo = to;
		this.mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.onItemChildViewClick = mOnItemChildViewClick;
	}

	/**
	 * 支持多种视图
	 * 
	 * @param context
	 * @param map
	 * @param itemLayouts
	 *            支持多种视图的ID数组 默认取模,否则重写{@link #getViewTypeByPosition(int)}
	 * @param from
	 * @param to
	 */
	protected void init(Context context, ArrayList<HashMap<String, String>> data,
			int[] itemLayouts, String[] from, int[] to,
			OnItemChildViewClick mOnItemChildViewClick) {
		mItemLayouts = itemLayouts;
		mResource = mDropDownResource = itemLayouts[0];
		init(context, data, mResource, from, to, mOnItemChildViewClick);
	}

	public SimpleAdapter(Context context, ArrayList<HashMap<String, String>> data,
			int resource, String[] from, int[] to,
			OnItemChildViewClick monItemChildViewClick) {
		this(context, data, resource, from, to);
		this.onItemChildViewClick = monItemChildViewClick;
	}

	public SimpleAdapter() {
	};

	public SimpleAdapter(Context context, AbsListView listview,
			ArrayList<HashMap<String, String>> data, int resource, String[] from,
			int[] to) {
		this(context, data, resource, from, to, null);
		listview.setOnScrollListener(this);
	}

	public SimpleAdapter(Context context, AbsListView listview,
			ArrayList<HashMap<String, String>> data, int resource, String[] from,
			int[] to, OnItemChildViewClick monItemChildViewClick) {
		this(context, data, resource, from, to, monItemChildViewClick);
		listview.setOnScrollListener(this);
	}

	OnItemChildViewClick onItemChildViewClick;

	public interface OnItemChildViewClick {
		public void onClick(View view, int position);
	}

	/**
	 * @see android.widget.Adapter#getCount()
	 */
	public int getCount() {
		return mData.size();
	}

	/**
	 * @see android.widget.Adapter#getItem(int)
	 */
	public Object getItem(int position) {
		return mData.get(position);
	}

	/**
	 * @see android.widget.Adapter#getItemId(int)
	 */
	public long getItemId(int position) {
		return position;
	}

	/**
	 * @see android.widget.Adapter#getView(int, View, ViewGroup)
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent,
				getViewTypeByPosition(mItemLayouts,position));
	}

	/**
	 * 默认按照求模决定item视图样式,子类可以复写,根据position决定
	 * @param mItemLayouts 
	 * 
	 * @param position
	 * @return
	 */
	protected int getViewTypeByPosition(int[] mItemLayouts, int position) {
		int resource = mResource;
		if (mItemLayouts != null) {
			resource = mItemLayouts[position % mItemLayouts.length];
		}
		return resource;
	}

	private View createViewFromResource(int position, View convertView,
			ViewGroup parent, int resource) {
		View v;
		if (convertView == null || mItemLayouts != null) {
			v = mInflater.inflate(resource, parent, false);
		} else {
			v = convertView;
		}

		bindView(position, v);
		AutoUtils.autoSize(v);
		return v;
	}

	/**
	 * <p>
	 * Sets the layout resource to create the drop down views.
	 * </p>
	 * 
	 * @param resource
	 *            the layout resource defining the drop down views
	 * @see #getDropDownView(int, View, ViewGroup)
	 */
	public void setDropDownViewResource(int resource) {
		this.mDropDownResource = resource;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent,
				mDropDownResource);
	}

	public static class ViewHodler {
		public HashMap<Object, View> viewMap = new HashMap<Object, View>();
	}

	protected void bindView(final int position, View view) {
		final Map<String, ?> dataSet = mData.get(position);
		if (dataSet == null) {
			return;
		}
		final ViewBinder binder = mViewBinder;
		final String[] from = mFrom;
		final int[] to = mTo;
		final int count = to.length;
		final int keyCount = mFrom.length;

		ViewHodler mViewHodler = (ViewHodler) view.getTag();
		if (mViewHodler == null) {
			mViewHodler = new ViewHodler();
			for (int i = 0; i < count; i++) {
				mViewHodler.viewMap.put(to[i], view.findViewById(to[i]));
			}
			view.setTag(mViewHodler);
		}

		for (int i = 0; i < count; i++) {
			final View v = mViewHodler.viewMap.get(to[i]);
			if (v != null) {
				if (onItemChildViewClick != null) {
				}
				if (onItemChildViewClick != null && v.isClickable()) {
					v.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							onItemChildViewClick.onClick(v, position);
						}
					});
				}
				if (i >= keyCount) {
					continue;
				}
				final Object data = dataSet.get(from[i]);
				String text = data == null ? "" : data.toString();
				if (text == null) {
					text = "";
				}
				boolean bound = false;
				if (BGCOLOR.equals(from[i])) {
					try {
						v.setBackgroundColor(Integer.parseInt(text));
					} catch (Exception e) {
						e.printStackTrace();
					}
					continue;
				}
				if (binder != null) {
					bound = binder.setViewValue(v, data, text);
				}

				if (!bound) {
					setViewValue(v, data, text);
				}
			}
		}
	}

	private void setViewValue(final View v, final Object data, String text) {
		if (v instanceof Checkable) {
			if (data instanceof Boolean) {
				((Checkable) v).setChecked((Boolean) data);
			} else if (v instanceof TextView) {
				setViewText((TextView) v, text);
			} else {
				throw new IllegalStateException(v.getClass().getName()
						+ " should be bound to a Boolean, not a "
						+ (data == null ? "<unknown type>" : data.getClass()));
			}
		} else if (v instanceof TextView) {
			setViewText((TextView) v, text);
		} else if (v instanceof ImageView) {
			if (data instanceof Integer) {
				setViewImage((ImageView) v, (Integer) data);
			} else {
				setViewImage((ImageView) v, text);
			}
		} else {
			throw new IllegalStateException(v.getClass().getName()
					+ " is not a "
					+ " view that can be bounds by this SimpleAdapter");
		}
	}

	/**
	 * Returns the {@link ViewBinder} used to bind data to views.
	 * 
	 * @return a ViewBinder or null if the binder does not exist
	 * 
	 * @see #setViewBinder(android.widget.SimpleAdapter.ViewBinder)
	 */
	public ViewBinder getViewBinder() {
		return mViewBinder;
	}

	/**
	 * Sets the binder used to bind data to views.
	 * 
	 * @param viewBinder
	 *            the binder used to bind data to views, can be null to remove
	 *            the existing binder
	 * 
	 * @see #getViewBinder()
	 */
	public void setViewBinder(ViewBinder viewBinder) {
		mViewBinder = viewBinder;
	}

	/**
	 * Called by bindView() to set the image for an ImageView but only if there
	 * is no existing ViewBinder or if the existing ViewBinder cannot handle
	 * binding to an ImageView.
	 * 
	 * This method is called instead of {@link #setViewImage(ImageView, String)}
	 * if the supplied data is an int or Integer.
	 * 
	 * @param v
	 *            ImageView to receive an image
	 * @param value
	 *            the value retrieved from the data set
	 * 
	 * @see #setViewImage(ImageView, String)
	 */
	public void setViewImage(ImageView v, int value) {
		v.setImageResource(value);
	}

	/**
	 * Called by bindView() to set the image for an ImageView but only if there
	 * is no existing ViewBinder or if the existing ViewBinder cannot handle
	 * binding to an ImageView.
	 * 
	 * By default, the value will be treated as an image resource. If the value
	 * cannot be used as an image resource, the value is used as an image Uri.
	 * 
	 * This method is called instead of {@link #setViewImage(ImageView, int)} if
	 * the supplied data is not an int or Integer.
	 * 
	 * @param v
	 *            ImageView to receive an image
	 * @param value
	 *            the value retrieved from the data set
	 * 
	 * @see #setViewImage(ImageView, int)
	 */
	public void setViewImage(ImageView v, String value) {
		try {
			v.setImageResource(Integer.parseInt(value));
		} catch (NumberFormatException nfe) {
			throw new RuntimeException("ImageLoader not be config");
		}
	}

	/**
	 * Called by bindView() to set the text for a TextView but only if there is
	 * no existing ViewBinder or if the existing ViewBinder cannot handle
	 * binding to a TextView.
	 * 
	 * @param v
	 *            TextView to receive text
	 * @param text
	 *            the text to be set for the TextView
	 */
	public void setViewText(TextView v, String text) {
		v.setText(text);
	}

	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new SimpleFilter();
		}
		return mFilter;
	}

	/**
	 * This class can be used by external clients of SimpleAdapter to bind
	 * values to views.
	 * 
	 * You should use this class to bind values to views that are not directly
	 * supported by SimpleAdapter or to change the way binding occurs for views
	 * supported by SimpleAdapter.
	 * 
	 * @see SimpleAdapter#setViewImage(ImageView, int)
	 * @see SimpleAdapter#setViewImage(ImageView, String)
	 * @see SimpleAdapter#setViewText(TextView, String)
	 */
	public static interface ViewBinder {
		/**
		 * Binds the specified data to the specified view.
		 * 
		 * When binding is handled by this ViewBinder, this method must return
		 * true. If this method returns false, SimpleAdapter will attempts to
		 * handle the binding on its own.
		 * 
		 * @param view
		 *            the view to bind the data to
		 * @param data
		 *            the data to bind to the view
		 * @param textRepresentation
		 *            a safe String representation of the supplied data: it is
		 *            either the result of data.toString() or an empty String
		 *            but it is never null
		 * 
		 * @return true if the data was bound to the view, false otherwise
		 */
		boolean setViewValue(View view, Object data, String textRepresentation);
	}

	/**
	 * <p>
	 * An array filters constrains the content of the array adapter with a
	 * prefix. Each item that does not start with the supplied prefix is removed
	 * from the list.
	 * </p>
	 */
	@SuppressLint("DefaultLocale")
	private class SimpleFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			FilterResults results = new FilterResults();

			if (mUnfilteredData == null) {
				mUnfilteredData = new ArrayList<HashMap<String, String>>(mData);
			}

			if (prefix == null || prefix.length() == 0) {
				ArrayList<HashMap<String, String>> list = mUnfilteredData;
				results.values = list;
				results.count = list.size();
			} else {
				String prefixString = prefix.toString().toLowerCase();

				ArrayList<HashMap<String, String>> unfilteredValues = mUnfilteredData;
				int count = unfilteredValues.size();

				ArrayList<Map<String, ?>> newValues = new ArrayList<Map<String, ?>>(
						count);

				for (int i = 0; i < count; i++) {
					Map<String, ?> h = unfilteredValues.get(i);
					if (h != null) {

						int len = mTo.length;

						for (int j = 0; j < len; j++) {
							String str = (String) h.get(mFrom[j]);

							String[] words = str.split(" ");
							int wordCount = words.length;

							for (int k = 0; k < wordCount; k++) {
								String word = words[k];

								if (word.toLowerCase().startsWith(prefixString)) {
									newValues.add(h);
									break;
								}
							}
						}
					}
				}

				results.values = newValues;
				results.count = newValues.size();
			}

			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			// noinspection unchecked
			mData = (ArrayList<HashMap<String, String>>) results.values;
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}

	protected View getViewFromViewHolder(ViewHodler viewholder, View view,
			int id) {
		View imageView = viewholder.viewMap.get(id);
		if (imageView == null) {
			imageView = view.findViewById(id);
			viewholder.viewMap.put(id, imageView);
		}
		return imageView;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState != SCROLL_STATE_TOUCH_SCROLL) {
			// TODO LAZY LOADING
		}

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
	}

	public void append(ArrayList<HashMap<String, String>> map) {
		if (map==null) {
			return;
		}
		this.mData.addAll(map);
		notifyDataSetChanged();
		
	}
}
