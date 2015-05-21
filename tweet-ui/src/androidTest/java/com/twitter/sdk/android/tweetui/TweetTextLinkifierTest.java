package com.twitter.sdk.android.tweetui;

import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;

import com.twitter.sdk.android.core.TwitterAndroidTestCase;
import com.twitter.sdk.android.core.models.MediaEntity;
import com.twitter.sdk.android.core.models.UrlEntity;

import org.easymock.EasyMock;

import java.util.ArrayList;
import java.util.List;

public class TweetTextLinkifierTest extends TwitterAndroidTestCase {
    static final String BASE_TEXT = "just setting up my twttr";
    static final EntityFactory entityFactory = new EntityFactory();

    public void testLinkifyUrls_nullFormattedTweetText() {
        try {
            TweetTextLinkifier.linkifyUrls(null, null, false, 0);
        } catch (Exception e) {
            fail("threw unexpected exception");
        }
    }

    public void testLinkifyUrls_newFormattedTweetText() {
        try {
            TweetTextLinkifier.linkifyUrls(new FormattedTweetText(), null, false, 0);
        } catch (Exception e) {
            fail("threw unexpected exception");
        }
    }

    public void testLinkifyUrls_oneUrlEntity() {
        final String url = "http://t.co/foo";
        final String displayUrl = "dev.twitter.com";
        final String fullText = BASE_TEXT + " " + "http://t.co/foo";
        final UrlEntity urlEntity
                = EntityFactory.newUrlEntity(fullText, url, displayUrl);

        final FormattedTweetText formattedText = new FormattedTweetText();
        formattedText.text = fullText;
        formattedText.urlEntities.add(new FormattedUrlEntity(urlEntity));

        final CharSequence linkifiedText
                = TweetTextLinkifier.linkifyUrls(formattedText, null, false, 0);
        final String displayUrlFromEntity =
                linkifiedText.subSequence(urlEntity.getStart(), urlEntity.getEnd()).toString();
        assertEquals(urlEntity.displayUrl, displayUrlFromEntity);
    }

    public void testLinkifyUrls_oneInvalidUrlEntity() {
        final String fullText = "";
        final UrlEntity urlEntity = new UrlEntity("x z", "y", "z", -1, 30);
        final FormattedTweetText formattedText = new FormattedTweetText();
        formattedText.text = fullText;
        formattedText.urlEntities.add(new FormattedUrlEntity(urlEntity));

        final CharSequence linkifiedText
                = TweetTextLinkifier.linkifyUrls(formattedText, null, false, 0);
        assertEquals("", linkifiedText.toString());
    }

    public void testLinkifyUrls_linkClickListener() {
        final String url = "http://t.co/foo";
        final String displayUrl = "dev.twitter.com";
        final String fullText = BASE_TEXT + " " + "http://t.co/foo";

        final LinkClickListener listenerMock = EasyMock.createMock(LinkClickListener.class);
        listenerMock.onUrlClicked(EasyMock.isA(String.class));
        EasyMock.replay(listenerMock);

        final UrlEntity urlEntity =
                EntityFactory.newUrlEntity(fullText, url, displayUrl);
        final FormattedTweetText formattedText = new FormattedTweetText();
        formattedText.text = fullText;
        formattedText.urlEntities.add(new FormattedUrlEntity(urlEntity));

        final SpannableStringBuilder linkifiedText = (SpannableStringBuilder)
                TweetTextLinkifier.linkifyUrls(formattedText, listenerMock, false, 0);
        final ClickableSpan[] clickables =
                linkifiedText.getSpans(urlEntity.getStart(), urlEntity.getEnd(),
                        ClickableSpan.class);
        assertEquals(1, clickables.length);
    }

    public void testLinkifyUrls_stripPhotoUrlTrue() {
        final FormattedTweetText formattedText = setupPicTwitterEntities();
        final FormattedMediaEntity lastPhotoUrl = formattedText.mediaEntities.get(0);
        final CharSequence linkifiedText
                = TweetTextLinkifier.linkifyUrls(formattedText, null, true, 0);

        // make sure we are stripping out a photo entity since it is the only media entity
        // that we can render inline
        assertEquals("photo", lastPhotoUrl.type);
        // assert that we do not strip it here and display it in the middle
        assertTrue(!linkifiedText.toString().contains(lastPhotoUrl.displayUrl));
    }

    public void testLinkifyUrls_stripPhotoUrlFalse() {
        final FormattedTweetText formattedText = setupPicTwitterEntities();
        final FormattedMediaEntity lastPhotoUrl = formattedText.mediaEntities.get(0);
        final CharSequence linkifiedText
                = TweetTextLinkifier.linkifyUrls(formattedText, null, false, 0);

        // make sure we are making assertions about the photo entity
        assertEquals("photo", lastPhotoUrl.type);
        // assert that we do not strip it here and display it in the middle
        assertTrue(linkifiedText.toString().contains(lastPhotoUrl.displayUrl));
    }

    private FormattedTweetText setupPicTwitterEntities() {
        final String text = "first link is a pictwitter http://t.co/PFHCdlr4i0 " +
                "http://t.co/V3hLRdFdeN final text";

        final MediaEntity mediaEntity = new MediaEntity("http://t.co/PFHCdlr4i0", null,
                "pic.twitter.com/abc", 27, 49, 0L, null, null, null, null, 0L, null, "photo");

        final UrlEntity urlEntity = new UrlEntity("http://t.co/PFHCdlr4i0", null, "example.com", 50,
                72);

        final FormattedTweetText formattedText = new FormattedTweetText();
        formattedText.text = text;
        formattedText.urlEntities.add(new FormattedUrlEntity(urlEntity));
        formattedText.mediaEntities.add(new FormattedMediaEntity(mediaEntity));

        return formattedText;
    }

    /*
     * mergeAndSortEntities method
     */
    public void testMergeAndSortEntities_nullMedia() {
        final List<FormattedUrlEntity> urls
                = new ArrayList<FormattedUrlEntity>();
        assertEquals(urls, TweetTextLinkifier.mergeAndSortEntities(urls, null));
    }

    public void testMergeAndSortEntities_sortUrlsAndMedia() {
        final List<FormattedUrlEntity> urls = new ArrayList<>();
        final UrlEntity url = TestFixtures.newUrlEntity(2, 5);
        final FormattedUrlEntity adjustedUrl = new FormattedUrlEntity(url);
        urls.add(adjustedUrl);

        final List<FormattedMediaEntity> media = new ArrayList<>();
        final MediaEntity photo = TestFixtures.newMediaEntity(1, 5, "photo");
        final FormattedMediaEntity adjustedPhoto = new FormattedMediaEntity(photo);
        media.add(adjustedPhoto);

        final List<? extends FormattedUrlEntity> combined
                = TweetTextLinkifier.mergeAndSortEntities(urls, media);
        assertEquals(adjustedPhoto, combined.get(0));
        assertEquals(adjustedUrl, combined.get(1));
    }
}
