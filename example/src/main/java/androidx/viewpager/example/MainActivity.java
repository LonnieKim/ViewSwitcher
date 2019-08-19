package androidx.viewpager.example;

import android.os.Bundle;
import android.view.MenuItem;

import com.fiberthemax.example.R;

import androidx.viewpager.widget.ViewSwitcher;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ViewSwitcher viewSwitcher = findViewById(R.id.viewSwitcher);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        viewSwitcher.setAdapter(new TextFragmentPagerAdapter(getSupportFragmentManager()));
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_tab1:
                        viewSwitcher.setCurrentItem(0);
                        break;
                    case R.id.action_tab2:
                        viewSwitcher.setCurrentItem(1);
                        break;
                    case R.id.action_tab3:
                        viewSwitcher.setCurrentItem(2);
                        break;
                    case R.id.action_tab4:
                        viewSwitcher.setCurrentItem(3);
                        break;
                    case R.id.action_tab5:
                        viewSwitcher.setCurrentItem(4);
                        break;
                }
                return true;
            }
        });
    }

}
