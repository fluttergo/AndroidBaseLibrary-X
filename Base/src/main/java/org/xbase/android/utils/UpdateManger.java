package org.xbase.android.utils;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UpdateManger {
	public interface ComparisonVersion {
		/**
		 * ���÷������ϵİ汾�ԱȽӿ�URL
		 * 
		 * @return
		 */
		public String configHttpUrl();

		/**
		 * �ԱȰ汾��Ϣ
		 * 
		 * @param httpVersionInfo
		 * @return �Ƿ���Ҫ��
		 */
		public boolean comparisonVersion(String httpVersionInfo);

		/**
		 * �����µİ汾���ص�ַ
		 * 
		 * @return
		 */
		public String configNewVersionDownloadUrl();

		/**
		 * �����µİ汾������Ϣ
		 * 
		 * @return
		 */
		public String configNewVersionInfo();
	}

	private Context mContext;

	// ��ʾ��
	private String updateMsg = "有新的版本";

	// ���صİ�װ��url
	private String apkUrl = "http://softfile.3g.qq.com:8080/msoft/179/24659/43549/qq_hd_mini_1.4.apk";

	private Dialog noticeDialog;

	private Dialog downloadDialog;
	/* ���ذ�װ·�� */
	private static final String savePath = Environment
			.getExternalStorageDirectory().getPath();

	private static final String saveFileName = savePath
			+ "/UpdateDemoRelease.apk";

	/* �������֪ͨuiˢ�µ�handler��msg���� */
	private ProgressBar mProgress;
	private TextView mTextView;

	private static final int DOWN_UPDATE = 1;

	private static final int DOWN_OVER = 2;

	private int progress;

	private Thread downLoadThread;

	private boolean interceptFlag = false;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWN_UPDATE:
				mProgress.setProgress(progress);
				mTextView.setText("..."+progress);
				break;
			case DOWN_OVER:

				installApk();
				break;
			default:
				break;
			}
		};
	};

	public UpdateManger(Context context) {
		this.mContext = context;
	}

	public void checkUpdateInfo(final GLApkVersionManger glApkVersionManger) {
//		FinalHttp fh = new FinalHttp();
//		fh.get(glApkVersionManger.configHttpUrl(), new AjaxCallBack<String>() {
//			@Override
//			public void onSuccess(String t) {
//				super.onSuccess(t);
//				if ("shutdown".equals(t)) {
//					System.exit(0);
//				}
//				if (glApkVersionManger.comparisonVersion(t)) {
//					showNoticeDialog(
//							glApkVersionManger.configNewVersionDownloadUrl(),
//							glApkVersionManger.configNewVersionInfo());
//				}
//				;
//			}
//		});
	}

	private void showNoticeDialog(String url, String info) {
		apkUrl = url;
		updateMsg = info;
		Builder builder = new Builder(mContext);
		builder.setTitle("有新的版本");
		builder.setMessage(updateMsg);
		builder.setPositiveButton("更新", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				showDownloadDialog();
			}
		});
		builder.setNegativeButton("不更新", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		noticeDialog = builder.create();
		noticeDialog.show();
	}

	private void showDownloadDialog() {
		Builder builder = new Builder(mContext);
		builder.setTitle("下载中");

		// final LayoutInflater inflater = LayoutInflater.from(mContext);
		LinearLayout ll = new LinearLayout(mContext);
		ProgressBar pbar = new ProgressBar(mContext);
		mTextView = new TextView(mContext);
		
		ll.addView(pbar);
		ll.addView(mTextView);
		
		// View v =
		// inflater.inflate(android.R.layout.browser_link_context_header, null);
		// mProgress = (ProgressBar)v.findViewById(R.id.progress);
		View v = ll;
		mProgress = pbar;
		builder.setView(v);
		builder.setNegativeButton("取消下载", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				interceptFlag = true;
			}
		});
		builder.setNeutralButton("后台下载", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		downloadDialog = builder.create();
		downloadDialog.show();

		downloadApk();
	}

	private Runnable mdownApkRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				URL url = new URL(apkUrl);

				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.connect();
				int length = conn.getContentLength();
				InputStream is = conn.getInputStream();

				File file = new File(savePath);
				if (!file.exists()) {
					file.mkdir();
				}
				File ApkFile = new File(saveFileName);
				FileOutputStream fos = new FileOutputStream(ApkFile);

				int count = 0;
				byte buf[] = new byte[1024];

				do {
					int numread = is.read(buf);
					count += numread;
					progress = (int) (((float) count / length) * 100);
					// ���½��
					mHandler.sendEmptyMessage(DOWN_UPDATE);
					if (numread <= 0) {
						// �������֪ͨ��װ
						mHandler.sendEmptyMessage(DOWN_OVER);
						break;
					}
					fos.write(buf, 0, numread);
				} while (!interceptFlag);// ���ȡ���ֹͣ����.

				fos.close();
				is.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	};


	private void downloadApk() {
		downLoadThread = new Thread(mdownApkRunnable);
		downLoadThread.start();
	}

	public void installApk() {
		File apkfile = new File(saveFileName);
		if (!apkfile.exists()) {
			return;
		}
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
				"application/vnd.android.package-archive");
		mContext.startActivity(i);

	}

	public void installApk(Uri uri) {
		if (uri == null) {
			throw new NullPointerException("installApk:Uri is  null");
		}
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(uri, "application/vnd.android.package-archive");
		mContext.startActivity(i);
	}

	public void installApk(File file) {
		if (file == null) {
			throw new NullPointerException("installApk:File is  null");
		}
		installApk(Uri.fromFile(file));
	}

	public void updateFromAsset(String fileName) {
		installApk(getAssetFile(fileName));
	}

	public File getAssetFile(String fileName) {

		AssetManager asset = mContext.getAssets();
		try {

			InputStream is = asset.open(fileName);
			File tempSaveFile = new File(savePath + File.separator + fileName);
			FileOutputStream fos = new FileOutputStream(tempSaveFile);
			byte[] buffer = new byte[1024];

			int len = 0;

			while ((len = is.read(buffer)) != -1) {

				fos.write(buffer, 0, len);

			}

			fos.flush();

			is.close();

			fos.close();

			return tempSaveFile;

		} catch (IOException e) {

			e.printStackTrace();

		}
		return null;
	}

	public int getLoacalVersionCode() {
		try {
			PackageManager pm = mContext.getPackageManager();
			PackageInfo info = pm.getPackageInfo(
					mContext.getApplicationInfo().packageName, 0);
			return info.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return 0;
	}
}