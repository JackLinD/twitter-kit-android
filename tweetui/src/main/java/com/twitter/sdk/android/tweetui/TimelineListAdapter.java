package com.twitter.sdk.android.tweetui;

import android.content.Context;
import android.database.DataSetObserver;
import android.widget.BaseAdapter;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.models.Identifiable;
import com.twitter.sdk.android.tweetui.internal.TimelineDelegate;

/**
 * TimelineListAdapter is a ListAdapter providing timeline items for ListViews.
 * Concrete subclasses must define a type parameter and implement getView.
 */
abstract class TimelineListAdapter<T extends Identifiable> extends BaseAdapter {
    protected final Context context;
    private final TimelineDelegate<T> delegate;

    /**
     * Constructs a TimelineListAdapter for the given Timeline.
     * @param context the context for row views.
     * @param timeline a Timeline providing access to timeline data items.
     * @throws java.lang.IllegalArgumentException if timeline is null
     */
    public TimelineListAdapter(Context context, Timeline<T> timeline) {
        this(context, new TimelineDelegate<>(timeline));
    }

    /* for testing */
    TimelineListAdapter(Context context, TimelineDelegate<T> delegate) {
        this.context = context;
        this.delegate = delegate;
        delegate.refresh(null);
    }

    /**
     * Clears the items and loads the latest Timeline items.
     */
    public void refresh(Callback<TimelineResult<T>> cb) {
        delegate.refresh(cb);
    }

    @Override
    public int getCount() {
        return delegate.getCount();
    }

    @Override
    public T getItem(int position) {
        return delegate.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return delegate.getItemId(position);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        delegate.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        delegate.unregisterDataSetObserver(observer);
    }

    @Override
    public void notifyDataSetChanged() {
        delegate.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetInvalidated() {
        delegate.notifyDataSetInvalidated();
    }
}
