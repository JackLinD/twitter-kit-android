package com.digits.sdk.android;

import com.twitter.sdk.android.core.MockTwitterApiException;
import com.twitter.sdk.android.core.TwitterApiErrorConstants;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.ApiError;

import org.mockito.Mockito;

import retrofit.RetrofitError;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DigitsExceptionTests extends DigitsAndroidTestCase {

    private static final String RANDOM_ERROR = "Random error";
    private static final String KNOWN_ERROR_MESSAGE = "Something bad happened call batman";
    private static final String NETWORK_ERROR_MESSAGE = "network wonky";
    private static final String DEFAULT_ERROR_MESSAGE = "try again";
    private static final int SOME_ERROR_CODE = 0;
    private RetrofitError retrofitError;
    private ErrorCodes errorCodes;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        retrofitError = mock(RetrofitError.class);
        when(retrofitError.isNetworkError()).thenReturn(false);
        errorCodes = Mockito.mock(ErrorCodes.class);
        when(errorCodes.getMessage(TwitterApiErrorConstants.REGISTRATION_GENERAL_ERROR))
                .thenReturn(KNOWN_ERROR_MESSAGE);
        when(errorCodes.getMessage(TwitterApiErrorConstants.COULD_NOT_AUTHENTICATE))
                .thenReturn(KNOWN_ERROR_MESSAGE);
        when(errorCodes.getNetworkError()).thenReturn(NETWORK_ERROR_MESSAGE);
        when(errorCodes.getDefaultMessage()).thenReturn(DEFAULT_ERROR_MESSAGE);
        when(errorCodes.getMessage(SOME_ERROR_CODE)).thenReturn(DEFAULT_ERROR_MESSAGE);
    }

    public void testCreate_unknownError() throws Exception {
        final TwitterException exception = new TwitterException(RANDOM_ERROR);
        final DigitsException digitsException = DigitsException.create(errorCodes, exception);
        assertEquals(DEFAULT_ERROR_MESSAGE, digitsException.getLocalizedMessage());
        assertEquals(TwitterApiErrorConstants.UNKNOWN_ERROR, digitsException.getErrorCode());
    }


    public void testCreate_unknownErrorTwitterApiException() throws Exception {
        final TwitterException exception = new MockTwitterApiException(new ApiError("",
                SOME_ERROR_CODE), null, retrofitError);
        when(retrofitError.isNetworkError()).thenReturn(false);
        final DigitsException digitsException = DigitsException.create(errorCodes, exception);
        assertEquals(DEFAULT_ERROR_MESSAGE, digitsException.getLocalizedMessage());
        assertEquals(SOME_ERROR_CODE, digitsException.getErrorCode());
    }

    public void testCreate_networkError() throws Exception {
        final TwitterException exception = new MockTwitterApiException(new ApiError("",
                SOME_ERROR_CODE), null, retrofitError);
        when(retrofitError.isNetworkError()).thenReturn(true);
        final DigitsException digitsException = DigitsException.create(errorCodes, exception);
        assertEquals(NETWORK_ERROR_MESSAGE, digitsException.getLocalizedMessage());
        assertEquals(SOME_ERROR_CODE, digitsException.getErrorCode());
    }

    public void testCreate_knownError() throws Exception {
        final TwitterException exception = new MockTwitterApiException
                (new ApiError("", TwitterApiErrorConstants.REGISTRATION_GENERAL_ERROR), null,
                        retrofitError);
        when(retrofitError.isNetworkError()).thenReturn(false);
        final DigitsException digitsException = DigitsException.create(errorCodes, exception);
        assertEquals(KNOWN_ERROR_MESSAGE, digitsException.getLocalizedMessage());
        assertTrue(digitsException instanceof UnrecoverableException);
        assertEquals(TwitterApiErrorConstants.REGISTRATION_GENERAL_ERROR,
                digitsException.getErrorCode());
    }

    public void testCreate_couldNotAuthenticate() throws Exception {
        final TwitterException exception = new MockTwitterApiException
                (new ApiError("", TwitterApiErrorConstants.COULD_NOT_AUTHENTICATE), null,
                        retrofitError);
        when(retrofitError.isNetworkError()).thenReturn(false);
        final DigitsException digitsException = DigitsException.create(errorCodes, exception);
        assertEquals(KNOWN_ERROR_MESSAGE, digitsException.getLocalizedMessage());
        assertTrue(digitsException instanceof CouldNotAuthenticateException);
        assertEquals(TwitterApiErrorConstants.COULD_NOT_AUTHENTICATE,
                digitsException.getErrorCode());
    }

    public void testCreate_operatorUnsupported() throws Exception {
        verifyUnrecovarableException(TwitterApiErrorConstants.OPERATOR_UNSUPPORTED);
    }

    public void testCreate_notElegibleForOneFactor() throws Exception {
        verifyUnrecovarableException(TwitterApiErrorConstants.USER_IS_NOT_SDK_USER);
    }

    public void testCreate_expiredLoginVerification() throws Exception {
        verifyUnrecovarableException(TwitterApiErrorConstants.EXPIRED_LOGIN_VERIFICATION_REQUEST);
    }

    public void testCreate_missingLoginVerification() throws Exception {
        verifyUnrecovarableException(TwitterApiErrorConstants.MISSING_LOGIN_VERIFICATION_REQUEST);
    }

    public void testCreate_deviceRegistrationRate() throws Exception {
        verifyUnrecovarableException(TwitterApiErrorConstants.DEVICE_REGISTRATION_RATE_EXCEEDED);
    }

    public void verifyUnrecovarableException(int error) {
        final TwitterException exception = new MockTwitterApiException(new ApiError("", error),
                null, retrofitError);
        when(retrofitError.isNetworkError()).thenReturn(false);
        final DigitsException digitsException = DigitsException.create(errorCodes, exception);
        assertTrue(digitsException instanceof UnrecoverableException);
    }
}
