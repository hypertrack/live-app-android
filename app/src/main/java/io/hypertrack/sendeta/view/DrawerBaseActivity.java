package io.hypertrack.sendeta.view;

import android.content.Intent;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.store.AnalyticsStore;

/**
 * Created by piyush on 22/07/16.
 */
public class DrawerBaseActivity extends BaseActivity {
    NavigationView navigationView;
    DrawerLayout drawerLayout;

    public void initToolbarWithDrawer() {
        initToolbarWithDrawer(null);
    }

    public void initToolbarWithDrawer(String title) {

        if (title != null)
            initToolbar(title);

        drawerLayout = (DrawerLayout) findViewById(R.id.home_drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.home_drawer);

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {

                        drawerLayout.closeDrawers();

                        switch (item.getItemId()) {
                            case R.id.drawer_send_eta: {
                                break;
                            }
                            case R.id.drawer_receive_eta: {
//                                Intent receiveETAIntent = new Intent(DrawerBaseActivity.this, MyBookingsActivity.class);
//                                startActivity(receiveETAIntent);
                                break;
                            }
                            case R.id.drawer_settings: {
                                AnalyticsStore.getLogger().tappedProfile();

                                Intent settingsIntent = new Intent(DrawerBaseActivity.this, UserProfile.class);
                                startActivity(settingsIntent);
                                break;
                            }
                        }

                        return true;
                    }
                }
        );

        if (navigationView != null) {
            NavigationMenuView navigationMenuView = (NavigationMenuView) navigationView.getChildAt(0);
            if (navigationMenuView != null) {
                navigationMenuView.setVerticalScrollBarEnabled(false);
            }
        }

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                getToolbar(), R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as
                // we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as
                // we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }
}
