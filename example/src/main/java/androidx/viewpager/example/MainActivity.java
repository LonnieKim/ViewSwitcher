package androidx.viewpager.example;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewSwitcher;

import com.fiberthemax.example.R;
import com.fiberthemax.viewswitcher.TabLayoutMediator;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TabLayout tabLayout = findViewById(R.id.tabLayout);
        final ViewSwitcher viewSwitcher = findViewById(R.id.viewSwitcher);
        viewSwitcher.setAdapter(new TextFragmentPagerAdapter(getSupportFragmentManager()));
        final TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewSwitcher);
        tabLayoutMediator.attach();
    }

}
