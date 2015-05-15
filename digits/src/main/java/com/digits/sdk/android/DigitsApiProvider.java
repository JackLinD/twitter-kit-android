package com.digits.sdk.android;


import com.twitter.sdk.android.core.AuthenticatedClient;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.SSLSocketFactory;

import retrofit.RestAdapter;
import retrofit.android.MainThreadExecutor;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

class DigitsApiProvider {
    private final ConcurrentHashMap<Class, Object> services;
    private final RestAdapter restAdapter;


    DigitsApiProvider(DigitsSession session, TwitterAuthConfig authConfig,
            SSLSocketFactory sslFactory, ExecutorService executorService,
            DigitsUserAgent userAgent) {
        this.services = new ConcurrentHashMap<>();
        this.restAdapter = new RestAdapter.Builder()
                .setEndpoint(new DigitsApi().getBaseHostUrl())
                .setRequestInterceptor(new DigitsRequestInterceptor(userAgent))
                .setExecutors(executorService, new MainThreadExecutor())
                .setClient(new AuthenticatedClient(authConfig, session, sslFactory))
                .build();
    }


    public SdkService getSdkService() {
        return getService(SdkService.class);
    }

    public DeviceService getDeviceService() {
        return getService(DeviceService.class);
    }

    private <T> T getService(Class<T> cls) {
        if (!services.containsKey(cls)) {
            services.put(cls, restAdapter.create(cls));
        }
        return (T) services.get(cls);
    }


    protected interface DeviceService {
        @FormUrlEncoded
        @POST("/1.1/device/register.json")
        void register(@Field("raw_phone_number") String rawPhoneNumber,
                             @Field("text_key") String textKey,
                             @Field("send_numeric_pin") Boolean sendNumericPin,
                             Callback<DeviceRegistrationResponse> cb);
    }

    protected interface SdkService {
        @FormUrlEncoded
        @POST("/1.1/sdk/account.json")
        void account(@Field("phone_number") String phoneNumber,
                            @Field("numeric_pin") String numericPin,
                            Callback<DigitsUser> cb);

        @FormUrlEncoded
        @POST("/1/sdk/login")
        void auth(@Field("x_auth_phone_number") String phoneNumber,
                         Callback<AuthResponse> cb);

        @FormUrlEncoded
        @POST("/auth/1/xauth_challenge.json")
        void login(@Field("login_verification_request_id") String requestId,
                          @Field("login_verification_user_id") long userId,
                          @Field("login_verification_challenge_response") String code,
                          Callback<DigitsSessionResponse> cb);

        @FormUrlEncoded
        @POST("/auth/1/xauth_pin.json")
        void verifyPin(@Field("login_verification_request_id") String requestId,
                       @Field("login_verification_user_id") long userId,
                       @Field("pin") String pin,
                       Callback<DigitsSessionResponse> cb);
    }
}
