package com.twitter.sdk.android.core.internal.scribe;

import android.os.Build;
import android.text.TextUtils;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.twitter.sdk.android.core.BuildConfig;
import com.twitter.sdk.android.core.Session;
import com.twitter.sdk.android.core.SessionManager;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import io.fabric.sdk.android.Kit;
import io.fabric.sdk.android.services.common.ExecutorUtils;
import io.fabric.sdk.android.services.common.IdManager;
import io.fabric.sdk.android.services.settings.Settings;
import io.fabric.sdk.android.services.settings.SettingsData;

/**
 * Instances of this class should always be created on a background thread.
 */
public class DefaultScribeClient extends ScribeClient {
    /*
     * We are using the syndication backend for all scribing until there is a separate schema and
     * category for other Fabric events.
     */
    private static final String SCRIBE_URL = "https://syndication.twitter.com";
    private static final String SCRIBE_PATH_VERSION = "i";
    private static final String SCRIBE_PATH_TYPE = "sdk";

    private static final String DEBUG_BUILD = "debug";

    private static volatile ScheduledExecutorService executor;

    private final Kit kit;
    private final List<SessionManager<? extends Session>> sessionManagers;
    private final String advertisingId;

    public DefaultScribeClient(Kit kit, String kitName,
                               List<SessionManager<? extends Session>> sessionManagers,
                               IdManager idManager) {
        this(kit, kitName, getGson(), sessionManagers, idManager);
    }

    public DefaultScribeClient(Kit kit, String kitName, Gson gson,
            List<SessionManager<? extends Session>> sessionManagers, IdManager idManager) {
        super(kit, getExecutor(),
                getScribeConfig(Settings.getInstance().awaitSettingsData(),
                        getUserAgent(kitName, kit)),
                new ScribeEvent.Transform(gson),
                TwitterCore.getInstance().getAuthConfig(),
                sessionManagers, TwitterCore.getInstance().getSSLSocketFactory(),
                idManager);

        this.sessionManagers = sessionManagers;
        this.kit = kit;
        this.advertisingId = idManager.getAdvertisingId();
    }

    public void scribeSyndicatedSdkImpressionEvents(EventNamespace... namespaces) {
        final String language;
        if (kit.getContext() != null) {
            language = kit.getContext().getResources().getConfiguration().locale.getLanguage();
        } else {
            language = "";
        }
        final long timestamp = System.currentTimeMillis();

        /**
         * The advertising ID may be null  depending on the users preferences and if Google Play
         * Services has been installed on the device.
         */
        for (EventNamespace ns : namespaces) {
            scribe(new SyndicatedSdkImpressionEvent(ns, timestamp, language, advertisingId));
        }
    }

    public void scribe(ScribeEvent event) {
        super.scribe(event, getScribeSessionId(getActiveSession()));
    }

    // visible for tests
    Session getActiveSession() {
        Session session = null;
        for (SessionManager<? extends Session> sessionManager : sessionManagers) {
            session = sessionManager.getActiveSession();
            if (session != null) {
                break;
            }
        }
        return session;
    }

    // visible for tests
    long getScribeSessionId(Session activeSession) {
        final long scribeSessionId;
        if (activeSession != null) {
            scribeSessionId = activeSession.getId();
        } else {
            // It's possible that we're attempting to load a tweet before we have a valid
            // session. Store the scribe event locally with the logged out user id so that we can
            // send it up at a later time with the logged out session.
            scribeSessionId = TwitterSession.LOGGED_OUT_USER_ID;
        }
        return scribeSessionId;
    }

    private static Gson getGson() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    private static ScheduledExecutorService getExecutor() {
        if (executor == null) {
            synchronized (DefaultScribeClient.class) {
                if (executor == null) {
                    executor = ExecutorUtils.buildSingleThreadScheduledExecutorService("scribe");
                }
            }
        }
        return executor;
    }

    static ScribeConfig getScribeConfig(SettingsData settingsData, String userAgent) {
        // Get scribe configuration using analytics settings, which is used by crashlytics for
        // configuring Answers. This is temporary until we have can get our scribe settings from the
        // backend. If analytics settings are not available, fallback to defaults.
        final int maxFilesToKeep;
        final int sendIntervalSeconds;
        if (settingsData != null && settingsData.analyticsSettingsData != null) {
            maxFilesToKeep = settingsData.analyticsSettingsData.maxPendingSendFileCount;
            sendIntervalSeconds = settingsData.analyticsSettingsData.flushIntervalSeconds;
        } else {
            maxFilesToKeep = ScribeConfig.DEFAULT_MAX_FILES_TO_KEEP;
            sendIntervalSeconds = ScribeConfig.DEFAULT_SEND_INTERVAL_SECONDS;
        }

        final String scribeUrl = getScribeUrl(SCRIBE_URL, BuildConfig.SCRIBE_ENDPOINT_OVERRIDE);
        return new ScribeConfig(isEnabled(), scribeUrl, SCRIBE_PATH_VERSION,
                SCRIBE_PATH_TYPE, BuildConfig.SCRIBE_SEQUENCE, userAgent, maxFilesToKeep,
                sendIntervalSeconds);
    }

    /*
     * This method serves to disable the scribe strategy in testing, it causes massive memory leaks
     * that are not easily cleaned up unless we have a teardown method added to the kit class
     * interface.
     */
    private static boolean isEnabled() {
        return !BuildConfig.BUILD_TYPE.equals(DEBUG_BUILD);
    }

    static String getUserAgent(String kitName, Kit kit) {
        return new StringBuilder()
                .append("Fabric/")
                .append(kit.getFabric().getVersion())
                .append(" (Android ")
                .append(Build.VERSION.SDK_INT)
                .append(") ")
                .append(kitName)
                .append("/")
                .append(kit.getVersion())
                .toString();
    }

    // visible for tests
    static String getScribeUrl(String defaultUrl, String overrideUrl) {
        if (!TextUtils.isEmpty(overrideUrl)) {
            return overrideUrl;
        } else {
            return defaultUrl;
        }
    }
}
