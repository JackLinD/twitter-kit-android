package com.digits.sdk.android;

import android.os.Bundle;
import android.text.Editable;

import com.twitter.sdk.android.core.Result;

import org.mockito.ArgumentCaptor;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PinCodeControllerTests extends DigitsControllerTests<PinCodeController> {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        controller = new PinCodeController(resultReceiver, sendButton, phoneEditText,
                sessionManager, digitsClient, REQUEST_ID, USER_ID, PHONE_WITH_COUNTRY_CODE,
                errors, new ActivityClassManagerImp());
    }

    @Override
    public void testShowTOS() throws Exception {
        controller.showTOS(context);
        verifyNoInteractions(context);
    }

    public void testExecuteRequest_success() throws Exception {
        final DigitsCallback<DigitsSessionResponse> callback = executeRequest();
        final DigitsSessionResponse response = DigitsSessionTests.getNewDigitsSessionResponse();
        final Result<DigitsSessionResponse> result = new Result(response, null);
        callback.success(result);
        verify(sessionManager).setActiveSession(DigitsSession.create(response));
        verify(sendButton).showFinish();
        final ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass
                (Runnable.class);
        verify(phoneEditText).postDelayed(runnableArgumentCaptor.capture(),
                eq(DigitsControllerImpl.POST_DELAY_MS));
        final Runnable runnable = runnableArgumentCaptor.getValue();
        runnable.run();

        final ArgumentCaptor<Bundle> bundleArgumentCaptor = ArgumentCaptor.forClass(Bundle.class);
        verify(resultReceiver).send(eq(LoginResultReceiver.RESULT_OK),
                bundleArgumentCaptor.capture());
        assertEquals(PHONE_WITH_COUNTRY_CODE, bundleArgumentCaptor.getValue().getString
                (DigitsClient.EXTRA_PHONE));
    }

    @Override
    DigitsCallback executeRequest() {
        when(phoneEditText.getText()).thenReturn(Editable.Factory.getInstance().newEditable(CODE));
        final ArgumentCaptor<DigitsCallback> callbackArgumentCaptor = ArgumentCaptor.forClass
                (DigitsCallback.class);
        controller.executeRequest(context);
        verify(sendButton).showProgress();
        verify(digitsClient).verifyPin(eq(REQUEST_ID), eq(USER_ID), eq(CODE),
                callbackArgumentCaptor.capture());
        return callbackArgumentCaptor.getValue();
    }

    public void testValidateInput_null() throws Exception {
        assertFalse(controller.validateInput(null));
    }

    public void testValidateInput_empty() throws Exception {
        assertFalse(controller.validateInput(EMPTY_CODE));
    }
}
