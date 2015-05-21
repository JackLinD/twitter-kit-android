package com.twitter.sdk.android.tweetcomposer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.FabricAndroidTestCase;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TweetComposerTest extends FabricAndroidTestCase {

    public void testBuilder_constructor() {
        final TweetComposer.Builder builder = new TweetComposer.Builder(getContext());
        assertNotNull(builder);
    }

    public void testBuilder_constructorNullContext() {
        try {
            new TweetComposer.Builder(null);
            fail();
        } catch (IllegalArgumentException ignored) {

        }
    }

    public void testBuilder_text() {
        final Context context = createIntentContext(true);
        final String text = "test";
        final TweetComposer.Builder builder = new TweetComposer.Builder(context).text(text);
        final Intent intent = builder.createTwitterIntent();
        assertEquals(text, intent.getStringExtra(Intent.EXTRA_TEXT));
    }

    public void testBuilder_textNull() {
        try {
            new TweetComposer.Builder(getContext()).text(null);
            fail();
        } catch (IllegalArgumentException ignored) {
            assertEquals("text must not be null.", ignored.getMessage());
        }
    }

    public void testBuilder_textAlreadySet() {
        final String text = "test";
        try {
            new TweetComposer.Builder(getContext()).text(text).text(text);
            fail();
        } catch (IllegalStateException ignored) {
            assertEquals("text already set.", ignored.getMessage());
        }
    }

    public void testBuilder_textAndUrl() throws MalformedURLException {
        final Context context = createIntentContext(true);
        final String text = "test";
        final URL url = new URL("http://www.twitter.com");

        final String result = text + " " + url.toString();
        final TweetComposer.Builder builder = new TweetComposer.Builder(context)
                .text(text)
                .url(url);
        final Intent intent = builder.createTwitterIntent();
        assertEquals(result, intent.getStringExtra(Intent.EXTRA_TEXT));
    }

    public void testBuilder_url() throws MalformedURLException {
        final Context context = createIntentContext(true);
        final URL url = new URL("http://www.twitter.com");
        final TweetComposer.Builder builder = new TweetComposer.Builder(context).url(url);
        final Intent intent = builder.createTwitterIntent();
        assertEquals(url.toString(), intent.getStringExtra(Intent.EXTRA_TEXT));
    }

    public void testBuilder_urlNull() {
        try {
            new TweetComposer.Builder(getContext()).url(null);
            fail();
        } catch (IllegalArgumentException ignored) {
            assertEquals("url must not be null.", ignored.getMessage());
        }
    }

    public void testBuilder_urlAlreadySet() throws MalformedURLException {
        final URL url = new URL("http://www.twitter.com");
        try {
            new TweetComposer.Builder(getContext()).url(url).url(url);
            fail();
        } catch (IllegalStateException ignored) {
            assertEquals("url already set.", ignored.getMessage());
        }
    }

    public void testBuilder_image() {
        final Context context = createIntentContext(true);
        final Uri uri = Uri.parse("http://www.twitter.com");
        final TweetComposer.Builder builder = new TweetComposer.Builder(context).image(uri);
        final Intent intent = builder.createTwitterIntent();
        assertEquals(uri, intent.getParcelableExtra(Intent.EXTRA_STREAM));
    }

    public void testBuilder_imageNull() {
        try {
            new TweetComposer.Builder(getContext()).image(null);
            fail();
        } catch (IllegalArgumentException ignored) {
            assertEquals("imageUri must not be null.", ignored.getMessage());
        }
    }

    public void testBuilder_imageAlreadySet() {
        final Uri uri = Uri.parse("http://www.twitter.com");
        try {
            new TweetComposer.Builder(getContext()).image(uri).image(uri);
            fail();
        } catch (IllegalStateException ignored) {
            assertEquals("imageUri already set.", ignored.getMessage());
        }
    }

    public void testBuilder_createIntentTwitterInstalled() {
        final Context context = createIntentContext(true);
        final TweetComposer.Builder builder = new TweetComposer.Builder(context);
        final Intent intentTwitter = builder.createTwitterIntent();
        final Intent intent = builder.createIntent();

        assertNotNull(intent);
        assertNotNull(intentTwitter);
        assertIntentEquals(intentTwitter, intent);
    }

    public void testBuilder_createIntentTwitterNotInstalled() {
        final Context context = createIntentContext(false);
        final TweetComposer.Builder builder = new TweetComposer.Builder(context);
        final Intent intent = builder.createIntent();
        final Intent intentTwitter = builder.createTwitterIntent();
        final Intent intentWeb = builder.createWebIntent();

        assertNotNull(intent);
        assertNull(intentTwitter);
        assertIntentEquals(intentWeb, intent);
    }

    public void testBuilder_show() {
        final Context context = createIntentContext(true);
        final TweetComposer.Builder builder = new TweetComposer.Builder(context);
        builder.show();

        verify(context).startActivity(any(Intent.class));
    }

    public void testGetVersion() {
        final TweetComposer composer = new TweetComposer();
        final String version = BuildConfig.VERSION_NAME + "." + BuildConfig.BUILD_NUMBER;
        assertEquals(version, composer.getVersion());
    }

    public void testDoInBackground() {
        final TweetComposer composer = new TweetComposer();
        assertTrue(composer.doInBackground());
    }

    public void testGetIdentifier() {
        final TweetComposer composer = new TweetComposer();
        final String identifier = BuildConfig.GROUP + ":" + BuildConfig.ARTIFACT_ID;
        assertEquals(identifier, composer.getIdentifier());
    }

    private Context createIntentContext(boolean twitterInstalled) {

        final List<ResolveInfo> resolveInfoList = new ArrayList<ResolveInfo>();
        final ResolveInfo info = new ResolveInfo();

        info.activityInfo = new ActivityInfo();
        if (twitterInstalled) {
            info.activityInfo.packageName = "com.twitter.android";
            info.activityInfo.name = "Twitter";
        } else {
            info.activityInfo.packageName = "not.twitter.android";
            info.activityInfo.name = "NotTwitter";
        }
        resolveInfoList.add(info);

        final Context context = mock(Context.class);
        final PackageManager manager = mock(PackageManager.class);

        when(context.getPackageManager()).thenReturn(manager);
        when(manager.queryIntentActivities(any(Intent.class), anyInt()))
                .thenReturn(resolveInfoList);
        return context;
    }

    private void assertIntentEquals(Intent intent, Intent otherIntent) {
        assertEquals(intent.getType(), otherIntent.getType());
        assertEquals(intent.getAction(), otherIntent.getAction());
        assertEquals(intent.getStringExtra(Intent.EXTRA_TEXT),
                otherIntent.getStringExtra(Intent.EXTRA_TEXT));
        assertEquals(intent.getStringExtra(intent.getStringExtra(Intent.EXTRA_STREAM)),
                otherIntent.getStringExtra(Intent.EXTRA_STREAM));
    }
}
