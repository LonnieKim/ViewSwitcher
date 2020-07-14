package androidx.viewpager.example;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewSwitcher;

import com.fiberthemax.example.R;
import com.fiberthemax.viewswitcher.TabLayoutMediator;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TabLayout tabLayout = findViewById(R.id.tabLayout);
        final ViewSwitcher viewSwitcher = findViewById(R.id.viewSwitcher);
        final TextFragmentPagerAdapter textFragmentPagerAdapter = new TextFragmentPagerAdapter(getSupportFragmentManager());
        viewSwitcher.setAdapter(textFragmentPagerAdapter);
        final TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewSwitcher);
        tabLayoutMediator.attach();

        List<String> list = new ArrayList<>();
        list.add("3");
        textFragmentPagerAdapter.setList(list);

        viewSwitcher.postDelayed(new Runnable() {
            @Override
            public void run() {
                List<String> list = new ArrayList<>();
                list.add("1");
                list.add("2");
                list.add("3");
                list.add("4");
                list.add("5");
                textFragmentPagerAdapter.setList(list);

            }
        }, 1000L);

    }

}
