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
