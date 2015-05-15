package com.twitter.sdk.android.core;

import com.twitter.sdk.android.core.internal.TwitterApi;
import com.twitter.sdk.android.core.models.SafeListAdapter;
import com.twitter.sdk.android.core.models.SafeMapAdapter;
import com.twitter.sdk.android.core.services.AccountService;
import com.twitter.sdk.android.core.internal.CollectionService;
import com.twitter.sdk.android.core.services.FavoriteService;
import com.twitter.sdk.android.core.services.SearchService;
import com.twitter.sdk.android.core.services.StatusesService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.SSLSocketFactory;

import retrofit.RestAdapter;
import retrofit.android.MainThreadExecutor;
import retrofit.converter.GsonConverter;

/**
 * A class to allow authenticated access to Twitter API endpoints.
 * Can be extended to provided additional endpoints by extending and providing Retrofit API
 * interfaces to {@link com.twitter.sdk.android.core.TwitterApiClient#getService(Class)}
 */
public class TwitterApiClient {

    final ConcurrentHashMap<Class, Object> services;
    final RestAdapter adapter;

    TwitterApiClient(TwitterAuthConfig authConfig,
                     Session session,
                     TwitterApi twitterApi,
                     SSLSocketFactory sslSocketFactory, ExecutorService executorService) {

        if (session == null) {
            throw new IllegalArgumentException("Session must not be null.");
        }

        this.services = new ConcurrentHashMap<Class, Object>();

        final Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(new SafeListAdapter())
                .registerTypeAdapterFactory(new SafeMapAdapter())
                .create();

        adapter = new RestAdapter.Builder()
                .setClient(new AuthenticatedClient(authConfig, session, sslSocketFactory))
                .setEndpoint(twitterApi.getBaseHostUrl())
                .setConverter(new GsonConverter(gson))
                .setExecutors(executorService, new MainThreadExecutor())
                .build();
    }

    /**
     * Must be instantiated after {@link com.twitter.sdk.android.core.TwitterCore} has been
     * initialized via {@link io.fabric.sdk.android.Fabric#with(android.content.Context, io.fabric.sdk.android.Kit[])}.
     *
     * @param session Session to be used to create the API calls.
     *
     * @throws @{link java.lang.IllegalArgumentException} if TwitterSession argument is null
     */
    public TwitterApiClient(Session session) {
        this(TwitterCore.getInstance().getAuthConfig(), session, new TwitterApi(),
                TwitterCore.getInstance().getSSLSocketFactory(),
                TwitterCore.getInstance().getFabric().getExecutorService());
    }

    /**
     * @return {@link com.twitter.sdk.android.core.services.AccountService} to access TwitterApi
     */
    public AccountService getAccountService() {
        return getService(AccountService.class);
    }

    /**
     * @return {@link com.twitter.sdk.android.core.services.FavoriteService} to access TwitterApi
     */
    public FavoriteService getFavoriteService() {
        return getService(FavoriteService.class);
    }

    /**
     * @return {@link com.twitter.sdk.android.core.services.StatusesService} to access TwitterApi
     */
    public StatusesService getStatusesService() {
        return getService(StatusesService.class);
    }

    /**
     * @return {@link com.twitter.sdk.android.core.services.SearchService} to access TwitterApi
     */
    public SearchService getSearchService() {
        return getService(SearchService.class);
    }

    /**
     * Use CollectionTimeline directly, CollectionService is expected to change.
     * @return {@link com.twitter.sdk.android.core.internal.CollectionService} to access TwitterApi
     */
    public CollectionService getCollectionService() {
        return getService(CollectionService.class);
    }

    /**
     * Converts Retrofit style interface into instance for API access
     *
     * @param cls Retrofit style interface
     * @return instance of cls
     */
    @SuppressWarnings("unchecked")
    protected <T> T getService(Class<T> cls) {
        if (!services.contains(cls)) {
            services.putIfAbsent(cls, adapter.create(cls));
        }
        return (T) services.get(cls);
    }
}
