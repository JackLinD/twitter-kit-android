package com.twitter.sdk.android.core;

import android.content.SharedPreferences;

import io.fabric.sdk.android.services.persistence.PreferenceStore;
import io.fabric.sdk.android.services.persistence.PreferenceStoreImpl;
import io.fabric.sdk.android.services.persistence.PreferenceStoreStrategy;
import io.fabric.sdk.android.services.persistence.SerializationStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class PersistedSessionManagerTest extends TwitterAndroidTestCase {

    static final String PREF_KEY_ACTIVE_SESSION = "active_session";
    static final String PREF_KEY_SESSION = "session";

    private static final long TEST_SESSION_ID = 1L;
    private static final String PREF_RANDOM_KEY = "random_key";
    private static final String RESTORED_USER = "restoredUser";

    private PreferenceStore preferenceStore;
    private SerializationStrategy<TwitterSession> mockSerializer;
    private ConcurrentHashMap<Long, TwitterSession> sessionMap;
    private ConcurrentHashMap<Long, PreferenceStoreStrategy<TwitterSession>> storageMap;
    private PreferenceStoreStrategy<TwitterSession> mockActiveSessionStorage;
    private PersistedSessionManager<TwitterSession> sessionManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        preferenceStore = new PreferenceStoreImpl(getContext(), "testSession");
        mockSerializer = mock(SerializationStrategy.class);
        sessionMap = new ConcurrentHashMap<Long, TwitterSession>();
        storageMap = new ConcurrentHashMap<Long, PreferenceStoreStrategy<TwitterSession>>();
        mockActiveSessionStorage = mock(PreferenceStoreStrategy.class);
        sessionManager = new PersistedSessionManager<TwitterSession>(preferenceStore,
                mockSerializer, sessionMap, storageMap, mockActiveSessionStorage,
                PREF_KEY_ACTIVE_SESSION, PREF_KEY_SESSION);
    }

    @Override
    protected void tearDown() throws Exception {
        preferenceStore.edit().clear().commit();
        super.tearDown();
    }

    public void testIsSessionPreferenceKey_validKey() {
        final String preferenceKey = PREF_KEY_SESSION + "_" + USER_ID;
        assertTrue(sessionManager.isSessionPreferenceKey(preferenceKey));
    }

    public void testIsSessionPreferenceKey_invalidKey() {
        assertFalse(sessionManager.isSessionPreferenceKey(PREF_RANDOM_KEY));
    }

    public void testRestoreSession_noSavedSession() {
        when(mockActiveSessionStorage.restore()).thenReturn(null);
        sessionManager.restoreAllSessionsIfNecessary();
        assertNull(sessionManager.getActiveSession());
    }

    public void testRestoreSession_savedSession() {
        final TwitterSession mockSession = mock(TwitterSession.class);
        when(mockActiveSessionStorage.restore()).thenReturn(mockSession);
        sessionManager.restoreAllSessionsIfNecessary();
        assertEquals(mockSession, sessionManager.getActiveSession());
    }

    public void testRestoreSession_multipleSavedSessions() {
        // Set up test by creating and serializing some test TwitterSessions.
        final SharedPreferences.Editor editor = preferenceStore.edit();
        final TwitterSession[] sessions = new TwitterSession[]{
                new TwitterSession(new TwitterAuthToken(TOKEN, SECRET), USER_ID, USER),
                new TwitterSession(new TwitterAuthToken(TOKEN, SECRET), USER_ID + 1,
                        USER + 1)
        };
        final TwitterSession.Serializer serializer = new TwitterSession.Serializer();
        final PersistedSessionManager<TwitterSession> localSessionManager =
                new PersistedSessionManager<TwitterSession>(preferenceStore,
                        serializer, sessionMap, storageMap, mockActiveSessionStorage,
                        PREF_KEY_ACTIVE_SESSION, PREF_KEY_SESSION);
        for (TwitterSession session : sessions) {
            final String serializedObject = serializer.serialize(session);
            editor.putString(localSessionManager.getPrefKey(session.getId()), serializedObject);
        }
        preferenceStore.save(editor);

        localSessionManager.restoreAllSessionsIfNecessary();
        assertMapSizes(sessions.length);
        for (TwitterSession session : sessions) {
            assertEquals(session, localSessionManager.getSession(session.getId()));
        }
    }

    public void testRestoreSession_invalidPreferenceKey() {
        final SharedPreferences.Editor editor = preferenceStore.edit();
        editor.putString(PREF_RANDOM_KEY, "random value");
        preferenceStore.save(editor);

        sessionManager.restoreAllSessionsIfNecessary();
        assertMapSizes(0);
    }

    public void testRestoreSession_multipleRestoreCalls() throws Exception {
        final TwitterSession mockSession = mock(TwitterSession.class);
        when(mockActiveSessionStorage.restore()).thenReturn(mockSession);

        assertEquals(mockSession, sessionManager.getActiveSession());
        sessionManager.restoreAllSessionsIfNecessary();

        // restore should only be called once.
        verify(mockActiveSessionStorage).restore();
    }

    public void testRestoreSession_afterActiveSessionSetExternally() throws Exception {
        final TwitterSession mockRestoredSession = mock(TwitterSession.class);
        when(mockActiveSessionStorage.restore()).thenReturn(mockRestoredSession);

        final TwitterSession mockActiveSession = mock(TwitterSession.class);
        sessionManager.setActiveSession(mockActiveSession);
        sessionManager.restoreAllSessionsIfNecessary();

        assertEquals(mockActiveSession, sessionManager.getActiveSession());
    }

    public void testGetActiveSession_restoredSession() {
        final TwitterSession mockRestoredSession = mock(TwitterSession.class);
        when(mockActiveSessionStorage.restore()).thenReturn(mockRestoredSession);

        final TwitterSession activeSession = sessionManager.getActiveSession();
        assertEquals(mockRestoredSession, activeSession);
        verify(mockActiveSessionStorage).restore();
    }

    public void testGetActiveSession_nullSession() {
        assertNull(sessionManager.getActiveSession());
    }

    public void testGetActiveSession_validSession() {
        final TwitterSession session = setupActiveSessionTest();
        assertEquals(session, sessionManager.getActiveSession());
    }

    private TwitterSession setupActiveSessionTest() {
        final TwitterSession mockSession = mock(TwitterSession.class);
        when(mockSession.getId()).thenReturn(TEST_SESSION_ID);
        sessionManager.setActiveSession(mockSession);
        return mockSession;
    }

    public void testSetActiveSession_nullSession() {
        try {
            sessionManager.setActiveSession(null);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    public void testSetActiveSession_validSession() {
        final TwitterSession session = setupActiveSessionTest();
        final int numSessionsThisTest = 1;
        assertMapSizes(numSessionsThisTest);

        verify(mockActiveSessionStorage).save(session);
        assertEquals(session, sessionManager.getActiveSession());
        assertEquals(session, sessionManager.getSession(session.getId()));
    }

    private void assertMapSizes(int count) {
        assertEquals(count, sessionMap.size());
        assertEquals(count, storageMap.size());
    }

    public void testSetActiveSession_differentSession() {
        final TwitterSession session = setupActiveSessionTest();
        int numSessionsThisTest = 1;
        assertMapSizes(numSessionsThisTest);
        verify(mockActiveSessionStorage).save(session);
        assertEquals(session, sessionManager.getActiveSession());

        final TwitterSession session2 = mock(TwitterSession.class);
        final long differentSessionId = session.getId() + 1;
        when(session2.getId()).thenReturn(differentSessionId);
        sessionManager.setActiveSession(session2);
        numSessionsThisTest++;
        assertMapSizes(numSessionsThisTest);
        verify(mockActiveSessionStorage).save(session2);
        assertNotSame(session, session2);
        assertEquals(session2, sessionManager.getActiveSession());
    }

    public void testClearActiveSession() {
        setupActiveSessionTest();
        sessionManager.clearActiveSession();
        assertMapSizes(0);
        verify(mockActiveSessionStorage).clear();
        assertNull(sessionManager.getActiveSession());
    }

    public void testClearActiveSession_noActiveSession() {
        try {
            sessionManager.clearActiveSession();
        } catch (Exception e) {
            fail();
        }
    }

    public void testClearActiveSession_beforeRestoreSession() {
        setupActiveSessionTest();
        sessionManager.clearActiveSession();
        assertNull(sessionManager.getActiveSession());
    }

    public void testGetSession() {
        final TwitterSession session = setupActiveSessionTest();
        assertEquals(session, sessionManager.getSession(session.getId()));
    }

    public void testGetSession_multipleSessions() {
        final int count = 2;
        final List<TwitterSession> sessions = setupMultipleSessionsTest(count);
        for (int i = 0; i < count; i++) {
            final TwitterSession session = sessions.get(i);
            assertEquals(session, sessionManager.getSession(session.getId()));
        }
    }

    private List<TwitterSession> setupMultipleSessionsTest(int count) {
        final List<TwitterSession> sessions = new ArrayList<TwitterSession>(count);
        for (int i = 0; i < count; i++) {
            final long id = i;
            final TwitterSession session = mock(TwitterSession.class);
            when(session.getId()).thenReturn(id);
            sessionManager.setSession(id, session);
            sessions.add(session);
        }
        return sessions;
    }

    public void testSetSession_nullSession() {
        try {
            sessionManager.setSession(TEST_SESSION_ID, null);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    public void testSetSession_noActiveSession() {
        final TwitterSession session = mock(TwitterSession.class);
        when(session.getId()).thenReturn(TEST_SESSION_ID);
        sessionManager.setSession(TEST_SESSION_ID, session);
        final int numSessionsThisTest = 1;
        assertMapSizes(numSessionsThisTest);
        // Verify that when setSession is called and there is no active session, the specified
        // session becomes the active session.
        verify(mockActiveSessionStorage).save(session);
        assertEquals(session, sessionManager.getSession(TEST_SESSION_ID));
        assertEquals(session, sessionManager.getActiveSession());
    }

    public void testSetSession_multipleSessions() {
        final int count = 2;
        final List<TwitterSession> sessions = setupMultipleSessionsTest(count);
        assertMapSizes(count);

        for (int i = 0; i < count; i++) {
            final TwitterSession session = sessions.get(i);
            assertEquals(session, sessionManager.getSession(session.getId()));
        }
        // Verify that the first session is still the active session.
        assertEquals(sessions.get(0), sessionManager.getActiveSession());
    }

    public void testSetSession_updateExistingSession() {
        final TwitterAuthToken authToken = mock(TwitterAuthToken.class);
        final TwitterSession session = new TwitterSession(authToken, USER_ID, USER);
        final long sessionId = session.getId();
        sessionManager.setSession(sessionId, session);
        assertEquals(session, sessionManager.getSession(sessionId));

        final TwitterSession sessionWithDifferentUserName = new TwitterSession(authToken, sessionId,
                "differentUserName");
        sessionManager.setSession(sessionId, sessionWithDifferentUserName);
        assertEquals(sessionWithDifferentUserName, sessionManager.getSession(sessionId));
        assertMapSizes(1);
    }

    public void testSetSession_beforeRestoreSession() {
        final TwitterAuthToken authToken = mock(TwitterAuthToken.class);

        final TwitterSession newSession = new TwitterSession(authToken, USER_ID, USER);
        final TwitterSession restoredSession =
                new TwitterSession(authToken, USER_ID, RESTORED_USER);

        setupSessionForRestore(restoredSession);

        sessionManager.setSession(newSession.getId(), newSession);
        sessionManager.restoreAllSessionsIfNecessary();

        // We want to make sure that even if restore sessions is called after setSession.
        // session set in setSession will not be overwritten.
        assertEquals(newSession, sessionManager.getSession(newSession.getId()));
    }

    private void setupSessionForRestore(final TwitterSession restoredSession) {
        final SharedPreferences.Editor editor = preferenceStore.edit();
        final TwitterSession.Serializer serializer = new TwitterSession.Serializer();
        final String serializedObject = serializer.serialize(restoredSession);
        editor.putString(sessionManager.getPrefKey(restoredSession.getId()), serializedObject);
        editor.commit();
    }

    public void testClearSession() {
        final TwitterSession session = setupActiveSessionTest();
        sessionManager.clearSession(session.getId());
        assertMapSizes(0);
        assertNull(sessionManager.getActiveSession());
        assertNull(sessionManager.getSession(session.getId()));
    }

    public void testClearSession_noSessions() {
        try {
            sessionManager.clearSession(TEST_SESSION_ID);
        } catch (Exception e) {
            fail();
        }
    }

    public void testClearSession_multipleSessionsClearFirstSession() {
        final int count = 2;
        final List<TwitterSession> sessions = setupMultipleSessionsTest(count);
        int numSessionsThisTest = count;
        assertMapSizes(numSessionsThisTest);

        // Clear the first session
        final long firstSessionId = sessions.get(0).getId();
        sessionManager.clearSession(firstSessionId);
        numSessionsThisTest--;
        assertMapSizes(numSessionsThisTest);
        assertNull(sessionManager.getSession(firstSessionId));
        // Make sure the second session is still there
        final long secondSessionId = sessions.get(1).getId();
        assertEquals(sessions.get(1), sessionManager.getSession(secondSessionId));
        // TODO: What happens to active session? We require it to be explicitly set?
    }

    public void testClearSession_multipleSessionsClearSecondSession() {
        final int count = 2;
        final List<TwitterSession> sessions = setupMultipleSessionsTest(count);
        int numSessionsThisTest = count;
        assertMapSizes(numSessionsThisTest);

        // Clear the second session
        final long secondSessionId = sessions.get(1).getId();
        sessionManager.clearSession(secondSessionId);
        numSessionsThisTest--;
        assertMapSizes(numSessionsThisTest);
        assertNull(sessionManager.getSession(secondSessionId));
        // Make sure the first session is still there
        final long firstSessionId = sessions.get(0).getId();
        assertEquals(sessions.get(0), sessionManager.getSession(firstSessionId));
    }

    public void testClearSession_beforeRestoreSession() {
        final TwitterSession restoredSession =
                new TwitterSession(mock(TwitterAuthToken.class), USER_ID, RESTORED_USER);
        setupSessionForRestore(restoredSession);
        sessionManager.clearSession(USER_ID);
        sessionManager.restoreAllSessionsIfNecessary();

        assertNull(sessionManager.getSession(USER_ID));
    }

    public void testGetPrefKey() {
        assertEquals(PREF_KEY_SESSION + "_" + TEST_SESSION_ID,
                sessionManager.getPrefKey(TEST_SESSION_ID));
    }

    public void testGetSessionMap() {
        try {
            sessionManager.getSessionMap().put(1L, null);
            fail("should be unmodifiable map");
        } catch (UnsupportedOperationException e) {
            // success
        }
    }

    public void testGetSessionMap_restoresSessionsIfNecessary() {
        final TwitterSession mockSession = mock(TwitterSession.class);
        when(mockActiveSessionStorage.restore()).thenReturn(mockSession);
        sessionManager.getSessionMap();
        assertEquals(mockSession, sessionManager.getActiveSession());
    }
}
