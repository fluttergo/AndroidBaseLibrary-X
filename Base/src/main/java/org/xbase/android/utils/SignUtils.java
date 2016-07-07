package org.xbase.android.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;

public class SignUtils {
    public static final String                   OPENID_KEY               = "KO_APP_KEY";
    public static final String                   KEY_INVALID              = "UNVAILEKEY";

    
    /**
     * 验证{@link#OPENID_KEY },输出警告日志 ,并为密钥赋值{@link EncryptUtils#setDesKey(String)}
     * @param context
     * @throws IllegalAccessException
     */
    public static void verifySign(Context context) throws IllegalAccessException {
        verifySign(context,"");
    }
    /**
     * 验证{@link#OPENID_KEY },输出警告日志 ,并为密钥赋值{@link EncryptUtils#setDesKey(String)}
     * @param context
     * @param appKey APP的KO_APP_KEY
     * @throws IllegalAccessException
     */
    public static void verifySign(Context context,String appKey) throws IllegalAccessException {
        ApplicationInfo appInfo;
        try {
            String key;
            if (!TextUtils.isEmpty(appKey)) {
                key = appKey;
                String isOK = JNIGameHelper.key(context, key);
                if (KEY_INVALID.equals(isOK)||TextUtils.isEmpty(isOK)) {
                }else{
                    EncryptUtils.setDesKey(isOK);
                    return;
                }
            }
            appInfo =
                context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo == null) {
                throw new NullPointerException(context.getPackageName() + "is not exist");
            }
            if (appInfo.metaData == null) {
                throw new IllegalAccessException(
                    " <meta-data> not exist in 'AndroidManifest.xml'. <meta-data android:name=\"" + OPENID_KEY
                        + "\" android:value=\"KO_APP_KEY\"/>");
            }
            key = appInfo.metaData.getString(OPENID_KEY);
            if (TextUtils.isEmpty(key)) {
                throw new IllegalAccessException(OPENID_KEY
                    + "is not exist in <meta-data> not exist in 'AndroidManifest.xml'. <meta-data android:name=\""
                    + OPENID_KEY + "\" android:value=\"KO_APP_KEY\"/>");
            }

            String isOK = JNIGameHelper.key(context, key);
            if (KEY_INVALID.equals(isOK) || TextUtils.isEmpty(isOK)) {
                final String msg_key_invaild = "the " + OPENID_KEY + " is invalid";
                throw new IllegalAccessException(msg_key_invaild);
            } else {
                EncryptUtils.setDesKey(isOK);
            }
            
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static String decode(String pInput) throws Exception{
        return EncryptUtils.decrypt(pInput);
    }
    public static String encode(String pInput) throws Exception{
        return EncryptUtils.encrypt(pInput);
    }

}
