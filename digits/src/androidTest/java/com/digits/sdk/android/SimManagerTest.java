package com.digits.sdk.android;

import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SimManagerTest extends DigitsAndroidTestCase {
    private TelephonyManager telephonyManager;
    private SimManager simManager;
    private Context context;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        context = mock(Context.class);
        telephonyManager = mock(TelephonyManager.class);
        when(context.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(telephonyManager);
        simManager = DummySimManager.createSimManager(context);
    }

    public void testConstructor_nullTelephonyManager() throws Exception {
        when(context.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(null);
        simManager = DummySimManager.createSimManager(context);
        assertEquals("", simManager.getRawPhoneNumber());
        assertEquals("", simManager.getCountryIso());
    }

    public void testGetCountryIso_iso2() throws Exception {
        when(telephonyManager.getSimCountryIso()).thenReturn(US_ISO2);
        assertEquals(US_ISO2.toUpperCase(), simManager.getCountryIso());
    }

    public void testGetCountryIso_iso3NoNetworkInfo() throws Exception {
        when(telephonyManager.getSimCountryIso()).thenReturn(US_ISO3);
        assertEquals("", simManager.getCountryIso());
    }

    public void testGetCountryIso_iso3WithNetworkCountryIso2() throws Exception {
        when(telephonyManager.getSimCountryIso()).thenReturn(US_ISO3);
        when(telephonyManager.getNetworkCountryIso()).thenReturn(US_ISO2);
        assertEquals(US_ISO2.toUpperCase(), simManager.getCountryIso());
    }

    public void testGetCountryIso_iso3WithNetworkCountryIso3() throws Exception {
        when(telephonyManager.getSimCountryIso()).thenReturn(US_ISO3);
        when(telephonyManager.getNetworkCountryIso()).thenReturn(US_ISO3);
        assertEquals("", simManager.getCountryIso());
    }

    public void testGetCountryIso_noPermission() throws Exception {
        when(context.checkCallingOrSelfPermission(android.Manifest.permission.READ_PHONE_STATE))
                .thenReturn(PackageManager.PERMISSION_DENIED);
        simManager = DummySimManager.createSimManager(context);
        testGetCountryIso_iso2();
    }

    public void testGetPhoneNumber_noPermission() throws Exception {
        when(context.checkCallingOrSelfPermission(android.Manifest.permission.READ_PHONE_STATE))
                .thenReturn(PackageManager.PERMISSION_DENIED);
        simManager = DummySimManager.createSimManager(context);
        when(telephonyManager.getLine1Number()).thenReturn(PHONE);
        assertEquals("", simManager.getRawPhoneNumber());
    }

    public void testGetCountryIso_iso3isCdma() throws Exception {
        when(telephonyManager.getSimCountryIso()).thenReturn(US_ISO3);
        when(telephonyManager.getPhoneType()).thenReturn(TelephonyManager.PHONE_TYPE_CDMA);
        assertEquals("", simManager.getCountryIso());
    }

    /**
     * Test extracted from Android
     * http://androidxref.com/5.0.0_r2/xref/frameworks/opt/telephony/tests/telephonytests/src/com
     * * /android/internal/telephony/PhoneNumberUtilsTest.java
     */
    public void testGetPhoneNumber() throws Exception {
        when(telephonyManager.getLine1Number()).thenReturn("650 2910000");
        assertEquals("6502910000", simManager.getRawPhoneNumber());
        when(telephonyManager.getLine1Number()).thenReturn("12,3#4*567");
        assertEquals("1234567", simManager.getRawPhoneNumber());
        when(telephonyManager.getLine1Number()).thenReturn("800-GOOG-114");
        assertEquals("8004664114", simManager.getRawPhoneNumber());
        when(telephonyManager.getLine1Number()).thenReturn("+1 650 2910000");
        assertEquals("+16502910000", simManager.getRawPhoneNumber());
    }

    public class DummySimManager extends SimManager {
        private DummySimManager(TelephonyManager telephonyManager, boolean hasPermission) {
            super(telephonyManager, hasPermission);
        }
    }

}
