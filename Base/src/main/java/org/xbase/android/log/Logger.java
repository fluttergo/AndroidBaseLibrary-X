package org.xbase.android.log;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;

/**
 *          <p>
 *          通用Log工具类 可以通过setLogLevel控制log 输出级别
 *          </p>
 */
public class Logger {

    // ===========================================================
    // Constants
    // +==========================================================

    public static final String                            TAG                     = "Logger";
    public static final String                            DEFAULT_PACKAGE_NAME    = "com.zz";

    // -==========================================================

    // ===========================================================
    // Fields
    // +==========================================================
    public static boolean                                 needEncryptFile          = true;

    private String                                        mDefaultTag;

    private LogLevel mLevel;
    private boolean                                       mIsLog2File;

    // 当前日志的级别
    private static LogLevel sCurrentLogLevel        = LogLevel.LOG_LEVEL_DEBUG;

    private static HashMap<String, SoftReference<Logger>> sCacheLogger            =
                                                                                      new HashMap<String, SoftReference<Logger>>();

    private static List<LogConfig>                        sConfigs                = null;

    private static Handler                                sWriteFileHandler;
    private static LogHandlerThread sLogHandlerThread;

    public static boolean                                 isLogFileEncrypt        = true;
    // -==========================================================

    // ===========================================================
    // Constructors
    // +==========================================================

    static {
        //initConfig();
    }

    // ===========================================================
    // Methods
    // +==========================================================

    public static void startLogThread() {
        sLogHandlerThread = new LogHandlerThread();
        sLogHandlerThread.start();
        sWriteFileHandler = new Handler(sLogHandlerThread.getLooper(), sLogHandlerThread);
        checkClearLogFile();
    }

    public static void postDestoryLogThread() {
        postCloseLogStream();
        if (sLogHandlerThread != null) {
            sLogHandlerThread.quit();
        }
    }

    private Logger(String defaultTag, LogLevel level) {
        mDefaultTag = defaultTag;
        mLevel = level;
    }

    public String getDefultTag() {
        return mDefaultTag;
    }

    public void setLogWriter(OutputStreamWriter writer) {
        if (sLogHandlerThread != null) {
            sLogHandlerThread.setLogWriter(new LogBufferedWriter(writer));
        }
    }

    public static Logger getLogger(String tag) {
        return getLogger(tag, "", false);
    }

    public static Logger getLogger(String tag, boolean isLog2File) {
        return getLogger(tag, "", isLog2File);
    }

    public static Logger getLogger(Class<?> cls, boolean isLog2File) {
        String tag = cls.getSimpleName();
        Package pkg = cls.getPackage();
        String pkgName = "";
        if (pkg != null) {
            pkgName = pkg.getName();
        }
        return getLogger(tag, pkgName, isLog2File);
    }

    public static Logger getLogger(String tag, String pkgName, boolean isLog2File) {

        if (TextUtils.isEmpty(pkgName)) {
            pkgName = DEFAULT_PACKAGE_NAME;
        }

        Logger logger = null;
        if (!sCacheLogger.containsKey(tag)) {
            logger = newInstance2Cache(tag, pkgName, isLog2File);
        } else {
            SoftReference<Logger> sr = sCacheLogger.get(tag);
            logger = sr.get();
            if (logger == null) {
                logger = newInstance2Cache(tag, pkgName, isLog2File);
            }
        }
        return logger;
    }

    public static Logger getLogger(Class<?> cls) {
        return getLogger(cls, false);
    }

    private static Logger newInstance2Cache(String tag, String pkgName, boolean isLog2File) {
        Logger logger;
        LogConfig config = null;
        if (sConfigs != null) {
            for (LogConfig cfg : sConfigs) {
                if (tag.equals(cfg.filter)) {
                    config = cfg;
                    break;
                }
                // 例如 com.zz.*， 表示com.zz下面的所有子包， 都是用该配置
                if (!TextUtils.isEmpty(cfg.filter) && cfg.filter.endsWith(".*")) {
                    String cfgTagPrefix = cfg.filter.substring(0, cfg.filter.lastIndexOf('.'));
                    if (pkgName.startsWith(cfgTagPrefix)) {
                        config = cfg;
                    }
                    break;
                }
            }
        }
        if (config != null) {
            logger = new Logger(tag, config.logLevel);
            logger.mIsLog2File = isLog2File;
        } else {
            logger = new Logger(tag, sCurrentLogLevel);
            logger.mIsLog2File = isLog2File;
        }
        SoftReference<Logger> srLogger = new SoftReference<Logger>(logger);
        sCacheLogger.put(tag, srLogger);
        return logger;
    }

    public static void initConfig(String pLogConfigPath) {
        // String logConfigPath =
        // Environment.getExternalStorageDirectory().getPath() + "/" +
        // APP_ROOT_DIRECTORY_NAME + "/logger.json";
        File logConfigFlie = new File(pLogConfigPath);
        if (logConfigFlie.exists()) {
            try {
                FileReader fr = new FileReader(logConfigFlie);
                Gson gson = new Gson();
                sConfigs = gson.fromJson(fr, new TypeToken<List<LogConfig>>() {
                }.getType());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "failed to load " + pLogConfigPath + " on SDCard.", e);
            } catch (JsonParseException je) {
                Log.e(TAG, "failed to load " + pLogConfigPath + " on SDCard.", je);
            }
        }
    }

    public static void setGlobalLogLevel(LogLevel logLevel) {
        sCurrentLogLevel = logLevel;
    }

    public static LogLevel getGlobalLogLevel() {
        return sCurrentLogLevel;
    }

    public void e(String msg) {
        e(mDefaultTag, msg);
    }

    public void ee(String pFormat, Object... args) {
        e(String.format(pFormat, args));
    }
        
    public void e(Throwable throwable) {
        e(mDefaultTag, throwable);
    }

    public void e(String msg, Throwable throwable) {
        if (sCurrentLogLevel.getValue() >= LogLevel.LOG_LEVEL_ERROR.getValue()) {
            Log.e(mDefaultTag, msg, throwable);
            writeFile(LogLevel.LOG_LEVEL_ERROR, mDefaultTag, msg + "--->" + throwable.getMessage());
        }
    }

    public void w(String msg) {
        w(mDefaultTag, msg);
    }
    
    public void ww(String pFormat, Object... args) {
        w(String.format(pFormat, args));
    }

    public void d(String msg) {
        d(mDefaultTag, msg);
    }

    
    public void dd(String pFormat, Object... args) {
        d(String.format(pFormat, args));
    }
    
    public void i(String msg) {
        i(mDefaultTag, msg);
    }
    
    public void ii(String pFormat, Object... args) {
        i(String.format(pFormat, args));
    }

    public void w(String tag, String msg) {
        if (sCurrentLogLevel.getValue() >= LogLevel.LOG_LEVEL_WARN.getValue()) {
            Log.w(tag, msg);
            writeFile(LogLevel.LOG_LEVEL_WARN, tag, msg);
        }
    }

    public void d(String tag, String msg) {
        if (sCurrentLogLevel.getValue() >= LogLevel.LOG_LEVEL_DEBUG.getValue()) {
            Log.d(tag, msg);
            if (mIsLog2File) {
                writeFile(LogLevel.LOG_LEVEL_DEBUG, tag, msg);
            }
        }
    }

    public void e(String tag, String msg) {
        if (sCurrentLogLevel.getValue() >= LogLevel.LOG_LEVEL_ERROR.getValue()) {
            Log.e(tag, msg);
            writeFile(LogLevel.LOG_LEVEL_ERROR, tag, msg);
        }
    }

    public void e(String tag, String msg, Throwable throwable) {
        if (sCurrentLogLevel.getValue() >= LogLevel.LOG_LEVEL_ERROR.getValue()) {
            Log.e(tag, msg, throwable);
            writeFile(LogLevel.LOG_LEVEL_ERROR, tag, msg + "--->" + throwable.getMessage());
        }
    }

    public void i(String tag, String msg) {
        if (sCurrentLogLevel.getValue() >= LogLevel.LOG_LEVEL_INFO.getValue()) {
            Log.i(tag, msg);
            if (mIsLog2File) {
                writeFile(LogLevel.LOG_LEVEL_INFO, tag, msg);
            }
        }
    }

    /**
     * repot级别的日志,写入文件并上报
     * 
     * @param message
     */
    public void r(String tag, String msg) {
        if (sCurrentLogLevel.getValue() >= LogLevel.LOG_LEVEL_REPORT.getValue()) {
            Log.println(Log.DEBUG, tag, msg);
            writeFile(LogLevel.LOG_LEVEL_REPORT, tag, msg);
        }
    }

    /**
     * repot级别的日志,写入文件并上报
     * 
     * @param message
     */
    public void r(String msg) {
        r(mDefaultTag, msg);
    }
    
    public void rr(String pFormat, Object... args) {
        r(String.format(pFormat, args));
    }

    private boolean writeFile(LogLevel level, String tag, String content) {
        if (sWriteFileHandler != null && sLogHandlerThread.isPrepared()) {
            Message message = Message.obtain();
            LogMsg logMsg = new LogMsg();
            logMsg.level = level;
            logMsg.tag = tag;
            logMsg.content = content;
            message.what = LogHandlerThread.MSG_WRITE_LOG_FILE;
            message.obj = logMsg;
            sWriteFileHandler.sendMessage(message);
            return true;
        } else {
            return false;
        }
    }

    public static boolean readLogFile(OnReadFileLogListener l, int logFileType, final int pFeedId) {
        if (sLogHandlerThread.isPrepared()) {
            Message message = Message.obtain();
            message.obj = l;
            message.what = LogHandlerThread.MSG_READ_LOG_FILE;
            message.arg1 = logFileType;
            message.arg2 = pFeedId;
            sWriteFileHandler.sendMessage(message);
            return true;
        } else {
            return false;
        }
    }

    public static boolean uploadErrorLogFile(Context ctx) {
        if (sLogHandlerThread.isPrepared()) {
            Message message = Message.obtain();
            message.arg1 = LogHandlerThread.ERROR_LOG_TYPE;
            message.what = LogHandlerThread.MSG_UPLOAD_LOG_FILE;
            message.obj = ctx;
            sWriteFileHandler.sendMessage(message);
            return true;
        } else {
            return false;
        }
    }

    public static boolean uploadReportLogFile(Context ctx) {
        if (sLogHandlerThread.isPrepared()) {
            Message message = Message.obtain();
            message.arg1 = LogHandlerThread.ERROR_LOG_TYPE;
            message.what = LogHandlerThread.MSG_UPLOAD_LOG_FILE;
            message.obj = ctx;
            sWriteFileHandler.sendMessage(message);
            return true;
        } else {
            return false;
        }
    }

    public static boolean uploadCommonLogFile(Context ctx) {
        if (sLogHandlerThread.isPrepared()) {
            Message message = Message.obtain();
            message.what = LogHandlerThread.MSG_UPLOAD_LOG_FILE;
            message.arg1 = LogHandlerThread.COMMON_LOG_TYPE;
            message.obj = ctx;
            sWriteFileHandler.sendMessage(message);
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkClearLogFile() {
        if (sLogHandlerThread.isPrepared()) {
            Message message = Message.obtain();
            message.what = LogHandlerThread.MSG_CHECK_CLEAR_LOG_FILE;
            sWriteFileHandler.sendMessage(message);
            return true;
        } else {
            return false;
        }
    }

    public static boolean postCloseLogStream() {
        if (sLogHandlerThread.isPrepared()) {
            Message message = Message.obtain();
            message.what = LogHandlerThread.MSG_CLOSE_LOG_STREAM;
            sWriteFileHandler.sendMessage(message);
            return true;
        } else {
            return false;
        }
    }

    public static void setUploadLogFileListener(OnUploadFileLogListener l) {
        sLogHandlerThread.setUploadLisenter(l);
    }

    public LogLevel getLogLevel() {
        return mLevel;
    }

    public void setLogLevel(LogLevel level) {
        mLevel = level;
    }

    public interface OnReadFileLogListener {

        public static final int LOG_FILE_TYPE_COMMON = 1;
        public static final int LOG_FILE_TYPE_ERROR  = 2;
        public static final int LOG_FILE_TYPE_REPORT = 3;

        public void onFinished(int feedID, int type, String result);

        public void onError(int feedID, int type, int code, String msg);
    }
}
