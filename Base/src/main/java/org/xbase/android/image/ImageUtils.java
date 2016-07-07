package org.xbase.android.image;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.impl.BaseDiscCache;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.StorageUtils;

import org.xbase.android.log.Logger;
import org.xbase.android.utils.BuilderConfig;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ImageUtils {
	private static final Logger LOG = Logger.getLogger(ImageUtils.class);

	private static final String URL_HTTP_PREFIX = "http://";
	private static final String URL_HTTPS_PREFIX = "https://";
	private static String uiImgPrefix = "http://";
	private static volatile ImageUtils mUtils;
	private static final Object mLock = new Object();
	private ImageLoader mImageLoader;
	private DisplayImageOptions mDefaultOptions;
	private BaseDiscCache mDiscCache;
	private String mUrlPrefix;
	private OnConfigListener mOnConfigListener;

	private ImageUtils() {
		mImageLoader = ImageLoader.getInstance();
		DisplayImageOptions.Builder optionsBuilder = new DisplayImageOptions.Builder();
		optionsBuilder.resetViewBeforeLoading(false);
		optionsBuilder.cacheInMemory(true);
		optionsBuilder.cacheOnDisk(true);
		optionsBuilder.imageScaleType(ImageScaleType.EXACTLY);
		optionsBuilder.bitmapConfig(Bitmap.Config.RGB_565);
		optionsBuilder.displayer(new FadeInBitmapDisplayer(100));
		mDefaultOptions = optionsBuilder.build();
	}

	public static ImageUtils getInstance() {
		if (mUtils==null){
			synchronized (mLock) {
				if (mUtils == null) {
					mUtils = new ImageUtils();
				}
			}
		}
		return mUtils;
	}

	public void configUrlPrefix(String pPrefix, String pUiImgPrefix) {
		if (!TextUtils.isEmpty(pPrefix) && isContainsScheme(pUiImgPrefix)) {
			mUrlPrefix = pPrefix;
		}
		if (!TextUtils.isEmpty(pUiImgPrefix) && isContainsScheme(pUiImgPrefix)) {
			uiImgPrefix = pUiImgPrefix;
			if (mOnConfigListener != null) {
				mOnConfigListener.onConfigSucc();
			}
		} else {
			if (mOnConfigListener != null) {
				mOnConfigListener.onConfigFailed();
			}
		}
	}

	/**
	 * drawable 图片不缓存
	 *
	 * @param url
	 * @param pBuilder
	 */
	private void drawableNoCache(String pUrl, Builder pBuilder) {
		if (!TextUtils.isEmpty(pUrl) && !pUrl.startsWith(URL_HTTP_PREFIX)
				&& !pUrl.startsWith(URL_HTTPS_PREFIX)) {
			// 非HTTP图片不做磁盘缓存
			pBuilder.cacheOnDisk(false);
		} else {
			// HTTP图片需要磁盘缓存
			pBuilder.cacheOnDisk(true);
		}
	}

	/**
	 * 初始化ImageLoader，推荐在Application创建时调用
	 *
	 * @param pContext
	 */
	public void init(Context pContext) {
		LOG.d("ImageUtils.init() Start!");
		// 内存缓存大小，APP可用内存的1/6
		int memoryCacheSize = 0;
		memoryCacheSize = getMemoryCacheSize(pContext);
		LOG.d("memoryCacheSize: " + memoryCacheSize);
		// 磁盘缓存路径
		File cacheDir = StorageUtils.getCacheDirectory(pContext);
		if (cacheDir != null) {
			LOG.d("Image cache dir: " + cacheDir.getPath());
		}
		mDiscCache = new UnlimitedDiscCache(cacheDir);

		ImageLoaderConfiguration.Builder configBuilder = new ImageLoaderConfiguration.Builder(
				pContext);
		configBuilder.threadPoolSize(3);
		configBuilder.threadPriority(Thread.NORM_PRIORITY - 1);
		configBuilder.tasksProcessingOrder(QueueProcessingType.LIFO);
		configBuilder.memoryCache(new LruMemoryCache(memoryCacheSize));
		// configBuilder.memoryCache(new WeakMemoryCache());
		configBuilder.diskCache(mDiscCache);
		// configBuilder.imageDownloader(new BaseImageDownloader(pContext));
		configBuilder.imageDownloader(new LibImageDownloader(pContext, 3000,
				10000));
		configBuilder.imageDecoder(new BaseImageDecoder(BuilderConfig.IsDebug));
		configBuilder.defaultDisplayImageOptions(mDefaultOptions);
		configBuilder.diskCacheExtraOptions(1920, 1080, null);
		ImageLoader.getInstance().init(configBuilder.build());
		LOG.d("ImageUtils.init() Finished!");

	}

	/*
	 * --------------------copy start---------------------------------------
	 * --com.nostra13.universalimageloader.core.DefaultConfigurationFactory-
	 */

	/**
	 * Default cache size = 1/8 of available app memory.
	 *
	 * @param pContext
	 * @return
	 */
	private int getMemoryCacheSize(Context pContext) {
		ActivityManager am = (ActivityManager) pContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		int memoryClass = am.getMemoryClass();
		if (hasHoneycomb() && isLargeHeap(pContext)) {
			memoryClass = getLargeMemoryClass(am);
		}
		int memoryCacheSize = 1024 * 1024 * memoryClass / 8;
		return memoryCacheSize;
	}

	private static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static boolean isLargeHeap(Context context) {
		return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_LARGE_HEAP) != 0;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static int getLargeMemoryClass(ActivityManager am) {
		return am.getLargeMemoryClass();
	}

	/*
	 * --------------------copy end-----------------------------------------
	 * --com.nostra13.universalimageloader.core.DefaultConfigurationFactory-
	 */

	/**
	 * 加载圆角图
	 *
	 * @param pUrl
	 *            图片地址，可有如下定义<br/>
	 *            &emsp; String pUrl = "http://site.com/image.png"; // 网络图片
	 *            注意：如果有调用过 configUrlPrefix， 则传入相对地址，否则是绝对地址 <br/>
	 *            &emsp; String pUrl = "file:///mnt/sdcard/image.png"; // 本地图片 <br/>
	 *            &emsp; String pUrl =
	 *            "content://media/external/audio/albumart/13"; // Content
	 *            Provider图片 <br/>
	 *            &emsp; String pUrl = "assets://image.png"; // Assert图片 <br/>
	 *            &emsp; String pUrl = "drawable://" + R.drawable.image; //
	 *            Drawables (<strong>only images, non-9patch</strong>) <br/>
	 * @param pImageView
	 *            需要设置图片的{@link ImageView}
	 * @param pDefaultImageResid
	 *            默认图片资源ID，将用在加载过程中及加载失败时
	 */
	public void showImageRounded(String pUrl, ImageView pImageView,
			int pDefaultImageResid, int pCornerRadiusPixels) {
		showImageRounded(pUrl, pImageView, pDefaultImageResid, false, null,
				pCornerRadiusPixels);
	}

	/**
	 * 加载圆角图
	 *
	 * @param pUrl
	 *            图片地址，可有如下定义<br/>
	 *            &emsp; String pUrl = "http://site.com/image.png"; // 网络图片
	 *            注意：如果有调用过 configUrlPrefix， 则传入相对地址，否则是绝对地址 <br/>
	 *            &emsp; String pUrl = "file:///mnt/sdcard/image.png"; // 本地图片 <br/>
	 *            &emsp; String pUrl =
	 *            "content://media/external/audio/albumart/13"; // Content
	 *            Provider图片 <br/>
	 *            &emsp; String pUrl = "assets://image.png"; // Assert图片 <br/>
	 *            &emsp; String pUrl = "drawable://" + R.drawable.image; //
	 *            Drawables (<strong>only images, non-9patch</strong>) <br/>
	 * @param pImageView
	 *            需要设置图片的{@link ImageView}
	 * @param pDefaultImageResid
	 *            默认图片资源ID，将用在加载过程中及加载失败时
	 * @param pNeedScaleUp
	 *            当图片小于参数{@link pImageView}定义尺寸时是否需要放大，true为放大
	 */
	public void showImageRounded(String pUrl, ImageView pImageView,
			int pDefaultImageResid, boolean pNeedScaleUp,
			int pCornerRadiusPixels) {
		showImageRounded(pUrl, pImageView, pDefaultImageResid, pNeedScaleUp,
				null, pCornerRadiusPixels);
	}

	/**
	 * 加载圆角图
	 *
	 * @param pUrl
	 *            图片地址，可有如下定义<br/>
	 *            &emsp; String pUrl = "http://site.com/image.png"; // 网络图片
	 *            注意：如果有调用过 configUrlPrefix， 则传入相对地址，否则是绝对地址 <br/>
	 *            &emsp; String pUrl = "file:///mnt/sdcard/image.png"; // 本地图片 <br/>
	 *            &emsp; String pUrl =
	 *            "content://media/external/audio/albumart/13"; // Content
	 *            Provider图片 <br/>
	 *            &emsp; String pUrl = "assets://image.png"; // Assert图片 <br/>
	 *            &emsp; String pUrl = "drawable://" + R.drawable.image; //
	 *            Drawables (<strong>only images, non-9patch</strong>) <br/>
	 * @param pImageView
	 *            需要设置图片的{@link ImageView}
	 * @param pDefaultImageResid
	 *            默认图片资源ID，将用在加载过程中及加载失败时
	 * @param pNeedScaleUp
	 *            当图片小于参数{@link pImageView}定义尺寸时是否需要放大，true为放大
	 * @param pLoadingListener
	 *            图片加载监听器，可用于图片预加载
	 */
	public void showImageRounded(String pUrl, ImageView pImageView,
								 int pDefaultImageResid, boolean pNeedScaleUp,
								 ImageLoadingListener pLoadingListener, int pCornerRadiusPixels) {
		if (pImageView != null) {
			Builder builder = new DisplayImageOptions.Builder()
					.cloneFrom(mDefaultOptions);
			if (pDefaultImageResid > 0) {
				builder.showImageOnLoading(pDefaultImageResid);
				builder.showImageForEmptyUri(pDefaultImageResid);
				builder.showImageOnFail(pDefaultImageResid);
			}
			if (pNeedScaleUp) {
				builder.imageScaleType(ImageScaleType.EXACTLY_STRETCHED);
			} else {
				builder.imageScaleType(ImageScaleType.IN_SAMPLE_INT);
			}
			builder.displayer(new RoundedBitmapDisplayer(pCornerRadiusPixels));
			LayoutParams params = pImageView.getLayoutParams();
			LOG.d("params.width : " + params.width + ", params.height : "
					+ params.height);
			LOG.d("width : " + pImageView.getWidth() + ", height : "
					+ pImageView.getHeight());
			if ((params.width <= 0 || params.height <= 0)
					&& (pImageView.getWidth() > 0 && pImageView.getHeight() > 0)) {
				params.width = pImageView.getWidth();
				params.height = pImageView.getHeight();
				pImageView.setLayoutParams(params);
			}
			String fullUrl = getFullUrl(pUrl);
			LOG.d("displayImage: " + fullUrl);
			drawableNoCache(fullUrl, builder);
			mImageLoader.displayImage(fullUrl, pImageView, builder.build(),
					pLoadingListener);
		}
	}

	public void showImage(String pUrl, ImageView pImageView) {
		showImageFadeIn(pUrl, pImageView, 0);
	}

	/**
	 * 以渐入方式加载图
	 *
	 * @param pUrl
	 *            图片地址，可有如下定义<br/>
	 *            &emsp; String pUrl = "http://site.com/image.png"; // 网络图片
	 *            注意：如果有调用过 configUrlPrefix， 则传入相对地址，否则是绝对地址 <br/>
	 *            &emsp; String pUrl = "file:///mnt/sdcard/image.png"; // 本地图片 <br/>
	 *            &emsp; String pUrl =
	 *            "content://media/external/audio/albumart/13"; // Content
	 *            Provider图片 <br/>
	 *            &emsp; String pUrl = "assets://image.png"; // Assert图片 <br/>
	 *            &emsp; String pUrl = "drawable://" + R.drawable.image; //
	 *            Drawables (<strong>only images, non-9patch</strong>) <br/>
	 * @param pImageView
	 *            需要设置图片的{@link ImageView}
	 * @param pDefaultImageResid
	 *            默认图片资源ID，将用在加载过程中及加载失败时
	 */
	public void showImageFadeIn(String pUrl, ImageView pImageView,
			int pDefaultImageResid) {
		showImageFadeIn(pUrl, pImageView, pDefaultImageResid, false, null);
	}

	public void showImageFadeIn(String pUrl, ImageView pImageView,
			int pDefaultImageResid, int pFadeInAnimDuration) {
		showImage(pUrl, pImageView, pDefaultImageResid, false,
				pFadeInAnimDuration, null);
	}

	/**
	 * 以渐入方式加载图
	 *
	 * @param pUrl
	 *            图片地址，可有如下定义<br/>
	 *            &emsp; String pUrl = "http://site.com/image.png"; // 网络图片
	 *            注意：如果有调用过 configUrlPrefix， 则传入相对地址，否则是绝对地址 <br/>
	 *            &emsp; String pUrl = "file:///mnt/sdcard/image.png"; // 本地图片 <br/>
	 *            &emsp; String pUrl =
	 *            "content://media/external/audio/albumart/13"; // Content
	 *            Provider图片 <br/>
	 *            &emsp; String pUrl = "assets://image.png"; // Assert图片 <br/>
	 *            &emsp; String pUrl = "drawable://" + R.drawable.image; //
	 *            Drawables (<strong>only images, non-9patch</strong>) <br/>
	 * @param pImageView
	 *            需要设置图片的{@link ImageView}
	 * @param pDefaultImageResid
	 *            默认图片资源ID，将用在加载过程中及加载失败时
	 * @param pNeedScaleUp
	 *            当图片小于参数{@link pImageView}定义尺寸时是否需要放大，true为放大
	 */
	public void showImageFadeIn(String pUrl, ImageView pImageView,
			int pDefaultImageResid, boolean pNeedScaleUp) {
		showImageFadeIn(pUrl, pImageView, pDefaultImageResid, pNeedScaleUp,
				null);
	}

	public void showImageFadeIn(String pUrl, ImageView pImageView,
			int pDefaultImageResid, boolean pNeedScaleUp,
			int pFadeInAnimDuration) {
		showImage(pUrl, pImageView, pDefaultImageResid, pNeedScaleUp,
				pFadeInAnimDuration, null);
	}

	/**
	 * 以渐入方式加载图
	 *
	 * @param pUrl
	 *            图片地址，可有如下定义<br/>
	 *            &emsp; String pUrl = "http://site.com/image.png"; // 网络图片
	 *            注意：如果有调用过 configUrlPrefix， 则传入相对地址，否则是绝对地址 <br/>
	 *            &emsp; String pUrl = "file:///mnt/sdcard/image.png"; // 本地图片 <br/>
	 *            &emsp; String pUrl =
	 *            "content://media/external/audio/albumart/13"; // Content
	 *            Provider图片 <br/>
	 *            &emsp; String pUrl = "assets://image.png"; // Assert图片 <br/>
	 *            &emsp; String pUrl = "drawable://" + R.drawable.image; //
	 *            Drawables (<strong>only images, non-9patch</strong>) <br/>
	 * @param pImageView
	 *            需要设置图片的{@link ImageView}
	 * @param pDefaultImageResid
	 *            默认图片资源ID，将用在加载过程中及加载失败时
	 * @param pNeedScaleUp
	 *            当图片小于参数{@link pImageView}定义尺寸时是否需要放大，true为放大
	 * @param pLoadingListener
	 *            图片加载监听器，可用于图片预加载
	 */
	public void showImageFadeIn(String pUrl, ImageView pImageView,
			int pDefaultImageResid, boolean pNeedScaleUp,
			ImageLoadingListener pLoadingListener) {
		showImage(pUrl, pImageView, pDefaultImageResid, pNeedScaleUp, 200,
				pLoadingListener);
	}

	public void showImage(String pUrl, ImageView pImageView,
			int pDefaultImageResid) {
		if (pImageView != null) {
			showImage(pUrl, pImageView, pDefaultImageResid, false, 0, null);
		}
	}

	/**
	 *
	 * @param pUrl
	 * @param pImageView
	 * @param pDefaultImageResid
	 * @param pNeedScaleUp
	 * @param pFadeInAnimDuration
	 * @param pLoadingListener
	 */
	public void showImage(String pUrl, ImageView pImageView,
			int pDefaultImageResid, boolean pNeedScaleUp,
			int pFadeInAnimDuration, ImageLoadingListener pLoadingListener) {
		if (pImageView != null && !TextUtils.isEmpty(pUrl)) {
			Builder builder = new DisplayImageOptions.Builder()
					.cloneFrom(mDefaultOptions);

			if (pDefaultImageResid > 0) {
				builder.showImageOnLoading(pDefaultImageResid);
				builder.showImageForEmptyUri(pDefaultImageResid);
				builder.showImageOnFail(pDefaultImageResid);
			}
			if (pNeedScaleUp) {
				builder.imageScaleType(ImageScaleType.EXACTLY_STRETCHED);
			} else {
				builder.imageScaleType(ImageScaleType.IN_SAMPLE_INT);
			}
			BitmapDisplayer displayer = null;
			if (pFadeInAnimDuration > 0) {
				displayer = new FadeInBitmapDisplayer(pFadeInAnimDuration);
				builder.resetViewBeforeLoading(true);
			} else {
				displayer = new SimpleBitmapDisplayer();
				builder.resetViewBeforeLoading(false);
			}
			builder.displayer(displayer);
			String fullUrl = getFullUrl(pUrl);
			LOG.d("displayImage: " + fullUrl);
			drawableNoCache(fullUrl, builder);
			mImageLoader.displayImage(fullUrl, pImageView, builder.build(),
					pLoadingListener);
		}
	}

	public void showImageAsBackground(String pUrl, View pView) {
		showImageAsBackground(pUrl, pView, 0, false, null);
	}

	/**
	 * 1.2新增，处理首页黑块问题通用设置RGB_5555,此方法使用ARGB_8888加载图片
	 *
	 * @param pUrl
	 * @param pView
	 */
	public void showHighQualityImageAsBackground(String pUrl, View pView) {
		showHighQualityImageAsBackground(pUrl, pView, 0, false, null);
	}

	public void showHighQualityImageAsBackground(String pUrl, View pView,
			int pDefaultImageResid, boolean pNeedScaleUp,
			ImageLoadingListener pLoadingListener) {
		if (pView != null) {
			Builder builder = new DisplayImageOptions.Builder()
					.cloneFrom(mDefaultOptions);
			builder.bitmapConfig(Bitmap.Config.ARGB_8888);
			if (pDefaultImageResid > 0) {
				builder.showImageOnLoading(pDefaultImageResid);
				builder.showImageForEmptyUri(pDefaultImageResid);
				builder.showImageOnFail(pDefaultImageResid);
			}
			if (pNeedScaleUp) {
				builder.imageScaleType(ImageScaleType.EXACTLY_STRETCHED);
			} else {
				builder.imageScaleType(ImageScaleType.IN_SAMPLE_INT);
			}
			builder.displayer(new SimpleBitmapDisplayer());
			builder.resetViewBeforeLoading(false);
			String fullUrl = getFullUrl(pUrl);
			LOG.d("displayImage HighQuality: " + fullUrl);
			drawableNoCache(fullUrl, builder);
			BackgroundViewAware bgViewAware = new BackgroundViewAware(pView,
					true);

			mImageLoader.displayImage(fullUrl, bgViewAware, builder.build(),
					pLoadingListener);
		}
	}

	public void showImageAsBackground(String pUrl, View pView,
			int pDefaultImageResid, boolean pNeedScaleUp,
			ImageLoadingListener pLoadingListener) {
		if (pView != null) {
			Builder builder = new DisplayImageOptions.Builder()
					.cloneFrom(mDefaultOptions);
			if (pDefaultImageResid > 0) {
				builder.showImageOnLoading(pDefaultImageResid);
				builder.showImageForEmptyUri(pDefaultImageResid);
				builder.showImageOnFail(pDefaultImageResid);
			}
			if (pNeedScaleUp) {
				builder.imageScaleType(ImageScaleType.EXACTLY_STRETCHED);
			} else {
				builder.imageScaleType(ImageScaleType.IN_SAMPLE_INT);
			}
			builder.displayer(new SimpleBitmapDisplayer());
			builder.resetViewBeforeLoading(false);
			String fullUrl = getFullUrl(pUrl);
			LOG.d("displayImage: " + fullUrl);
			drawableNoCache(fullUrl, builder);
			BackgroundViewAware bgViewAware = new BackgroundViewAware(pView,
					true);

			mImageLoader.displayImage(fullUrl, bgViewAware, builder.build(),
					pLoadingListener);
		}
	}

	/**
	 * 异步加载图片
	 * 
	 * @param pUrl
	 *            如果有调用过 configUrlPrefix， 则传入相对地址，否则是绝对地址
	 */
	public void loadImage(String pUrl) {
		loadImage(pUrl, null);
	}

	/**
	 * 异步加载图片
	 * 
	 * @param pUrl
	 *            如果有调用过 configUrlPrefix， 则传入相对地址，否则是绝对地址
	 * @param pListener
	 *            过程监听
	 */
	public void loadImage(String pUrl, ImageLoadingListener pListener) {
		String fullUrl = getFullUrl(pUrl);
		if (mImageLoader != null) {
			mImageLoader.loadImage(fullUrl, pListener);
		}
	}

	/**
	 * 同步加载图片
	 * 
	 * @param pUrl
	 *            如果有调用过 configUrlPrefix， 则传入相对地址，否则是绝对地址
	 */
	public Bitmap loadImageSync(String pUrl) {
		String fullUrl = getFullUrl(pUrl);
		if (mImageLoader != null) {
			return mImageLoader.loadImageSync(fullUrl);
		} else {
			return null;
		}
	}

	/**
	 * 异步下载图片，保存到指定文件下
	 * 
	 * @param pUrl
	 * @param pSaveFileName
	 */
	public void loadImageAsyn(String pUrl, String pSaveFileName) {
		try {
			String fullUrl = getFullUrl(pUrl);
			byte[] data = getImage(fullUrl);
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			saveBitmap(pSaveFileName, bitmap);
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get image from newwork
	 * 
	 * @param path
	 *            The path of image
	 * @return byte[]
	 * @throws Exception
	 */
	public byte[] getImage(String pPath) throws Exception {
		URL url = new URL(pPath);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5 * 1000);
		conn.setRequestMethod("GET");
		InputStream inStream = conn.getInputStream();
		if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
			return readStream(inStream);
		}
		return null;
	}

	/**
	 * Get data from stream
	 * 
	 * @param inStream
	 * @return byte[]
	 * @throws Exception
	 */
	public static byte[] readStream(InputStream inStream) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		outStream.close();
		inStream.close();
		return outStream.toByteArray();
	}

	/**
	 * 清除内存缓存
	 */
	public void clearMemoryCache() {
		if (mImageLoader != null) {
			mImageLoader.clearMemoryCache();
		}
	}

	public String getFullUrl(String pUrl) {
		String fullUrl;
		if (!TextUtils.isEmpty(mUrlPrefix) && pUrl != null
				&& !isContainsScheme(pUrl)) {
			if (mUrlPrefix.endsWith("/") || pUrl.startsWith("/")) {
				fullUrl = mUrlPrefix + pUrl;
			} else {
				fullUrl = mUrlPrefix + "/" + pUrl;
			}
		} else {
			fullUrl = pUrl;
		}
		return fullUrl;
	}

	private boolean isContainsScheme(String pUrl) {
		if (!TextUtils.isEmpty(pUrl)) {
			return pUrl.startsWith("http://") || pUrl.startsWith("https://")
					|| pUrl.startsWith("file://")
					|| pUrl.startsWith("drawable://")
					|| pUrl.startsWith("assets://")
					|| pUrl.startsWith("content://");
		}
		return false;
	}

	public static void saveBitmapAsZipFile(String fileName,
			final String zipEntryName, Bitmap bitmap) {
		ZipOutputStream out = null;
		try {
			out = new ZipOutputStream(new BufferedOutputStream(
					new FileOutputStream(fileName)));
			out.putNextEntry(new ZipEntry(zipEntryName));
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			bitmap.recycle();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 保存图片
	 * 
	 * @param fileName
	 * @param bitmap
	 */
	public static void saveBitmap(String fileName, Bitmap bitmap) {
		FileOutputStream out = null;
		try {
			File file = new File(fileName);
			if (!file.exists()) {
				File parentF = file.getParentFile();
				if (parentF != null && !parentF.exists()) {
					parentF.mkdirs();
				}
			}
			out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			bitmap.recycle();
		} catch (Exception e) {
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/*
	 * 获取压缩文件里面的bitmap
	 */
	public static Bitmap loadBitmapFromZip(File file, final String zipEntryName) {
		Bitmap bmp = null;
		ZipInputStream in = null;
		try {
			BufferedInputStream br = new BufferedInputStream(
					new FileInputStream(file));
			in = new ZipInputStream(br);
			ZipEntry entry;
			while ((entry = in.getNextEntry()) != null) {
				if (entry.getName().contains(zipEntryName)) {
					break;
				}
			}
			if (entry != null && in != null) {
				bmp = BitmapFactory.decodeStream(in);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.e("loadBitmap from file: ", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return bmp;
	}

	/*
	 * 获取文件里面的bitmap
	 */
	public static Bitmap loadBitmap(File file) {
		Bitmap bmp = null;
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			if (in != null) {
				bmp = BitmapFactory.decodeStream(in);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.e("loadBitmap from file: ", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return bmp;
	}

	public static void saveBitmap(Bitmap pBitmap, String pSavePath,
			String pFileName) {
		if (pBitmap != null && !pBitmap.isRecycled()) {
			File dir = new File(pSavePath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(pSavePath + File.separator
						+ pFileName);
				pBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.flush();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static Bitmap loadBitmap(String pSavePath, String pFileName) {
		Bitmap result = null;
		String fullPath = pSavePath + File.separator + pFileName;
		File file = new File(fullPath);
		if (file.exists()) {
			result = BitmapFactory.decodeFile(fullPath);
		}

		return result;
	}

	public boolean clearAllDiskCache(Context pContext) {
		boolean isCleared = false;
		if (mDiscCache != null) {
			mDiscCache.clear();
			isCleared = true;
		}
		return isCleared;
	}

	public void clearURLDiskCache(String pUrl) {
		if (mDiscCache != null) {
			String fullUrl = getFullUrl(pUrl);
			mDiscCache.remove(fullUrl);
		}
	}

	private String processUrl(String pUrl) {
		if (isContainsScheme(pUrl)) {
			return pUrl;
		}
		if (TextUtils.isEmpty(pUrl)) {
			return "";
		}

		return "";
	}

	/**
	 * 显示界面UI图片
	 * 
	 * @param pUrl
	 *            可以传文件名(带后缀), 也可以传网络上的全路径
	 * @param pImageView
	 *            用来展示的控件
	 */
	public void showUiImage(String pUrl, ImageView pImageView) {
		showUiImage(processUrl(pUrl), pImageView, 0);
	}

	/**
	 * 显示界面UI图片
	 * 
	 * @param pUrl
	 *            可以传文件名(带后缀), 也可以传网络上的全路径
	 * @param pImageView
	 *            用来展示的控件
	 * @param pDefaultImageResid
	 *            默认图片
	 */
	public void showUiImage(String pUrl, ImageView pImageView,
			int pDefaultImageResid) {
		ImageUtils.getInstance().showImage(processUrl(pUrl), pImageView,
				pDefaultImageResid);
	}

	/**
	 * 显示界面UI图片,作为背景
	 * 
	 * @param pUrl
	 *            可以传文件名(带后缀), 也可以传网络上的全路径
	 * @param pView
	 *            用来展示的控件
	 */
	public void showUiImageAsBackground(String pUrl, View pView) {
		showUiImageAsBackground(processUrl(pUrl), pView, 0, false, null);
	}

	/**
	 * 显示界面UI图片,作为背景
	 * 
	 * @param pUrl
	 *            可以传文件名(带后缀), 也可以传网络上的全路径
	 * @param pView
	 *            用来展示的控件
	 * @param pNeedScaleUp
	 *            是否缩放
	 */
	public void showUiImageAsBackground(String pUrl, View pView,
			boolean pNeedScaleUp) {
		showUiImageAsBackground(processUrl(pUrl), pView, 0, pNeedScaleUp, null);
	}

	/**
	 * 显示界面UI图片,作为背景
	 * 
	 * @param pUrl
	 *            可以传文件名(带后缀), 也可以传网络上的全路径
	 * @param pView
	 *            用来展示的控件
	 * @param pNeedScaleUp
	 *            是否缩放
	 * @param pLoadingListener
	 *            图片加载监听
	 */
	public void showUiImageAsBackground(String pUrl, View pView,
			int pDefaultImageResid, boolean pNeedScaleUp,
			ImageLoadingListener pLoadingListener) {
		ImageUtils.getInstance().showImageAsBackground(processUrl(pUrl), pView,
				pDefaultImageResid, pNeedScaleUp, pLoadingListener);
	}

	public void setOnConfigListener(OnConfigListener pOnConfigListener) {
		this.mOnConfigListener = pOnConfigListener;
		if (this.mOnConfigListener == null) {
			return;
		}
		if (!TextUtils.isEmpty(this.mUrlPrefix)) {
			this.mOnConfigListener.onConfigSucc();
		}
	}

	public void removeOnConfigListener() {
		this.mOnConfigListener = null;
	}

	public interface OnConfigListener {
		public void onConfigSucc();

		public void onConfigFailed();
	}
}
