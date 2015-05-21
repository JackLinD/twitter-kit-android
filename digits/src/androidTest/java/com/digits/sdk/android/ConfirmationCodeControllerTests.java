package com.digits.sdk.android;

import android.os.Bundle;
import android.text.Editable;

import org.apache.http.HttpStatus;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;

import retrofit.client.Header;
import retrofit.client.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConfirmationCodeControllerTests extends
        DigitsControllerTests<ConfirmationCodeController> {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        controller = new ConfirmationCodeController(resultReceiver, sendButton,
                phoneEditText, PHONE_WITH_COUNTRY_CODE, sessionManager, digitsClient, errors,
                new ActivityClassManagerImp());
    }

    public void testExecuteRequest_success() throws Exception {
        final DigitsCallback callback = executeRequest();
        final Response response = new Response(TWITTER_URL, HttpStatus.SC_ACCEPTED, "",
                new ArrayList<Header>(), null);
        final DigitsUser user = new DigitsUser(USER_ID, "");
        callback.success(user, response);
        verify(sessionManager).setActiveSession(any(DigitsSession.class));
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

    DigitsCallback executeRequest() {
        when(phoneEditText.getText()).thenReturn(Editable.Factory.getInstance().newEditable
                (CODE));
        controller.executeRequest(context);
        verify(sendButton).showProgress();
        final ArgumentCaptor<DigitsCallback> argumentCaptor = ArgumentCaptor.forClass
                (DigitsCallback.class);
        verify(digitsClient).createAccount(eq(CODE), eq(PHONE_WITH_COUNTRY_CODE),
                argumentCaptor.capture());
        assertNotNull(argumentCaptor.getValue());
        return argumentCaptor.getValue();
    }

}
