package org.xbase.android.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

import org.xbase.android.log.Logger;

import java.util.List;


/**
 * <p>
 * 提供了应用VersionCode、VersionName、UmengChannel的获取
 * </p>
 * <p>
 * 提供了应用ProductID的设置和获取；由于各个产品线所使用的ID不同， 推荐在Application创建时设置相应的ProductID到此类
 * </p>
 */
public class AppBasicUtils {
    // ===========================================================
    // Constants
    // ===========================================================

    private static final Logger LOG                  = Logger.getLogger(AppBasicUtils.class);
    private static final String META_DATA_STAT_KEY   = "XXX_STAT_KEY";

    public static final String  UNKNOWN_VERSION_NAME = "UNKNOWN";
    public static final int     UNKNOWN_VERSION_CODE = -1;
    public static final String  CHANNEL_UNKNOWN      = "UNKNOWN";
    public static final String  STAT_KEY_UNKNOWN     = "UNKNOWN";

    // ===========================================================
    // Fields
    // ===========================================================

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

    /**
     * 获取应用VersionName
     * 
     * @param pContext
     * @return
     */
    public static final String getVersionName(Context pContext) {

        if (pContext == null) {
            return UNKNOWN_VERSION_NAME;
        }
        PackageManager packageManager = pContext.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(pContext.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            LOG.e("initVersionInfo.error " + e.toString());
        }
        if (packageInfo != null) {
            LOG.dd("packageInfo.versionName:%s, pContext:%s", packageInfo.versionName, pContext.getClass().getName());
            return packageInfo.versionName;
        } else {
            return UNKNOWN_VERSION_NAME;
        }
    }

    /**
     * 获取应用VersionCode
     * 
     * @param pContext
     * @return
     */
    public static final int getVersionCode(Context pContext) {

        if (pContext == null) {
            return UNKNOWN_VERSION_CODE;
        }

        String packageName = pContext.getPackageName();

        return getVersionCode(pContext, packageName);
    }

    /**
     * 根据包名获取程序的VersionCode
     * 
     * @param pContext
     * @param pPackageName
     * @return
     */
    public static final int getVersionCode(Context pContext, String pPackageName) {
        PackageManager packageManager = pContext.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(pPackageName, 0);
        } catch (NameNotFoundException e) {
            LOG.e("initVersionInfo.error " + e.toString());
        }
        if (packageInfo != null) {
            LOG.dd("packageInfo.versionCode: %s, pContext:%s", packageInfo.versionCode, pContext.getClass().getName());
            return packageInfo.versionCode;
        } else {
            return UNKNOWN_VERSION_CODE;
        }
    }

    /**
     * 获取应用DC渠道ID
     * 
     * @param pContext
     * @return
     */
    public static final String getStatKey(Context pContext, String pPackageName) {
        try {
            ApplicationInfo applicationInfo =
                pContext.getPackageManager().getApplicationInfo(pPackageName,
                    PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);
            if (applicationInfo.metaData != null) {
                return applicationInfo.metaData.getString(META_DATA_STAT_KEY);
            }
        } catch (Exception e) {
            LOG.e("getStatKey.error " + e.toString());
        }
        return STAT_KEY_UNKNOWN;
    }

    /**
     * Android 系统是否在4.1及以上(Api Code >=16)
     * 
     * @return
     */
    public static boolean isJellyBeanUpperVersion() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    /**
     * Android 系统是否在4.2及以上(Api Code >=17)
     * 
     * @return
     */
    public static boolean isJellyBeanMR1UpperVersion() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    /**
     * 该应用是否作为系统应用形式运行在Android系统上
     * 
     * @param pContext
     * @return
     */
    public static boolean isAsSystemApp(Context pContext) {
        return (pContext.getApplicationInfo().flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    /**
     * 判断当前进程是否为主进程。 例如 和包名相同的 进程 com.xxx 是主进程 com.xxx:psp 是子进程
     * 
     * @param pContext
     * @return
     */
    public static boolean isInMainProcess(Context pContext) {
        return pContext.getPackageName().equalsIgnoreCase(getCurrentProcessName(pContext));
    }

    public static String getCurrentProcessName(Context pContext) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) pContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return "";
    }

    /**
     * 根据包名判断该应用是否处在当前界面
     * 
     * @param packageName
     * @return
     */
    @SuppressWarnings("deprecation")
    public static boolean isAppOnForeground(Context pContext, String packageName) {
        ActivityManager activityManager = (ActivityManager) pContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
        if (tasksInfo.size() > 0) {
            // 应用程序位于堆栈的顶层
            if (packageName.equals(tasksInfo.get(0).topActivity.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据包名判断该应用是否已经启动
     * 
     * @param packageName
     * @return
     */
    @SuppressWarnings("deprecation")
    public static boolean isAppStart(Context pContext, String packageName) {
        ActivityManager activityManager = (ActivityManager) pContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(100);
        for (RunningTaskInfo runningTaskInfo : tasksInfo) {
            if (packageName.equals(runningTaskInfo.topActivity.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 用来判断服务是否运行.
     * 
     * @param context
     * @param className
     *            判断的服务名字
     * @return true 在运行 false 不在运行
     */
    public static boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(30);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    /**
     * 是否是系统App
     * 
     * @param pInfo
     * @return
     */
    public static boolean isSystemApp(ApplicationInfo pInfo) {
        return ((pInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
