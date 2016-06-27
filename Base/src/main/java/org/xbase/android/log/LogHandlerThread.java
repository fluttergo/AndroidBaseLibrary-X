package org.xbase.android.log;

import android.content.Context;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//@formatter:off

/**
 * <p><strong>日志写文件读文件的线程</strong></p>
 */
//@formatter:on
class LogHandlerThread extends HandlerThread implements Callback {

    // ===========================================================
    // Constants
    // +==========================================================
    private static final String     TAG                           = LogHandlerThread.class.getSimpleName();
    public static final String      KEY_UPDATELOAD_LISTENER       = "uploadListener";

    public static final int         MSG_WRITE_LOG_FILE            = 0x1001;
    public static final int         MSG_READ_LOG_FILE             = 0x1002;
    public static final int         MSG_UPLOAD_LOG_FILE           = 0x1003;
    public static final int         MSG_CHECK_CLEAR_LOG_FILE      = 0x1004;
    public static final int         MSG_FLUSH_LOG_FILE            = 0x1005;
    public static final int         MSG_CLOSE_LOG_STREAM          = 0x1006;

    public static final int         ERROR_LOG_TYPE                = 0;
    public static final int         REPORT_LOG_TYPE               = 1;
    public static final int         COMMON_LOG_TYPE               = 2;

    public static final String      LOG_FILE_PATH_DEFAULT         = "%1$s/%2$s/.log/%3$s.log";
    public static final String      LOG_DIRECTORY_PATH            = "%1$s/%2$s/.log";

    public static final String      LOG_OLD_DIRECTORY_PARENT_PATH = "%1$s/" + "xxx.com";
    private static final String     LOG_LINE_FORMAT               = "%1$s:%2$s\t%3$s\t%4$s\n";
    private static final String     ENCRYPT_BLOCK_DELIMITER       = "|~|";
    private static final int        WRITE_CHAR_BUFFER_SIZE        = 2 * 1024;
    private static final String     UPLOAD_LOG_FILE_URI           = "";
    private boolean                 mIsPrepared                   = true;

    // -==========================================================

    // ===========================================================
    // Fields
    // +==========================================================

    private LogBufferedWriter mCommonWriter;
    private LogBufferedWriter mErrorWriter;
    private LogBufferedWriter mReportWriter;

    private static SimpleDateFormat mDateFormatter                = new SimpleDateFormat("yyyy-MM-dd",
                                                                      Locale.getDefault());

    private static SimpleDateFormat mTimeFormatter                = new SimpleDateFormat("MM-dd HH:mm:ss",
                                                                      Locale.getDefault());

    private boolean                 mIsSdcardAccessable           = false;

    private String                  mCommonLogFilePath;
    private String                  mReportLogFilePath;
    private String                  mErrorLogFilePath;
    private String                  mLogDirectoryPath;

    private OnUploadFileLogListener mUploadLisenter;

    // -==========================================================

    // ===========================================================
    // Constructors
    // +==========================================================

    public LogHandlerThread() {
        super("LogHandler");
    }

    // -==========================================================

    // ===========================================================
    // Getter & Setter
    // +==========================================================

    // TODO 代码在+-号之间编写

    // -==========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // +==========================================================

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (mCommonWriter != null) {
            try {
                mCommonWriter.flush();
                mCommonWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mCommonWriter = null;
        }
        if (mErrorWriter != null) {
            try {
                mErrorWriter.flush();
                mErrorWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mErrorWriter = null;
        }
        if (mReportWriter != null) {
            try {
                mReportWriter.flush();
                mReportWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mReportWriter = null;
        }
    }

    // -==========================================================

    // ===========================================================
    // Methods
    // +==========================================================

    public void setLogWriter(LogBufferedWriter writer) {
        mCommonWriter = writer;
    }

    private void writeFile(LogLevel level, String tag, String msg) {
        switch (level) {
            case LOG_LEVEL_ERROR:
            case LOG_LEVEL_WARN:
                if (mErrorWriter != null) {
                    try {
                        writeFile(mErrorWriter, level, tag, msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            break;
            case LOG_LEVEL_REPORT:
                if (mReportWriter != null) {
                    try {
                        writeFile(mReportWriter, level, tag, msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            break;
            default:
                if (mCommonWriter != null) {
                    try {
                        writeFile(mCommonWriter, level, tag, msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                ;
        }
    }

    private void writeFile(LogBufferedWriter pWriter, LogLevel level, String tag, String msg) throws IOException {
    }

    private String readLogFile(final int logFileType) {
        return "";
    }

    void setUploadLisenter(OnUploadFileLogListener lisenter) {
        this.mUploadLisenter = lisenter;
    }

    private void handleUploadLogFile(Context pCtx, final String pPath, final int pResultType) {
    }

    private void handleCheckCleanLogFile() {
        String directoryPath = this.mLogDirectoryPath;
        File d = new File(directoryPath);
        if (d != null && d.isDirectory()) {
            File[] files = d.listFiles();
            Date date = new Date();
            String todayDateFormat = mDateFormatter.format(date);
            String yesterdayDateFormat = mDateFormatter.format(new Date(date.getTime() - 1000 * 60 * 60 * 24));
            if (files != null && files.length >= 4) {
                String tmp;
                for (File f : files) {
                    // 根据文件名做筛选
                    tmp = f.getName();
                    if (tmp.contains(todayDateFormat) || tmp.contains(yesterdayDateFormat)) {
                        continue;
                    } else {
                        f.delete();
                    }
                }
            }
        }
    }

    private void flushBufferLog() throws IOException {
        if (this.mCommonWriter != null) {
            this.mCommonWriter.flush();
        }
        if (this.mErrorWriter != null) {
            this.mErrorWriter.flush();
        }
        if (this.mReportWriter != null) {
            this.mReportWriter.flush();
        }
    }

    private void closeLogStream() throws IOException {
        if (!mIsSdcardAccessable) {
            return;
        }
        if (this.mCommonWriter != null) {
            this.mCommonWriter.close();
        }
        if (this.mErrorWriter != null) {
            this.mErrorWriter.close();
        }
        if (this.mReportWriter != null) {
            this.mReportWriter.close();
        }
    }

    @Override
    public boolean quit() {
        Looper looper = getLooper();
        this.mIsPrepared = false;
        if (looper != null) {
            looper.quit();
            return true;
        }
        return false;
    }

    public boolean isPrepared() {
        return this.mIsPrepared && isAlive();
    }

    // -==========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // +==========================================================

    // -==========================================================
}
