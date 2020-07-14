package androidx.viewpager.example;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class TextFragmentPagerAdapter extends FragmentPagerAdapter {

    private List<String> list = new ArrayList<>();

    public TextFragmentPagerAdapter(FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return TextFragment.newInstance(list.get(position));
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return list.get(position);
    }

    public void setList(List<String> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).hashCode();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        for (int i = 0; i < list.size(); i++) {
            String text = ((TextFragment) object).mText;
            if (TextUtils.equals(text, list.get(i))) {
                return i;
            }
        }
        return PagerAdapter.POSITION_NONE;
    }
}
