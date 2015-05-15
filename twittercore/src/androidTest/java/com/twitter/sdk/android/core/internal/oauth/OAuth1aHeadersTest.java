package com.twitter.sdk.android.core.internal.oauth;


import io.fabric.sdk.android.FabricAndroidTestCase;
import io.fabric.sdk.android.services.network.HttpMethod;

import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;

import java.util.Map;

import static org.mockito.Mockito.mock;

public class OAuth1aHeadersTest extends FabricAndroidTestCase {
    private static final String VERIFY_CREDENTIALS_URL = "api.digits.com";
    private static final String ANY_AUTH_CREDENTIALS = "auth_credentials";
    private OAuth1aParameters oAuth1aParameters;
    private OAuth1aHeaders oAuthHeaders;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        oAuth1aParameters = new MockOAuth1aParameters();
        oAuthHeaders = new MockOAuth1aHeaders();
    }

    public void testGetOAuthEchoHeaders() throws Exception {
        final TwitterAuthConfig config = mock(TwitterAuthConfig.class);
        final TwitterAuthToken token = mock(TwitterAuthToken.class);

        final Map<String, String> headers = oAuthHeaders.getOAuthEchoHeaders(config, token, null,
                HttpMethod.GET.name(), VERIFY_CREDENTIALS_URL, null);
        assertEquals(VERIFY_CREDENTIALS_URL, headers.get(OAuth1aHeaders
                .HEADER_AUTH_SERVICE_PROVIDER));
        assertEquals(ANY_AUTH_CREDENTIALS, headers.get(OAuth1aHeaders
                .HEADER_AUTH_CREDENTIALS));
    }

    public void testGetAuthorizationHeader() throws Exception {
        final TwitterAuthConfig config = mock(TwitterAuthConfig.class);
        final TwitterAuthToken token = mock(TwitterAuthToken.class);

        assertEquals(ANY_AUTH_CREDENTIALS, oAuthHeaders.getAuthorizationHeader(config, token, null,
                HttpMethod.GET.name(), VERIFY_CREDENTIALS_URL, null));
    }

    private class MockOAuth1aParameters extends OAuth1aParameters {
        public MockOAuth1aParameters() {
            super(null, null, null, null, null, null);
        }

        @Override
        public String getAuthorizationHeader() {
            return ANY_AUTH_CREDENTIALS;
        }
    }

    private class MockOAuth1aHeaders extends OAuth1aHeaders {
        @Override
        OAuth1aParameters getOAuth1aParameters(TwitterAuthConfig authConfig, TwitterAuthToken
                authToken, String callback, String method, String url,
                Map<String, String> postParams) {
            return oAuth1aParameters;
        }
    }
}
