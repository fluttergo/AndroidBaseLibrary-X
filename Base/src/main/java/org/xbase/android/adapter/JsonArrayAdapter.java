package org.xbase.android.adapter;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.xbase.android.utils.GsonUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class JsonArrayAdapter extends SimpleAdapter {
	public JsonArrayAdapter(Context context, String jsonArrayData,
			int resource, String[] from, int[] to) {
		Type stringStringMap = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		ArrayList<HashMap<String, String>> map = new Gson().fromJson(jsonArrayData,
				stringStringMap);
		init(context, map, resource, from, to, null);
	}

	public JsonArrayAdapter(Context context, JSONArray data, int resource,
			String[] from, int[] to) {
		ArrayList<HashMap<String, String>> map = JSON2MapArrayList(data);
		init(context, map, resource, from, to, null);
	}

	public static ArrayList<HashMap<String, String>> JSON2MapArrayList(
			JSONArray data) {
		return GsonUtils.JSON2MapArrayList(data);
	}

	public JsonArrayAdapter(Context context, String jsonArrayData,
			int[] itemLayouts, String[] from, int[] to) {
		ArrayList<HashMap<String, String>> map = jsonStrng2HashMap(jsonArrayData);
		init(context, map, itemLayouts, from, to, null);
	}


	public JsonArrayAdapter(Context context, String jsonArrayData,
			int[] itemLayouts, String[] from, int[] to,
			OnItemChildViewClick mOnItemChildViewClick) {
		ArrayList<HashMap<String, String>> map = jsonStrng2HashMap(jsonArrayData);
		init(context, map, itemLayouts, from, to, mOnItemChildViewClick);
	}

	public JsonArrayAdapter(Context context, String jsonArrayData,
			int resource, String[] from, int[] to,
			OnItemChildViewClick mOnItemChildViewClick) {
		ArrayList<HashMap<String, String>> map = jsonStrng2HashMap(jsonArrayData);
		init(context, map, resource, from, to, mOnItemChildViewClick);
	}
	
	public ArrayList<HashMap<String, String>> jsonStrng2HashMap(String jsonArrayData) {
		Type stringStringMap = new TypeToken<ArrayList<HashMap<String, String>>>() {
		}.getType();
		ArrayList<HashMap<String, String>> map = new Gson().fromJson(jsonArrayData,
				stringStringMap);
		return map;
	}
	public  void append(JSONArray data){
		if (data !=null&&data.length()>0) {
			ArrayList<HashMap<String, String>> map = jsonStrng2HashMap(data.toString());
			super.append(map);
		}
	}
	public  void append(String data){
		if (data !=null&&data.length()>0) {
			ArrayList<HashMap<String, String>> map = jsonStrng2HashMap(data);
			super.append(map);
		}
	}
}
