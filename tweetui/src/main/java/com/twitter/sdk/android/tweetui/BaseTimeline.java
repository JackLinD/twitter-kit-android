package com.twitter.sdk.android.tweetui;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.internal.scribe.EventNamespace;
import com.twitter.sdk.android.core.internal.scribe.SyndicatedSdkImpressionEvent;

/**
 * BaseTimeline which handles TweetUi instance argument.
 */
abstract class BaseTimeline {
    // syndicated_sdk_impression_values
    private static final String SDK_IMPRESSION_PAGE = "timeline";
    private static final String SDK_IMPRESSION_COMPONENT = "initial";
    private static final String SDK_IMPRESSION_ELEMENT = ""; // intentionally blank
    // tfw_client_event values
    private static final String TFW_IMPRESSION_PAGE = "android";
    private static final String TFW_IMPRESSION_SECTION = "timeline";
    private static final String TFW_IMPRESSION_ELEMENT = "initial";
    // general event values
    private static final String IMPRESSION_ACTION = "impression";

    protected final TweetUi tweetUi;

    BaseTimeline(TweetUi tweetUi) {
        if (tweetUi == null) {
            throw new IllegalArgumentException("TweetUi instance must not be null");
        }
        this.tweetUi = tweetUi;
        scribeImpression();
    }

    abstract String getTimelineType();

    private void scribeImpression() {
        tweetUi.scribe(
                getSyndicatedSdkImpressionNamespace(),
                getTfwClientEventNamespace()
        );
    }

    private EventNamespace getSyndicatedSdkImpressionNamespace() {
        return new EventNamespace.Builder()
                .setClient(SyndicatedSdkImpressionEvent.CLIENT_NAME)
                .setPage(SDK_IMPRESSION_PAGE)
                .setSection(getTimelineType())
                .setComponent(SDK_IMPRESSION_COMPONENT)
                .setElement(SDK_IMPRESSION_ELEMENT)
                .setAction(IMPRESSION_ACTION)
                .builder();
    }

    private EventNamespace getTfwClientEventNamespace() {
        return new EventNamespace.Builder()
                .setClient(SyndicationClientEvent.CLIENT_NAME)
                .setPage(TFW_IMPRESSION_PAGE)
                .setSection(TFW_IMPRESSION_SECTION)
                .setComponent(getTimelineType())
                .setElement(TFW_IMPRESSION_ELEMENT)
                .setAction(IMPRESSION_ACTION)
                .builder();
    }

    /**
     * Returns a decremented maxId if the given id is non-null. Otherwise returns the given maxId.
     * Suitable for REST Timeline endpoints which return inclusive previous results when exclusive
     * is desired.
     */
    static Long decrementMaxId(Long maxId) {
        return maxId == null ? null : maxId - 1;
    }

    /**
     * Adds the request to the AuthRequestQueue where guest auth will be setup.
     */
    void addRequest(final Callback<TwitterApiClient> cb) {
        tweetUi.getAuthRequestQueue().addRequest(cb);
    }
}
