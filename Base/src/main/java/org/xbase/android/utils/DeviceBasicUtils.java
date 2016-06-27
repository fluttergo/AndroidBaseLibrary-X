package org.xbase.android.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import org.xbase.android.log.Logger;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;


/**
 * 设备相关的工具类， 包括 设备名称 CPU DeviceId(网卡物理地址，WIFI物理地址) ...
 */
public class DeviceBasicUtils {

    @SuppressWarnings("unused")
    private static final Logger LOG                   = Logger.getLogger(DeviceBasicUtils.class);

    private static final String ETHERNET_ADDRESS_PATH = "/sys/class/net/eth0/address";

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
     * 获取手机的电话号码（测试证明，只有联通部分卡可以获取到电话号码，例如186开头的)
     * 
     * @param ctx
     * @return 手机的电话号码
     */
    public static String getPhoneNumber(Context ctx) {
        String phoneNumber = "";
        if (ctx != null) {
            TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);

            phoneNumber = tm.getLine1Number();
        }

        return phoneNumber;

    }

    /**
     * 获取设备的型号名称
     * 
     * @return
     */
    public static String getModelString() {
        String s = Build.MODEL;
        s = s.replace(" ", "_");
        return s;
    }

    /**
     * 获取设备的制造商
     * 
     * @return
     */
    public static String getManufacturer() {
        return Build.MANUFACTURER;
    }

    /**
     * 获取设备名称（制造商+型号名称)
     * 
     * @return
     */
    public static String getDeviceName() {
        return String.format("%1$s:%2$s", getManufacturer(), getModelString());
    }

    /**
     * 唯一识别码(IMEI)｜设备号｜序号｜UUID, 先取网卡物理地址， 其次Wifi模块的macAddress， 如果为空如果deviceId为空,
     * 
     * @param context
     * @return
     */
    public static String getUDID(Context context) {
        // 1. 获取有限网络地址
        // 2. 获取WIFI网络地址
        // 3. 获取设备号

        String udid = null;
        String ethernetMac = getEthernet0MacAddress();
        if (TextUtils.isEmpty(ethernetMac)) {
            // 先取mac地址 16bite
            String wifiMac = getWifiMacAddress(context);
            if (TextUtils.isEmpty(wifiMac)) {
                // 无mac地址，取imei 15bite
                String deviceId = getDeviceId(context);
                udid = deviceId + "C";
            } else {
                udid = wifiMac + "000B";
            }
        } else {
            udid = ethernetMac + "000A";
        }

        return udid;
    }

    public static String getWifiMacAddress(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        String mac = null;
        if (info != null) {
            mac = info.getMacAddress();
            if (mac != null) {
                mac = mac.replace(":", "").toUpperCase(Locale.getDefault());
            }
        }
        return mac;
    }

    @SuppressWarnings("deprecation")
    public static String getDeviceId(Context context) {
        // TelephonyManager tm = (TelephonyManager)
        // context.getSystemService(Context.TELEPHONY_SERVICE);
        // String deviceId = tm.getDeviceId();
        String deviceId = System.getString(context.getContentResolver(), System.ANDROID_ID);
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = "0000000000000000";
        }
        return deviceId;
    }

    public static String getIMEI(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = "0000000000000000";
        }
        return deviceId;
    }

    public static String getEthernet0MacAddress() {
        String result = FileBasicUtils.readFile(ETHERNET_ADDRESS_PATH);
        if (!TextUtils.isEmpty(result)) {
            result = result.replace(":", "").toUpperCase(Locale.getDefault());
        }
        return result;
    }

    public static InetAddress getLocalInetAddress() {
        InetAddress ip = null;
        try {
            Enumeration<NetworkInterface> en_netInterface = NetworkInterface.getNetworkInterfaces();
            while (en_netInterface.hasMoreElements()) {
                NetworkInterface ni = en_netInterface.nextElement();
                Enumeration<InetAddress> en_ip = ni.getInetAddresses();
                while (en_ip.hasMoreElements()) {
                    ip = en_ip.nextElement();
                    if (!ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1)
                        break;
                    else
                        ip = null;
                }

                if (ip != null) {
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ip;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
