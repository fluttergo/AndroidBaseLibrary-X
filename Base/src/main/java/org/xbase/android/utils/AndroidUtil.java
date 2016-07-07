package org.xbase.android.utils;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.Toast;

public class AndroidUtil {
	/**
	 * 唯一的设备ID： GSM手机的 IMEI 和 CDMA手机的 MEID. Return null if device ID is not
	 * available.
	 */
	public static String getIMEI() {
		TelephonyManager tm = (TelephonyManager) App.getInstace()
				.getSystemService(Context.TELEPHONY_SERVICE);

		return tm.getDeviceId();// String
	}
	
	public static void genDialog(Context content, String message, String title,
			DialogInterface.OnClickListener yesOnclickImp) {
		new AlertDialog.Builder(content)
				.setMessage(message)
				.setPositiveButton("是", yesOnclickImp)
				.setNegativeButton("否", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				})
				.setNeutralButton("不再提示",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
								isShowCheckNetDialog = false;
							}
						}).setTitle(title).show();

	}

	public static void genForceDialog(Context content, String message,
			DialogInterface.OnClickListener yesOnclickImp) {
		AlertDialog mAlertDialog = new AlertDialog.Builder(content)
				.setMessage(message).setCancelable(false)
				.setPositiveButton("确定", yesOnclickImp).setTitle("提示").create();
		mAlertDialog.getWindow().setType(
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		mAlertDialog.show();

	}

	static boolean isShowCheckNetDialog = true;
	public static boolean checkNetwork() {
		ConnectivityManager conn = (ConnectivityManager) App.getInstace()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo net = conn.getActiveNetworkInfo();
		if (net != null && net.isConnected()) {
			return true;
		}
		return false;
	}

	public static void checkNetInActivity(Activity activity) {
		final Context mcontext = activity;
		if (!AndroidUtil.checkNetwork() && isShowCheckNetDialog) {
			AndroidUtil.genDialog(mcontext, "没有可用的网络连接,是否去设置?", "提示",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(
									"android.settings.WIRELESS_SETTINGS");
							mcontext.startActivity(intent);
							dialog.cancel();
						}
					});
		} else {
		}

	}

	public static boolean saveToPNG(byte rgb24[], int width, int height,
			String filename) {
		byte data[] = rgb24;
		try {
			Bitmap bitmap = creatBitmap(width, height, data);
			if (bitmap != null) {
				File file2 = new File(filename);
				OutputStream os = new FileOutputStream(file2);
				file2.createNewFile();
				bitmap.compress(CompressFormat.PNG, 100, os);
				bitmap.recycle();
			} else {
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	private static Bitmap creatBitmap(int width, int height, byte[] rgb24) {
		int Rgb[] = convertByteToColor(rgb24);
		Bitmap bitmap = Bitmap.createBitmap(Rgb, 0, width, width, height,
				Bitmap.Config.ARGB_8888);
		return bitmap;
	}

	/*
	 * 灏�4RGB鏁扮粍杞寲涓哄儚绱犳暟缁�
	 */
	public static int[] convertByteToColor(byte[] data) {
		int size = data.length;
		if (size == 0) {
			return null;
		}
		// 鐞嗚涓奷ata鐨勯暱搴﹀簲璇ユ槸3鐨勫�鏁帮紝杩欓噷鍋氫釜鍏煎
		int arg = 0;
		if (size % 3 != 0) {
			arg = 1;
		}

		int[] color = new int[size / 3 + arg];

		if (arg == 0) { // 姝ｅソ鏄�鐨勫�鏁�
			for (int i = 0; i < color.length; ++i) {

				color[i] = (data[i * 3] << 16 & 0x00FF0000)
						| (data[i * 3 + 1] << 8 & 0x0000FF00)
						| (data[i * 3 + 2] & 0x000000FF) | 0xFF000000;
			}
		} else { // 涓嶆槸3鐨勫�鏁�
			for (int i = 0; i < color.length - 1; ++i) {
				color[i] = (data[i * 3] << 16 & 0x00FF0000)
						| (data[i * 3 + 1] << 8 & 0x0000FF00)
						| (data[i * 3 + 2] & 0x000000FF) | 0xFF000000;
			}

			color[color.length - 1] = 0xFF000000; // 鏈�悗涓�釜鍍忕礌鐢ㄩ粦鑹插～鍏�
		}

		return color;
	}

	public static String getLocalIP() {
		String macAddress = null, ip = null;
		WifiManager wifiMgr = (WifiManager) App.getInstace()
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = (null == wifiMgr ? null : wifiMgr.getConnectionInfo());
		if (null != info) {
			macAddress = info.getMacAddress();
			ip = int2ip(info.getIpAddress());
		}
		System.out.println("mac:" + macAddress + ",ip:" + ip);
		return ip;

	}

	public static String int2ip(long ipInt) {
		StringBuilder sb = new StringBuilder();
		sb.append(ipInt & 0xFF).append(".");
		sb.append((ipInt >> 8) & 0xFF).append(".");
		sb.append((ipInt >> 16) & 0xFF).append(".");
		sb.append((ipInt >> 24) & 0xFF);
		return sb.toString();
	}

	public static SharedPreferences getConfigSharedPreferences() {
		SharedPreferences configSharedPreferences = App.getInstace()
				.getSharedPreferences("config", 0);
		return configSharedPreferences;
	}

	public static boolean isFirstUseApp() {
		SharedPreferences configSharedPreferences = getConfigSharedPreferences();
		boolean is = configSharedPreferences.getBoolean("ISFIRSTUSEAPP", true);
		setFirstUseApp(false);
		return is;
	}

	public static boolean setFirstUseApp(boolean is) {
		SharedPreferences configSharedPreferences = getConfigSharedPreferences();
		Editor edit = configSharedPreferences.edit();
		edit.putBoolean("ISFIRSTUSEAPP", is);
		return edit.commit();
	}

	/**
	 * save to sharedPreferences
	 * 
	 */
	public static void saveToSharePreferences(HashMap<String, String> kys) {
		SharedPreferences configSharedPreferences = getConfigSharedPreferences();
		Editor edit = configSharedPreferences.edit();
		String[] keys = (String[]) kys.keySet().toArray();
		for (String string : keys) {
			edit.putString(string, kys.get(string));
		}
		edit.commit();
	}

	private static void saveToSharePreferences(String key, String value) {
		SharedPreferences configSharedPreferences = getConfigSharedPreferences();
		Editor edit = configSharedPreferences.edit();
		edit.putString(key, value);
		edit.commit();
	}

	public static String getValueFromSharePreferences(String key) {
		SharedPreferences configSharedPreferences = getConfigSharedPreferences();
		return configSharedPreferences.getString(key, "");
	}

	public static String getValueFromSharePreferences(String key,
			String getUserName4Push) {
		SharedPreferences configSharedPreferences = getConfigSharedPreferences();
		return configSharedPreferences.getString(getUserName4Push + "@@" + key,
				"");
	}

	public static void saveToSharePreferences(String key,
			String getUserName4Push, String value) {
		saveToSharePreferences(getUserName4Push + "@@" + key, value);
	}
	private static LayoutInflater ll;
	public static LayoutInflater getLayoutInflater() {
		if (ll==null) {
			ll = ((LayoutInflater)App.getInstace().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
		}
		return ll;
	}

	public static void show(Context activity, String string) {
		Toast.makeText(activity, string, Toast.LENGTH_SHORT).show();
		
	}
}
