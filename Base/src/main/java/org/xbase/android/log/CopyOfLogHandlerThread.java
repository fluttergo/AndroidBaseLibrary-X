package org.xbase.android.log;
//package com.zz.lib.log;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.net.URLEncoder;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;
//
//import android.content.Context;
//import android.os.Environment;
//import android.os.Handler.Callback;
//import android.os.HandlerThread;
//import android.os.Looper;
//import android.os.Message;
//import android.text.TextUtils;
//import android.util.Log;
//import cn.vszone.ko.log.Logger.OnReadFileLogListener;
//import cn.vszone.ko.net.KORequest;
//import cn.vszone.ko.net.KORequestWorker;
//import cn.vszone.ko.net.KOResponseCallback;
//import cn.vszone.ko.net.NetRequestCacheManager;
//import cn.vszone.ko.net.Response;
//import cn.vszone.ko.util.EncryptUtils;
//import cn.vszone.ko.util.FileSystemBasicUtils;
//import cn.vszone.ko.vo.UploadLogResult;
//
////@formatter:off
///**
// * <p><strong>日志写文件读文件的线程</strong></p>
// */
////@formatter:on
//class CopyOfLogHandlerThread extends HandlerThread implements Callback {
//
//    // ===========================================================
//    // Constants
//    // +==========================================================
//    private static final String     TAG                           = CopyOfLogHandlerThread.class.getSimpleName();
//    public static final String      KEY_UPDATELOAD_LISTENER       = "uploadListener";
//
//    public static final int         MSG_WRITE_LOG_FILE            = 0x1001;
//    public static final int         MSG_READ_LOG_FILE             = 0x1002;
//    public static final int         MSG_UPLOAD_LOG_FILE           = 0x1003;
//    public static final int         MSG_CHECK_CLEAR_LOG_FILE      = 0x1004;
//    public static final int         MSG_FLUSH_LOG_FILE            = 0x1005;
//    public static final int         MSG_CLOSE_LOG_STREAM          = 0x1006;
//
//    public static final int         ERROR_LOG_TYPE                = 0;
//    public static final int         REPORT_LOG_TYPE               = 1;
//    public static final int         COMMON_LOG_TYPE               = 2;
//
//    public static final String      LOG_FILE_PATH_DEFAULT         = "%1$s/%2$s/.log/%3$s.log";
//    public static final String      LOG_DIRECTORY_PATH            = "%1$s/%2$s/.log";
//    
//    public static final String      LOG_OLD_DIRECTORY_PARENT_PATH = "%1$s/" + "xxx.com";
//    private static final String     LOG_LINE_FORMAT               = "%1$s:%2$s\t%3$s\t%4$s\n";
//    private static final String     ENCRYPT_BLOCK_DELIMITER       = "|~|";
//    private static final int        WRITE_CHAR_BUFFER_SIZE        = 2 * 1024;
//    private static final String     UPLOAD_LOG_FILE_URI           =
//                                                                      "";
//    private boolean                 mIsPrepared                   = true;
//
//    // -==========================================================
//
//    // ===========================================================
//    // Fields
//    // +==========================================================
//
//    private LogBufferedWriter       mCommonWriter;
//    private LogBufferedWriter       mErrorWriter;
//    private LogBufferedWriter       mReportWriter;
//
//    private static SimpleDateFormat mDateFormatter                = new SimpleDateFormat("yyyy-MM-dd",
//                                                                      Locale.getDefault());
//
//    private static SimpleDateFormat mTimeFormatter                = new SimpleDateFormat("MM-dd HH:mm:ss",
//                                                                      Locale.getDefault());
//
//    private boolean                 mIsSdcardAccessable           = false;
//
//    private String                  mCommonLogFilePath;
//    private String                  mReportLogFilePath;
//    private String                  mErrorLogFilePath;
//    private String                  mLogDirectoryPath;
//
//    private OnUploadFileLogListener mUploadLisenter;
//
//    // -==========================================================
//
//    // ===========================================================
//    // Constructors
//    // +==========================================================
//
//    public CopyOfLogHandlerThread() {
//        super("LogHandler");
//        if (FileSystemBasicUtils.checkSDCard()) {
//            this.mIsSdcardAccessable = true;
//            String sdcardPath = Environment.getExternalStorageDirectory().getPath();
//            Date date = new Date();
//            String commonLogFilePath =
//                String.format(LOG_FILE_PATH_DEFAULT, sdcardPath, FileSystemBasicUtils.getKORootDirNameWithPidChannel(), "common_" + mDateFormatter.format(date));
//            String errorLogFilePath =
//                String.format(LOG_FILE_PATH_DEFAULT, sdcardPath, FileSystemBasicUtils.getKORootDirNameWithPidChannel(), "error_" + mDateFormatter.format(date));
//            String reportLogFilePath =
//                String.format(LOG_FILE_PATH_DEFAULT, sdcardPath, FileSystemBasicUtils.getKORootDirNameWithPidChannel(), "report_" + mDateFormatter.format(date));
//            this.mCommonLogFilePath = commonLogFilePath;
//            this.mReportLogFilePath = reportLogFilePath;
//            this.mErrorLogFilePath = errorLogFilePath;
//            this.mLogDirectoryPath = String.format(LOG_DIRECTORY_PATH, FileSystemBasicUtils.getKORootDirNameWithPidChannel(), sdcardPath);
//            try {
//                File logFile = new File(commonLogFilePath);
//                if (!logFile.getParentFile().exists()) {
//                    logFile.getParentFile().mkdirs();
//                }
//                // 删除旧的目录
//                logFile = new File(String.format(LOG_OLD_DIRECTORY_PARENT_PATH, sdcardPath));
//                if (logFile.exists()) {
//                    logFile.delete();
//                }
//                if (mCommonWriter == null) {
//                    mCommonWriter =
//                        new LogBufferedWriter(new FileWriter(commonLogFilePath, true), WRITE_CHAR_BUFFER_SIZE);
//                }
//                if (mErrorWriter == null) {
//                    mErrorWriter =
//                        new LogBufferedWriter(new FileWriter(errorLogFilePath, true), WRITE_CHAR_BUFFER_SIZE);
//                }
//                if (mReportWriter == null) {
//                    mReportWriter =
//                        new LogBufferedWriter(new FileWriter(reportLogFilePath, true), WRITE_CHAR_BUFFER_SIZE);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                Log.e(Logger.class.getSimpleName(), e.getMessage());
//            }
//        } else {
//            this.mIsSdcardAccessable = false;
//        }
//    }
//
//    // -==========================================================
//
//    // ===========================================================
//    // Getter & Setter
//    // +==========================================================
//
//    // TODO 代码在+-号之间编写
//
//    // -==========================================================
//
//    // ===========================================================
//    // Methods for/from SuperClass/Interfaces
//    // +==========================================================
//
//    @Override
//    public boolean handleMessage(Message msg) {
//        final int what = msg.what;
//        if (what == MSG_WRITE_LOG_FILE) {
//            if (msg.obj != null && msg.obj instanceof LogMsg) {
//                if (this.mIsSdcardAccessable) {
//                    LogMsg logMsg = (LogMsg) msg.obj;
//                    writeFile(logMsg.level, logMsg.tag, logMsg.content);
//                }
//            }
//        } else if (what == MSG_READ_LOG_FILE) {
//            if (this.mIsSdcardAccessable) {
//                Object o = msg.obj;
//                int type = msg.arg1;
//                int feedID = msg.arg2;
//                try {
//                    flushBufferLog();
//                } catch (IOException e) {
//                }
//                String out = readLogFile(type);
//                if (o != null && o instanceof OnReadFileLogListener) {
//                    OnReadFileLogListener l = (OnReadFileLogListener) o;
//                    l.onFinished(feedID, type, out);
//                }
//                Log.d("LogHandlerThread", "handleMesssage MSG_READ_FILE_LOG");
//            }
//        } else if (what == MSG_UPLOAD_LOG_FILE) {
//            Object obj = msg.obj;
//            Context ctx;
//            if (obj != null && obj instanceof Context) {
//                ctx = (Context) obj;
//            } else {
//                // 直接返回
//                return true;
//            }
//            String path;
//            final int resultType;
//            switch (msg.arg1) {
//                case COMMON_LOG_TYPE:
//                    path = this.mCommonLogFilePath;
//                    resultType = OnUploadFileLogListener.COMMON_LOG;
//                break;
//                case ERROR_LOG_TYPE:
//                    path = this.mErrorLogFilePath;
//                    resultType = OnUploadFileLogListener.ERROR_LOG;
//                break;
//                case REPORT_LOG_TYPE:
//                    path = this.mReportLogFilePath;
//                    resultType = OnUploadFileLogListener.REPORT_LOG;
//                break;
//                default:
//                    path = this.mReportLogFilePath;
//                    resultType = OnUploadFileLogListener.REPORT_LOG;
//            }
//            if (this.mIsSdcardAccessable) {
//                handleUploadLogFile(ctx, path, resultType);
//            } else {
//                if (mUploadLisenter != null) {
//                    mUploadLisenter.onFailure(resultType,
//                        OnUploadFileLogListener.ERROR_CODE_CLIENT_SDCARD_NOT_ACCESSABLE, "Sdcard can not accessable!");
//                }
//            }
//        } else if (what == MSG_CHECK_CLEAR_LOG_FILE) {
//            if (this.mIsSdcardAccessable) {
//                handleCheckCleanLogFile();
//            }
//        } else if (what == MSG_FLUSH_LOG_FILE) {
//            if (this.mIsSdcardAccessable) {
//                try {
//                    flushBufferLog();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        } else if (what == MSG_CLOSE_LOG_STREAM) {
//            try {
//                closeLogStream();
//            } catch (IOException e) {
//            }
//        }
//        return true;
//    }
//
//    @Override
//    protected void finalize() throws Throwable {
//        super.finalize();
//        if (mCommonWriter != null) {
//            try {
//                mCommonWriter.flush();
//                mCommonWriter.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            mCommonWriter = null;
//        }
//        if (mErrorWriter != null) {
//            try {
//                mErrorWriter.flush();
//                mErrorWriter.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            mErrorWriter = null;
//        }
//        if (mReportWriter != null) {
//            try {
//                mReportWriter.flush();
//                mReportWriter.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            mReportWriter = null;
//        }
//    }
//
//    // -==========================================================
//
//    // ===========================================================
//    // Methods
//    // +==========================================================
//
//    public void setLogWriter(LogBufferedWriter writer) {
//        mCommonWriter = writer;
//    }
//
//    private void writeFile(LogLevel level, String tag, String msg) {
//        switch (level) {
//            case LOG_LEVEL_ERROR:
//            case LOG_LEVEL_WARN:
//                if (mErrorWriter != null) {
//                    try {
//                        writeFile(mErrorWriter, level, tag, msg);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            break;
//            case LOG_LEVEL_REPORT:
//                if (mReportWriter != null) {
//                    try {
//                        writeFile(mReportWriter, level, tag, msg);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            break;
//            default:
//                if (mCommonWriter != null) {
//                    try {
//                        writeFile(mCommonWriter, level, tag, msg);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                ;
//        }
//    }
//
//    private void writeFile(LogBufferedWriter pWriter, LogLevel level, String tag, String msg) throws IOException {
//        String line = String.format(LOG_LINE_FORMAT, level, tag, mTimeFormatter.format(new Date()), msg);
//        if (Logger.needEncryptFile) {
//            String encrypted = "";
//            try {
//                encrypted = URLEncoder.encode(EncryptUtils.encrypt(line), "UTF-8") + ENCRYPT_BLOCK_DELIMITER;
//            } catch (Exception e1) {
//                e1.printStackTrace();
//            }
//            pWriter.write(encrypted);
//        } else {
//            pWriter.write(line);
//        }
//        //pWriter.flush();
//    }
//
//    private String readLogFile(final int logFileType) {
//        Log.d("LogHandlerThread", "start readLogFile()");
//        String path;
//        switch (logFileType) {
//            case OnReadFileLogListener.LOG_FILE_TYPE_COMMON:
//                path = this.mCommonLogFilePath;
//            break;
//            case OnReadFileLogListener.LOG_FILE_TYPE_ERROR:
//                path = this.mErrorLogFilePath;
//            break;
//            case OnReadFileLogListener.LOG_FILE_TYPE_REPORT:
//                path = this.mReportLogFilePath;
//            break;
//            default:
//                return null;
//        }
//        StringBuilder sb = new StringBuilder();
//        File file = null;
//        BufferedReader br = null;
//        // 先读取report
//        if (!TextUtils.isEmpty(path)) {
//            file = new File(path);
//            if (file != null && file.exists() && file.canRead()) {
//                try {
//                    br = new BufferedReader(new FileReader(file));
//                    String tmp;
//                    while ((tmp = br.readLine()) != null) {
//                        sb.append(tmp);
//                    }
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } finally {
//                    if (br != null) {
//                        try {
//                            br.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        }
//        return sb.toString();
//    }
//
//    void setUploadLisenter(OnUploadFileLogListener lisenter) {
//        this.mUploadLisenter = lisenter;
//    }
//
//    private void handleUploadLogFile(Context pCtx, final String pPath, final int pResultType) {
//        final OnUploadFileLogListener listener = this.mUploadLisenter;
//        File file = new File(pPath);
//        KORequestWorker<UploadLogResult> worker =
//            new KORequestWorker<UploadLogResult>(NetRequestCacheManager.CACHE_POLICY_NONE);
//        KORequest req = new KORequest(UPLOAD_LOG_FILE_URI, false);
//        try {
//            req.put("file_data", file);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            if (listener != null) {
//                listener.onFailure(pResultType, -1, e.getMessage());
//            }
//        }
//        worker.doPostRequest(pCtx, req, UploadLogResult.class, new KOResponseCallback<Response<UploadLogResult>>() {
//
//            public void onResponseSucceed(Response<UploadLogResult> result) {
//                Log.d(TAG, "onResponseSucceed result = " + result.dataJson);
//                if (listener != null) {
//                    listener.onSuccess(pResultType);
//                }
//            }
//
//            public void onRequestError(int pErrorCode, String pErrMsg) {
//                Log.d(TAG, "onResponseError result = " + pErrMsg);
//                if (listener != null) {
//                    listener.onFailure(pResultType, OnUploadFileLogListener.ERROR_CODE_CLIENT_NO_RESULT_RETURN, pErrMsg);
//                }
//            }
//
//            public void onRequestCancelled() {
//                Log.d(TAG, "onResponseCancelled");
//                if (listener != null) {
//                    listener.onFailure(pResultType, OnUploadFileLogListener.ERROR_CODE_CLIENT_REQUEST_CANCEL,
//                        "Http request cancelled!");
//                }
//            }
//
//            public void onResponseFailure(Response<UploadLogResult> pResult) {
//                Log.d(TAG, "onResponseFailure()");
//                if (listener != null) {
//                    listener.onFailure(pResultType, pResult.code, pResult.message);
//                }
//            }
//
//            public void beforeRequestStart() {
//            }
//
//            public void onLoading(long count, long current) {
//            }
//
//            public void afterResponseEnd() {
//            }
//        });
//    }
//
//    private void handleCheckCleanLogFile() {
//        String directoryPath = this.mLogDirectoryPath;
//        File d = new File(directoryPath);
//        if (d != null && d.isDirectory()) {
//            File[] files = d.listFiles();
//            Date date = new Date();
//            String todayDateFormat = mDateFormatter.format(date);
//            String yesterdayDateFormat = mDateFormatter.format(new Date(date.getTime() - 1000 * 60 * 60 * 24));
//            if (files != null && files.length >= 4) {
//                String tmp;
//                for (File f : files) {
//                    // 根据文件名做筛选
//                    tmp = f.getName();
//                    if (tmp.contains(todayDateFormat) || tmp.contains(yesterdayDateFormat)) {
//                        continue;
//                    } else {
//                        f.delete();
//                    }
//                }
//            }
//        }
//    }
//
//    private void flushBufferLog() throws IOException {
//        if (this.mCommonWriter != null) {
//            this.mCommonWriter.flush();
//        }
//        if (this.mErrorWriter != null) {
//            this.mErrorWriter.flush();
//        }
//        if (this.mReportWriter != null) {
//            this.mReportWriter.flush();
//        }
//    }
//
//    private void closeLogStream() throws IOException {
//        if (!mIsSdcardAccessable) {
//            return;
//        }
//        if (this.mCommonWriter != null) {
//            this.mCommonWriter.close();
//        }
//        if (this.mErrorWriter != null) {
//            this.mErrorWriter.close();
//        }
//        if (this.mReportWriter != null) {
//            this.mReportWriter.close();
//        }
//    }
//
//    @Override
//    public boolean quit() {
//        Looper looper = getLooper();
//        this.mIsPrepared = false;
//        if (looper != null) {
//            looper.quit();
//            return true;
//        }
//        return false;
//    }
//
//    public boolean isPrepared() {
//        return this.mIsPrepared && isAlive();
//    }
//
//    // -==========================================================
//
//    // ===========================================================
//    // Inner and Anonymous Classes
//    // +==========================================================
//
//    // -==========================================================
//}
