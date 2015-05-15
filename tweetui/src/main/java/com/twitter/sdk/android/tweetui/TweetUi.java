package com.twitter.sdk.android.tweetui;

import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.Kit;
import io.fabric.sdk.android.services.concurrency.DependsOn;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;
import com.twitter.cobalt.metrics.ConsoleReporter;
import com.twitter.cobalt.metrics.MetricsManager;
import com.twitter.sdk.android.core.Session;
import com.twitter.sdk.android.core.SessionManager;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.internal.scribe.DefaultScribeClient;
import com.twitter.sdk.android.core.internal.scribe.EventNamespace;
import com.twitter.sdk.android.tweetui.internal.ActiveSessionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The TweetUi Kit provides views to render Tweets.
 */
@DependsOn(TwitterCore.class)
public class TweetUi extends Kit<Boolean> {
    static final String LOGTAG = "TweetUi";
    static final String NOT_STARTED_ERROR = "Must start TweetUi Kit in Fabric.with().";

    private static final String KIT_SCRIBE_NAME = "TweetUi";

    List<SessionManager<? extends Session>> sessionManagers;
    ActiveSessionProvider activeSessionProvider;
    String advertisingId;
    DefaultScribeClient scribeClient;

    private final AtomicReference<Gson> gsonRef;
    private TweetRepository tweetRepository;
    private AuthRequestQueue queue;
    private Picasso imageLoader;
    private MetricsManager metricsManager;

    // Singleton class, should only be created using getInstance()
    public TweetUi() {
        gsonRef = new AtomicReference<>();
    }

    /**
     * @return the TweetUi singleton.
     * @throws IllegalStateException if the kit has not been started.
     */
    public static TweetUi getInstance() {
        checkInitialized();
        return Fabric.getKit(TweetUi.class);
    }

    @Override
    public String getIdentifier() {
        return BuildConfig.GROUP + ":" + BuildConfig.ARTIFACT_ID;
    }

    @Override
    public String getVersion() {
        return BuildConfig.VERSION_NAME + "." + BuildConfig.BUILD_NUMBER;
    }

    @Override
    protected boolean onPreExecute() {
        super.onPreExecute();
        final TwitterCore twitterCore = TwitterCore.getInstance();
        sessionManagers = new ArrayList<>(2);
        sessionManagers.add(twitterCore.getSessionManager());
        sessionManagers.add(twitterCore.getAppSessionManager());
        activeSessionProvider = new ActiveSessionProvider(sessionManagers);

        queue = new AuthRequestQueue(twitterCore, activeSessionProvider);
        tweetRepository = new TweetRepository(this, getFabric().getExecutorService(),
                getFabric().getMainHandler(), queue);
        return true;
    }

    @Override
    protected Boolean doInBackground() {
        /*
         * Picasso creation was moved to doInBackground because in the presence of okHttp there
         * ends up being strict mode violations if it is initialized on the main thread.
         */
        imageLoader = Picasso.with(getContext());
        queue.sessionRestored(activeSessionProvider.getActiveSession());

        // ensure initialization of gson, this initialization in most cases will always
        // happen here.
        initGson();
        setUpScribeClient();
        initMetricsManager();
        advertisingId = getIdManager().getAdvertisingId();
        return true;
    }

    /**
     * Checks that the TweetUi kit has a singleton instance available.
     * @throws IllegalStateException if the kit instance is null.
     */
    private static void checkInitialized() {
        if (Fabric.getKit(TweetUi.class) == null) {
            throw new IllegalStateException(NOT_STARTED_ERROR);
        }
    }

    private void setUpScribeClient() {
        scribeClient = new DefaultScribeClient(this, KIT_SCRIBE_NAME, gsonRef.get(),
                sessionManagers, getIdManager());
    }

    void scribe(EventNamespace... namespaces) {
        if (scribeClient == null) return;

        final String language = getContext().getResources().getConfiguration().locale.getLanguage();
        final long timestamp = System.currentTimeMillis();

        /*
         * The advertising ID may be null if this method is called before doInBackground completes.
         * It also may be null depending on the users preferences and if Google Play Services has
         * been installed on the device.
         */
        for (EventNamespace ns : namespaces) {
            scribeClient.scribe(ScribeEventFactory.newScribeEvent(ns, timestamp, language,
                    advertisingId));
        }
    }

    // idempotent init
    void initGson() {
        if (gsonRef.get() == null) {
            final Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();

            gsonRef.compareAndSet(null, gson);
        }
    }

    void initMetricsManager() {
        MetricsManager.initialize(getContext().getApplicationContext());
        metricsManager = MetricsManager.getInstance();
        metricsManager.addReporter(new ConsoleReporter());
    }

    MetricsManager getMetricsManager() {
        return metricsManager;
    }

    TweetRepository getTweetRepository() {
        return tweetRepository;
    }

    AuthRequestQueue getAuthRequestQueue() {
        return queue;
    }

    // Testing purposes only
    void setTweetRepository(TweetRepository tweetRepository) {
        this.tweetRepository = tweetRepository;
    }

    Picasso getImageLoader() {
        return imageLoader;
    }

    // Testing purposes only
    void setImageLoader(Picasso imageLoader) {
        this.imageLoader = imageLoader;
    }

    void clearAppSession(long sessionId) {
        TwitterCore.getInstance().getAppSessionManager().clearSession(sessionId);
    }
}
