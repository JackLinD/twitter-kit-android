package com.twitter.sdk.android.core.models;

import io.fabric.sdk.android.services.common.CommonUtils;

import com.twitter.sdk.android.core.TwitterAndroidTestCase;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class PlaceTest extends TwitterAndroidTestCase {

    private static final String EXPECTED_COUNTRY = "United States";
    private static final String EXPECTED_COUNTRY_CODE = "US";
    private static final String EXPECTED_FULL_NAME = "Twitter HQ, San Francisco";
    private static final String EXPECTED_ID = "247f43d441defc03";
    private static final String EXPECTED_NAME = "Twitter HQ";
    private static final String EXPECTED_PLACE_TYPE = "poi";
    private static final String EXPECTED_URL = "https://api.twitter.com/1.1/geo/id/247f43d441defc03.json";

    private static final String EXPECTED_ATTR_STREET_ADDRESS = "street_address";
    private static final String EXPECTED_ATTR_STREET_ADDRESS_VALUE = "795 Folsom St";
    private static final String EXPECTED_ATTR_623_ID = "623:id";
    private static final String EXPECTED_ATTR_623_ID_VALUE = "210176";
    private static final String EXPECTED_ATTR_TWITTER = "twitter";
    private static final String EXPECTED_ATTR_TWITTER_VALUE = "twitter";

    private static final Double EXPECTED_BOUNDING_BOX_LONGITUDE = -122.400612831116;
    private static final Double EXPECTED_BOUNDING_BOX_LATITUDE = 37.7821120598956;
    private static final String EXPECTED_BOUNDING_BOX_TYPE = "Polygon";

    private Gson gson;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        gson = new Gson();
    }

    // TODO: Add tests for serialization if these models will be used for serialization.

    public void testDeserialization() throws IOException {
        JsonReader reader = null;
        try {
            reader = new JsonReader(new InputStreamReader(
                    getContext().getAssets().open("model_places.json")));
            final Place place = gson.fromJson(reader, Place.class);
            assertAttributes(place.attributes);
            assertBoundingBox(place.boundingBox);
            assertEquals(EXPECTED_COUNTRY, place.country);
            assertEquals(EXPECTED_COUNTRY_CODE, place.countryCode);
            assertEquals(EXPECTED_FULL_NAME, place.fullName);
            assertEquals(EXPECTED_ID, place.id);
            assertEquals(EXPECTED_NAME, place.name);
            assertEquals(EXPECTED_PLACE_TYPE, place.placeType);
            assertEquals(EXPECTED_URL, place.url);
        } finally {
            CommonUtils.closeQuietly(reader);
        }
    }

    private void assertAttributes(Map<String, String> attributes) {
        assertEquals(EXPECTED_ATTR_STREET_ADDRESS_VALUE,
                attributes.get(EXPECTED_ATTR_STREET_ADDRESS));
        assertEquals(EXPECTED_ATTR_623_ID_VALUE, attributes.get(EXPECTED_ATTR_623_ID));
        assertEquals(EXPECTED_ATTR_TWITTER_VALUE, attributes.get(EXPECTED_ATTR_TWITTER));
    }

    private void assertBoundingBox(Place.BoundingBox boundingBox) {
        assertEquals(EXPECTED_BOUNDING_BOX_TYPE, boundingBox.type);
        assertEquals(4, boundingBox.coordinates.get(0).size());
        for (List<Double> d: boundingBox.coordinates.get(0)) {
            assertEquals(EXPECTED_BOUNDING_BOX_LONGITUDE, d.get(0));
            assertEquals(EXPECTED_BOUNDING_BOX_LATITUDE, d.get(1));
        }
    }
}
