package com.digits.sdk.android;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;


public abstract class DigitsActivityDelegateTests<T extends DigitsActivityDelegateImpl> extends
        DigitsAndroidTestCase {
    private static final int ANY_CODE = 0;
    private static final int ANY_ACTION = 1;
    T delegate;
    Activity activity;
    DigitsController controller;
    StateButton button;
    ArgumentCaptor<View.OnClickListener> captorClick;
    ArgumentCaptor<TextView.OnEditorActionListener> captorEditor;
    EditText editText;
    TextView textView;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        delegate = getDelegate();
        activity = mock(Activity.class);
        controller = mock(DigitsController.class);
        button = mock(StateButton.class);
        captorClick = ArgumentCaptor.forClass(View.OnClickListener.class);
        captorEditor = ArgumentCaptor.forClass(TextView.OnEditorActionListener.class);
        editText = mock(EditText.class);
        textView = mock(TextView.class);

    }

    public abstract T getDelegate();

    public void testSetUpSendButton() throws Exception {
        delegate.setUpSendButton(activity, controller, button);

        verify(button).setOnClickListener(captorClick.capture());
        final View.OnClickListener listener = captorClick.getValue();
        listener.onClick(null);
        verify(controller).clearError();
        verify(controller).executeRequest(activity);
    }

    public void testSetUpEditText_nextAction() throws Exception {
        delegate.setUpEditText(activity, controller, editText);

        verify(editText).setOnEditorActionListener(captorEditor.capture());
        final TextView.OnEditorActionListener listener = captorEditor.getValue();
        listener.onEditorAction(editText, EditorInfo.IME_ACTION_NEXT, new KeyEvent(ANY_ACTION,
                ANY_CODE));
        verify(controller).clearError();
        verify(controller).executeRequest(activity);
        verify(controller).getTextWatcher();
    }

    public void testSetUpEditText_noNextAction() throws Exception {
        delegate.setUpEditText(activity, controller, editText);

        verify(editText).setOnEditorActionListener(captorEditor.capture());
        final TextView.OnEditorActionListener listener = captorEditor.getValue();
        listener.onEditorAction(editText, EditorInfo.IME_ACTION_DONE, new KeyEvent(ANY_ACTION,
                ANY_CODE));
        verify(controller).getTextWatcher();
        verifyNoMoreInteractions(controller);
    }

    public void testSetUpTermsText() throws Exception {
        delegate.setUpTermsText(activity, controller, textView);
        verify(textView).setOnClickListener(captorClick.capture());
        final View.OnClickListener listener = captorClick.getValue();
        listener.onClick(null);
        verify(controller).clearError();
        verify(controller).showTOS(activity);
    }

}
