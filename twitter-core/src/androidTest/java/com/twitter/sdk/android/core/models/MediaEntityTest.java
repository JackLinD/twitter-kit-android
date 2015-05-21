package com.twitter.sdk.android.core.models;

import com.twitter.sdk.android.core.TwitterAndroidTestCase;

import com.google.gson.Gson;

import java.io.IOException;

public class MediaEntityTest extends TwitterAndroidTestCase {

    private static final String TEST_JSON = "{\"type\":\"photo\", \"sizes\":{"
            + "\"thumb\":{\"h\":150, \"resize\":\"crop\", \"w\":150},"
            + "\"large\":{\"h\":238, \"resize\":\"fit\", \"w\":226},"
            + "\"medium\":{\"h\":238, \"resize\":\"fit\", \"w\":226},"
            + "\"small\":{\"h\":238, \"resize\":\"fit\", \"w\":226}},"
            + "\"indices\":[15,35], \"url\":\"http:\\/\\/t.co\\/rJC5Pxsu\","
            + "\"media_url\":\"http:\\/\\/p.twimg.com\\/AZVLmp-CIAAbkyy.jpg\","
            + "\"display_url\":\"pic.twitter.com\\/rJC5Pxsu\","
            + "\"id\":114080493040967680, \"id_str\":\"114080493040967680\","
            + "\"source_status_id\": 205282515685081088,"
            + "\"source_status_id_str\": \"205282515685081088\","
            + "\"expanded_url\":"
            + " \"http:\\/\\/twitter.com\\/yunorno\\/status\\/114080493036773378\\/photo\\/1\","
            + "\"media_url_https\":\"https:\\/\\/p.twimg.com\\/AZVLmp-CIAAbkyy.jpg\"}";
    private static final int TEST_INDICES_START = 15;
    private static final int TEST_INDICES_END = 35;
    private static final String TEST_URL = "http://t.co/rJC5Pxsu";
    private static final String TEST_DISPLAY_URL = "pic.twitter.com/rJC5Pxsu";
    private static final String TEST_EXPANDED_URL = "http://twitter.com/yunorno/status/114080493036773378/photo/1";
    private static final long TEST_ID = 114080493040967680L;
    private static final String TEST_ID_STR = "114080493040967680";
    private static final String TEST_MEDIA_URL = "http://p.twimg.com/AZVLmp-CIAAbkyy.jpg";
    private static final String TEST_MEDIA_URL_HTTPS = "https://p.twimg.com/AZVLmp-CIAAbkyy.jpg";
    private static final MediaEntity.Size TEST_SIZE_THUMB = new MediaEntity.Size(150, 150, "crop");
    private static final MediaEntity.Size TEST_SIZE_SMALL = new MediaEntity.Size(226, 238, "fit");
    private static final MediaEntity.Size TEST_SIZE_MEDIUM = new MediaEntity.Size(226, 238, "fit");
    private static final MediaEntity.Size TEST_SIZE_LARGE = new MediaEntity.Size(226, 238, "fit");
    private static final long TEST_SOURCE_STATUS_ID = 205282515685081088L;
    private static final String TEST_SOURCE_STATUS_ID_STR = "205282515685081088";
    private static final String TEST_TYPE = "photo";

    private Gson gson;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        gson = new Gson();
    }

    // TODO: Add tests for serialization if this model will be used for serialization.

    public void testDeserialization() throws IOException {
        final MediaEntity entity = gson.fromJson(TEST_JSON, MediaEntity.class);
        assertEquals(TEST_INDICES_START, entity.getStart());
        assertEquals(TEST_INDICES_END, entity.getEnd());
        assertEquals(TEST_URL, entity.url);
        assertEquals(TEST_DISPLAY_URL, entity.displayUrl);
        assertEquals(TEST_EXPANDED_URL, entity.expandedUrl);
        assertEquals(TEST_ID, entity.id);
        assertEquals(TEST_ID_STR, entity.idStr);
        assertEquals(TEST_MEDIA_URL, entity.mediaUrl);
        assertEquals(TEST_MEDIA_URL_HTTPS, entity.mediaUrlHttps);
        assertSizeEquals(TEST_SIZE_THUMB, entity.sizes.thumb);
        assertSizeEquals(TEST_SIZE_SMALL, entity.sizes.small);
        assertSizeEquals(TEST_SIZE_MEDIUM, entity.sizes.medium);
        assertSizeEquals(TEST_SIZE_LARGE, entity.sizes.large);
        assertEquals(TEST_SOURCE_STATUS_ID, entity.sourceStatusId);
        assertEquals(TEST_SOURCE_STATUS_ID_STR, entity.sourceStatusIdStr);
        assertEquals(TEST_TYPE, entity.type);
    }

    private void assertSizeEquals(MediaEntity.Size expected, MediaEntity.Size actual) {
        assertEquals(expected.h, actual.h);
        assertEquals(expected.w, actual.w);
        assertEquals(expected.resize, actual.resize);
    }
}
