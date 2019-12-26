/*
 * Copyright 2018 The Android Open Source Project
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
 * limitations under the License.
 */

package com.fiberthemax.viewswitcher;

import android.database.DataSetObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewSwitcher;

import com.google.android.material.tabs.TabLayout;

import java.lang.ref.WeakReference;

/**
 * A mediator to link a TabLayout with a ViewSwitcher. The mediator will synchronize the ViewSwitcher's
 * position with the selected tab when a tab is selected, and the TabLayout's scroll position when
 * the user drags the ViewSwitcher. TabLayoutMediator will listen to ViewSwitcher's OnPageChangeListener
 * to adjust tab when ViewSwitcher moves.
 *
 * <p>Establish the link by creating an instance of this class, make sure the ViewSwitcher has an
 * adapter and then call {@link #attach()} on it. Instantiating a TabLayoutMediator will only create
 * the mediator object, {@link #attach()} will link the TabLayout and the ViewSwitcher together. When
 * creating an instance of this class, you must supply an implementation of {@link
 * TabConfigurationStrategy} in which you set the text of the tab, and/or perform any styling of the
 * tabs that you require. Changing ViewSwitcher's adapter will require a {@link #detach()} followed by
 * {@link #attach()} call. Changing the ViewSwitcher or TabLayout will require a new instantiation of
 * TabLayoutMediator.
 */
public final class TabLayoutMediator {
    @NonNull
    private final TabLayout tabLayout;
    @NonNull
    private final ViewSwitcher viewSwitcher;
    private final boolean autoRefresh;
    @Nullable
    private TabConfigurationStrategy tabConfigurationStrategy;
    @Nullable
    private PagerAdapter adapter;
    private boolean attached;

    @Nullable
    private TabLayoutOnPageChangeListener onPageChangeListener;
    @Nullable
    private TabLayout.OnTabSelectedListener onTabSelectedListener;
    @Nullable
    private DataSetObserver pagerAdapterObserver;

    /**
     * A callback interface that must be implemented to set the text and styling of newly created
     * tabs.
     */
    public interface TabConfigurationStrategy {
        /**
         * Called to configure the tab for the page at the specified position. Typically calls {@link
         * TabLayout.Tab#setText(CharSequence)}, but any form of styling can be applied.
         *
         * @param tab      The Tab which should be configured to represent the title of the item at the given
         *                 position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        void onConfigureTab(@NonNull TabLayout.Tab tab, int position);
    }


    public TabLayoutMediator(
            @NonNull TabLayout tabLayout,
            @NonNull ViewSwitcher viewSwitcher) {
        this(tabLayout, viewSwitcher, null);
    }

    public TabLayoutMediator(
            @NonNull TabLayout tabLayout,
            @NonNull ViewSwitcher viewSwitcher,
            @Nullable TabConfigurationStrategy tabConfigurationStrategy) {
        this(tabLayout, viewSwitcher, true, tabConfigurationStrategy);
    }

    public TabLayoutMediator(
            @NonNull TabLayout tabLayout,
            @NonNull ViewSwitcher viewSwitcher,
            boolean autoRefresh,
            @Nullable TabConfigurationStrategy tabConfigurationStrategy) {
        this.tabLayout = tabLayout;
        this.viewSwitcher = viewSwitcher;
        this.autoRefresh = autoRefresh;
        this.tabConfigurationStrategy = tabConfigurationStrategy;
    }

    /**
     * Link the TabLayout and the ViewSwitcher together. Must be called after ViewSwitcher has an adapter
     * set. To be called on a new instance of TabLayoutMediator or if the ViewSwitcher's adapter
     * changes.
     *
     * @throws IllegalStateException If the mediator is already attached, or the ViewSwitcher has no
     *                               adapter.
     */
    public void attach() {
        if (attached) {
            throw new IllegalStateException("TabLayoutMediator is already attached");
        }
        adapter = viewSwitcher.getAdapter();
        if (adapter == null) {
            throw new IllegalStateException(
                    "TabLayoutMediator attached before ViewSwitcher has an " + "adapter");
        }
        attached = true;

        // Add our custom OnPageChangeListener to the ViewSwitcher
        onPageChangeListener = new TabLayoutOnPageChangeListener(tabLayout);
        viewSwitcher.addOnPageChangeListener(onPageChangeListener);

        // Now we'll add a tab selected listener to set ViewSwitcher's current item
        onTabSelectedListener = new ViewSwitcherOnTabSelectedListener(viewSwitcher);
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);

        // Now we'll populate ourselves from the pager adapter, adding an observer if
        // autoRefresh is enabled
        if (autoRefresh) {
            // Register our observer on the new adapter
            pagerAdapterObserver = new PagerAdapterObserver();
            adapter.registerDataSetObserver(pagerAdapterObserver);
        }

        populateTabsFromPagerAdapter();

        // Now update the scroll position to match the ViewSwitcher's current item
        tabLayout.setScrollPosition(viewSwitcher.getCurrentItem(), 0f, true);
    }

    /**
     * Unlink the TabLayout and the ViewSwitcher. To be called on a stale TabLayoutMediator if a new one
     * is instantiated, to prevent holding on to a view that should be garbage collected. Also to be
     * called before {@link #attach()} when a ViewSwitcher's adapter is changed.
     */
    public void detach() {
        adapter.unregisterDataSetObserver(pagerAdapterObserver);
        tabLayout.removeOnTabSelectedListener(onTabSelectedListener);
        viewSwitcher.removeOnPageChangeListener(onPageChangeListener);
        pagerAdapterObserver = null;
        onTabSelectedListener = null;
        onPageChangeListener = null;
        adapter = null;
        attached = false;
    }

    @SuppressWarnings("WeakerAccess")
    void populateTabsFromPagerAdapter() {
        tabLayout.removeAllTabs();

        if (adapter != null) {
            int adapterCount = adapter.getCount();
            for (int i = 0; i < adapterCount; i++) {
                TabLayout.Tab tab = tabLayout.newTab();
                if (tabConfigurationStrategy != null) {
                    tabConfigurationStrategy.onConfigureTab(tab, i);
                } else {
                    tab.setText(adapter.getPageTitle(i));
                }
                tabLayout.addTab(tab, false);
            }
            // Make sure we reflect the currently set ViewSwitcher item
            if (adapterCount > 0) {
                int lastItem = tabLayout.getTabCount() - 1;
                int currItem = Math.min(viewSwitcher.getCurrentItem(), lastItem);
                if (currItem != tabLayout.getSelectedTabPosition()) {
                    tabLayout.selectTab(tabLayout.getTabAt(currItem));
                }
            }
        }
    }

    /**
     * A {@link ViewSwitcher.OnPageChangeListener} class which contains the necessary calls back to the
     * provided {@link TabLayout} so that the tab position is kept in sync.
     *
     * <p>This class stores the provided TabLayout weakly, meaning that you can use {@link
     * ViewSwitcher#addOnPageChangeListener(ViewSwitcher.OnPageChangeListener)} without removing the
     * callback and not cause a leak.
     */
    private static class TabLayoutOnPageChangeListener implements ViewSwitcher.OnPageChangeListener {
        @NonNull
        private final WeakReference<TabLayout> tabLayoutRef;

        TabLayoutOnPageChangeListener(TabLayout tabLayout) {
            tabLayoutRef = new WeakReference<>(tabLayout);
        }

        @Override
        public void onPageSelected(final int position) {
            TabLayout tabLayout = tabLayoutRef.get();
            if (tabLayout != null
                    && tabLayout.getSelectedTabPosition() != position
                    && position < tabLayout.getTabCount()) {
                // Select the tab, only updating the indicator if we're not being dragged/settled
                // (since onPageScrolled will handle that).
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        }
    }

    /**
     * A {@link TabLayout.OnTabSelectedListener} class which contains the necessary calls back to the
     * provided {@link ViewSwitcher} so that the tab position is kept in sync.
     */
    private static class ViewSwitcherOnTabSelectedListener implements TabLayout.OnTabSelectedListener {
        private final ViewSwitcher viewSwitcher;

        ViewSwitcherOnTabSelectedListener(ViewSwitcher viewSwitcher) {
            this.viewSwitcher = viewSwitcher;
        }

        @Override
        public void onTabSelected(@NonNull TabLayout.Tab tab) {
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
            populateTabsFromPagerAdapter();
        }
    }
}
