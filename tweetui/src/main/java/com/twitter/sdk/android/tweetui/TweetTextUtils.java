package com.twitter.sdk.android.tweetui;

import android.text.TextUtils;

import com.twitter.sdk.android.core.models.MediaEntity;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.TweetEntities;
import com.twitter.sdk.android.core.models.UrlEntity;
import com.twitter.sdk.android.tweetui.internal.util.HtmlEntities;

import java.util.ArrayList;
import java.util.List;

final class TweetTextUtils {
    private static final String PHOTO_TYPE = "photo";

    private TweetTextUtils() {}

    /**
     * Should not be called directly outside of TweetRepository, the return value should be cached
     * or memoized.
     *
     * @param tweet The tweet to format
     * @return      The formatted Tweet text
     */
    static FormattedTweetText formatTweetText(Tweet tweet) {
        if (tweet == null) return null;

        final FormattedTweetText adjustedTweet = new FormattedTweetText();

        convertEntities(adjustedTweet, tweet);
        format(adjustedTweet, tweet);

        return adjustedTweet;
    }

    /**
     * Populates the list of formatted entities within the formattedTweetText.
     *
     * @param formattedTweetText The formatted tweet text that is to be populated
     * @param tweet The source Tweet
     */
    static void convertEntities(FormattedTweetText formattedTweetText, Tweet tweet) {
        if (tweet.entities == null) return;

        final List<UrlEntity> coreUrls = tweet.entities.urls;
        if (coreUrls != null) {
            for (UrlEntity entity : coreUrls) {
                final FormattedUrlEntity formattedUrlEntity = new FormattedUrlEntity(entity);
                formattedTweetText.urlEntities.add(formattedUrlEntity);
            }
        }

        final List<MediaEntity> coreMedia = tweet.entities.media;
        if (coreMedia != null) {
            for (MediaEntity entity : coreMedia) {
                final FormattedMediaEntity formattedMediaEntity = new FormattedMediaEntity(entity);
                formattedTweetText.mediaEntities.add(formattedMediaEntity);
            }
        }
    }

    /**
     * Calls the html unescaper and then the method to fix the entity indices errors caused by
     * emoji/supplementary characters.
     *
     * @param adjustedTweet
     * @param tweet
     */
    static void format(FormattedTweetText adjustedTweet, Tweet tweet) {
        if (TextUtils.isEmpty(tweet.text)) return;

        final HtmlEntities.Unescaped u = HtmlEntities.HTML40.unescape(tweet.text);
        final StringBuilder result = new StringBuilder(u.unescaped);

        adjustIndicesForEscapedChars(adjustedTweet.urlEntities, u.indices);
        adjustIndicesForEscapedChars(adjustedTweet.mediaEntities, u.indices);
        adjustIndicesForSupplementaryChars(result, adjustedTweet);
        adjustedTweet.text = result.toString();
    }

    /**
     * Since the unescaping of html causes for example &amp; to turn into & we need to adjust
     * the entity indices after that by 4 characters. This loops through the entities and adjusts
     * them as necessary.
     *
     * @param entities The entities that need to be adjusted
     * @param indices The indices of where there were escaped html chars that we unescaped
     */
    static void adjustIndicesForEscapedChars(
            List<? extends FormattedUrlEntity> entities,
            List<int[]> indices) {
        if (entities == null || indices == null || indices.isEmpty()) {
            return;
        }
        final int size = indices.size();
        int m = 0; // marker
        int diff = 0; // accumulated difference
        int inDiff; // end difference for escapes in range
        int len; // escaped length
        int start; // escaped start
        int end; // escaped end
        int i; // reusable index
        int[] index;
        // For each of the entities, update the start and end indices
        // Note: tweet entities are sorted.

        for (FormattedUrlEntity entity : entities) {
            inDiff = 0;
            // Go through the escaped entities' indices
            for (i = m; i < size; i++) {
                index = indices.get(i);
                start = index[0];
                end = index[1];
                // len is actually (end - start + 1) - 1
                len = end - start;
                if (end < entity.start) {
                    // bump position of the next marker
                    diff += len;
                    m++;
                } else if (end < entity.end) {
                    inDiff += len;
                }
            }
            // Once we've accumulated diffs, calc the offset
            entity.start = entity.start - diff;
            entity.end = entity.end - (diff + inDiff);
        }
    }

    /**
     * Adjusts entity indices for supplementary characters (Emoji being the most common example) in
     * UTF-8 (ones outside of U+0000 to U+FFFF range) are represented as a pair of char values, the
     * first from the high-surrogates range, and the second from the low-surrogates range.
     *
     * @param content The content of the tweet
     * @param formattedTweetText The formatted tweet text with entities that we need to adjust
     */
    static void adjustIndicesForSupplementaryChars(StringBuilder content,
            FormattedTweetText formattedTweetText) {
        final List<Integer> highSurrogateIndices = new ArrayList<Integer>();
        final int len = content.length() - 1;
        for (int i = 0; i < len; ++i) {
            if (Character.isHighSurrogate(content.charAt(i))
                    && Character.isLowSurrogate(content.charAt(i + 1))) {
                highSurrogateIndices.add(i);
            }
        }

        adjustEntitiesWithOffsets(formattedTweetText.urlEntities, highSurrogateIndices);
        adjustEntitiesWithOffsets(formattedTweetText.mediaEntities, highSurrogateIndices);
    }

    /**
     * Shifts indices by 1 since the Twitter REST Api does not count them correctly for our language
     * runtime.
     *
     * @param entities The entities that need to be adjusted
     * @param indices The indices in the string where there are supplementary chars
     */
    static void adjustEntitiesWithOffsets(List<? extends FormattedUrlEntity> entities,
            List<Integer> indices) {
        if (entities == null || indices == null) return;
        for (FormattedUrlEntity entity : entities) {
            // find all indices <= start and update offsets by that much
            final int start = entity.start;
            int offset = 0;
            for (Integer index : indices) {
                if (index - offset <= start) {
                    offset += 1;
                } else {
                    break;
                }
            }
            entity.start = entity.start + offset;
            entity.end = entity.end + offset;
        }
    }

    /**
     * This method gets the last photo entity out of the tweet, this is the photo to display inline
     *
     * @param entities The Tweet entities
     * @return         The last photo entity of
     */
    static MediaEntity getLastPhotoEntity(final TweetEntities entities) {
        if (entities == null) return null;

        final List<MediaEntity> mediaEntityList = entities.media;
        if (mediaEntityList == null || mediaEntityList.isEmpty()) return null;

        MediaEntity entity;
        for (int i = mediaEntityList.size() - 1; i >= 0; i--) {
            entity = mediaEntityList.get(i);
            if (entity.type != null && entity.type.equals(PHOTO_TYPE)) {
                return entity;
            }
        }
        return null;
    }

    /**
     * Returns true if there is a media entity with the type of "photo"
     *
     * @param entities The Tweet entities
     * @return         true if there is a media entity with the type of "photo"
     */
    static boolean hasPhotoUrl(TweetEntities entities) {
        return getLastPhotoEntity(entities) != null;
    }
}
