package com.digits.sdk.android;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;

import org.mockito.ArgumentCaptor;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FailureControllerImplTests extends DigitsAndroidTestCase {
    private static final String RANDOM_ERROR_MSG = "Random error message";
    FailureControllerImpl controller;
    ArgumentCaptor<Intent> intentArgumentCaptor;
    ArgumentCaptor<Bundle> bundleArgumentCaptor;
    Activity activity;
    ResultReceiver receiver;
    DigitsException exception;
    Class<?> activityClass;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        activity = mock(Activity.class);
        intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);
        bundleArgumentCaptor = ArgumentCaptor.forClass(Bundle.class);
        receiver = mock(ResultReceiver.class);
        exception = mock(DigitsException.class);
        controller = new FailureControllerImpl(new ActivityClassManagerImp());
        activityClass = controller.classManager.getPhoneNumberActivity();

        when(activity.getPackageName()).thenReturn(activityClass.getPackage().toString());
    }

    public void testTryAnotherNumber() {
        controller.tryAnotherNumber(activity, receiver);

        verify(activity).startActivity(intentArgumentCaptor.capture());
        final Intent intent = intentArgumentCaptor.getValue();
        verifyFlags(intent.getFlags());
        final Bundle bundle = intent.getExtras();
        assertTrue(BundleManager.assertContains(bundle, DigitsClient.EXTRA_RESULT_RECEIVER));
        final ComponentName activityComponent = new ComponentName(activity, activityClass);
        assertEquals(activityComponent, intent.getComponent());
    }

    public void testSendFailure() {
        when(exception.getLocalizedMessage()).thenReturn(RANDOM_ERROR_MSG);

        controller.sendFailure(receiver, exception);

        verify(receiver).send(eq(LoginResultReceiver.RESULT_ERROR), bundleArgumentCaptor.capture());
        final Bundle bundle = bundleArgumentCaptor.getValue();
        assertEquals(RANDOM_ERROR_MSG, bundle.getString(LoginResultReceiver.KEY_ERROR));
    }

    public void testFlags() {
        verifyFlags(controller.getFlags());
    }

    void verifyFlags(int actual) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            final int expected = Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK;
            assertEquals(expected, actual);
        } else {
            final int expected = Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK;
            assertEquals(expected, actual);
        }
    }
}
