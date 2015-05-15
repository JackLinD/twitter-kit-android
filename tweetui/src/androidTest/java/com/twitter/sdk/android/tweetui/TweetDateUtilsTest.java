package com.twitter.sdk.android.tweetui;

import android.content.res.Resources;
import android.text.format.DateUtils;

import java.util.TimeZone;

public class TweetDateUtilsTest extends EnglishLocaleTestCase {
    // this is an arbitrary date, but the relative date assertions are all based off of it
    private static final long NOW_IN_MILLIS = 1395345704198L;
    private static final long JACKS_FIRST_TWEET_IN_MILLIS = 1142974214000L;

    private Resources res;
    private TimeZone realDefaultTimeZone;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        res = getContext().getResources();

        // force timezone in utc so we get consistent values out of the formatter classes that rely
        // on using the default timezone. We restore in tearDown whatever the real default timezone
        // was in order to not interfere with other tests
        realDefaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        TweetDateUtils.RELATIVE_DATE_FORMAT.setTimeZone(TimeZone.getDefault());
        TweetDateUtils.DATE_TIME_RFC822.setTimeZone(TimeZone.getDefault());
    }

    @Override
    public void tearDown() throws Exception {
        TimeZone.setDefault(realDefaultTimeZone);
        scrubClass(TweetDateUtilsTest.class);
        super.tearDown();
    }

    public void testApiTimeToLong_jacksFirstTweet() {
        assertEquals(JACKS_FIRST_TWEET_IN_MILLIS,
                TweetDateUtils.apiTimeToLong("Tue Mar 21 20:50:14 +0000 2006"));
    }

    public void testApiTimeToLong_emptyString() {
        assertEquals(TweetDateUtils.INVALID_DATE,
                TweetDateUtils.apiTimeToLong(""));
    }

    public void testApiTimeToLong_nullString() {
        assertEquals(TweetDateUtils.INVALID_DATE,
                TweetDateUtils.apiTimeToLong(null));
    }

    public void testApiTimeToLong_invalidString() {
        assertEquals(TweetDateUtils.INVALID_DATE,
                TweetDateUtils.apiTimeToLong("11111"));
    }

    public void testGetRelativeTimeString_now() {
        assertEquals("0s",
                TweetDateUtils.getRelativeTimeString(res, NOW_IN_MILLIS, NOW_IN_MILLIS));
    }

    public void testGetRelativeTimeString_secondsAgo() {
        final long tenSecondsAgo = NOW_IN_MILLIS - DateUtils.SECOND_IN_MILLIS * 10;
        assertEquals("10s",
                TweetDateUtils.getRelativeTimeString(res, NOW_IN_MILLIS, tenSecondsAgo));
    }

    public void testGetRelativeTimeString_minutesAgo() {
        final long twoMinutesAgo = NOW_IN_MILLIS - DateUtils.MINUTE_IN_MILLIS * 2;
        assertEquals("2m",
                TweetDateUtils.getRelativeTimeString(res, NOW_IN_MILLIS, twoMinutesAgo));
    }

    public void testGetRelativeTimeString_hoursAgo() {
        final long twoHoursAgo = NOW_IN_MILLIS - DateUtils.HOUR_IN_MILLIS * 2;
        assertEquals("2h",
                TweetDateUtils.getRelativeTimeString(res, NOW_IN_MILLIS, twoHoursAgo));
    }

    public void testGetRelativeTimeString_daysAgo() {
        final long twoDaysAgo = NOW_IN_MILLIS - DateUtils.DAY_IN_MILLIS * 2;
        assertEquals("Mar 18",
                TweetDateUtils.getRelativeTimeString(res, NOW_IN_MILLIS, twoDaysAgo));
    }

    public void testGetRelativeTimeString_lessThanAYearAgoWithinSameYear() {
        final long sixtyDaysAgo = NOW_IN_MILLIS - DateUtils.DAY_IN_MILLIS * 60;
        assertEquals("Jan 19",
                TweetDateUtils.getRelativeTimeString(res, NOW_IN_MILLIS, sixtyDaysAgo));
    }

    public void testGetRelativeTimeString_moreThanAYearAgo() {
        final long twoYearsAgo = NOW_IN_MILLIS - DateUtils.DAY_IN_MILLIS * 730;
        assertEquals("03/20/12",
                TweetDateUtils.getRelativeTimeString(res, NOW_IN_MILLIS, twoYearsAgo));
    }

    public void testGetRelativeTimeString_inTheFuture() {
        final long twoYearsIntoTheFuture = NOW_IN_MILLIS + DateUtils.DAY_IN_MILLIS * 730;
        assertEquals("03/19/16",
                TweetDateUtils.getRelativeTimeString(res, NOW_IN_MILLIS,
                        twoYearsIntoTheFuture));
    }

    public void testGetRelativeTimeString_negativeTime() {
        final long wayInthePast = -DateUtils.DAY_IN_MILLIS;
        assertEquals("12/31/69",
                TweetDateUtils.getRelativeTimeString(res, NOW_IN_MILLIS, wayInthePast));
    }

    public void testGetRelativeTimeString_zeroTime() {
        assertEquals("01/01/70",
                TweetDateUtils.getRelativeTimeString(res, NOW_IN_MILLIS, 0));
    }
}
