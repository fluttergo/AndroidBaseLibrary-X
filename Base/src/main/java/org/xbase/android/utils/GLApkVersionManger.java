package org.xbase.android.utils;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONArray;


/**
 * ʵ�� ��HDTV �������ϵİ汾����ӿ�
 * 
 * @author think
 * 
 */
public class GLApkVersionManger implements UpdateManger.ComparisonVersion {
	String host = "http://dboomsky.com";
	String url = host + "/APKServletDownload";
	String project = "fuliduo";
	private String about;
	UpdateManger um;

	public void autoManger(Context context, String projectName) {
		try {
			um = new UpdateManger(context);
			if (!TextUtils.isEmpty(projectName)) {
				this.project = projectName;
			}
			um.checkUpdateInfo(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}

	@Override
	public String configHttpUrl() {
		return url + "?name=" + project;
	}

	@Override
	public boolean comparisonVersion(String httpVersionInfo) {
		int version = 0;
		JSONArray ja = null;
		try {
			try {
				ja = new JSONArray(httpVersionInfo);
			} catch (Exception e) {
				e.printStackTrace();
				try {
					ja = new JSONArray(
							httpVersionInfo);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
			if (ja == null) {
				return false;
			}
			for (int i = 0; i < ja.length(); i++) {
				String jo = ja.optJSONObject(i).optString("image");
				if (Integer.parseInt(jo) > version) {
					version = Integer.parseInt(jo);
					about = ja.optJSONObject(i).optString("about");
					url = ja.optJSONObject(i).optString("url");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return version <= um.getLoacalVersionCode() ? false : true;
	}

	@Override
	public String configNewVersionDownloadUrl() {
		return host + "/" + url;
	}

	@Override
	public String configNewVersionInfo() {
		return about;
	}

}