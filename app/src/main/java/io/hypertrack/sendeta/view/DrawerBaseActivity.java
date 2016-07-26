package io.hypertrack.sendeta.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;

import java.util.List;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.adapter.MembershipSpinnerAdapter;
import io.hypertrack.sendeta.model.Membership;
import io.hypertrack.sendeta.model.User;
import io.hypertrack.sendeta.store.AnalyticsStore;
import io.hypertrack.sendeta.store.UserStore;

/**
 * Created by piyush on 22/07/16.
 */
public class DrawerBaseActivity extends BaseActivity {

    private User user;
    private ImageView profileImageView;
    private Spinner drawerMembershipsSpinner;
    private ImageView addBusinessProfileIcon;

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    private List<Membership> drawerMembershipsList;

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
            drawerMembershipsSpinner = (Spinner) navigationHeaderView.findViewById(R.id.drawer_header_memberships);
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

                if (drawerMembershipsList != null && drawerMembershipsSpinner != null)
                    setSelectedMembership();

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

            if (drawerMembershipsSpinner != null) {
                // Fetch Memberships saved in DB
                drawerMembershipsList = user.getAcceptedMemberships();
                setupDrawerMembershipsSpinner();
            }
        }
    }

    private void setupDrawerMembershipsSpinner() {
        if (drawerMembershipsList != null) {
            MembershipSpinnerAdapter adapter = new MembershipSpinnerAdapter(this, R.layout.layout_drawer_spinner,
                    R.layout.layout_drawer_spinner_dropdown_item, user.getFullName(), this.drawerMembershipsList);
            drawerMembershipsSpinner.setAdapter(adapter);
            drawerMembershipsSpinner.setOnItemSelectedListener(mOnDrawerMembershipSelectedListener);

            // Set Previously Selected Membership in Spinner
            setSelectedMembership();
        } else {
            drawerMembershipsSpinner.setVisibility(View.GONE);
        }
    }

    private void setSelectedMembership(){
        Integer selectedMembershipAccId = UserStore.sharedStore.getSelectedMembershipAccountId();

        if (selectedMembershipAccId != null) {
            // Set Default selection to the last selected Membership
            Membership selectedMembership = user.getMembershipForAccountId(selectedMembershipAccId);

            if (selectedMembership != null && selectedMembership.isAccepted() && drawerMembershipsList.contains(selectedMembership)) {
                drawerMembershipsSpinner.setSelection(drawerMembershipsList.indexOf(selectedMembership));
            }
        }
    }

    private AdapterView.OnItemSelectedListener mOnDrawerMembershipSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            if (drawerMembershipsList != null) {
                // Update the selected Membership in UserStore
                UserStore.sharedStore.updateSelectedMembership(drawerMembershipsList.get(position).getAccountId());
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };
}
