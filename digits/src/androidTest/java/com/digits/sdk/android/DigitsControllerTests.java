package com.digits.sdk.android;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.widget.EditText;

import com.twitter.sdk.android.core.SessionManager;
import com.twitter.sdk.android.core.TwitterApiErrorConstants;
import com.twitter.sdk.android.core.TwitterException;

import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public abstract class DigitsControllerTests<T extends DigitsControllerImpl> extends
        DigitsAndroidTestCase {
    static final String ERROR_MESSAGE = "random error";
    static final Integer COUNTRY_CODE = 123;
    static final String PHONE = "123456789";
    static final String PHONE_WITH_COUNTRY_CODE = "+" + COUNTRY_CODE + "123456789";
    static final String CODE = "123456";
    static final String EMPTY_CODE = "";
    static final long USER_ID = 1234567;
    static final String REQUEST_ID = "881984";


    T controller;
    EditText phoneEditText;
    StateButton sendButton;
    DigitsClient digitsClient;
    ArgumentCaptor<Intent> intentCaptor;
    ArgumentCaptor<DigitsCallback> callbackCaptor;
    ArgumentCaptor<Bundle> bundleCaptor;
    ResultReceiver resultReceiver;
    ErrorCodes errors;
    SessionManager<DigitsSession> sessionManager;
    Activity context;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        bundleCaptor = ArgumentCaptor.forClass(Bundle.class);
        callbackCaptor = ArgumentCaptor.forClass(DigitsCallback.class);
        intentCaptor = ArgumentCaptor.forClass(Intent.class);
        phoneEditText = mock(EditText.class);
        sendButton = mock(StateButton.class);
        digitsClient = mock(DigitsClient.class);
        context = mock(Activity.class);
        resultReceiver = mock(ResultReceiver.class);
        sessionManager = mock(SessionManager.class);
        errors = mock(ErrorCodes.class);
        when(context.getPackageName()).thenReturn(getClass().getPackage().toString());
    }

    public void testShowTOS() throws Exception {
        controller.showTOS(context);
        verify(context).startActivity(intentCaptor.capture());
        final Intent intent = intentCaptor.getValue();
        assertEquals(controller.getTOSUri(), intent.getData());
        assertEquals(Intent.ACTION_VIEW, intent.getAction());
    }

    public void testHandleError() throws Exception {
        controller.handleError(context, new DigitsException(ERROR_MESSAGE));
        verify(phoneEditText).setError(ERROR_MESSAGE);
        verify(sendButton).showError();
        verifyZeroInteractions(context);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void testShowError_fiveTimesStartFallback() throws Exception {
        controller.handleError(context, new DigitsException(ERROR_MESSAGE));
        controller.handleError(context, new DigitsException(ERROR_MESSAGE));
        controller.handleError(context, new DigitsException(ERROR_MESSAGE));
        controller.handleError(context, new DigitsException(ERROR_MESSAGE));
        controller.handleError(context, new DigitsException(ERROR_MESSAGE));

        verify(phoneEditText, atMost(4)).setError(ERROR_MESSAGE);
        verify(sendButton, atMost(4)).showError();
        verify(context).startActivity(intentCaptor.capture());
        final Intent intent = intentCaptor.getValue();
        assertEquals(FailureActivity.class.getName(), intent.getComponent().getClassName());
        assertEquals(resultReceiver, intent.getExtras().get(DigitsClient.EXTRA_RESULT_RECEIVER));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            verify(context).finishAffinity();
        } else {
            verify(context).finish();
        }
    }

    public void testHandleError_unrecoverableExceptionStartFallback() throws Exception {
        controller.handleError(context, new UnrecoverableException(ERROR_MESSAGE));
        verifyNoInteractions(sendButton, phoneEditText);
        verify(context).startActivity(intentCaptor.capture());
        final Intent intent = intentCaptor.getValue();
        assertEquals(FailureActivity.class.getName(), intent.getComponent().getClassName());
        assertEquals(resultReceiver, intent.getExtras().get(DigitsClient.EXTRA_RESULT_RECEIVER));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            verify(context).finishAffinity();
        } else {
            verify(context).finish();
        }
    }

    public void testStartFallback() throws Exception {
        controller.startFallback(context, resultReceiver, new DigitsException("",
                TwitterApiErrorConstants.USER_IS_NOT_SDK_USER));
        verify(context).startActivity(intentCaptor.capture());
        final Intent intent = intentCaptor.getValue();
        assertEquals(FailureActivity.class.getName(), intent.getComponent().getClassName());
        assertEquals(resultReceiver, intent.getExtras().get(DigitsClient.EXTRA_RESULT_RECEIVER));

        final DigitsException reason = (DigitsException) intent.getExtras().get(DigitsClient
                .EXTRA_FALLBACK_REASON);
        assertEquals(TwitterApiErrorConstants.USER_IS_NOT_SDK_USER, reason.getErrorCode());
    }


    public void testOnResume() throws Exception {
        controller.onResume();
        verify(sendButton).showStart();
    }

    public void testClearError() throws Exception {
        controller.clearError();
        verify(phoneEditText).setError(null);
        verifyNoInteractions(sendButton, digitsClient);
    }

    public void testAfterTextChanged() throws Exception {
        controller.onTextChanged(null, 0, 0, 0);
        verify(phoneEditText).setError(null);
        verifyNoInteractions(sendButton, digitsClient);
    }

    public void testExecuteRequest_failure() throws Exception {
        when(errors.getDefaultMessage()).thenReturn(ERROR_MESSAGE);
        final DigitsCallback callback = executeRequest();
        callback.failure(new TwitterException(ERROR_MESSAGE));
        verify(phoneEditText).setError(ERROR_MESSAGE);
        verify(sendButton).showError();
    }

    public void testExecuteRequest_noInput() throws Exception {
        controller.executeRequest(context);
        verifyNoInteractions(sendButton);
        verifyNoInteractions(digitsClient);
    }

    abstract DigitsCallback executeRequest();

    public void testValidateInput_null() throws Exception {
        assertFalse(controller.validateInput(null));
    }

    public void testValidateInput_empty() throws Exception {
        assertFalse(controller.validateInput(EMPTY_CODE));
    }
}
