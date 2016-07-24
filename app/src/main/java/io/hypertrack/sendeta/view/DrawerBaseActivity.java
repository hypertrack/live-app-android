package io.hypertrack.sendeta.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;

import java.util.ArrayList;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.adapter.AccountProfileSpinnerAdapter;
import io.hypertrack.sendeta.model.User;
import io.hypertrack.sendeta.store.AccountProfileSharedPrefsManager;
import io.hypertrack.sendeta.store.AnalyticsStore;
import io.hypertrack.sendeta.store.UserStore;

/**
 * Created by piyush on 22/07/16.
 */
public class DrawerBaseActivity extends BaseActivity {

    private User user;
    private ImageView profileImageView;
    private Spinner profileAccountsSpinner;
    private ImageView addBusinessProfileIcon;

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    private ArrayList<String> accountProfilesList;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent addBusinessProfileIntent = new Intent(DrawerBaseActivity.this, BusinessProfile.class);
            startActivity(addBusinessProfileIntent);
        }
    };

    public void initToolbarWithDrawer() {
        initToolbarWithDrawer(null);
    }

    public void initToolbarWithDrawer(String title) {

        if (title != null)
            initToolbar(title);

        drawerLayout = (DrawerLayout) findViewById(R.id.home_drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.home_drawer);
        View navigationHeaderView = navigationView.getHeaderView(0);

        if (navigationHeaderView != null) {
            profileImageView = (ImageView) navigationHeaderView.findViewById(R.id.drawer_header_profile_image);
            profileAccountsSpinner = (Spinner) navigationHeaderView.findViewById(R.id.drawer_header_profile_accounts);
            addBusinessProfileIcon = (ImageView) navigationHeaderView.findViewById(R.id.drawer_header_add_business_profile);
            addBusinessProfileIcon.setOnClickListener(mOnClickListener);
        }

        // Update User Data in Navigation Drawer Header
        updateUserData();

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

                                Intent settingsIntent = new Intent(DrawerBaseActivity.this, SettingsScreen.class);
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

    private void updateUserData() {
        // Set Toolbar Title as User Name
        user = UserStore.sharedStore.getUser();

        if (user != null) {

            // Set Profile Picture if one exists for Current User
            if (profileImageView != null) {
                Bitmap bitmap = user.getImageBitmap();

                if (bitmap != null) {
                    profileImageView.setImageBitmap(bitmap);
                    profileImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }

            // Set Profile Accounts if there exists more than one
            if (profileAccountsSpinner != null) {
                // Fetch AccountProfiles saved in SharedPreferences
                accountProfilesList = AccountProfileSharedPrefsManager.getAccountProfileNamesList(this);

                AccountProfileSpinnerAdapter adapter = new AccountProfileSpinnerAdapter(this,
                        R.layout.layout_drawer_spinner_dropdown_item, user.getFullName(), this.accountProfilesList);
                profileAccountsSpinner.setAdapter(adapter);
                profileAccountsSpinner.setOnItemSelectedListener(mOnItemSelectedListener);

                // Set Default s election to the last selected AccountProfile
                String accountProfileSelected = AccountProfileSharedPrefsManager.getAccountProfileSelected(this);
                if (!TextUtils.isEmpty(accountProfileSelected) && this.accountProfilesList.contains(accountProfileSelected)) {
                    profileAccountsSpinner.setSelection(this.accountProfilesList.indexOf(accountProfileSelected));
                }
            }
        }
    }

    private AdapterView.OnItemSelectedListener mOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            if (accountProfilesList != null && !TextUtils.isEmpty(accountProfilesList.get(position))) {
                // Save the selected Account Profile in SharedPreferences
                AccountProfileSharedPrefsManager.saveAccountProfileSelected(DrawerBaseActivity.this,
                        accountProfilesList.get(position));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };
}
