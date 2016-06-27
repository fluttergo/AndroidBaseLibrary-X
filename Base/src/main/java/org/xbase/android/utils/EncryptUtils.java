package org.xbase.android.utils;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

/**
 * 加解密工具类
 */
public class EncryptUtils {

    /**
     * 
     */
    private static final String DES_CBC_PKCS5_PADDING = "DES/CBC/PKCS5Padding";
    private final static String Algorithm_MD5         = "MD5";
    private static String       mDesKey               = "XXXXXXX";

    public static void setDesKey(String desKey) {
        EncryptUtils.mDesKey = desKey;
    }

    private static final byte[] IV        = { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00 };

    private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f'                     };

    public static void configDESKey(String pKey) {
        mDesKey = pKey;
    }

    // ===========================================================
    // Methods
    // ===========================================================

    /**
     * 计算字符长度，（小于128算一个字符，大于128算两个字符）
     * 
     * @param pInput
     * @return
     */
    public static int calStringLength(String pInput, int pMaxLength) {
        int index = 0;
        int iInputLength = 0;
        if (null != pInput && (iInputLength = pInput.length()) > 0) {
            int length = 0;
            while (length < pMaxLength && index < iInputLength) {
                int c = pInput.charAt(index);
                if (c >= 128) {
                    length += 2;
                } else {
                    length += 1;
                }
                index++;
            }
            if (length > pMaxLength) {
                index--;
            }
        }
        return index;
    }

    public static String md5(String pInput) {
        try {

            MessageDigest md;
            md = MessageDigest.getInstance("MD5");
            byte[] buf = pInput.getBytes();
            md.update(buf, 0, buf.length);
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder(32);
            for (byte b : bytes) {
                sb.append(HEX_CHARS[((b >> 4) & 0xF)]).append(HEX_CHARS[((b >> 0) & 0xF)]);
            }
            pInput = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return pInput;
    }

    public static String sha1(String pInput) {
        String result = null;
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
            byte[] buf = pInput.getBytes();
            byte[] digestBytes = md.digest(buf);
            StringBuilder sb = new StringBuilder();
            if (digestBytes != null) {
                for (byte b : digestBytes) {
                    sb.append(HEX_CHARS[((b >> 4) & 0xF)]).append(HEX_CHARS[((b >> 0) & 0xF)]);
                }
            }
            result = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * BASE64 加密
     * 
     * @param pStr
     * @return
     */
    public static String encryptBASE64(String pStr) {
        if (pStr == null || pStr.length() == 0) {
            return null;
        }
        try {
            byte[] encode = pStr.getBytes("UTF-8");
            // base64 加密
            return new String(Base64.encode(encode, 0, encode.length, Base64.DEFAULT), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * DES 加密
     * 
     * @param srcData
     * @return
     * @throws Exception
     */
    @SuppressLint("TrulyRandom")
    public static byte[] encryptDES(byte[] srcData) throws Exception {
        byte encryptedData[] = null;
        if (srcData != null) {
            byte rawKeyData[] = mDesKey.getBytes();
            DESKeySpec dks = new DESKeySpec(rawKeyData);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(dks);
            Cipher cipher = Cipher.getInstance(DES_CBC_PKCS5_PADDING);

            IvParameterSpec iv = new IvParameterSpec(IV);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            encryptedData = cipher.doFinal(srcData);

        }
        return encryptedData;
    }

    public static byte[] encryptDES(byte[] srcData, String privateKey) throws Exception {
        byte encryptedData[] = null;
        if (srcData != null) {
            byte rawKeyData[] = privateKey.getBytes();
            DESKeySpec dks = new DESKeySpec(rawKeyData);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(dks);
            Cipher cipher = Cipher.getInstance(DES_CBC_PKCS5_PADDING);

            IvParameterSpec iv = new IvParameterSpec(IV);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            encryptedData = cipher.doFinal(srcData);

        }
        return encryptedData;
    }

    // des 解密
    public static byte[] desDecrypt(byte[] encryptText) throws Exception {
        byte rawKeyData[] = mDesKey.getBytes();
        DESKeySpec dks = new DESKeySpec(rawKeyData);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey key = keyFactory.generateSecret(dks);
        Cipher cipher = Cipher.getInstance(DES_CBC_PKCS5_PADDING);
        IvParameterSpec iv = new IvParameterSpec(IV);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte encryptedData[] = encryptText;
        byte decryptedData[] = cipher.doFinal(encryptedData);
        return decryptedData;
    }

    // des 解密
    public static byte[] desDecrypt(byte[] encryptText, String privateKey) throws Exception {
        byte rawKeyData[] = privateKey.getBytes();
        DESKeySpec dks = new DESKeySpec(rawKeyData);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey key = keyFactory.generateSecret(dks);
        Cipher cipher = Cipher.getInstance(DES_CBC_PKCS5_PADDING);
        IvParameterSpec iv = new IvParameterSpec(IV);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte encryptedData[] = encryptText;
        byte decryptedData[] = cipher.doFinal(encryptedData);
        return decryptedData;
    }

    public static String encrypt(String input) throws Exception {
        String output = null;
        if (!TextUtils.isEmpty(input)) {
            output = encodeBase64(encryptDES(input.getBytes("UTF-8")));
        }
        return output;
    }

    public static String encrypt(String input, String pkey) throws Exception {
        String output = null;
        if (!TextUtils.isEmpty(input)) {
            output = encodeBase64(encryptDES(input.getBytes("UTF-8"), pkey));
        }
        return output;
    }

    public static String decrypt(String pInput, String pkey) throws Exception {
        String output = null;
        if (!TextUtils.isEmpty(pInput)) {
            byte[] result = base64Decode(pInput);
            output = new String(desDecrypt(result, pkey));
        }
        return output;
    }

    public static String decrypt(String pInput) throws Exception {
        String output = null;
        if (!TextUtils.isEmpty(pInput)) {
            byte[] result = base64Decode(pInput);
            output = new String(desDecrypt(result));
        }
        return output;
    }

    public static String encodeBase64(byte[] pInputBytes) {
        if (pInputBytes == null)
            return null;
        String destStr = new String(Base64.encode(pInputBytes, Base64.DEFAULT));
        return destStr;
    }

    public static byte[] base64Decode(String pInput) throws IOException {
        if (pInput == null)
            return null;
        return Base64.decode(pInput, Base64.DEFAULT);
    }

    public static String md5UpperCase(byte[] hexBytes) {

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(Algorithm_MD5);
            byte[] md5digest = new byte[0];
            if (digest != null) {
                md5digest = digest.digest(hexBytes);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < md5digest.length; ++i) {
                    sb.append((Integer.toHexString((md5digest[i] & 0xFF) | 0x100)).substring(1, 3));
                }
                return sb.toString().toUpperCase(Locale.getDefault());
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
