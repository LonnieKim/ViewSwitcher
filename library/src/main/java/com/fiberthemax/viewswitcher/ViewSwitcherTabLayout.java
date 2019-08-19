package com.fiberthemax.viewswitcher;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewSwitcher;

import com.google.android.material.shape.MaterialShapeUtils;
import com.google.android.material.tabs.TabLayout;

import java.lang.ref.WeakReference;

public class ViewSwitcherTabLayout extends TabLayout {
    private OnTabSelectedListener currentVpSelectedListener;

    ViewSwitcher viewSwitcher;
    private PagerAdapter pagerAdapter;
    private DataSetObserver pagerAdapterObserver;
    private TabLayoutOnPageChangeListener pageChangeListener;
    private AdapterChangeListener adapterChangeListener;
    private boolean setupViewSwitcherImplicitly;

    public ViewSwitcherTabLayout(Context context) {
        super(context);
    }

    public ViewSwitcherTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewSwitcherTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * The one-stop shop for setting up this {@link TabLayout} with a {@link ViewSwitcher}.
     *
     * <p>This is the same as calling {@link #setupWithViewSwitcher(ViewSwitcher, boolean)} with
     * auto-refresh enabled.
     *
     * @param viewSwitcher the ViewSwitcher to link to, or {@code null} to clear any previous link
     */
    public void setupWithViewSwitcher(@Nullable ViewSwitcher viewSwitcher) {
        setupWithViewSwitcher(viewSwitcher, true);
    }

    /**
     * The one-stop shop for setting up this {@link TabLayout} with a {@link ViewSwitcher}.
     *
     * <p>This method will link the given ViewSwitcher and this TabLayout together so that changes in one
     * are automatically reflected in the other. This includes scroll state changes and clicks. The
     * tabs displayed in this layout will be populated from the ViewSwitcher adapter's page titles.
     *
     * <p>If {@code autoRefresh} is {@code true}, any changes in the {@link PagerAdapter} will trigger
     * this layout to re-populate itself from the adapter's titles.
     *
     * <p>If the given ViewSwitcher is non-null, it needs to already have a {@link PagerAdapter} set.
     *
     * @param viewSwitcher the ViewSwitcher to link to, or {@code null} to clear any previous link
     * @param autoRefresh  whether this layout should refresh its contents if the given ViewSwitcher's
     *                     content changes
     */
    public void setupWithViewSwitcher(@Nullable final ViewSwitcher viewSwitcher, boolean autoRefresh) {
        setupWithViewSwitcher(viewSwitcher, autoRefresh, false);
    }

    private void setupWithViewSwitcher(
            @Nullable final ViewSwitcher viewSwitcher, boolean autoRefresh, boolean implicitSetup) {
        if (this.viewSwitcher != null) {
            // If we've already been setup with a ViewSwitcher, remove us from it
            if (pageChangeListener != null) {
                this.viewSwitcher.removeOnPageChangeListener(pageChangeListener);
            }
            if (adapterChangeListener != null) {
                this.viewSwitcher.removeOnAdapterChangeListener(adapterChangeListener);
            }
        }

        if (currentVpSelectedListener != null) {
            // If we already have a tab selected listener for the ViewSwitcher, remove it
            removeOnTabSelectedListener(currentVpSelectedListener);
            currentVpSelectedListener = null;
        }

        if (viewSwitcher != null) {
            this.viewSwitcher = viewSwitcher;

            // Add our custom OnPageChangeListener to the ViewSwitcher
            if (pageChangeListener == null) {
                pageChangeListener = new TabLayoutOnPageChangeListener(this);
            }
//            pageChangeListener.reset();
            viewSwitcher.addOnPageChangeListener(pageChangeListener);

            // Now we'll add a tab selected listener to set ViewSwitcher's current item
            currentVpSelectedListener = new ViewSwitcherOnTabSelectedListener(viewSwitcher);
            addOnTabSelectedListener(currentVpSelectedListener);

            final PagerAdapter adapter = viewSwitcher.getAdapter();
            if (adapter != null) {
                // Now we'll populate ourselves from the pager adapter, adding an observer if
                // autoRefresh is enabled
                setPagerAdapter(adapter, autoRefresh);
            }

            // Add a listener so that we're notified of any adapter changes
            if (adapterChangeListener == null) {
                adapterChangeListener = new AdapterChangeListener();
            }
            adapterChangeListener.setAutoRefresh(autoRefresh);
            viewSwitcher.addOnAdapterChangeListener(adapterChangeListener);

            // Now update the scroll position to match the ViewSwitcher's current item
            setScrollPosition(viewSwitcher.getCurrentItem(), 0f, true);
        } else {
            // We've been given a null ViewSwitcher so we need to clear out the internal state,
            // listeners and observers
            this.viewSwitcher = null;
            setPagerAdapter(null, false);
        }

        setupViewSwitcherImplicitly = implicitSetup;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        MaterialShapeUtils.setParentAbsoluteElevation(this);

        if (viewSwitcher == null) {
            // If we don't have a ViewSwitcher already, check if our parent is a ViewSwitcher to
            // setup with it automatically
            final ViewParent vp = getParent();
            if (vp instanceof ViewSwitcher) {
                // If we have a ViewSwitcher parent and we've been added as part of its decor, let's
                // assume that we should automatically setup to display any titles
                setupWithViewSwitcher((ViewSwitcher) vp, true, true);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (setupViewSwitcherImplicitly) {
            // If we've been setup with a ViewSwitcher implicitly, let's clear out any listeners, etc
            setupWithViewSwitcher(null);
            setupViewSwitcherImplicitly = false;
        }
    }

    void setPagerAdapter(@Nullable final PagerAdapter adapter, final boolean addObserver) {
        if (pagerAdapter != null && pagerAdapterObserver != null) {
            // If we already have a PagerAdapter, unregister our observer
            pagerAdapter.unregisterDataSetObserver(pagerAdapterObserver);
        }

        pagerAdapter = adapter;

        if (addObserver && adapter != null) {
            // Register our observer on the new adapter
            if (pagerAdapterObserver == null) {
                pagerAdapterObserver = new PagerAdapterObserver();
            }
            adapter.registerDataSetObserver(pagerAdapterObserver);
        }

        // Finally make sure we reflect the new adapter
        populateFromPagerAdapter();
    }

    void populateFromPagerAdapter() {
        removeAllTabs();

        if (pagerAdapter != null) {
            final int adapterCount = pagerAdapter.getCount();
            for (int i = 0; i < adapterCount; i++) {
                addTab(newTab().setText(pagerAdapter.getPageTitle(i)), false);
            }

            // Make sure we reflect the currently set ViewSwitcher item
            if (viewSwitcher != null && adapterCount > 0) {
                final int curItem = viewSwitcher.getCurrentItem();
                if (curItem != getSelectedTabPosition() && curItem < getTabCount()) {
                    selectTab(getTabAt(curItem));
                }
            }
        }
    }

    /**
     * A {@link TabLayout.OnTabSelectedListener} class which contains the necessary calls back to the
     * provided {@link ViewSwitcher} so that the tab position is kept in sync.
     */
    public static class ViewSwitcherOnTabSelectedListener implements TabLayout.OnTabSelectedListener {
        private final ViewSwitcher viewSwitcher;

        public ViewSwitcherOnTabSelectedListener(ViewSwitcher viewSwitcher) {
            this.viewSwitcher = viewSwitcher;
        }

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            viewSwitcher.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            // No-op
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            // No-op
        }
    }

    private class PagerAdapterObserver extends DataSetObserver {
        PagerAdapterObserver() {
        }

        @Override
        public void onChanged() {
            populateFromPagerAdapter();
        }

        @Override
        public void onInvalidated() {
            populateFromPagerAdapter();
        }
    }

    private class AdapterChangeListener implements ViewSwitcher.OnAdapterChangeListener {
        private boolean autoRefresh;

        AdapterChangeListener() {
        }

        void setAutoRefresh(boolean autoRefresh) {
            this.autoRefresh = autoRefresh;
        }

        @Override
        public void onAdapterChanged(@NonNull ViewSwitcher viewSwitcher, @Nullable PagerAdapter oldAdapter, @Nullable PagerAdapter newAdapter) {
            if (ViewSwitcherTabLayout.this.viewSwitcher == viewSwitcher) {
                setPagerAdapter(newAdapter, autoRefresh);
            }
        }
    }

    /**
     * A {@link ViewSwitcher.OnPageChangeListener} class which contains the necessary calls back to the
     * provided {@link TabLayout} so that the tab position is kept in sync.
     *
     * <p>This class stores the provided TabLayout weakly, meaning that you can use {@link
     * ViewSwitcher#addOnPageChangeListener(ViewSwitcher.OnPageChangeListener)
     * addOnPageChangeListener(OnPageChangeListener)} without removing the listener and not cause a
     * leak.
     */
    public static class TabLayoutOnPageChangeListener implements ViewSwitcher.OnPageChangeListener {
        private final WeakReference<TabLayout> tabLayoutRef;

        public TabLayoutOnPageChangeListener(TabLayout tabLayout) {
            tabLayoutRef = new WeakReference<>(tabLayout);
        }

        @Override
        public void onPageSelected(final int position) {
            final TabLayout tabLayout = tabLayoutRef.get();
            if (tabLayout != null
                    && tabLayout.getSelectedTabPosition() != position
                    && position < tabLayout.getTabCount()) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        }
    }
}
