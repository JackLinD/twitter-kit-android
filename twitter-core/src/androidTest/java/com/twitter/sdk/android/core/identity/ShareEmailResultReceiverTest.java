package com.twitter.sdk.android.core.identity;

import android.os.Bundle;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAndroidTestCase;
import com.twitter.sdk.android.core.TwitterException;

import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ShareEmailResultReceiverTest extends TwitterAndroidTestCase {

    private static final String TEST_EMAIL_ADDRESS = "test@test.com";
    private static final String TEST_MESSAGE = "test message";
    private static final String TEST_EXCEPTION_MESSAGE = "test exception message";
    private static final int TEST_RESULT_CODE_UNKNOWN = -1;

    private Callback<String> mockCallback;
    private ArgumentCaptor<Result> resultArgCaptor;
    private ArgumentCaptor<TwitterException> exceptionArgCaptor;
    private ShareEmailResultReceiver resultReceiver;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mockCallback = mock(Callback.class);
        resultArgCaptor = ArgumentCaptor.forClass(Result.class);
        exceptionArgCaptor = ArgumentCaptor.forClass(TwitterException.class);

        resultReceiver = new ShareEmailResultReceiver(mockCallback);
    }

    public void testConstructor_nullCallback() {
        try {
            new ShareEmailResultReceiver(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Callback must not be null", e.getLocalizedMessage());
        }
    }

    public void testOnReceiveResult_resultCodeOk() {
        final Bundle resultData = new Bundle();
        resultData.putString(ShareEmailClient.RESULT_DATA_EMAIL, TEST_EMAIL_ADDRESS);
        resultReceiver.onReceiveResult(ShareEmailClient.RESULT_CODE_OK, resultData);

        verify(mockCallback).success(resultArgCaptor.capture());
        assertEquals(TEST_EMAIL_ADDRESS, (String) resultArgCaptor.getValue().data);
    }

    public void testOnReceiveResult_resultCodeCanceled() {
        final Bundle resultData = new Bundle();
        resultData.putString(ShareEmailClient.RESULT_DATA_MSG, TEST_MESSAGE);
        resultReceiver.onReceiveResult(ShareEmailClient.RESULT_CODE_CANCELED, resultData);

        verify(mockCallback).failure(exceptionArgCaptor.capture());
        assertEquals(TEST_MESSAGE, exceptionArgCaptor.getValue().getLocalizedMessage());
    }

    public void testOnReceiveResult_resultCodeError() {
        final TwitterException exception = new TwitterException(TEST_EXCEPTION_MESSAGE);
        final Bundle resultData = new Bundle();
        resultData.putSerializable(ShareEmailClient.RESULT_DATA_ERROR, exception);
        resultReceiver.onReceiveResult(ShareEmailClient.RESULT_CODE_ERROR, resultData);

        verify(mockCallback).failure(exceptionArgCaptor.capture());
        assertEquals(TEST_EXCEPTION_MESSAGE, exceptionArgCaptor.getValue().getLocalizedMessage());
    }

    public void testOnReceiveResult_resultCodeUnknown() {
        try {
            resultReceiver.onReceiveResult(TEST_RESULT_CODE_UNKNOWN, new Bundle());
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid result code " + TEST_RESULT_CODE_UNKNOWN,
                    e.getLocalizedMessage());
        }
    }
}
