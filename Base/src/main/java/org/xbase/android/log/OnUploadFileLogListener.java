package org.xbase.android.log;

/**
 * <p><strong>日志上传结果监听</strong></p>
 */
public interface OnUploadFileLogListener {

    // ===========================================================
    // Constants
    // ===========================================================

    // 上传日志类型
    public static final int ERROR_LOG                                      = 0;
    public static final int COMMON_LOG                                     = 1;
    public static final int REPORT_LOG                                     = 2;

    // 异常code
    // 客户端
    public static final int ERROR_CODE_CLIENT_FILE_NOT_FOUND               = 0;
    public static final int ERROR_CODE_CLIENT_REQUEST_CANCEL               = 1;
    public static final int ERROR_CODE_CLIENT_NO_RESULT_RETURN             = 2;
    public static final int ERROR_CODE_CLIENT_SDCARD_NOT_ACCESSABLE        = 3;
    // 服务器端
    public static final int ERROR_CODE_SERVER_CREATE_FILE_FAILURE          = 300; // 创建目录失败
    public static final int ERROR_CODE_SERVER_FILE_SIZE_EXCEED_RESTRICTION = 301; // 文件大小超过服务器限制
    public static final int ERROR_CODE_SERVER_FILE_SIZE_TO_BIG             = 302; // 文件太大
    public static final int ERROR_CODE_SERVER_FILE_IN_COMPLETE             = 303; // 文件不完整
    public static final int ERROR_CODE_SERVER_FILE_LOAD_FAILURE            = 304; // 文件加载失败
    public static final int ERROR_CODE_SERVER_FILE_TYPE_INVALID            = 305; // 文件类型不合法
    public static final int ERROR_CODE_SERVER_FILE_MOVE_FAILURE            = 306; // 文件移動失敗
    public static final int ERROR_CODE_SERVER_UPLOAD_WAY_INVALID           = 307; // 上传方式不合法
    public static final int ERROR_CODE_SERVER_NO_TMP_FILE                  = 308; // 找不到临时文件
    public static final int ERROR_CODE_SERVER_FILE_WRITE_FAILURE           = 309; // 文件写入失败
    public static final int ERROR_CODE_SERVER_UPLAOD_NO_FILE               = 310; // 找不到临时文件
    
    // ===========================================================
    // Methods
    // ===========================================================
    
    public void onSuccess(int logType);

    public void onFailure(int logType, int code, String msg);
    
}
