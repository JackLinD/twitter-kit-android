package com.twitter.sdk.android.tweetui;

import android.net.Uri;

import io.fabric.sdk.android.FabricTestUtils;
import io.fabric.sdk.android.KitStub;
import io.fabric.sdk.android.Logger;

import com.twitter.sdk.android.core.TwitterAndroidTestCase;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.TweetBuilder;
import com.twitter.sdk.android.core.models.UserBuilder;

import static org.mockito.Mockito.*;

public class TweetUtilsTest extends TwitterAndroidTestCase {
    private static final String A_FULL_PERMALINK = "https://twitter.com/jack/status/20";
    private static final String A_PERMALINK_WITH_NO_SCREEN_NAME
            = "https://twitter.com/twitter_unknown/status/20";
    private static final String A_VALID_SCREEN_NAME = "jack";
    private static final int A_VALID_TWEET_ID = 20;
    private static final int AN_INVALID_TWEET_ID = 0;

    public void testLoadTweet_beforeKitStart() {
        FabricTestUtils.resetFabric();
        try {
            FabricTestUtils.with(getContext(), new KitStub<TwitterCore>());
            TweetUtils.loadTweet(TestFixtures.TEST_TWEET_ID, null);
            fail("IllegalStateException not thrown");
        } catch (IllegalStateException e) {
            assertEquals(TweetUi.NOT_STARTED_ERROR, e.getMessage());
        } catch (Exception ex) {
            fail();
        } finally {
            FabricTestUtils.resetFabric();
        }
    }

    public void testLoadTweets_beforeKitStart() {
        FabricTestUtils.resetFabric();
        try {
            FabricTestUtils.with(getContext(), new KitStub<TwitterCore>());
            TweetUtils.loadTweets(TestFixtures.TWEET_IDS, null);
            fail("IllegalStateException not thrown");
        } catch (IllegalStateException e) {
            assertEquals(TweetUi.NOT_STARTED_ERROR, e.getMessage());
        } catch (Exception ex) {
            fail();
        } finally {
            FabricTestUtils.resetFabric();
        }
    }

    public void testLoggingCallback_callsCbOnSuccess() {
        final LoadCallback<Tweet> developerCallback = mock(LoadCallback.class);
        final TweetUtils.LoggingCallback cb
                = new TweetUtils.LoggingCallback(A_VALID_TWEET_ID, developerCallback, null);

        cb.success(mock(Tweet.class));
        verify(developerCallback).success(any(Tweet.class));
    }

    public void testLoggingCallback_callsCbOnFailure() {
        final LoadCallback<Tweet> developerCallback = mock(LoadCallback.class);
        final TweetUtils.LoggingCallback cb = new TweetUtils.LoggingCallback(A_VALID_TWEET_ID,
                developerCallback, mock(Logger.class));

        cb.failure(mock(TwitterException.class));
        verify(developerCallback).failure(any(TwitterException.class));
    }

    public void testLoggingCallback_logsOnFailure() {
        final LoadCallback<Tweet> developerCallback = mock(LoadCallback.class);
        final Logger logger = mock(Logger.class);
        final TweetUtils.LoggingCallback cb = new TweetUtils.LoggingCallback(A_VALID_TWEET_ID,
                developerCallback, logger);

        cb.failure(mock(TwitterException.class));
        verify(logger).d(any(String.class), any(String.class));
    }

    public void testLoggingCallback_handlesNullCallbackOnSuccess() {
        final TweetUtils.LoggingCallback cb = new TweetUtils.LoggingCallback(A_VALID_TWEET_ID,
                null, mock(Logger.class));

        try {
            cb.success(mock(Tweet.class));
        } catch (NullPointerException e) {
            fail("Should have handled null callback");
        }
    }

    public void testLoggingCallback_handlesNullCallbackOnFailure() {
        final Logger logger = mock(Logger.class);
        final TweetUtils.LoggingCallback cb = new TweetUtils.LoggingCallback(A_VALID_TWEET_ID,
                null, logger);

        try {
            cb.failure(mock(TwitterException.class));
            verify(logger).d(any(String.class), any(String.class));
        } catch (NullPointerException e) {
            fail("Should have handled null callback");
        }
    }

    public void testIsTweetResolvable_nullTweet() {
        assertFalse(TweetUtils.isTweetResolvable(null));
    }

    public void testIsTweetResolvable_hasInvalidIdAndNullUser() {
        final Tweet tweet = new TweetBuilder().build();
        assertNull(tweet.user);
        assertFalse(TweetUtils.isTweetResolvable(tweet));
    }

    public void testIsTweetResolvable_hasValidIdAndNullUser() {
        final Tweet tweet = new TweetBuilder().setId(TestFixtures.TEST_TWEET_ID).build();
        assertNull(tweet.user);
        assertFalse(TweetUtils.isTweetResolvable(tweet));
    }

    public void testIsTweetResolvable_hasInvalidIdAndUserWithNullScreenName() {
        final Tweet tweet = new TweetBuilder()
                .setUser(
                        new UserBuilder()
                                .setId(1)
                                .setName(null)
                                .setScreenName(null)
                                .setVerified(false)
                                .build())
                .build();
        assertFalse(TweetUtils.isTweetResolvable(tweet));
    }

    public void testIsTweetResolvable_hasValidIdAndUserWithNullScreenName() {
        final Tweet tweet = new TweetBuilder()
                .setId(TestFixtures.TEST_TWEET_ID)
                .setUser(
                        new UserBuilder()
                                .setId(1)
                                .setName(null)
                                .setScreenName(null)
                                .setVerified(false)
                                .build()
                ).build();
        assertFalse(TweetUtils.isTweetResolvable(tweet));
    }

    public void testIsTweetResolvable_hasInvalidIdAndUserWithEmptyScreenName() {
        final Tweet tweet = new TweetBuilder()
                .setUser(new UserBuilder()
                        .setId(1)
                        .setName(null)
                        .setScreenName("")
                        .setVerified(false)
                        .build())
                .build();
        assertFalse(TweetUtils.isTweetResolvable(tweet));
    }

    public void testIsTweetResolvable_hasValidIdAndUserWithEmptyScreenName() {
        final Tweet tweet = new TweetBuilder()
                .setId(TestFixtures.TEST_TWEET_ID)
                .setUser(new UserBuilder()
                        .setId(1)
                        .setName(null)
                        .setScreenName("")
                        .setVerified(false)
                        .build())
                .build();
        assertFalse(TweetUtils.isTweetResolvable(tweet));
    }

    public void testIsTweetResolvable_hasUserWithScreenNameAndValidId() {
        assertTrue(TweetUtils.isTweetResolvable(TestFixtures.TEST_TWEET));
    }

    public void testGetPermalink_nullScreenNameValidId() {
        assertEquals(A_PERMALINK_WITH_NO_SCREEN_NAME,
                TweetUtils.getPermalink(null, A_VALID_TWEET_ID).toString());
    }

    public void testGetPermalink_validScreenNameZeroId() {
        assertNull(TweetUtils.getPermalink(A_VALID_SCREEN_NAME, AN_INVALID_TWEET_ID));
    }

    public void testGetPermalink_validScreenNameAndId() {
        assertEquals(A_FULL_PERMALINK,
                TweetUtils.getPermalink(A_VALID_SCREEN_NAME, A_VALID_TWEET_ID).toString());
    }

    public void testGetPermalink_emptyScreenName() {
        final Uri permalink = TweetUtils.getPermalink("", 20);
        assertEquals(A_PERMALINK_WITH_NO_SCREEN_NAME, permalink.toString());
    }

    public void testGetDisplayTweet_nullTweet() {
        assertNull(TweetUtils.getDisplayTweet(null));
    }

    public void testGetDisplayTweet_retweet() {
        assertEquals(TestFixtures.TEST_RETWEET.retweetedStatus,
                TweetUtils.getDisplayTweet(TestFixtures.TEST_RETWEET));
    }

    public void testGetDisplayTweet_nonRetweet() {
        assertEquals(TestFixtures.TEST_TWEET, TweetUtils.getDisplayTweet(TestFixtures.TEST_TWEET));
    }
}
