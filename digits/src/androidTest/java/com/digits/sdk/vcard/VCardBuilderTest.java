/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.digits.sdk.vcard;

import android.content.ContentValues;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for VCardBuilder.
 */
public class VCardBuilderTest extends TestCase {
    private static final String EMAIL_CARD_1 = "BEGIN:VCARD\r\nVERSION:3.0\r\nEMAIL:goose@twitter" +
            ".com\r\nEND:VCARD\r\n";
    private static final String EMAIL_CARD_2 = "BEGIN:VCARD\r\nVERSION:3.0\r\nEMAIL;" +
            "TYPE=PREF:goose@twitter.com\r\nEND:VCARD\r\n";
    private static final String PHONE_CARD = "BEGIN:VCARD\r\nVERSION:3.0\r\nTEL;" +
            "TYPE=HOME:1-231-234-567\r\nEND:VCARD\r\n";
    private static final String NAME_CARD = "BEGIN:VCARD\r\nVERSION:3.0\r\nN:Grouse;Spruce;;;" +
            "\r\nFN:Spruce Grouse\r\nEND:VCARD\r\n";

    public void testVCardNameFieldFromDisplayName() {
        final ArrayList<ContentValues> contentList = new ArrayList<ContentValues>();

        final ContentValues values = new ContentValues();
        values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, "ने");
        contentList.add(values);

        final VCardBuilder builder = new VCardBuilder(VCardConfig.VCARD_TYPE_DEFAULT);
        builder.appendNameProperties(contentList);
        final String actual = builder.toString();

        final String expectedCommon = ";CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:" +
                "=E0=A4=A8=E0=A5=87";

        final String expectedName = "N" + expectedCommon + ";;;;";
        final String expectedFullName = "FN" + expectedCommon;

        assertTrue("Actual value:\n" + actual + " expected to contain\n" + expectedName +
                "\nbut does not.", actual.contains(expectedName));
        assertTrue("Actual value:\n" + actual + " expected to contain\n" + expectedFullName +
                "\nbut does not.", actual.contains(expectedFullName));
    }

    public void testAppendNameProperties_email() {
        final VCardBuilder builder = new VCardBuilder(VCardConfig.VCARD_TYPE_V30_GENERIC,
                VCardConfig.DEFAULT_EXPORT_CHARSET);
        final ContentValues cv = new ContentValues();
        final List<ContentValues> group = new ArrayList<ContentValues>();

        cv.put(ContactsContract.Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
        cv.put(Email.IS_PRIMARY, 0);
        cv.put(Email.DATA1, "goose@twitter.com");

        group.add(cv);

        final String card = builder.appendEmails(group).toString();

        assertEquals(EMAIL_CARD_1, card);
    }

    // The HTC One was returning a different string for emails. The issue was a
    // package name conflict with internal vcard library. This test should fail if the vcard
    // package is renamed to com.android.vcard.
    public void testAppendNameProperties_htcOneDifferentOutput() {
        final VCardBuilder builder = new VCardBuilder(VCardConfig.VCARD_TYPE_V30_GENERIC,
                VCardConfig.DEFAULT_EXPORT_CHARSET);
        final ContentValues cv = new ContentValues();
        final List<ContentValues> group = new ArrayList<ContentValues>();

        cv.put(ContactsContract.Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
        cv.put(Email.DATA, "goose@twitter.com");
        cv.put(Email.TYPE, 3);
        cv.put(Email.LABEL, "");
        cv.put(Email.IS_PRIMARY, 1);

        group.add(cv);

        final String card = builder.appendEmails(group).toString();

        assertEquals(EMAIL_CARD_2, card);
    }

    public void testAppendNameProperties_phone() {
        final VCardBuilder builder = new VCardBuilder(VCardConfig.VCARD_TYPE_V30_GENERIC,
                VCardConfig.DEFAULT_EXPORT_CHARSET);
        final ContentValues cv = new ContentValues();
        final List<ContentValues> group = new ArrayList<ContentValues>();

        cv.put(ContactsContract.Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        cv.put(Email.IS_PRIMARY, 0);
        cv.put(Email.DATA1, "(123) 123-4567");

        group.add(cv);

        final String card = builder.appendPhones(group, null).toString();

        assertEquals(PHONE_CARD, card);
    }

    public void testAppendNameProperties_name() {
        final VCardBuilder builder = new VCardBuilder(VCardConfig.VCARD_TYPE_V30_GENERIC,
                VCardConfig.DEFAULT_EXPORT_CHARSET);
        final ContentValues cv = new ContentValues();
        final List<ContentValues> group = new ArrayList<ContentValues>();

        cv.put(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
        cv.put(Email.DATA1, "Spruce Grouse");
        cv.put(Email.DATA2, "Spruce");
        cv.put(Email.DATA3, "Grouse");

        group.add(cv);

        final String card = builder.appendNameProperties(group).toString();

        assertEquals(NAME_CARD, card);
    }
}
