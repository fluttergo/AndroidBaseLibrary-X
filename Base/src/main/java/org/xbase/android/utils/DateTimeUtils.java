package org.xbase.android.utils;

import android.content.Context;

import org.xbase.android.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


//@formatter:off

/**
 * <p><strong>Features draft description.主要功能介绍</strong></p>
 */
//@formatter:on
public class DateTimeUtils {

    // ===========================================================
    // Constants
    // ===========================================================
    public final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    public final static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    public final static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public final static int ONE_MINUTES = 1000 * 60;
    public final static int ONE_HOURS = ONE_MINUTES * 60;
    public final static int ONE_DAY = ONE_HOURS * 24;
    public final static int ONE_MONTH = ONE_DAY * 30;
    public final static int ONE_YEAR = ONE_MONTH * 12;

    // ===========================================================
    // Methods
    // ===========================================================

    /**
     * 如果是一天内的，显示为"XX小时/分钟 前"，超过一天的，直接显示日期
     *
     * @param pContext
     * @param pTime
     * @return
     */
    public static String computeHowLongAgo(Context pContext, long pTime) {
        long now = System.currentTimeMillis();
        long diff = now - pTime;
        String timeAgo = "";

        if (diff > ONE_DAY) {
            timeAgo = dateFormat.format(new Date(pTime));
        } else if (diff > ONE_HOURS) {
            int hours = (int) (diff / ONE_HOURS);
            timeAgo = pContext.getString(R.string.hours_ago, hours);

        } else if (diff > ONE_MINUTES) {
            int minutes = (int) (diff / ONE_MINUTES);
            timeAgo = pContext.getString(R.string.minutes_ago, minutes);
        } else {
            timeAgo = pContext.getString(R.string.just_now);
        }

        return timeAgo;
    }


    /**
     * @param pContext Context
     * @param pTime    Unix时间戳
     * @return 从今天开始倒数，昨天，n天前。超过30天是n月前。超过365就是n年前
     */
    public static String computeHowDayAgo(Context pContext, long pTime) {
        long now = System.currentTimeMillis();
        // String nowTimeStr = dateFormat.format(new Date(now));
        int dayNum = 0;
        try {
            dayNum = daysBetween(new Date(now), new Date(pTime * 1000));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int year=dayNum/365;

        int  month=dayNum/30;

        String timeAgo = "";
        if (year > 0) {
            timeAgo = pContext.getString(R.string.years_ago, year);
        } else if (month > 0) {
            timeAgo = pContext.getString(R.string.months_ago, month);
        } else if (dayNum == 1) {
            timeAgo = pContext.getString(R.string.yesterday);
        } else if (dayNum > 1) {
            timeAgo = pContext.getString(R.string.days_ago, dayNum);
        } else {
            timeAgo = pContext.getString(R.string.today);
        }
        return timeAgo;
    }

    /**
     * 计算两个日期之间相差的天数
     *
     * @param smdate 较小的时间
     * @param bdate  较大的时间
     * @return 相差天数
     * @throws ParseException
     */
    /**
     * 计算两个日期之间相差的天数
     *
     * @param smdate 较小的时间
     * @param bdate  较大的时间
     * @return 相差天数
     * @throws ParseException
     */
    public static int daysBetween(Date smdate, Date bdate) throws ParseException {

        smdate = dateFormat.parse(dateFormat.format(smdate));
        bdate = dateFormat.parse(dateFormat.format(bdate));
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long between_days = (time1 - time2) / (1000 * 3600 * 24);

        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     * @param pContext
     * @param pTime    UNIX时间， 从1970年到现在的毫秒数
     * @return
     */
    public static String getTimeTitle(Context pContext, long pTime) {
        long now = System.currentTimeMillis();

        if (dateFormat.format(new Date(pTime)).equals(dateFormat.format(new Date(now)))) {
            return pContext.getString(R.string.today);
        } else {
            return dateFormat.format(new Date(pTime));
        }
    }

    public static boolean isToday(long pTime) {
        String pDay = dateFormat.format(new Date(pTime));
        String today = dateFormat.format(new Date());
        return pDay.equals(today);
    }

    /**
     * 主要用于每日奖励，破产奖励
     * 1、确认是当天
     * 2、防止用户手动修改盒子时间
     *
     * @param pTime
     * @return
     */
    public static boolean isTodayAndBeforeNow(long pTime) {
        return isToday(pTime) && pTime < System.currentTimeMillis();
    }
}
