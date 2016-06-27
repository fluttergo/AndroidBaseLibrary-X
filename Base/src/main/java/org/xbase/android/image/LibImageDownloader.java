package org.xbase.android.image;

import android.content.Context;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ContentLengthInputStream;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.IoUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class LibImageDownloader extends BaseImageDownloader {

	private static final int REDIRECT_COUNT = 2;

	public LibImageDownloader(Context context) {
		super(context);
	}

	public LibImageDownloader(Context context, int connectTimeout,
			int readTimeout) {
		super(context, connectTimeout, readTimeout);
	}

	/**
	 * Retrieves {@link InputStream} of image by URI (image is located in the
	 * network).
	 * 
	 * @param imageUri
	 *            Image URI
	 * @param extra
	 *            Auxiliary object which was passed to
	 *            {@link DisplayImageOptions.Builder#extraForDownloader(Object)
	 *            DisplayImageOptions.extraForDownloader(Object)}; can be null
	 * @return {@link InputStream} of image
	 * @throws IOException
	 *             if some I/O error occurs during network request or if no
	 *             InputStream could be created for URL.
	 */
	@Override
	protected InputStream getStreamFromNetwork(String imageUri, Object extra)
			throws IOException {
		HttpURLConnection conn = createConnection(imageUri, extra);

		int redirectCount = 0;
		while (conn.getResponseCode() / 100 == 3
				&& redirectCount < REDIRECT_COUNT) {
			conn = createConnection(conn.getHeaderField("Location"), extra);
			redirectCount++;
		}

		InputStream imageStream;
		try {
			imageStream = conn.getInputStream();
		} catch (IOException e) {
			// Read all data to allow reuse connection (http://bit.ly/1ad35PY)
			IoUtils.readAndCloseStream(conn.getErrorStream());
			throw e;
		}
		return new ContentLengthInputStream(new BufferedInputStream(
				imageStream, BUFFER_SIZE), conn.getContentLength());
	}

}
