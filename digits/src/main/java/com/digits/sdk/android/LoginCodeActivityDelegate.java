package com.digits.sdk.android;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import io.fabric.sdk.android.services.common.CommonUtils;

class LoginCodeActivityDelegate extends DigitsActivityDelegateImpl {
    EditText editText;
    StateButton stateButton;
    TextView termsText;
    DigitsController controller;
    SmsBroadcastReceiver receiver;
    Activity activity;
    boolean tosUpdated;

    @Override
    public void init(Activity activity, Bundle bundle) {
        this.activity = activity;
        editText = (EditText) activity.findViewById(R.id.dgts__confirmationEditText);
        stateButton = (StateButton) activity.findViewById(R.id.dgts__createAccount);
        termsText = (TextView) activity.findViewById(R.id.dgts__termsTextCreateAccount);
        final TextView resendText = (TextView) activity.findViewById(R.id.dgts__resendConfirmation);
        tosUpdated = bundle.getBoolean(DigitsClient.EXTRA_TOS_UPDATED, false);

        controller = initController(bundle);

        setUpEditText(activity, controller, editText);
        setUpSendButton(activity, controller, stateButton);
        setUpTermsText(activity, controller, termsText);
        setUpResendText(activity, resendText);
        setUpSmsIntercept(activity, editText);

        CommonUtils.openKeyboard(activity, editText);
    }

    DigitsController initController(Bundle bundle) {
        return new LoginCodeController(bundle
                .<ResultReceiver>getParcelable(DigitsClient.EXTRA_RESULT_RECEIVER),
                stateButton, editText, bundle.getString(DigitsClient.EXTRA_REQUEST_ID),
                bundle.getLong(DigitsClient.EXTRA_USER_ID), bundle.getString(DigitsClient
                .EXTRA_PHONE));
    }

    @Override
    public void setUpTermsText(Activity activity, DigitsController controller, TextView termsText) {
        if (tosUpdated) {
            termsText.setText(getFormattedTerms(activity, R.string.dgts__terms_text_sign_in));
            super.setUpTermsText(activity, controller, termsText);
        } else {
            termsText.setVisibility(View.GONE);
        }
    }

    @Override
    public void setUpSendButton(Activity activity, DigitsController controller,
            StateButton stateButton) {
        stateButton.setStatesText(R.string.dgts__sign_in, R.string.dgts__signing_in,
                R.string.dgts__sign_in);
        stateButton.showStart();
        super.setUpSendButton(activity, controller, stateButton);
    }

    protected void setUpResendText(final Activity activity, TextView resendText) {
        resendText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.dgts__activity_confirmation;
    }

    @Override
    public boolean isValid(Bundle bundle) {
        return BundleManager.assertContains(bundle, DigitsClient.EXTRA_RESULT_RECEIVER,
                DigitsClient.EXTRA_PHONE, DigitsClient.EXTRA_REQUEST_ID,
                DigitsClient.EXTRA_USER_ID);
    }

    @Override
    public void onResume() {
        controller.onResume();
    }

    @Override
    public void onDestroy() {
        if (receiver != null) {
            activity.unregisterReceiver(receiver);
        }
    }

    protected void setUpSmsIntercept(Activity activity, EditText editText) {
        if (CommonUtils.checkPermission(activity, "android.permission.RECEIVE_SMS")) {
            final IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
            receiver = new SmsBroadcastReceiver(editText);
            activity.registerReceiver(receiver, filter);
        }
    }
}
