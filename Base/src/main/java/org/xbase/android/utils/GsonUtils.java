package org.xbase.android.utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class GsonUtils {
	public static ArrayList<HashMap<String, String>> JSON2MapArrayList(JSONArray data) {
		Type stringStringMap = new TypeToken<ArrayList<HashMap<String, String>>>() {
        }.getType();
        ArrayList<HashMap<String, String>> map = new Gson().fromJson(data.toString(), stringStringMap);
		return map;
	}
}
