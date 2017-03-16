package io.hypertrack.sendeta.view;

import android.graphics.Bitmap;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.widget.ImageView;
import android.widget.TextView;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.OnboardingUser;
import io.hypertrack.sendeta.store.OnboardingManager;

/**
 * Created by piyush on 22/07/16.
 */
public class DrawerBaseActivity extends BaseActivity {

    private OnboardingUser user;
    private ImageView profileImageView;
    private TextView profileName;
    private DrawerLayout drawerLayout;

    public void initToolbarWithDrawer() {
        initToolbarWithDrawer(null);
    }

    public void initToolbarWithDrawer(String title) {

        if (title != null)
            initToolbar(title);

        drawerLayout = (DrawerLayout) findViewById(R.id.home_drawer_layout);
      /*  NavigationView navigationView = (NavigationView) findViewById(R.id.home_drawer);

        if (navigationView != null) {
            View navigationHeaderView = navigationView.getHeaderView(0);
            if (navigationHeaderView != null) {
                profileImageView = (ImageView) navigationHeaderView.findViewById(R.id.drawer_header_profile_image);
                profileName = (TextView) navigationHeaderView.findViewById(R.id.drawer_header_profile_name);
            }

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
                                    Intent receiveETAIntent = new Intent(DrawerBaseActivity.this, RequestETA.class);
                                    startActivity(receiveETAIntent);
                                    break;
                                }

                               *//* case R.id.drawer_user_activities: {
                                    Intent activitiesIntent = new Intent(DrawerBaseActivity.this, UserActivities.class);
                                    startActivity(activitiesIntent);
                                    break;
                                }*//*

                                case R.id.drawer_settings: {
                                    AnalyticsStore.getLogger().tappedProfile();

                                    Intent settingsIntent = new Intent(DrawerBaseActivity.this, SettingsScreen.class);
                                    startActivity(settingsIntent);
                                    break;
                                }
                            }

                            return true;
                        }
                    }
            );

            NavigationMenuView navigationMenuView = (NavigationMenuView) navigationView.getChildAt(0);
            if (navigationMenuView != null) {
                navigationMenuView.setVerticalScrollBarEnabled(false);
            }
        }*/

        // Update User Data in Navigation Drawer Header
        updateUserData();

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                getToolbar(), R.string.drawer_open, R.string.drawer_close);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    private void updateUserData() {
        // Set Toolbar Title as User Name
        user = OnboardingManager.sharedManager().getUser();

        if (user != null) {

            // Set Profile Picture if one exists for Current User
            if (profileImageView != null) {
                Bitmap bitmap = user.getImageBitmap();

                if (bitmap != null) {
                    profileImageView.setImageBitmap(bitmap);
                    profileImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }

            if (profileName != null) {
                profileName.setText(user.getName());
            }
        }
    }
}
