package com.twitter.sdk.android.core;

import com.twitter.sdk.android.core.internal.TwitterApi;
import com.twitter.sdk.android.core.internal.oauth.GuestAuthToken;
import com.twitter.sdk.android.core.internal.oauth.OAuth2Service;
import com.twitter.sdk.android.core.internal.oauth.OAuth2Token;

import javax.net.ssl.SSLSocketFactory;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class GuestAuthClientTest extends TwitterAndroidTestCase {
    private TwitterCore mockTwitterCore;
    private TwitterApi mockTwitterApi;
    private OAuth2Service fakeOAuth2Service;
    private SessionManager<AppSession> appSessionManager;
    private GuestAuthClient guestAuthClient;
    private Callback<AppSession> mockCallback;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockTwitterCore = mock(TwitterCore.class);
        mockTwitterApi = new TwitterApi();
        appSessionManager = new SimpleSessionManager<>();
        mockCallback = mock(Callback.class);
    }

    public void testConstructor_nullService() {
        try {
            guestAuthClient = new GuestAuthClient(null);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("OAuth2Service must not be null", e.getMessage());
        }
    }

    public void testAuthorize_nullAppSessionManager() {
        fakeOAuth2Service = new FakeSuccessOAuth2Service(mockTwitterCore, null, mockTwitterApi);
        guestAuthClient = new GuestAuthClient(fakeOAuth2Service);
        try {
            guestAuthClient.authorize(null, mockCallback);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("SessionManager must not be null", e.getMessage());
        }
    }

    public void testAuthorize_serviceSuccess() {
        fakeOAuth2Service = new FakeSuccessOAuth2Service(mockTwitterCore, null, mockTwitterApi);
        guestAuthClient = new GuestAuthClient(fakeOAuth2Service);
        assertNull(appSessionManager.getActiveSession());
        guestAuthClient.authorize(appSessionManager, mockCallback);
        // assert an AppSession was set in the AppSessionManager and made primary
        assertNotNull(appSessionManager.getActiveSession());
        // assert that GuestAuthClient invokes the success callback with a Result
        verify(mockCallback, times(1)).success(any(Result.class));
    }

    public void testAuthorize_serviceSuccessNullCallback() {
        fakeOAuth2Service = new FakeSuccessOAuth2Service(mockTwitterCore, null, mockTwitterApi);
        guestAuthClient = new GuestAuthClient(fakeOAuth2Service);
        assertNull(appSessionManager.getActiveSession());
        guestAuthClient.authorize(appSessionManager, null);
        // assert an AppSession was set in the AppSessionManager and made primary
        assertNotNull(appSessionManager.getActiveSession());
        // assert that GuestAuthClient does NOT call the success method on a null callback
        verifyZeroInteractions(mockCallback);
    }

    public void testAuthorize_serviceFailure() {
        fakeOAuth2Service = new FakeFailureOAuth2Service(mockTwitterCore, null, mockTwitterApi);
        guestAuthClient = new GuestAuthClient(fakeOAuth2Service);
        guestAuthClient.authorize(appSessionManager, mockCallback);
        // assert that GuestAuthClient invokes the failure callback when service fails to get auth
        verify(mockCallback, times(1)).failure(any(TwitterException.class));
    }

    public void testAuthorize_serviceFailureNullCallback() {
        fakeOAuth2Service = new FakeFailureOAuth2Service(mockTwitterCore, null, mockTwitterApi);
        guestAuthClient = new GuestAuthClient(fakeOAuth2Service);
        guestAuthClient.authorize(appSessionManager, null);
        // assert that GuestAuthClient does NOT call the failure method on a null callback
        verifyZeroInteractions(mockCallback);
    }

    /**
     * Fakes an OAuth2Service where network requests for guest or app auth tokens succeed.
     */
    class FakeSuccessOAuth2Service extends OAuth2Service {

        FakeSuccessOAuth2Service(TwitterCore twitterCore, SSLSocketFactory sslSocketFactory,
                          TwitterApi api) {
            super(twitterCore, sslSocketFactory, api);
        }

        @Override
        public void requestGuestOrAppAuthToken(Callback<OAuth2Token> callback) {
            final GuestAuthToken guestAuthToken = mock(GuestAuthToken.class);
            callback.success(new Result<OAuth2Token>(guestAuthToken, null));
        }
    }

    /**
     * Fakes an OAuth2Service where network requests for auth tokens fail.
     */
    class FakeFailureOAuth2Service extends OAuth2Service {

        FakeFailureOAuth2Service(TwitterCore twitterCore, SSLSocketFactory sslSocketFactory,
            TwitterApi api) {
            super(twitterCore, sslSocketFactory, api);
        }

        @Override
        public void requestGuestOrAppAuthToken(Callback<OAuth2Token> callback) {
            callback.failure(new TwitterException("fake exception"));
        }
    }
}
