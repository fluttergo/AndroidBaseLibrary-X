package org.xbase.android.adapter;


import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xbase.android.adapter.SimpleAdapter.OnItemChildViewClick;
public class JsonArrayViewPagerAdapter extends PagerAdapter {

	protected int[] mTo;
	protected String[] mFrom;
	protected ViewBinder mViewBinder;

	protected List<? extends Map<String, ?>> mData;

	protected int mResource;
	protected LayoutInflater mInflater;

	public JsonArrayViewPagerAdapter(Context context, String jsonArrayData,
			int resource, String[] from, int[] to) {
		Type stringStringMap = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> map = new Gson().fromJson(jsonArrayData,
				stringStringMap);
		init(context, map, resource, from, to);
	}
	public JsonArrayViewPagerAdapter(Context context, String jsonArrayData,
			int resource, String[] from, int[] to ,OnItemChildViewClick onItemChildViewClick) {
		Type stringStringMap = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> map = new Gson().fromJson(jsonArrayData,
				stringStringMap);
		init(context, map, resource, from, to);
		this.onItemChildViewClick = onItemChildViewClick;
	}

	protected void init(Context context, List<? extends Map<String, ?>> data,
			int resource, String[] from, int[] to) {
		mData = data;
		mResource = resource;
		mFrom = from;
		mTo = to;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	OnItemChildViewClick onItemChildViewClick;
	@Override
	public CharSequence getPageTitle(int position) {
		final Object x = mData.get(position).get("title");
		if (x!=null) {
			return x.toString();
		}
		return super.getPageTitle(position);
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
		return createViewFromResource(position, convertView, parent, mResource);
	}

	private View createViewFromResource(int position, View convertView,
			ViewGroup parent, int resource) {
		View v;
		if (convertView == null) {
			v = mInflater.inflate(resource, parent, false);
		} else {
			v = convertView;
		}

		bindView(position, v);

		return v;
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
				if (onItemChildViewClick != null && v.isClickable()) {
					v.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							onItemChildViewClick.onClick(v, position);

						}
					});
				}
				if (i>=from.length) {
					continue;
				}
				final Object data = dataSet.get(from[i]);
				String text = data == null ? "" : data.toString();
				if (text == null) {
					text = "";
				}

				boolean bound = false;
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
	public void setViewImage(ImageView v, String value)  {
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

	public static interface ViewBinder {
		boolean setViewValue(View view, Object data, String textRepresentation);
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
	public boolean isViewFromObject(View arg0, Object arg1) {

		return arg0 == arg1;
	}

	@Override
	public int getItemPosition(Object object) {

		return super.getItemPosition(object);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView(ViewList.get(position));
	}

	ArrayList<View> ViewList = new ArrayList<View>();

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View view = getView(position, null, container);
		container.addView(view);
		ViewList.add(view);
		return view;
	}

}
