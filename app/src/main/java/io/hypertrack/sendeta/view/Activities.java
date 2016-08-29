package io.hypertrack.sendeta.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import io.hypertrack.sendeta.R;

/**
 * Created by piyush on 29/08/16.
 */
public class Activities extends BaseActivity {

    private ViewPager viewPager;

    private TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            viewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_activities);

        // Initialize Toolbar
        initToolbar(getString(R.string.title_activity_activities));

        // Initialize UI
        TabLayout tabLayout = (TabLayout) findViewById(R.id.activities_tab_layout);
        viewPager = (ViewPager) findViewById(R.id.activities_view_pager);

        if (tabLayout == null || viewPager == null) {
            Toast.makeText(Activities.this, "Error occurred while opening Activities screen.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Add Received & Sent Tabs
        tabLayout.addTab(tabLayout.newTab().setText(R.string.title_activities_tab_received));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.title_activities_tab_sent));

        // Add ViewPagerAdapter & TabLayout Listener for Activities Tabs
        ActivitiesPagerAdapter adapter = new ActivitiesPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        viewPager.setCurrentItem(0);
        tabLayout.setOnTabSelectedListener(onTabSelectedListener);
    }

    public class ActivitiesPagerAdapter extends FragmentStatePagerAdapter {

        private int tabCount;

        private Fragment[] mFragments = new Fragment[] {new ReceivedActivitiesFragment(), new SentActivitiesFragment()};

        public ActivitiesPagerAdapter(FragmentManager fragmentManager, int tabCount) {
            super(fragmentManager);
            this.tabCount = tabCount;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments[position];
        }

        @Override
        public int getCount() {
            return tabCount;
        }
    }
}
