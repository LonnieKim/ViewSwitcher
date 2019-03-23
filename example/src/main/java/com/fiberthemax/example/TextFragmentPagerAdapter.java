package com.fiberthemax.example;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TextFragmentPagerAdapter extends FragmentPagerAdapter {
    public TextFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return TextFragment.newInstance(Integer.toString(position));
    }

    @Override
    public int getCount() {
        return 5;
    }
}
