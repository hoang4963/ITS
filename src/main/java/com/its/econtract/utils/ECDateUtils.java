package com.its.econtract.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Calendar.MONDAY;
import static java.util.Calendar.SUNDAY;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.joda.time.DateTimeZone.UTC;

public class ECDateUtils {
    private static final String datePattern = "dd-M-yyyy hh:mm:ss";
    //
    public static final String MMDD_SLASH = "MM/dd";
    public static final String MM = "MM";
    public static final String YYYY = "YYYY";
    public static final String YY = "YY";
    public static final String YYYYMMDD = "yyyyMMdd";
    public static final String YYYYMM_SLASH = "yyyy/MM";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYYMMDD_SLASH = "yyyy/MM/dd";
    public static final String TIMESTAMP = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYYMMDDHHMMSS_SEAMLESS = "yyyyMMddHHmmss";
    public static final String YYYYMMDDHHMMSS_SLASH = "yyyy/MM/dd HH:mm:ss";
    public static final String YYYYMMDDHHMMSS_SLASH_ = "dd/MM/yyyy HH:mm:ss";

    //
    private static final long DAY_IN_MILLIS = 86400000L;
    private static final ISOChronology CHRONOLOGY = ISOChronology.getInstance();
    private static final int JANUARY = Calendar.JANUARY - DateTimeConstants.JANUARY;

    public static String buildTimeStamp() {
        return buildTimeStampByPattern(datePattern);
    }

    public static String buildTimeStampByPattern(String datePattern) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
        return sdf.format(timestamp);
    }

    public static String getCurrentMonth() {
        final long now = System.currentTimeMillis();
        return ECDateUtils.format(MM, now);
    }

    public static String getCurrentYear() {
        final long now = System.currentTimeMillis();
        return ECDateUtils.format(YY, now);
    }

    //
    public static interface DateTimeProvider {
        long now();
    }

    public static final DateTimeProvider getProvider() {
        return PROVIDER.get();
    }

    public static final void setProvider(DateTimeProvider provider) {
        PROVIDER.set(provider);
    }

    private static final AtomicReference<DateTimeProvider> PROVIDER = new AtomicReference<DateTimeProvider>();

    static {
        PROVIDER.set(new DateTimeProvider() {
            @Override
            public long now() {
                return System.currentTimeMillis();
            }
        });
    }

    /**
     *
     */
    public static long now() {
        return getProvider().now();
    }

    public static Date currentDate() {
        return date(now());
    }

    public static Date currentTime() {
        return new Date(now());
    }

    public static long currentTimeMillis() {
        return getProvider().now();
    }

    public static int getTimeZoneOffset(long millis) {
        return TimeZone.getDefault().getOffset(millis);
    }

    public static long toMillis(long nanos) {
        return MILLISECONDS.convert(nanos, NANOSECONDS);
    }

    public static long toNanos(long millis) {
        return NANOSECONDS.convert(millis, MILLISECONDS);
    }

    /**
     * Round
     */
    public static long roundHours(long millis, int hours) {
        final DateTime dt = new DateTime(millis);
        final int h = (dt.getHourOfDay() / hours) * hours;
        return dt.withHourOfDay(h).hourOfDay().roundFloorCopy().getMillis();
    }

    public static long roundMinutes(long millis, int minutes) {
        final DateTime d = new DateTime(millis);
        final int m = (d.getMinuteOfHour() / minutes) * minutes;
        return d.withMinuteOfHour(m).minuteOfHour().roundFloorCopy().getMillis();
    }

    /**
     * UTC & Local
     */

    public static final Date toLocalTime(final long millis) {
        return new Date(millis);
    }

    public static final Date toLocalTimeUTC0(final long millis) {
        return new Date(toLocalTimeMillis(millis));
    }

    public static final long toLocalTimeMillis(final Date date) {
        return toLocalTimeMillis(date.getTime());
    }

    public static final long toUtcTimeMillis(final long millis) {
        return new LocalDateTime(millis).toDateTime(UTC).getMillis();
    }

    public static final long toLocalTimeMillis(final long millis) {
        return new DateTime(millis, UTC).toLocalDateTime().toDateTime().getMillis();
    }

    /**
     * Merge & Truncate
     */
    public static Date date(Date date) {
        return date(date.getTime()); // Truncate time
    }

    public static Time time(Date date) {
        return time(date.getTime()); // Truncate date
    }

    public static Date merge(java.sql.Date date, Time time) {
        return merge(date.getTime(), time);
    }

    public static final Date merge(final long date, final Time time) {
        final DateTime d = new DateTime(date), t = new DateTime(time.getTime());
        return d.withHourOfDay(t.getHourOfDay()).withMinuteOfHour(t.getMinuteOfHour()).
                withSecondOfMinute(t.getSecondOfMinute()).withMillisOfSecond(0).toDate(); // Merge
    }

    public static final Date date(final long millis) {
        final int offset = getTimeZoneOffset(millis);
        long t = millis - ((t = (millis + offset) % DAY_IN_MILLIS) < 0 ? DAY_IN_MILLIS + t : t);
        t = t + (offset - getTimeZoneOffset(t));
        return new Date(t);
    }

    public static final Time time(final long millis) {
        final int offset1 = getTimeZoneOffset(0L), offset2 = getTimeZoneOffset(-offset1);
        long t = (t = (millis + getTimeZoneOffset(millis)) % DAY_IN_MILLIS) < 0 ? DAY_IN_MILLIS + t : t;
        t = t - (t < (offset2 - offset1) ? offset1 : offset2);
        return new Time(t);
    }

    /**
     * Format
     */
    public static String format(String pattern, long millis) {
        return DateTimeFormat.forPattern(pattern).print(millis);
    }

    public static String formatSFDateTime(Date date) {
        if (date == null) return null;
        return ISO_LOCAL_DATE_TIME.format(Instant.ofEpochMilli(date.getTime()).atZone(ZoneOffset.UTC));
    }

    public static String formatSFDate(Date date) {
        return date == null ? null : format("yyyy-MM-dd", date.getTime());
    }


    public static String format(final String pattern, Date date) {
        return date == null ? "" : format(pattern, date.getTime());
    }

    public static String format(String pattern, Date date, Locale locale) {
        return date == null ? "" : format(pattern, date.getTime(), locale);
    }

    public static String format(String pattern, long millis, Locale locale) {
        return DateTimeFormat.forPattern(pattern).withLocale(locale).print(millis); // Locale
    }

    public static String format(String pattern, long millis, DateTimeZone timezone) {
        return DateTimeFormat.forPattern(pattern).withZone(timezone).print(millis); // TimeZone
    }

    /**
     * Parse
     */
    public static long parse(final String pattern, String date) {
        return DateTimeFormat.forPattern(pattern).parseMillis(date);
    }

    public static long parse(final String pattern, String date, Locale locale) {
        return DateTimeFormat.forPattern(pattern).withLocale(locale).parseMillis(date); // Locale
    }

    public static long parse(final String pattern, String date, DateTimeZone timezone) {
        return DateTimeFormat.forPattern(pattern).withZone(timezone).parseMillis(date); // TimeZone
    }

    /**
     *
     */
    public static Date toDate(java.sql.Date date) {
        return date == null ? null : new Date(date.getTime());
    }

    public static java.sql.Date toSqlDate(long millis) {
        return millis == 0 ? null : toSqlDate(new Date(millis));
    }

    public static Date toDate(Timestamp millis) {
        return millis == null ? null : new Date(millis.getTime());
    }

    public static final Timestamp toTimestamp(Date date) {
        return date == null ? null : new Timestamp(date.getTime());
    }

    public static final java.sql.Date toSqlDate(final Date date) {
        return date == null ? null : new java.sql.Date(date(date).getTime());
    }

    public static final Date toDate(String pattern, String date) {
        try {
            return new Date(parse(pattern, date));
        } catch (Throwable tx) {
            return null;
        }
    }


    public static final String timestamp() {
        final long now = System.currentTimeMillis();
        return ECDateUtils.format(YYYYMMDDHHMMSS_SEAMLESS, now);
    }

    /**
     *
     */
    public static long addDays(long millis, int delta) {
        return CHRONOLOGY.days().add(millis, delta);
    }

    public static long addHours(long millis, int delta) {
        return CHRONOLOGY.hours().add(millis, delta);
    }

    public static long addYears(long millis, int delta) {
        return CHRONOLOGY.years().add(millis, delta);
    }

    public static long addMonths(long millis, int delta) {
        return CHRONOLOGY.months().add(millis, delta);
    }

    public static Date addMonths(Date date, int months) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        calendar.add(Calendar.MONTH, months);
        return new Date(calendar.getTimeInMillis());
    }

    public static long addMinutes(long millis, int delta) {
        return CHRONOLOGY.minutes().add(millis, delta);
    }

    public static long addSeconds(long millis, int delta) {
        return CHRONOLOGY.seconds().add(millis, delta);
    }

    public static long addDays(long millis, int delta, DateTimeZone timezone) {
        return ISOChronology.getInstance(timezone).days().add(millis, delta);
    }

    public static Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        calendar.add(Calendar.DATE, days);
        return new Date(calendar.getTimeInMillis());
    }

    public static long addHours(long millis, int delta, DateTimeZone timezone) {
        return ISOChronology.getInstance(timezone).hours().add(millis, delta);
    }

    public static long addYears(long millis, int delta, DateTimeZone timezone) {
        return ISOChronology.getInstance(timezone).years().add(millis, delta);
    }

    public static long addMonths(long millis, int delta, DateTimeZone timezone) {
        return ISOChronology.getInstance(timezone).months().add(millis, delta);
    }

    public static long addMinutes(long millis, int delta, DateTimeZone timezone) {
        return ISOChronology.getInstance(timezone).minutes().add(millis, delta);
    }

    public static long addSeconds(long millis, int delta, DateTimeZone timezone) {
        return ISOChronology.getInstance(timezone).seconds().add(millis, delta);
    }

    public static Date addSeconds(Date date, int seconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        calendar.add(Calendar.SECOND, seconds);
        return new Date(calendar.getTimeInMillis());
    }

    /**
     *
     */
    public static int getYear(long millis) {
        return CHRONOLOGY.year().get(millis);
    }

    public static int getDayOfMonth(long millis) {
        return CHRONOLOGY.dayOfMonth().get(millis);
    }

    public static int getHourOfDay(long millis) {
        return CHRONOLOGY.hourOfDay().get(millis);
    }

    public static int getMinute(final long millis) {
        return CHRONOLOGY.minuteOfHour().get(millis);
    }

    public static int getSecond(final long millis) {
        return CHRONOLOGY.secondOfMinute().get(millis);
    }

    public static int getMilliSecond(final long millis) {
        return CHRONOLOGY.millisOfSecond().get(millis);
    }

    public static final int getDayOfWeek(final long millis) {
        return CHRONOLOGY.dayOfWeek().get(millis) % 7 + 1;
    }

    public static final int getMonth(final long millis) {
        return CHRONOLOGY.monthOfYear().get(millis) + JANUARY;
    }

    public static int getYear(long millis, DateTimeZone timezone) {
        return ISOChronology.getInstance(timezone).year().get(millis);
    }

    public static int getDayOfMonth(long millis, DateTimeZone timezone) {
        return ISOChronology.getInstance(timezone).dayOfMonth().get(millis);
    }

    public static int getHourOfDay(long millis, DateTimeZone timezone) {
        return ISOChronology.getInstance(timezone).hourOfDay().get(millis);
    }

    public static int getMinute(final long millis, DateTimeZone timezone) {
        return ISOChronology.getInstance(timezone).minuteOfHour().get(millis);
    }

    public static int getSecond(final long millis, DateTimeZone timezone) {
        return ISOChronology.getInstance(timezone).secondOfMinute().get(millis);
    }

    public static int getMilliSecond(final long millis, DateTimeZone timezone) {
        return ISOChronology.getInstance(timezone).millisOfSecond().get(millis);
    }

    public static int getDayOfWeek(final long millis, DateTimeZone timezone) {
        return ISOChronology.getInstance(timezone).dayOfWeek().get(millis) % 7 + 1;
    }

    public static int getDayOfWeek() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.DAY_OF_WEEK);
    }

    public static final int getMonth(final long millis, DateTimeZone timezone) {
        return ISOChronology.getInstance(timezone).monthOfYear().get(millis) + JANUARY;
    }

    /**
     *
     */
    public static long getFirstDateOfWeek(long millis) {
        return CHRONOLOGY.dayOfWeek().set(millis, DateTimeConstants.MONDAY);
    }

    public static long getLastDateOfWeek(long millis) {
        return CHRONOLOGY.dayOfWeek().set(millis, DateTimeConstants.SUNDAY);
    }

    public static long getFirstDateOfYear(long millis) {
        return new DateTime(millis).dayOfYear().withMinimumValue().getMillis();
    }

    public static long getLastDateOfYear(long millis) {
        return new DateTime(millis).dayOfYear().withMaximumValue().getMillis();
    }

    public static long getFirstDateOfMonth(long millis) {
        return new DateTime(millis).dayOfMonth().withMinimumValue().getMillis();
    }

    public static long getLastDateOfMonth(long millis) {
        return new DateTime(millis).dayOfMonth().withMaximumValue().getMillis();
    }

    public static long getFirstDateOfWeek(long millis, DateTimeZone timezone) {
        return ISOChronology.getInstance(timezone).dayOfWeek().set(millis, MONDAY);
    }

    public static long getLastDateOfWeek(long millis, DateTimeZone timezone) {
        return ISOChronology.getInstance(timezone).dayOfWeek().set(millis, SUNDAY);
    }

    public static long getFirstDateOfYear(long millis, DateTimeZone timezone) {
        return new DateTime(millis, timezone).dayOfYear().withMinimumValue().getMillis();
    }

    public static long getLastDateOfYear(long millis, DateTimeZone timezone) {
        return new DateTime(millis, timezone).dayOfYear().withMaximumValue().getMillis();
    }

    public static long getFirstDateOfMonth(long millis, DateTimeZone timezone) {
        return new DateTime(millis, timezone).dayOfMonth().withMinimumValue().getMillis();
    }

    public static long getLastDateOfMonth(long millis, DateTimeZone timezone) {
        return new DateTime(millis, timezone).dayOfMonth().withMaximumValue().getMillis();
    }

    public static long convertUtc0AndLocal(long time, boolean toLocal) {
        // convert UTC 0 and time zone of server
        int offset = TimeZone.getDefault().getRawOffset();
        int h = offset / 3600000;
        int m = (offset / 60000) % 60;

        if (toLocal == false) { // local server time to UTC 0
            h = h * (-1);
            m = m * (-1);
        }
        time = ECDateUtils.addHours(time, h);
        time = ECDateUtils.addMinutes(time, m);
        return time;
    }

    public static Date setTime(final Date date, final int hourOfDay, final int minute, final int second, final int ms) {
        final GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        gc.set(Calendar.HOUR_OF_DAY, hourOfDay);
        gc.set(Calendar.MINUTE, minute);
        gc.set(Calendar.SECOND, second);
        gc.set(Calendar.MILLISECOND, ms);
        return gc.getTime();
    }

    public static Date setStartTimeOfDay(final Date date) {
        final GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        gc.set(Calendar.HOUR_OF_DAY, 0);
        gc.set(Calendar.MINUTE, 0);
        gc.set(Calendar.SECOND, 0);
        gc.set(Calendar.MILLISECOND, 0);
        return gc.getTime();
    }

    public static Date setEndTimeOfDay(final Date date) {
        final GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        gc.set(Calendar.HOUR_OF_DAY, 23);
        gc.set(Calendar.MINUTE, 59);
        gc.set(Calendar.SECOND, 59);
        gc.set(Calendar.MILLISECOND, 999);
        return gc.getTime();
    }

    /**
     * Used by mail scheduler
     */
    public static Date setTime(final int hourOfDay) {
        final GregorianCalendar gc = new GregorianCalendar();
        gc.set(Calendar.HOUR_OF_DAY, hourOfDay);
        gc.set(Calendar.MINUTE, 0);
        gc.set(Calendar.SECOND, 0);
        gc.set(Calendar.MILLISECOND, 0);
        return gc.getTime();
    }

    public static Date toStartOfDate(Long timestamp) {
        if (timestamp == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    public static Date toStartOfDate(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    public static Date toEndOfDate(Long timestamp) {
        if (timestamp == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }

    public static Date toEndOfDate(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }

    public static Date toCorpEstablishDate(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    public static Date toCorpSettleDate(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.YEAR, 2018);
        return cal.getTime();
    }

    /**
     * For scheduler
     */
    public static String getHourMinuteSecondString(Timestamp timestamp) {
        // Timestamp format 2016-12-29 11:36:14.318
        String strTimestamp = timestamp.toString();
        String year = strTimestamp.substring(0, 4);
        String month = strTimestamp.substring(5, 7);
        String day = strTimestamp.substring(8, 10);
        String hour = strTimestamp.substring(11, 13);
        String minute = strTimestamp.substring(14, 16);
        String second = strTimestamp.substring(17, 19);

        // CronExpression format "0 05 10 11 09 ? 2016" (2016-09-11 10:05:00)
        StringBuilder cronExpression = new StringBuilder();
        cronExpression.append(second).append(" ").append(minute).append(" ").append(hour)
                .append(" ").append(day).append(" ").append(month).append(" ? ").append(year);

        return cronExpression.toString();
    }
}
