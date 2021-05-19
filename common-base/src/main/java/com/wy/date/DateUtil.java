package com.wy.date;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @Classname DateUtil
 * @Description 日期简单处理工具类
 * @Date 2020/10/13 10:10
 * @Created wangyong
 */
@Slf4j
public class DateUtil {


    /**
     * excel时间分隔符
     */
    private static final String DATE_SLASH_SPLIT = "/";

    /**
     * 时间分隔符
     */
    private static final String DATE_HORIZONTAL_SPLIT = "-";

    /**
     * 存放不同的日期模板格式的sdf的Map
     */
    private static Map<String, ThreadLocal<SimpleDateFormat>> sdfMap = new ConcurrentHashMap<>();

    /**
     * 获取日期格式化对象
     *
     * @param pattern
     * @return
     */
    private static SimpleDateFormat getSdf(final String pattern) {
        ThreadLocal<SimpleDateFormat> tl = sdfMap.get(pattern);
        if (tl == null) {
            tl = new ThreadLocal<SimpleDateFormat>() {
                @Override
                protected SimpleDateFormat initialValue() {
                    return new SimpleDateFormat(pattern);
                }
            };
            sdfMap.put(pattern, tl);
        }
        return tl.get();
    }

    /**
     * 格式化时间
     *
     * @param date
     * @param pattern
     * @return
     */
    public static String format(Date date, PatternTime pattern) {
        return getSdf(pattern.getPattern()).format(date);
    }

    /**
     * 解析时间
     *
     * @param dateStr
     * @param pattern
     * @return
     * @throws ParseException
     */
    public static Date parse(String dateStr, PatternTime pattern) {
        try {
            return getSdf(pattern.getPattern()).parse(dateStr);
        } catch (ParseException ex) {
            return null;
        }
    }

    /**
     * 时间格式化数组
     */
    public static final String[] PATTERN_TIME = {"yyyy-MM-dd", "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss:SSS"};


    /**
     * 获取当前时间 精确到秒
     *
     * @return 秒级时间戳
     */
    public static Long secondNow() {
        return Instant.now().getEpochSecond();
    }

    /**
     * 获取当前时间 精确到豪秒
     *
     * @return 豪秒级时间戳
     */
    public static Long miNow() {
        return Instant.now().toEpochMilli();
    }

    /**
     * 将 PATTERN_TIME 中的格式相符的时间字符串转化为时间戳
     *
     * @param time 时间格式的单位
     * @return 秒级时间戳
     */
    public static Long stringToSecondTime(String time, PatternTime patternTime) {
        ZonedDateTime localTime = ZonedDateTime.parse(time, DateTimeFormatter.ofPattern(patternTime.getPattern()));
        return localTime.toEpochSecond();
    }

    /**
     * 自定义的格式相符的时间字符串转化为时间戳
     *
     * @param time 时间格式的单位
     * @return 秒级时间戳
     */
    public static Long stringToSecondTime(String time, String patternTime) {
        ZonedDateTime localTime = ZonedDateTime.parse(time, DateTimeFormatter.ofPattern(patternTime));
        return localTime.toEpochSecond();
    }

    /**
     * 获取某一日期之后的n秒之后时间戳(S)
     *
     * @param date    日期
     * @param seconds 秒
     * @return 时间戳
     */
    public static Long getDatePlusSeconds(Date date, Long seconds) {
        ZonedDateTime zonedDateTime = date.toInstant().atZone(ZoneId.systemDefault()).plusSeconds(seconds);
        return zonedDateTime.toEpochSecond();
    }

    /**
     * 获取某一日期之后的n分钟之后时间戳(S)
     *
     * @param date    日期
     * @param minutes 分钟数
     * @return 时间戳(S)
     */
    public static Long getDatePlusMinutes(Date date, Long minutes) {
        ZonedDateTime zonedDateTime = date.toInstant().atZone(ZoneId.systemDefault()).plusMinutes(minutes);
        return zonedDateTime.toEpochSecond();
    }

    /**
     * 获取plus hours之后的秒级别的时间戳
     *
     * @param date  日期
     * @param hours 获取plus hours之后的秒级别的时间戳
     * @return Long
     */
    public static Long getDatePlusHours(Date date, Integer hours) {
        ZonedDateTime zonedDateTime = date.toInstant().atZone(ZoneId.systemDefault()).plusHours(hours);
        return zonedDateTime.toEpochSecond();
    }

    /**
     * 根据某一天的日期获取days的天数之后的秒级别的时间戳
     *
     * @param date 日期
     * @param days 根据某一天的日期获取days的天数之后的日期
     * @return 秒级别的时间戳
     */
    public static Long getDatePlusDays(Date date, Integer days) {
        ZonedDateTime zonedDateTime = date.toInstant().atZone(ZoneId.systemDefault()).plusDays(days);
        return zonedDateTime.toEpochSecond();
    }

    /**
     * 根据某一天的日期获取months之后的日期
     *
     * @param date   日期
     * @param months 根据某一天的日期获取months之后的日期
     * @return 秒级别的时间戳
     */
    public static Long getDatePlusMonth(Date date, Integer months) {
        ZonedDateTime zonedDateTime = date.toInstant().atZone(ZoneId.systemDefault()).plusMonths(months);
        return zonedDateTime.toEpochSecond();
    }


    /**
     * 时间转化为秒级别的时间戳
     *
     * @param date 日期
     * @return 时间戳(S)
     */
    public static Long dateToShortLong(Date date) {
        return date.toInstant().getEpochSecond();
    }

    /**
     * 时间转化为豪秒级别的时间戳
     *
     * @param date 日期
     * @return 时间戳(ms)
     */
    public static Long dateToMiLong(Date date) {
        return date.toInstant().toEpochMilli();
    }

    /**
     * 将秒级别的时间戳转化我date
     *
     * @param timeStamp 秒级别的时间戳
     * @return 时间date
     */
    public static Date shortTimeToDate(Long timeStamp) {
        Instant instant = Instant.ofEpochSecond(timeStamp);
        Date date = Date.from(instant);
        return date;
    }

    /**
     * 将秒级别的时间戳转化localDate
     *
     * @param timeStamp 秒级别的时间戳
     * @return 时间LocalDate
     */
    public static LocalDate shortTimeToLocalDate(Long timeStamp) {
        Instant instant = Instant.ofEpochSecond(timeStamp);
        LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate;
    }

    /**
     * 返回日期
     *
     * @param timeStamp 毫秒级别的时间戳
     * @return 日期
     */
    public static Date miTimeToDate(Long timeStamp) {
        Instant instant = Instant.ofEpochMilli(timeStamp);
        Date date = Date.from(instant);
        return date;
    }

    /**
     * 返回日期
     *
     * @param timeStamp 毫秒级别的时间戳
     * @return 日期
     */
    public static LocalDate miTimeToLocalDate(Long timeStamp) {
        Instant instant = Instant.ofEpochMilli(timeStamp);
        LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate;
    }

    /**
     * 获取某一时间当天的开始时间
     *
     * @param date
     * @return
     */
    public static Long getDateOfStart(Date date) {
        LocalDate localDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalDate();
        return localDate.atStartOfDay().toEpochSecond(ZoneOffset.of("+8"));
    }

    /**
     * 获取某一天时间的某小时某分钟某秒
     *
     * @param date   日期
     * @param hours  时
     * @param minute 分
     * @param second 秒
     * @return 时间戳
     */
    public static Long getDateOfParamTime(Date date, Integer hours, Integer minute, Integer second) {
        LocalDate localDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalDate();
        LocalDateTime dateTime = localDate.atTime(hours, minute, second);
        return dateTime.toEpochSecond(ZoneOffset.of("+8"));
    }

    /**
     * 获取某一时间戳的格式化后的字符串
     *
     * @param secondTime
     * @param time
     * @return
     */
    public static String formatDate(Long secondTime, PatternTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(time.getPattern());
        String format = formatter.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(secondTime), ZoneId.systemDefault()));
        return format;
    }

    /**
     * 获取某一时间戳的格式化后的字符串
     *
     * @param secondTime
     * @param pattern    格式化样例
     * @return
     */
    public static String formatDate(Long secondTime, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        String format = formatter.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(secondTime), ZoneId.systemDefault()));
        return format;
    }

    /**
     * 日期型字符串转化为日期 格式
     *
     * @param str the str
     * @return the date
     */
    public static Date parseDate(String str, PatternTime time) {
        if (str == null) {
            return null;
        }
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(time.getPattern());
            return simpleDateFormat.parse(str);
        } catch (ParseException e) {
            return null;
        }
    }


    /**
     * 字符串转成指定格式的date
     *
     * @param str
     * @param patternTime
     * @return
     */
    public static Date stringToDate(String str, PatternTime patternTime) {
        if (StringUtils.isEmpty(str) || patternTime == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(patternTime.pattern);
        try {
            return sdf.parse(str);
        } catch (ParseException e) {
            log.error("DateUtil转换日期失败：", e);
        }
        return null;
    }

    /**
     * 获取一天的开始时间
     *
     * @param date
     * @return 2020-11-05 00:00:00
     */
    public static Date getDateBegin(Date date) {
        if (date == null) {
            return date;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        setTimeToWeeHours(calendar);
        return calendar.getTime();
    }

    /**
     * 获取一天结束时间
     *
     * @param date
     * @return 2020-11-05 23:59:59
     */
    public static Date getDateEnd(Date date) {
        if (date == null) {
            return date;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        setTimeToMidnight(calendar);
        return calendar.getTime();
    }

    /**
     * 设置为 23:59:59
     *
     * @param toCalendar
     */
    private static void setTimeToMidnight(Calendar toCalendar) {
        toCalendar.set(Calendar.HOUR_OF_DAY, 23);
        toCalendar.set(Calendar.MINUTE, 59);
        toCalendar.set(Calendar.SECOND, 59);
    }

    /**
     * 设置为 00：00：00
     *
     * @param toCalendar
     */
    private static void setTimeToWeeHours(Calendar toCalendar) {
        toCalendar.set(Calendar.HOUR_OF_DAY, 0);
        toCalendar.set(Calendar.MINUTE, 0);
        toCalendar.set(Calendar.SECOND, 0);
    }


    /**
     * 判断当前时间是否在[startTime, endTime]区间，注意时间格式要一致
     *
     * @param nowTime   当前时间
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    public static boolean isEffectiveDate(Date nowTime, Date startTime, Date endTime) {
        if (nowTime == null) {
            throw new IllegalArgumentException("当前时间不能为空");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("开始时间不能为空");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("结束时间不能为空");
        }
        if (nowTime.getTime() == startTime.getTime()
                || nowTime.getTime() == endTime.getTime()) {
            return true;
        }

        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);

        Calendar begin = Calendar.getInstance();
        begin.setTime(startTime);

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);

        if (date.after(begin) && date.before(end)) {
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println(getDateOfParamTime(new Date(), 11, 59, 59));
    }

    public enum PatternTime {

        PATTERN_OF_Y_INTO_DD("yyyy-MM-dd"),
        PATTERN_OF_Y_INTO_MM("yyyy-MM-dd HH:mm"),
        PATTERN_OF_Y_INTO_SS("yyyy-MM-dd HH:mm:ss"),
        PATTERN_OF_Y_INTO_SSS("yyyy-MM-dd HH:mm:ss:SSS"),
        PATTERN_OF_Y_INTO_HOUR("HH:mm"),
        PATTERN_OF_Y_INTO_ALL_SSS("yyyyMMddHHmmssSSS"),

        ;

        private String pattern;

        PatternTime(String pattern) {
            this.pattern = pattern;
        }

        public String getPattern() {
            return pattern;
        }
    }



    /**
     * 针对excel时间字符串（2020/12/23）获取当前的开始时间
     * 如果包含/，则替换成-
     * @param str
     * @param patternTime
     * @return
     */
    public static Date excelStringToDateBegin(String str, PatternTime patternTime) {
        if (StringUtils.isEmpty(str) || patternTime == null) {
            return null;
        }
        if (str.contains(DATE_SLASH_SPLIT)){
            str = str.replaceAll(DATE_SLASH_SPLIT,DATE_HORIZONTAL_SPLIT);
        }

        SimpleDateFormat sdf = new SimpleDateFormat(patternTime.pattern);
        try {
            return sdf.parse(str);
        } catch (ParseException e) {
            log.error("DateUtil转换日期失败：", e);
        }
        return null;
    }



    /**
     * 针对excel时间字符串（2020/12/23）获取当前的结束时间
     * 如果包含/，则替换成-
     * @param str
     * @param patternTime
     * @return
     */
    public static Date excelStringToDateEnd(String str, PatternTime patternTime) {
        if (StringUtils.isEmpty(str) || patternTime == null) {
            return null;
        }
        if (str.contains(DATE_SLASH_SPLIT)){
            str = str.replaceAll(DATE_SLASH_SPLIT,DATE_HORIZONTAL_SPLIT);
        }

        SimpleDateFormat sdf = new SimpleDateFormat(patternTime.pattern);
        try {
            return getDateEnd(sdf.parse(str));
        } catch (ParseException e) {
            log.error("DateUtil转换日期失败：", e);
        }
        return null;
    }

    /**
     * @Description 获取指定时间间隔的时间
     * @Author zkq
     * @Date 2018/11/5 15:15
     */
    public static Date getDate(Date date, Integer interval, TimeUnit unit) {
        Calendar expireDate = Calendar.getInstance();
        if (date != null) {
            expireDate.setTime(date);
        }
        switch (unit) {
            case SECONDS:
                expireDate.set(Calendar.SECOND, expireDate.get(Calendar.SECOND) + interval);
                break;
            case MINUTES:
                expireDate.set(Calendar.MINUTE, expireDate.get(Calendar.MINUTE) + interval);
                break;
            case HOURS:
                expireDate.set(Calendar.HOUR_OF_DAY, expireDate.get(Calendar.HOUR_OF_DAY) + interval);
                break;
            case DAYS:
                expireDate.set(Calendar.DAY_OF_MONTH, expireDate.get(Calendar.DAY_OF_MONTH) + interval);
                break;
            default:
                break;
        }
        return expireDate.getTime();
    }


}
