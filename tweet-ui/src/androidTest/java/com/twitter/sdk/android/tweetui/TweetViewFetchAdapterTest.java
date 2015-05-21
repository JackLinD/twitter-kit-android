package com.twitter.sdk.android.tweetui;

import android.os.Handler;

import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.TweetBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Tests the TweetViewFetchAdapter which can accept Tweet ids and retrieves the corresponding Tweet
 * objects across the network. Acts as a data provider to ListViews.
 */
public class TweetViewFetchAdapterTest extends TweetUiTestCase {
    private static final int TWEET_COUNT = 2;
    private static final long[] TWEET_IDS = {20L, 30L};
    private List<Long> tweetIds = new ArrayList<>();
    private final List<Tweet> expectedTweets = new ArrayList<>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        for (long tweetId : TWEET_IDS) {
            tweetIds.add(tweetId);
            final Tweet tweet = new TweetBuilder().setId(tweetId).build();
            expectedTweets.add(tweet);
        }
        final TweetRepository tweetRepository = new TestTweetRepository(tweetUi, executorService,
                mainHandler, queue);
        tweetUi.setTweetRepository(tweetRepository);
    }

    public void testAdapter_construction() {
        final TweetViewFetchAdapter<CompactTweetView> adapter
                = new TweetViewFetchAdapter<>(getContext());
        assertEquals(0, adapter.getCount());
        assertNotNull(adapter.getTweets());
        assertTrue(adapter.getTweets().isEmpty());
    }

    public void testAdapter_constructionWithTweetIds() {
        final TweetViewFetchAdapter<CompactTweetView> adapter
                = new TweetViewFetchAdapter<>(getContext(), tweetIds);
        assertEquals(2, adapter.getCount());
        final BaseTweetView zerothTweet = (BaseTweetView) adapter.getView(0, null, null);
        assertEquals(expectedTweets.get(0).id, zerothTweet.getTweetId());
        final BaseTweetView firstTweet = (BaseTweetView) adapter.getView(1, null, null);
        assertEquals(expectedTweets.get(1).id, firstTweet.getTweetId());
        assertNotNull(adapter.getTweets());
        assertEquals(TWEET_COUNT, adapter.getTweets().size());
    }

    public void testAdapter_setTweetIds() {
        final TweetViewFetchAdapter<CompactTweetView> adapter
                = new TweetViewFetchAdapter<>(getContext());
        assertEquals(0, adapter.getCount());
        adapter.setTweetIds(tweetIds);
        assertEquals(2, adapter.getCount());
        final BaseTweetView zerothTweet = (BaseTweetView) adapter.getView(0, null, null);
        assertEquals(expectedTweets.get(0).id, zerothTweet.getTweetId());
        final BaseTweetView firstTweet = (BaseTweetView) adapter.getView(1, null, null);
        assertEquals(expectedTweets.get(1).id, firstTweet.getTweetId());
    }

    public void testAdapter_setTweetIdsNull() {
        final TweetViewFetchAdapter<CompactTweetView> adapter
                = new TweetViewFetchAdapter<>(getContext());
        adapter.setTweetIds(null);
        assertNotNull(adapter.getTweets());
        assertEquals(0, adapter.getCount());
    }

    public void testAdapter_getTweets() {
        final TweetViewFetchAdapter<CompactTweetView> adapter
                = new TweetViewFetchAdapter<>(getContext(), tweetIds);
        final List<Tweet> foundTweets = adapter.getTweets();
        assertEquals(TWEET_COUNT, foundTweets.size());
        for (int i = 0; i < foundTweets.size(); i++) {
            assertEquals(TWEET_IDS[i], foundTweets.get(i).id);
        }
    }

    /**
     * TestTweetRepository with a loadTweets method that calls cb's success method with the
     * requested Tweets immediately.
     */
    public class TestTweetRepository extends TweetRepository {
        TestTweetRepository(TweetUi tweetUiKit, ExecutorService executorService,
                            Handler mainHandler, AuthRequestQueue queue) {
            super(tweetUiKit, executorService, mainHandler, queue);
        }

        @Override
        void loadTweets(List<Long> tweetIds, final LoadCallback<List<Tweet>> cb) {
            final List<Tweet> tweets = new ArrayList<>();
            if (tweetIds == null) {
                cb.success(tweets);
                return;
            }
            for (long id : tweetIds) {
                final Tweet tweet = new TweetBuilder().setId(id).build();
                tweets.add(tweet);
            }
            cb.success(tweets);
        }
    }
}
