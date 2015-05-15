package com.twitter.sdk.android.tweetui;

import com.twitter.sdk.android.core.TwitterAndroidTestCase;
import com.twitter.sdk.android.core.models.MediaEntity;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.TweetBuilder;
import com.twitter.sdk.android.core.models.TweetEntities;
import com.twitter.sdk.android.core.models.UrlEntity;

import java.util.ArrayList;

public class TweetTextUtilsTest extends TwitterAndroidTestCase {
    private static final String UNESCAPED_TWEET_TEXT = ">Hello there <\"What is a?\" &;";
    private static final String ESCAPED_TWEET_TEXT
            = "&gt;Hello there &lt;&quot;What is a?&quot; &;";
    private static final int TEST_INDICES_START = 0;
    private static final int TEST_INDICES_END = 13;
    private static final String TEST_MEDIA_TYPE_PHOTO = "photo";

    // test getLastPhotoEntity
    public void testGetLastPhotoEntity_nullEntities() {
        assertNull(TweetTextUtils.getLastPhotoEntity(null));
    }

    public void testGetLastPhotoEntity_nullMedia() {
        final TweetEntities entities = new TweetEntities(null, null, null, null);
        assertNull(TweetTextUtils.getLastPhotoEntity(entities));
    }

    public void testGetLastPhotoEntity_emptyMedia() {
        final TweetEntities entities = new TweetEntities(null, null, new ArrayList<MediaEntity>(),
                null);
        assertNull(TweetTextUtils.getLastPhotoEntity(entities));
    }

    public void testGetLastPhotoEntity_hasFinalPhotoEntity() {
        final MediaEntity entity = TestFixtures.newMediaEntity(TEST_INDICES_START, TEST_INDICES_END,
                TEST_MEDIA_TYPE_PHOTO);
        final ArrayList<MediaEntity> media = new ArrayList<>();
        media.add(entity);
        final TweetEntities entities = new TweetEntities(null, null, media, null);

        assertEquals(entity, TweetTextUtils.getLastPhotoEntity(entities));
    }

    public void testGetLastPhotoEntity_nonPhotoMedia() {
        final MediaEntity entity = TestFixtures.newMediaEntity(TEST_INDICES_START, TEST_INDICES_END,
                "imaginary");
        final ArrayList<MediaEntity> media = new ArrayList<>();
        media.add(entity);
        final TweetEntities entities = new TweetEntities(null, null, media, null);

        assertNull(TweetTextUtils.getLastPhotoEntity(entities));
    }

    public void testHasPhotoUrl_hasPhotoEntity() {
        final MediaEntity entity = TestFixtures.newMediaEntity(TEST_INDICES_START, TEST_INDICES_END,
                TEST_MEDIA_TYPE_PHOTO);
        final ArrayList<MediaEntity> media = new ArrayList<>();
        media.add(entity);
        final TweetEntities entities = new TweetEntities(null, null, media, null);

        assertTrue(TweetTextUtils.hasPhotoUrl(entities));
    }

    public void testHasPhotoUrl_noPhotoEntity() {
        final MediaEntity entity = TestFixtures.newMediaEntity(TEST_INDICES_START, TEST_INDICES_END,
                "imaginary");
        final ArrayList<MediaEntity> media = new ArrayList<>();
        media.add(entity);
        final TweetEntities entities = new TweetEntities(null, null, media, null);

        assertFalse(TweetTextUtils.hasPhotoUrl(entities));
    }

    public void testHasPhotoUrl_uninitializedMediaEntities() {
        assertFalse(TweetTextUtils.hasPhotoUrl(new TweetEntities(null, null, null, null)));
    }

    public void testHasPhotoUrl_nullEntities() {
        assertFalse(TweetTextUtils.hasPhotoUrl(null));
    }


    // test ported from:
    // twitter-android/app/src/androidTest/java/com/twitter/library/util/EntitiesTests.java
    // tests fixing up entity indices after unescaping html characters in tweet text
    public void testFormat_singleEscaping() {
        final FormattedTweetText formattedTweetText = setupAdjustedTweet();
        final Tweet tweet = setupTweetToBeFormatted();
        TweetTextUtils.format(formattedTweetText, tweet);

        assertEquals(UNESCAPED_TWEET_TEXT, formattedTweetText.text);
        assertEquals("Hello", 1, formattedTweetText.urlEntities.get(0).start);
        assertEquals("Hello", 5, formattedTweetText.urlEntities.get(0).end);
        assertEquals("There", 7, formattedTweetText.urlEntities.get(1).start);
        assertEquals("There", 11, formattedTweetText.urlEntities.get(1).end);

        assertEquals("What", 15, formattedTweetText.urlEntities.get(2).start);
        assertEquals("What", 18, formattedTweetText.urlEntities.get(2).end);

        assertEquals("is", 20, formattedTweetText.urlEntities.get(3).start);
        assertEquals("is", 21, formattedTweetText.urlEntities.get(3).end);

        assertEquals("a", 23, formattedTweetText.urlEntities.get(4).start);
        assertEquals("a", 23, formattedTweetText.urlEntities.get(4).end);
    }

    public void testFormat_htmlEntityEdgeCases() {
        final FormattedTweetText formattedTweetText = new FormattedTweetText();

        Tweet tweet = new TweetBuilder().setText("&amp;").build();
        TweetTextUtils.format(formattedTweetText, tweet);
        assertEquals("&", formattedTweetText.text);

        tweet = new TweetBuilder().setText("&#;").build();
        TweetTextUtils.format(formattedTweetText, tweet);
        assertEquals("&#;", formattedTweetText.text);

        tweet = new TweetBuilder().setText("&#34;").build();
        TweetTextUtils.format(formattedTweetText, tweet);
        assertEquals("\"", formattedTweetText.text);

        tweet = new TweetBuilder().setText("&#x22;").build();
        TweetTextUtils.format(formattedTweetText, tweet);
        assertEquals("\"", formattedTweetText.text);

        tweet = new TweetBuilder().setText("&lt; & Larry &gt; &").build();
        TweetTextUtils.format(formattedTweetText, tweet);
        assertEquals("< & Larry > &", formattedTweetText.text);

        tweet = new TweetBuilder().setText("&&amp;").build();
        TweetTextUtils.format(formattedTweetText, tweet);
        assertEquals("&&", formattedTweetText.text);

        tweet = new TweetBuilder().setText("&&&&&&&&amp;").build();
        TweetTextUtils.format(formattedTweetText, tweet);
        assertEquals("&&&&&&&&", formattedTweetText.text);

        tweet = new TweetBuilder().setText("&&&&gt&&lt&&amplt;").build();
        TweetTextUtils.format(formattedTweetText, tweet);
        assertEquals("&&&&gt&&lt&&amplt;", formattedTweetText.text);
    }

    private Tweet setupTweetToBeFormatted() {
        final Tweet tweet = new TweetBuilder().setText(ESCAPED_TWEET_TEXT).build();
        return tweet;
    }

    private FormattedTweetText setupAdjustedTweet() {
        final FormattedTweetText formattedTweetText = new FormattedTweetText();

        UrlEntity url = TestFixtures.newUrlEntity(4, 8);
        // Hello
        formattedTweetText.urlEntities.add(new FormattedUrlEntity(url));

        // There
        url = TestFixtures.newUrlEntity(10, 14);
        formattedTweetText.urlEntities.add(new FormattedUrlEntity(url));

        // What
        url = TestFixtures.newUrlEntity(26, 29);
        formattedTweetText.urlEntities.add(new FormattedUrlEntity(url));

        // is
        url = TestFixtures.newUrlEntity(31, 32);
        formattedTweetText.urlEntities.add(new FormattedUrlEntity(url));

        // a
        url = TestFixtures.newUrlEntity(34, 34);
        formattedTweetText.urlEntities.add(new FormattedUrlEntity(url));

        return formattedTweetText;
    }
}
