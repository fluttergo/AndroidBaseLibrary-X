package org.xbase.android.utils;

import org.xbase.android.log.Logger;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 各种格式化字符串方法
 */
public class FormatUtils {
    // ===========================================================
    // Constants
    // ===========================================================

    @SuppressWarnings("unused")
    private static final Logger           LOG                  = Logger.getLogger(FormatUtils.class);

    // ===========================================================
    // Fields
    // ===========================================================
    //
    private static final SimpleDateFormat MILLION_SECONDS_SDF1 = new SimpleDateFormat("mm:ss", Locale.getDefault());
    @SuppressWarnings("unused")
    private static final SimpleDateFormat MILLION_SECONDS_SDF2 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private static final DecimalFormat    PERCENTAGE_FORMAT    = new DecimalFormat("##.##%");

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
     * 通过时间戳获取日期字符串
     * 
     * @param pTimestamp
     * @return
     */
    public static final String parseDate(long pTimestamp) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(pTimestamp);
    }

    /**
     * 通过时间戳获取日期时间字符串
     * 
     * @param pTimestamp
     * @return
     */
    public static final String parseDateTime(long pTimestamp) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(pTimestamp);
    }

    /**
     * 将毫秒转换为指定格式的时间字符串 如：01:12 , 01:12:32 不足1s,按1秒处理
     * 
     * @param pMillisecond
     * @return
     */
    public static final String parseMillisecond(int pMillisecond) {
        if (pMillisecond < 0)
            throw new IllegalArgumentException();
        int t0 = pMillisecond % 1000;
        int t1 = pMillisecond / 1000;
        if (t0 > 0) {
            pMillisecond = (t1 + 1) * 1000;
        }
        if (pMillisecond < 3600000) {
            return MILLION_SECONDS_SDF1.format(pMillisecond);
        }
        return parseToHMS(pMillisecond);
    }

    private static final String parseToHMS(int pMillisecond) {
        if (pMillisecond < 0) {
            throw new IllegalArgumentException("Params can not lower than zero!");
        }
        StringBuilder sb = new StringBuilder();
        int seconds = pMillisecond / 1000;
        int hour = seconds / 3600;
        int minute = (seconds - hour * 3600) / 60;
        int second = seconds - hour * 3600 - minute * 60;
        sb.append(hour).append(":");
        if (minute < 10) {
            sb.append('0');
        }
        sb.append(minute).append(':');
        if (second < 10) {
            sb.append('0');
        }
        sb.append(second);
        return sb.toString();
    }

    /**
     * 
     * @param pDate
     * @param pFromFormat
     * @param pToFormat
     * @return
     */
    public static String changeDataFormat(String pDate, String pFromFormat, String pToFormat) {
        SimpleDateFormat fromFormat = new SimpleDateFormat(pFromFormat, Locale.getDefault());
        SimpleDateFormat toFormat = new SimpleDateFormat(pToFormat, Locale.getDefault());
        Date time = null;
        try {
            time = fromFormat.parse(pDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return toFormat.format(time);
    }

    /**
     * 格式化浮点数
     * 
     * @param pNumber
     *            需要格式化的浮点数
     * @param pMaxFraction
     *            精确到小数点后几位
     * @return
     */
    public static final String parseFloat(float pNumber, int pMaxFraction) {
        return String.format(Locale.getDefault(), "%." + pMaxFraction + "f", pNumber);
    }

    public static final String toPercentage(float pValue) {
        return PERCENTAGE_FORMAT.format(pValue);
    }

    /**
     * 根据所给日期格式，得出日期字符串的时间戳
     * 
     * @param pDate
     *            日期
     * @param pFormat
     *            日期格式
     * @return
     */
    public static final long getTimestamp(String pDate, String pFormat) {
        SimpleDateFormat fromFormat = new SimpleDateFormat(pFormat, Locale.getDefault());
        Date time = null;
        try {
            time = fromFormat.parse(pDate);
            return time.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static final boolean isHttpURL(String input) {
        if (input != null) {
            Pattern pattern =
                Pattern.compile("\\b((h|H)(t|T)(t|T)(p|P)s?)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]*[-A-Za-z0-9+&@#/%=~_|]");
            Matcher matcher = pattern.matcher(input);
            return matcher.matches();
        }
        return false;
    }

    public static final boolean isIPAddress(String input) {
        if (input != null) {
            Pattern pattern =
                Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9‌​]{2}|2[0-4][0-9]|25[0-5])$");
            Matcher matcher = pattern.matcher(input);
            return matcher.matches();
        }
        return false;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
