package com.digits.sdk.android;

public class CountryInfoTests extends DigitsAndroidTestCase {
    private static final String COUNTRY_NAME_US = "United States";
    private static final int COUNTRY_CODE_US = 1;
    private static final String COUNTRY_NAME_BS = "Bahamas";
    private static final int COUNTRY_CODE_JP = 81;

    public void testEquals_differentObject() throws Exception {
        final CountryInfo countryInfo1 = new CountryInfo(COUNTRY_NAME_US, COUNTRY_CODE_US);
        final CountryInfo countryInfo2 = new CountryInfo(COUNTRY_NAME_US, COUNTRY_CODE_US);

        assertEquals(countryInfo1, countryInfo2);
        assertEquals(countryInfo2, countryInfo1);
        assertEquals(countryInfo1, countryInfo1);
        assertEquals(countryInfo2, countryInfo2);
        assertNotSame(countryInfo2, countryInfo1);
    }

    public void testEquals_null() throws Exception {
        final CountryInfo countryInfo = new CountryInfo(COUNTRY_NAME_US, COUNTRY_CODE_US);

        assertFalse(countryInfo.equals(null));
    }

    public void testEquals_differentClass() throws Exception {
        final CountryInfo countryInfo = new CountryInfo(COUNTRY_NAME_US, COUNTRY_CODE_US);

        assertFalse(countryInfo.equals(new Integer(0)));
    }

    public void testEquals_differentCountryName() throws Exception {
        final CountryInfo usCountryInfo = new CountryInfo(COUNTRY_NAME_US, COUNTRY_CODE_US);
        final CountryInfo bsCountryInfo = new CountryInfo(COUNTRY_NAME_BS, COUNTRY_CODE_US);

        assertFalse(usCountryInfo.equals(bsCountryInfo));
    }

    public void testEquals_nullCountryName() throws Exception {
        final CountryInfo usCountryInfo = new CountryInfo(COUNTRY_NAME_US, COUNTRY_CODE_US);
        final CountryInfo bsCountryInfo = new CountryInfo(null, COUNTRY_CODE_US);

        assertFalse(usCountryInfo.equals(bsCountryInfo));
        assertFalse(bsCountryInfo.equals(usCountryInfo));
    }

    public void testEquals_differentCountryCode() throws Exception {
        final CountryInfo usCountryInfo = new CountryInfo(COUNTRY_NAME_US, COUNTRY_CODE_US);
        final CountryInfo jpCountryInfo = new CountryInfo(COUNTRY_NAME_US, COUNTRY_CODE_JP);

        assertFalse(usCountryInfo.equals(jpCountryInfo));
    }

    public void testHashCode() throws Exception {
        final CountryInfo usCountryInfo = new CountryInfo(COUNTRY_NAME_US, COUNTRY_CODE_US);
        final CountryInfo bsCountryInfo = new CountryInfo(null, COUNTRY_CODE_US);

        assertEquals(1416475714, usCountryInfo.hashCode());
        assertEquals(1, bsCountryInfo.hashCode());
    }

    public void testToString() throws Exception {
        final CountryInfo usCountryInfo = new CountryInfo(COUNTRY_NAME_US, COUNTRY_CODE_US);

        assertEquals(usCountryInfo.country + " +" + usCountryInfo.countryCode,
                usCountryInfo.toString());
    }
}
