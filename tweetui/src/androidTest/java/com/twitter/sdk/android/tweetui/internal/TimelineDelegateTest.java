package com.twitter.sdk.android.tweetui.internal;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.test.AndroidTestCase;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.tweetui.TestItem;
import com.twitter.sdk.android.tweetui.Timeline;
import com.twitter.sdk.android.tweetui.TimelineCursor;
import com.twitter.sdk.android.tweetui.TimelineResult;

import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class TimelineDelegateTest extends AndroidTestCase {
    private static final TestItem TEST_ITEM_1 = new TestItem(1111L);
    private static final TestItem TEST_ITEM_2 = new TestItem(2222L);
    private static final TestItem TEST_ITEM_3 = new TestItem(3333L);
    private static final TestItem TEST_ITEM_4 = new TestItem(4444L);
    private static final int TOTAL_ITEMS = 4;
    private static final int NUM_ITEMS = 100;
    private static final int ZERO_ITEMS = 0;
    private static final Long ANY_POSITION = 1234L;
    private static final Long TEST_MIN_POSITION = 3333L;
    private static final Long TEST_MAX_POSITION = 4444L;
    private static final TimelineCursor TEST_TIMELINE_CURSOR = new TimelineCursor(TEST_MIN_POSITION,
            TEST_MAX_POSITION);
    private static final String REQUIRED_MAX_CAPACITY_ERROR = "Max capacity reached";
    private static final String REQUIRED_REQUEST_IN_FLIGHT_ERROR = "Request already in flight";
    private static final TwitterException TEST_TWITTER_EXCEPTION
            = new TwitterException("Some exception");

    private TimelineDelegate<TestItem> delegate;
    private Timeline<TestItem> mockTimeline;
    private DataSetObservable mockObservable;
    private LinkedList<TestItem> testItems = new LinkedList<>();
    // test items for testing prepending and appending to another list
    private List<TestItem> testExtraItems = new ArrayList<>();
    private static Result<TimelineResult<TestItem>> testResult;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockTimeline = mock(Timeline.class);
        mockObservable = mock(DataSetObservable.class);
        // lists of items ordered from larger id to smaller
        testItems.add(TEST_ITEM_2);
        testItems.add(TEST_ITEM_1);
        // extra result items ordered from larger id to smaller
        testExtraItems.add(TEST_ITEM_4);
        testExtraItems.add(TEST_ITEM_3);
        testResult = new Result<>(new TimelineResult<>(TEST_TIMELINE_CURSOR, testItems), null);
    }

    public void testConstructor() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, testItems);
        assertEquals(mockTimeline, delegate.timeline);
        assertEquals(mockObservable, delegate.listAdapterObservable);
        assertEquals(testItems, delegate.itemList);
        assertNotNull(delegate.timelineStateHolder);
        // initial positions must be null
        assertNull(delegate.timelineStateHolder.positionForNext());
        assertNull(delegate.timelineStateHolder.positionForPrevious());
    }

    public void testConstructor_defaults() {
        delegate = new TimelineDelegate<>(mockTimeline);
        assertEquals(mockTimeline, delegate.timeline);
        assertNotNull(delegate.listAdapterObservable);
        assertNotNull(delegate.itemList);
        assertNotNull(delegate.timelineStateHolder);
        // initial positions must be null
        assertNull(delegate.timelineStateHolder.positionForNext());
        assertNull(delegate.timelineStateHolder.positionForPrevious());
    }

    public void testConstructor_nullTimeline() {
        try {
            delegate = new TimelineDelegate<>(null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertEquals("Timeline must not be null", e.getMessage());
        }
    }

    public void testGetCount() {
        delegate = new TimelineDelegate<>(mockTimeline);
        assertEquals(0, delegate.getCount());
        delegate = new TimelineDelegate<>(mockTimeline, null, testItems);
        assertEquals(testItems.size(), delegate.getCount());
    }

    public void testGetItem() {
        delegate = new TimelineDelegate<>(mockTimeline, null, testItems);
        assertEquals(TEST_ITEM_2, delegate.getItem(0));
        assertEquals(TEST_ITEM_1, delegate.getItem(1));
    }

    public void testGetLastItem_loadsPrevious() {
        final Timeline<TestItem> fakeTimeline = new FakeItemTimeline(NUM_ITEMS, ANY_POSITION,
                ANY_POSITION);
        delegate = new TimelineDelegate<>(fakeTimeline, mockObservable, null);
        delegate.refresh(null);
        // refresh loads latest items (notifyChange)
        verify(mockObservable).notifyChanged();
        delegate.getItem(NUM_ITEMS - 1);
        // assert items are added and notifyChanged is called again
        assertEquals(2 * NUM_ITEMS, delegate.getCount());
        verify(mockObservable, times(2)).notifyChanged();
    }

    public void testGetNonLastItem_doesNotLoadPrevious() {
        final Timeline<TestItem> fakeTimeline = new FakeItemTimeline(NUM_ITEMS, ANY_POSITION,
                ANY_POSITION);
        delegate = new TimelineDelegate<>(fakeTimeline, mockObservable, null);
        delegate.refresh(null);
        // refresh loads latest items (notifyChange)
        verify(mockObservable).notifyChanged();
        assertEquals(NUM_ITEMS, delegate.getCount());
        delegate.getItem(1);
        // assert no items added and notifyChanged is not called again
        assertEquals(NUM_ITEMS, delegate.getCount());
        verify(mockObservable, times(1)).notifyChanged();
    }

    public void testGetItemId() {
        delegate = new TimelineDelegate<>(mockTimeline, null, testItems);
        assertEquals(TEST_ITEM_2.getId(), delegate.getItemId(0));
        assertEquals(TEST_ITEM_1.getId(), delegate.getItemId(1));
    }

    public void testWithinMaxCapacity() {
        delegate = new TimelineDelegate<>(mockTimeline);
        assertTrue(delegate.withinMaxCapacity());
        TestItem.populateList(testItems, TimelineDelegate.CAPACITY);
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, testItems);
        assertFalse(delegate.withinMaxCapacity());
    }

    public void testIsLastPosition() {
        testItems = new LinkedList<>();
        TestItem.populateList(testItems, NUM_ITEMS);
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, testItems);
        assertFalse(delegate.isLastPosition(0));
        assertFalse(delegate.isLastPosition(NUM_ITEMS - 2));
        assertTrue(delegate.isLastPosition(NUM_ITEMS - 1));
    }

    // reset, next, previous

    public void testRefresh_resetsTimelineCursors() {
        delegate = new TimelineDelegate<>(mockTimeline);
        delegate.timelineStateHolder.setNextCursor(new TimelineCursor(ANY_POSITION, ANY_POSITION));
        delegate.timelineStateHolder.setPreviousCursor(new TimelineCursor(ANY_POSITION,
                ANY_POSITION));
        delegate.refresh(null);
        assertNull(delegate.timelineStateHolder.positionForNext());
        assertNull(delegate.timelineStateHolder.positionForPrevious());
    }

    public void testRefresh_callsNextForLatest() {
        delegate = new TimelineDelegate<>(mockTimeline);
        delegate.refresh(null);
        verify(mockTimeline).next(isNull(Long.class), any(TimelineDelegate.NextCallback.class));
    }

    public void testRefresh_replacesItems() {
        // refresh replaces initial items
        final Timeline<TestItem> fakeTimeline = new FakeItemTimeline(NUM_ITEMS, ANY_POSITION,
                ANY_POSITION);
        delegate = new TimelineDelegate<>(fakeTimeline, mockObservable, testItems);
        assertEquals(testItems, delegate.itemList);
        delegate.refresh(null);
        // assert that items were replaced and notifyChanged called
        assertEquals(NUM_ITEMS, delegate.itemList.size());
        verify(mockObservable).notifyChanged();
    }

    public void testNext_addsItems() {
        final Timeline<TestItem> fakeTimeline = new FakeItemTimeline(NUM_ITEMS, ANY_POSITION,
                ANY_POSITION);
        delegate = new TimelineDelegate<>(fakeTimeline, mockObservable, null);
        delegate.refresh(null);
        // refresh loads latest items (notifyChange)
        delegate.next(null);
        // assert items are added and notifyChanges is called again
        assertEquals(2 * NUM_ITEMS, delegate.getCount());
        verify(mockObservable, times(2)).notifyChanged();
    }

    public void testNext_doesNotAddItemsAtBeginningOfTimeline() {
        // when a Timeline successfully returns an empty set of items, there are no next items (yet)
        LinkedList<TestItem> initialItems = new LinkedList<>();
        final int INITIAL_COUNT = 5;
        initialItems = TestItem.populateList(initialItems, INITIAL_COUNT);
        final Timeline<TestItem> fakeEndTimeline = new FakeItemTimeline(ZERO_ITEMS, ANY_POSITION,
                ANY_POSITION);
        delegate = new TimelineDelegate<>(fakeEndTimeline, mockObservable, initialItems);
        delegate.next(null);
        // assert no items are added and notifyChanged is not called
        assertEquals(INITIAL_COUNT, delegate.getCount());
        verifyZeroInteractions(mockObservable);
    }

    public void testNext_updatesPositionForNext() {
        final Timeline<TestItem> fakeTimeline = new FakeItemTimeline(NUM_ITEMS, ANY_POSITION,
                TEST_MAX_POSITION);
        delegate = new TimelineDelegate<>(fakeTimeline);
        assertNull(delegate.timelineStateHolder.positionForPrevious());
        delegate.next(null);
        assertEquals(TEST_MAX_POSITION, delegate.timelineStateHolder.positionForNext());
    }

    public void testNext_doesNotUpdatePositionAtBeginningOfTimeline() {
        final Timeline<TestItem> fakeEndTimeline = new FakeItemTimeline(ZERO_ITEMS, ANY_POSITION,
                ANY_POSITION);
        delegate = new TimelineDelegate<>(fakeEndTimeline);
        delegate.timelineStateHolder.setPreviousCursor(new TimelineCursor(null, null));
        delegate.next(null);
        assertNull(delegate.timelineStateHolder.positionForNext());
    }

    public void testPrevious_addsItems() {
        final Timeline<TestItem> fakeTimeline = new FakeItemTimeline(NUM_ITEMS, ANY_POSITION,
                ANY_POSITION);
        delegate = new TimelineDelegate<>(fakeTimeline, mockObservable, null);
        delegate.refresh(null);
        // refresh loads latest items (notifyChange)
        delegate.previous();
        // assert items are added and notifyChanges is called again
        assertEquals(2 * NUM_ITEMS, delegate.getCount());
        verify(mockObservable, times(2)).notifyChanged();
    }

    public void testPrevious_doesNotAddItemsAtEndOfTimeline() {
        // when a Timeline successfully returns an empty set of items, its end has been reached
        LinkedList<TestItem> initialItems = new LinkedList<>();
        final int INITIAL_COUNT = 5;
        initialItems = TestItem.populateList(initialItems, INITIAL_COUNT);
        final Timeline<TestItem> fakeEndTimeline = new FakeItemTimeline(ZERO_ITEMS, ANY_POSITION,
                ANY_POSITION);
        delegate = new TimelineDelegate<>(fakeEndTimeline, mockObservable, initialItems);
        delegate.previous();
        // assert no items are added and notifyChanged is not called
        assertEquals(INITIAL_COUNT, delegate.getCount());
        verifyZeroInteractions(mockObservable);
    }

    public void testPrevious_updatesPositionForPrevious() {
        final Timeline<TestItem> fakeTimeline = new FakeItemTimeline(NUM_ITEMS, TEST_MIN_POSITION,
                ANY_POSITION);
        delegate = new TimelineDelegate<>(fakeTimeline);
        assertNull(delegate.timelineStateHolder.positionForPrevious());
        delegate.previous();
        assertEquals(TEST_MIN_POSITION, delegate.timelineStateHolder.positionForPrevious());
    }

    public void testPrevious_doesNotUpdatePositionAtEndOfTimeline() {
        final Timeline<TestItem> fakeEndTimeline = new FakeItemTimeline(ZERO_ITEMS, ANY_POSITION,
                ANY_POSITION);
        delegate = new TimelineDelegate<>(fakeEndTimeline);
        delegate.timelineStateHolder.setPreviousCursor(new TimelineCursor(null, null));
        delegate.previous();
        assertNull(delegate.timelineStateHolder.positionForPrevious());
    }

    // loadNext, loadPrevious

    public void testLoadNext() {
        delegate = new TimelineDelegate<>(mockTimeline);
        final Callback<TimelineResult<TestItem>> testCb = delegate.new NextCallback(null,
                delegate.timelineStateHolder);
        delegate.loadNext(TEST_MIN_POSITION, testCb);
        verify(mockTimeline).next(TEST_MIN_POSITION, testCb);
    }

    public void testLoadNext_respectsMaxCapacity() {
        delegate = new TimelineDelegate<>(mockTimeline);
        TestItem.populateList(delegate.itemList, TimelineDelegate.CAPACITY);
        final Callback<TimelineResult<TestItem>> mockCallback = mock(Callback.class);
        delegate.loadNext(ANY_POSITION, mockCallback);
        final ArgumentCaptor<TwitterException> exceptionCaptor
                = ArgumentCaptor.forClass(TwitterException.class);
        verifyZeroInteractions(mockTimeline);
        verify(mockCallback).failure(exceptionCaptor.capture());
        assertEquals(exceptionCaptor.getValue().getMessage(), REQUIRED_MAX_CAPACITY_ERROR);
    }

    public void testLoadNext_respectsRequestInFlight() {
        delegate = new TimelineDelegate<>(mockTimeline);
        delegate.timelineStateHolder.startTimelineRequest();
        final Callback<TimelineResult<TestItem>> mockCallback = mock(Callback.class);
        delegate.loadNext(ANY_POSITION, mockCallback);
        final ArgumentCaptor<TwitterException> exceptionCaptor
                = ArgumentCaptor.forClass(TwitterException.class);
        verifyZeroInteractions(mockTimeline);
        verify(mockCallback).failure(exceptionCaptor.capture());
        assertEquals(exceptionCaptor.getValue().getMessage(), REQUIRED_REQUEST_IN_FLIGHT_ERROR);
    }

    public void testLoadPrevious() {
        delegate = new TimelineDelegate<>(mockTimeline);
        final Callback<TimelineResult<TestItem>> testCb = delegate.new PreviousCallback(
                delegate.timelineStateHolder);
        delegate.loadPrevious(TEST_MAX_POSITION, testCb);
        verify(mockTimeline).previous(TEST_MAX_POSITION, testCb);
    }

    public void testLoadPrevious_respectsMaxCapacity() {
        delegate = new TimelineDelegate<>(mockTimeline);
        TestItem.populateList(delegate.itemList, TimelineDelegate.CAPACITY);
        final Callback<TimelineResult<TestItem>> mockCallback = mock(Callback.class);
        delegate.loadPrevious(ANY_POSITION, mockCallback);
        final ArgumentCaptor<TwitterException> exceptionCaptor
                = ArgumentCaptor.forClass(TwitterException.class);
        verifyZeroInteractions(mockTimeline);
        verify(mockCallback).failure(exceptionCaptor.capture());
        assertEquals(exceptionCaptor.getValue().getMessage(), REQUIRED_MAX_CAPACITY_ERROR);
    }

    public void testLoadPrevious_respectsRequestInFlight() {
        delegate = new TimelineDelegate<>(mockTimeline);
        delegate.timelineStateHolder.startTimelineRequest();
        final Callback<TimelineResult<TestItem>> mockCallback = mock(Callback.class);
        delegate.loadPrevious(ANY_POSITION, mockCallback);
        final ArgumentCaptor<TwitterException> exceptionCaptor
                = ArgumentCaptor.forClass(TwitterException.class);
        verifyZeroInteractions(mockTimeline);
        verify(mockCallback).failure(exceptionCaptor.capture());
        assertEquals(exceptionCaptor.getValue().getMessage(), REQUIRED_REQUEST_IN_FLIGHT_ERROR);
    }

    /* nested Callbacks */

    // should unconditionally set requestInFlight to false
    public void testDefaultCallback_successCallsFinishTimelineRequest() {
        delegate = new TimelineDelegate<>(mockTimeline);
        final TimelineStateHolder mockHolder = mock(TimelineStateHolder.class);
        final TimelineDelegate.DefaultCallback cb = delegate.new DefaultCallback(null, mockHolder);
        cb.success(null);
        verify(mockHolder).finishTimelineRequest();
    }

    public void testDefaultCallback_successCallsDeveloperCallback() {
        final Callback<TimelineResult<TestItem>> developerCb = mock(Callback.class);
        delegate = new TimelineDelegate<>(mockTimeline);
        final TimelineDelegate.DefaultCallback cb = delegate.new DefaultCallback(developerCb,
                delegate.timelineStateHolder);
        cb.success(testResult);
        verify(developerCb).success(testResult);
    }

    public void testDefaultCallback_successHandlesNullDeveloperCallback() {
        delegate = new TimelineDelegate<>(mockTimeline);
        final TimelineDelegate.DefaultCallback cb = delegate.new DefaultCallback(null,
                delegate.timelineStateHolder);
        try {
            cb.success(testResult);
        } catch (NullPointerException e) {
            fail("Should have handled null callback");
        }
    }

    // should unconditionally set requestInFlight to false
    public void testDefaultCallback_failureCallsFinishTimelineRequest() {
        delegate = new TimelineDelegate<>(mockTimeline);
        final TimelineStateHolder mockHolder = mock(TimelineStateHolder.class);
        final TimelineDelegate.DefaultCallback cb = delegate.new DefaultCallback(null, mockHolder);
        cb.failure((TwitterException) null);
        verify(mockHolder).finishTimelineRequest();
    }

    public void testDefaultCallback_failureCallsDeveloperCallback() {
        final Callback<TimelineResult<TestItem>> developerCb = mock(Callback.class);
        delegate = new TimelineDelegate<>(mockTimeline);
        final TimelineDelegate.DefaultCallback cb = delegate.new DefaultCallback(developerCb,
                delegate.timelineStateHolder);
        cb.failure(TEST_TWITTER_EXCEPTION);
        verify(developerCb).failure(TEST_TWITTER_EXCEPTION);
    }

    public void testDefaultCallback_failureHandlesNullDeveloperCallback() {
        delegate = new TimelineDelegate<>(mockTimeline);
        final TimelineDelegate.DefaultCallback cb = delegate.new DefaultCallback(null,
                delegate.timelineStateHolder);
        try {
            cb.failure(TEST_TWITTER_EXCEPTION);
        } catch (NullPointerException e) {
            fail("Should have handled null callback");
        }
    }

    // should prepend result items, set next cursor, and call notifyChanged
    public void testNextCallback_successReceivedItems() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, testItems);
        final TimelineStateHolder timelineStateHolder = new TimelineStateHolder(
                new TimelineCursor(ANY_POSITION, ANY_POSITION),
                new TimelineCursor(ANY_POSITION, ANY_POSITION));
        final TimelineDelegate.NextCallback cb = delegate.new NextCallback(null,
                timelineStateHolder);
        cb.success(new Result<>(new TimelineResult<>(TEST_TIMELINE_CURSOR, testExtraItems), null));
        // assert the next TimelineCursor is set on the ScrollStateHolder, previous unchanged
        assertEquals(TEST_MAX_POSITION, timelineStateHolder.positionForNext());
        assertEquals(ANY_POSITION, timelineStateHolder.positionForPrevious());
        // assert that extra items were prepended in reverse order
        assertEquals(TOTAL_ITEMS, delegate.itemList.size());
        assertEquals(TEST_ITEM_4, delegate.getItem(0));
        assertEquals(TEST_ITEM_3, delegate.getItem(1));
        assertEquals(TEST_ITEM_2, delegate.getItem(2));
        assertEquals(TEST_ITEM_1, delegate.getItem(3));
        // assert observer's notifyChanged is called
        verify(mockObservable).notifyChanged();
    }

    // should set both nextCursor and previousCursor to be non-null
    public void testNextCallback_successFirstReceivedItems() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, testItems);
        final TimelineStateHolder timelineStateHolder = new TimelineStateHolder();
        final TimelineDelegate.NextCallback cb = delegate.new NextCallback(null,
                timelineStateHolder);
        cb.success(new Result<>(new TimelineResult<>(TEST_TIMELINE_CURSOR, testExtraItems), null));
        // assert the next TimelineCursor is set on the ScrollStateHolder, previous unchanged
        assertEquals(TEST_MAX_POSITION, timelineStateHolder.positionForNext());
        assertEquals(TEST_MIN_POSITION, timelineStateHolder.positionForPrevious());
    }

    // should do nothing
    public void testNextCallback_successReceivedZeroItems() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, testItems);
        final TimelineStateHolder timelineStateHolder = new TimelineStateHolder();
        final TimelineDelegate.NextCallback cb = delegate.new NextCallback(null,
                timelineStateHolder);
        cb.success(new Result<>(new TimelineResult<>(TEST_TIMELINE_CURSOR, Collections.emptyList()),
                null));
        // assert that the cursors and itemList are left unmodified
        assertNull(timelineStateHolder.positionForNext());
        assertNull(timelineStateHolder.positionForPrevious());
        assertEquals(testItems.size(), delegate.itemList.size());
        verifyZeroInteractions(mockObservable);
    }

    // should clear items with result items, set next cursor, and call notifyChanged
    public void testRefreshCallback_successReceivedItems() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, testItems);
        final TimelineStateHolder timelineStateHolder = new TimelineStateHolder(
                new TimelineCursor(ANY_POSITION, ANY_POSITION),
                new TimelineCursor(ANY_POSITION, ANY_POSITION));
        final TimelineDelegate.RefreshCallback cb = delegate.new RefreshCallback(null,
                timelineStateHolder);
        cb.success(new Result<>(new TimelineResult<>(TEST_TIMELINE_CURSOR, testExtraItems), null));
        // assert the next TimelineCursor is set on the ScrollStateHolder, previous unchanged
        assertEquals(TEST_MAX_POSITION, timelineStateHolder.positionForNext());
        assertEquals(ANY_POSITION, timelineStateHolder.positionForPrevious());
        // assert that extra items replaced the old items
        assertEquals(testExtraItems.size(), delegate.itemList.size());
        assertEquals(TEST_ITEM_4, delegate.getItem(0));
        assertEquals(TEST_ITEM_3, delegate.getItem(1));
        // assert observer's notifyChanged is called
        verify(mockObservable).notifyChanged();
    }

    // should do nothing
    public void testRefreshCallback_successReceivedZeroItems() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, testItems);
        final TimelineStateHolder timelineStateHolder = new TimelineStateHolder();
        final TimelineDelegate.RefreshCallback cb = delegate.new RefreshCallback(null,
                timelineStateHolder);
        cb.success(new Result<>(new TimelineResult<>(TEST_TIMELINE_CURSOR, Collections.emptyList()),
                null));
        // assert that the cursors and itemList are left unmodified
        assertNull(timelineStateHolder.positionForNext());
        assertNull(timelineStateHolder.positionForPrevious());
        assertEquals(testItems.size(), delegate.itemList.size());
        verifyZeroInteractions(mockObservable);
    }

    // should append result items, set previous cursor, and call notifyChanged
    public void testPreviousCallback_successReceivedItems() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, testItems);
        final TimelineStateHolder timelineStateHolder = new TimelineStateHolder(
                new TimelineCursor(ANY_POSITION, ANY_POSITION),
                new TimelineCursor(ANY_POSITION, ANY_POSITION));
        final TimelineDelegate.PreviousCallback cb
                = delegate.new PreviousCallback(timelineStateHolder);
        cb.success(new Result<>(new TimelineResult<>(TEST_TIMELINE_CURSOR, testExtraItems), null));
        // assert the previous TimelineCursor is set on the ScrollStateHolder
        assertEquals(TEST_MIN_POSITION, timelineStateHolder.positionForPrevious());
        assertEquals(ANY_POSITION, timelineStateHolder.positionForNext());
        // assert that extra items were appended in order received
        assertEquals(TOTAL_ITEMS, delegate.itemList.size());
        assertEquals(TEST_ITEM_2, delegate.getItem(0));
        assertEquals(TEST_ITEM_1, delegate.getItem(1));
        assertEquals(TEST_ITEM_4, delegate.getItem(2));
        assertEquals(TEST_ITEM_3, delegate.getItem(3));
        // assert observer's notifyChanged is called
        verify(mockObservable).notifyChanged();
    }

    // should set both nextCursor and previousCursor to be non-null
    public void testPreviousCallback_successFirstReceivedItems() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, testItems);
        final TimelineStateHolder timelineStateHolder = new TimelineStateHolder();
        final TimelineDelegate.PreviousCallback cb
                = delegate.new PreviousCallback(timelineStateHolder);
        cb.success(new Result<>(new TimelineResult<>(TEST_TIMELINE_CURSOR, testExtraItems), null));
        // assert the previous TimelineCursor is set on the ScrollStateHolder
        assertEquals(TEST_MAX_POSITION, timelineStateHolder.positionForNext());
        assertEquals(TEST_MIN_POSITION, timelineStateHolder.positionForPrevious());
    }


    // should do nothing
    public void testPreviousCallback_successReceivedZeroItems() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, testItems);
        final TimelineStateHolder timelineStateHolder = new TimelineStateHolder();
        final TimelineDelegate.PreviousCallback cb
                = delegate.new PreviousCallback(timelineStateHolder);
        cb.success(new Result<>(new TimelineResult<>(TEST_TIMELINE_CURSOR, Collections.emptyList()),
                null));
        // assert that the cursors and itemList are left unmodified
        assertNull(timelineStateHolder.positionForNext());
        assertNull(timelineStateHolder.positionForPrevious());
        assertEquals(testItems.size(), delegate.itemList.size());
        verifyZeroInteractions(mockObservable);
    }

    /* test DataSetObservable */

    public void testRegisterDataSetObserver() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, null);
        delegate.registerDataSetObserver(mock(DataSetObserver.class));
        verify(mockObservable, times(1)).registerObserver(any(DataSetObserver.class));
    }

    public void testUnregisterDataSetObserver() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, null);
        delegate.unregisterDataSetObserver(mock(DataSetObserver.class));
        verify(mockObservable, times(1)).unregisterObserver(any(DataSetObserver.class));
    }

    public void testNotifyDataSetChanged() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, null);
        delegate.notifyDataSetChanged();
        verify(mockObservable, times(1)).notifyChanged();
    }

    public void testNotifyDataSetInvalidated() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, null);
        delegate.notifyDataSetInvalidated();
        verify(mockObservable, times(1)).notifyInvalidated();
    }

    /**
     * Timeline which loads numItems TestItems on each next/previous call. Use zero for numItems
     * to simulate reaching the end of a finite timeline.
     */
    public static class FakeItemTimeline implements Timeline<TestItem> {
        private long numItems;
        private Long minPosition;
        private Long maxPosition;

        /**
         * Constructs a FakeItemTimeline
         * @param numItems the number of TestItems to return per call to next/previous
         * @param minPosition the TimelineCursor minPosition returned by calls to next/previous
         * @param maxPosition the TimelineCursor maxPosition returned by calls to next/previous
         */
        public FakeItemTimeline(long numItems, Long minPosition, Long maxPosition) {
            this.numItems = numItems;
            this.minPosition = minPosition;
            this.maxPosition = maxPosition;
        }

        @Override
        public void next(Long sinceId, Callback<TimelineResult<TestItem>> cb) {
            final List<TestItem> testItems = new ArrayList<>();
            TestItem.populateList(testItems, numItems);
            final TimelineResult<TestItem> timelineResult
                    = new TimelineResult<>(new TimelineCursor(minPosition, maxPosition), testItems);
            cb.success(timelineResult, null);
        }

        @Override
        public void previous(Long maxId, Callback<TimelineResult<TestItem>> cb) {
            final List<TestItem> testItems = new ArrayList<>();
            TestItem.populateList(testItems, numItems);
            final TimelineResult<TestItem> timelineResult
                    = new TimelineResult<>(new TimelineCursor(minPosition, maxPosition), testItems);
            cb.success(timelineResult, null);
        }
    }
}
