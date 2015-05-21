package com.twitter.sdk.android.tweetui;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.internal.GuestCallback;
import com.twitter.sdk.android.core.models.Tweet;

import java.util.List;

import io.fabric.sdk.android.Fabric;

/**
 * UserTimeline provides a timeline of tweets from the statuses/userTimeline API source.
 */
public class UserTimeline extends BaseTimeline implements Timeline<Tweet> {
    private static final String SCRIBE_SECTION = "user";

    final Long userId;
    final String screenName;
    final Integer maxItemsPerRequest;
    final Boolean includeReplies;
    final Boolean includeRetweets;

    UserTimeline(TweetUi tweetUi, Long userId, String screenName, Integer maxItemsPerRequest,
            Boolean includeReplies, Boolean includeRetweets) {
        super(tweetUi);
        this.userId = userId;
        this.screenName = screenName;
        this.maxItemsPerRequest = maxItemsPerRequest;
        // null includeReplies should default to false
        this.includeReplies = includeReplies == null ? false : includeReplies;
        this.includeRetweets = includeRetweets;
    }

    /**
     * Loads Tweets with id greater than (newer than) sinceId. If sinceId is null, loads the newest
     * Tweets.
     * @param sinceId minimum id of the Tweets to load (exclusive).
     * @param cb callback.
     */
    @Override
    public void next(Long sinceId, Callback<TimelineResult<Tweet>> cb) {
        addRequest(createUserTimelineRequest(sinceId, null, cb));
    }

    /**
     * Loads Tweets with id less than (older than) maxId.
     * @param maxId maximum id of the Tweets to load (exclusive).
     * @param cb callback.
     */
    @Override
    public void previous(Long maxId, Callback<TimelineResult<Tweet>> cb) {
        // user timeline api provides results which are inclusive, decrement the maxId to get
        // exclusive results
        addRequest(createUserTimelineRequest(null, decrementMaxId(maxId), cb));
    }

    @Override
    String getTimelineType() {
        return SCRIBE_SECTION;
    }

    Callback<TwitterApiClient> createUserTimelineRequest(final Long sinceId,
            final Long maxId, final Callback<TimelineResult<Tweet>> cb) {
        return new LoggingCallback<TwitterApiClient>(cb, Fabric.getLogger()) {
            @Override
            public void success(Result<TwitterApiClient> result) {
                result.data.getStatusesService().userTimeline(userId, screenName,
                        maxItemsPerRequest, sinceId, maxId, false, !includeReplies, null,
                        includeRetweets, new GuestCallback<>(new TweetsCallback(cb)));
            }
        };
    }

    /**
     * Wrapper callback which unpacks a list of Tweets into a TimelineResult (cursor and items).
     */
    class TweetsCallback extends Callback<List<Tweet>> {
        protected final Callback<TimelineResult<Tweet>> cb;

        /**
         * Constructs a TweetsCallback
         * @param cb A callback which expects a TimelineResult
         */
        TweetsCallback(Callback<TimelineResult<Tweet>> cb) {
            this.cb = cb;
        }

        @Override
        public void success(Result<List<Tweet>> result) {
            final List<Tweet> tweets = result.data;
            final TimelineResult<Tweet> timelineResult
                    = new TimelineResult<>(new TimelineCursor(tweets), tweets);
            if (cb != null) {
                cb.success(timelineResult, result.response);
            }
        }

        @Override
        public void failure(TwitterException exception) {
            if (cb != null) {
                cb.failure(exception);
            }
        }
    }

    /**
     * UserTimeline Builder.
     */
    public static class Builder {
        private final TweetUi tweetUi;
        private Long userId;
        private String screenName;
        private Integer maxItemsPerRequest = 30;
        private Boolean includeReplies;
        private Boolean includeRetweets;

        /**
         * Constructs a Builder.
         */
        public Builder() {
            this(TweetUi.getInstance());
        }

        /**
         * Constructs a Builder.
         * @param tweetUi A TweetUi instance.
         */
        public Builder(TweetUi tweetUi) {
            if (tweetUi == null) {
                throw new IllegalArgumentException("TweetUi instance must not be null");
            }
            this.tweetUi = tweetUi;
        }

        /**
         * Sets the userId for the UserTimeline.
         * @param userId The ID of the user for whom to return results for.
         */
        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        /**
         * Sets the screenName for the UserTimeline.
         * @param screenName The screen name of the user for whom to return results for.
         */
        public Builder screenName(String screenName) {
            this.screenName = screenName;
            return this;
        }

        /**
         * Sets the number of Tweets returned per request for the UserTimeline.
         * @param maxItemsPerRequest The number of tweets to return per request, up to a maximum of
         * 200.
         */
        public Builder maxItemsPerRequest(Integer maxItemsPerRequest) {
            this.maxItemsPerRequest = maxItemsPerRequest;
            return this;
        }

        /**
         * Sets whether to includeReplies for the UserTimeline. Defaults to false.
         * @param includeReplies true to allow replies to be included in the returned timeline
         * result.
         */
        public Builder includeReplies(Boolean includeReplies) {
            this.includeReplies = includeReplies;
            return this;
        }

        /**
         * Sets whether to includeRetweets for the UserTimeline. Defaults to true.
         * @param includeRetweets When set to false, the timeline will strip any native retweets
         * (though they will still count toward both the maximal length of the timeline and the
         * slice selected by the count parameter).
         */
        public Builder includeRetweets(Boolean includeRetweets) {
            this.includeRetweets = includeRetweets;
            return this;
        }

        /**
         * Builds a UserTimeline from the Builder parameters.
         * @return a UserTimeline.
         */
        public UserTimeline build() {
            return new UserTimeline(tweetUi, userId, screenName, maxItemsPerRequest, includeReplies,
                    includeRetweets);
        }
    }
}
