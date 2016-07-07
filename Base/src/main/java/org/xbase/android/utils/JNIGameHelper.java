package org.xbase.android.utils;

import java.security.NoSuchAlgorithmException;

import android.content.Context;

/**
 * 
 * @author Ge Liang
 * @Create at 2014-12-17 上午10:13:53
 * @Version 1.0
 *          <p>
 *          <strong>游戏启动环境优化相关</strong>
 *          </p>
 */
public class JNIGameHelper {

    /**
     * <p>
     * <strong>memoryClean 与 stopMemoryClean 成对调用</strong>
     * </p>
     * 
     * @see #stopMemoryClean()
     * @param target
     *            程序启动的理想内存大小
     * @return
     */
    public static native int memoryClean(int target);

    /**
     * <p>
     * <strong>memoryClean 与 stopMemoryClean 成对调用</strong>
     * </p>
     * 
     * @see #memoryClean()
     * @return
     */
    public static native void stopMemoryClean();

    /**
     * 根据androidmainfest.xml中的值判断改应用是否合法,否则不返回密钥
     * 
     * @param value
     * @return 钥匙
     */
    public static native String key(Context context ,String value);
    /**
     * md5
     * @param hexBytes
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String peri(byte[] hexBytes) {
        return EncryptUtils.md5UpperCase(hexBytes);
       
    }
    /**
     * des
     * @param srcString
     * @param srcString1
     * @return
     * @throws Exception decrypt fail
     */
    public static String beast(String srcString,String srcString1)  {
        try {
            return EncryptUtils.decrypt(srcString,srcString1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    public static native void setFileDownloadPath(String value);
    public static native String init(Context context);
    
}
