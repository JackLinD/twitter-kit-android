package com.digits.sdk.android;

import android.annotation.TargetApi;
import android.os.Build;

import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.Kit;
import io.fabric.sdk.android.services.concurrency.DependsOn;
import io.fabric.sdk.android.services.persistence.PreferenceStoreImpl;

import com.twitter.sdk.android.core.PersistedSessionManager;
import com.twitter.sdk.android.core.Session;
import com.twitter.sdk.android.core.SessionManager;
import com.twitter.sdk.android.core.internal.SessionMonitor;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.internal.scribe.DefaultScribeClient;
import com.twitter.sdk.android.core.internal.scribe.EventNamespace;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Digits allows authentication based on a phone number.
 */
@DependsOn(TwitterCore.class)
public class Digits extends Kit<Void> {
    public static final String TAG = "Digits";

    static final String PREF_KEY_ACTIVE_SESSION = "active_session";
    static final String PREF_KEY_SESSION = "session";

    private static final String KIT_SCRIBE_NAME = "Digits";

    DefaultScribeClient scribeClient;

    private volatile DigitsClient digitsClient;
    private volatile ContactsClient contactsClient;
    private SessionManager<DigitsSession> sessionManager;
    private SessionMonitor<DigitsSession> sessionMonitor;
    private ActivityClassManager activityClassManager;

    private int themeResId;

    public static Digits getInstance() {
        return Fabric.getKit(Digits.class);
    }

    /**
     * Starts and sets the theme for the authentication flow
     *
     * @param callback will get the success or failure callback. It can be null,
     *                 but the developer will not get any callback.
     * @param themeResId Theme resource id
     */
    public static void authenticate(AuthCallback callback, int themeResId) {
        getInstance().setTheme(themeResId);
        getInstance().getDigitsClient().startSignUp(callback);
    }

    /**
     * Starts the authentication flow
     *
     * @param callback will get the success or failure callback. It can be null,
     *                 but the developer will not get any callback.
     */
    public static void authenticate(AuthCallback callback) {
        authenticate(callback, ThemeUtils.DEFAULT_THEME);
    }

    public static SessionManager<DigitsSession> getSessionManager() {
        return getInstance().sessionManager;
    }

    @Override
    public String getVersion() {
        return BuildConfig.VERSION_NAME + "." + BuildConfig.BUILD_NUMBER;
    }

    @Override
    protected boolean onPreExecute() {
        sessionManager = new PersistedSessionManager<>(new PreferenceStoreImpl(this),
                new DigitsSession.Serializer(), PREF_KEY_ACTIVE_SESSION, PREF_KEY_SESSION);

        sessionMonitor = new SessionMonitor<>(sessionManager, getExecutorService());
        return super.onPreExecute();
    }

    @Override
    protected Void doInBackground() {
        // Trigger restoration of session
        sessionManager.getActiveSession();
        createDigitsClient();
        createContactsClient();
        setUpScribing();
        sessionMonitor.triggerVerificationIfNecessary();
        // Monitor activity lifecycle after sessions have been restored. Otherwise we would not
        // have any sessions to monitor anyways.
        sessionMonitor.monitorActivityLifecycle(getFabric().getActivityLifecycleManager());
        return null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    int getTheme() {
        if (themeResId != ThemeUtils.DEFAULT_THEME) {
            return themeResId;
        }

        return R.style.Digits_default;
    }

    protected void setTheme(int themeResId) {
        this.themeResId = themeResId;
        createActivityClassManager();
    }

    @Override
    public String getIdentifier() {
        return BuildConfig.GROUP + ":" + BuildConfig.ARTIFACT_ID;
    }

    DigitsClient getDigitsClient() {
        if (digitsClient == null) {
            createDigitsClient();
        }
        return digitsClient;
    }

    private synchronized void createDigitsClient() {
        if (digitsClient == null) {
            digitsClient = new DigitsClient();
        }
    }

    public ContactsClient getContactsClient() {
        if (contactsClient == null) {
            createContactsClient();
        }
        return contactsClient;
    }

    private synchronized void createContactsClient() {
        if (contactsClient == null) {
            contactsClient = new ContactsClient();
        }
    }

    protected ExecutorService getExecutorService() {
        return getFabric().getExecutorService();
    }

    private void setUpScribing() {
        final List<SessionManager<? extends Session>> sessionManagers = new ArrayList<>();
        sessionManagers.add(sessionManager);
        scribeClient = new DefaultScribeClient(this, KIT_SCRIBE_NAME,
                sessionManagers, getIdManager());
    }

    protected void scribe(EventNamespace... namespaces) {
        if (scribeClient != null) {
            scribeClient.scribeSyndicatedSdkImpressionEvents(namespaces);
        }
    }

    protected ActivityClassManager getActivityClassManager() {
        if (activityClassManager == null) {
            createActivityClassManager();
        }
        return activityClassManager;
    }

    protected void createActivityClassManager() {
        final ActivityClassManagerFactory factory = new ActivityClassManagerFactory();
        activityClassManager = factory.createActivityClassManager(getContext(), themeResId);
    }
}
