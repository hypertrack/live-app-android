package io.hypertrack.sendeta.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.adapter.FavoritePlacesAdapter;
import io.hypertrack.sendeta.adapter.MembershipsAdapter;
import io.hypertrack.sendeta.adapter.callback.FavoritePlaceOnClickListener;
import io.hypertrack.sendeta.adapter.callback.MembershipOnClickListener;
import io.hypertrack.sendeta.model.Membership;
import io.hypertrack.sendeta.model.MetaPlace;
import io.hypertrack.sendeta.model.User;
import io.hypertrack.sendeta.store.AnalyticsStore;
import io.hypertrack.sendeta.store.LocationStore;
import io.hypertrack.sendeta.store.UserStore;
import io.hypertrack.sendeta.store.callback.UserStoreDeleteMembershipCallback;
import io.hypertrack.sendeta.store.callback.UserStoreGetUserDataCallback;
import io.hypertrack.sendeta.util.Constants;
import io.hypertrack.sendeta.util.SuccessErrorCallback;

public class SettingsScreen extends BaseActivity implements FavoritePlaceOnClickListener, MembershipOnClickListener {

    private final String TAG = "SettingsScreen";

    private ProgressDialog mProgressDialog;
    private ImageView profileImageView;

    private RecyclerView mFavoritePlacesRecyclerView;
    private FavoritePlacesAdapter favoritePlacesAdapter;

    private RecyclerView mMembershipsRecyclerView;
    private MembershipsAdapter membershipsAdapter;

    private NestedScrollView mScrollView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private User user;
    private boolean isRefreshFavoritesCompleted, isRefreshUserDataCompleted;

    private int profileImageClickedCount = 0;
    private Handler profileImageClickedHandler;

    private View.OnClickListener onProfileImageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            profileImageClickedCount++;

            if (profileImageClickedCount >= 5) {
                Intent shiftScreenIntent = new Intent(SettingsScreen.this, ShiftsScreen.class);
                startActivity(shiftScreenIntent);
                return;
            }

            if (profileImageClickedHandler == null) {
                profileImageClickedHandler = new Handler();

                profileImageClickedHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        profileImageClickedCount = 0;
                        profileImageClickedHandler = null;
                    }
                }, 3000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize Toolbar
        initToolbar();

        // Set Toolbar Title as User Name
        user = UserStore.sharedStore.getUser();
        if (user != null)
            setTitle(user.getFullName());

        // Initialize UI Views
        profileImageView = (ImageView) findViewById(R.id.settings_image);
        profileImageView.setOnClickListener(onProfileImageClickListener);
        mFavoritePlacesRecyclerView = (RecyclerView) findViewById(R.id.settings_saved_places);
        mMembershipsRecyclerView = (RecyclerView) findViewById(R.id.settings_business_profiles);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mScrollView = (NestedScrollView) findViewById(R.id.scrollView);

        // Scroll User Favorite List to top by default
        mScrollView.smoothScrollTo(0, 0);

        // Set Swipe Refresh Layout Listener
        mSwipeRefreshLayout.setColorSchemeColors(Color.GRAY);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshUserData();
            }
        });

        // Initialize Favorite Places RecyclerView & Set Adapter
        LinearLayoutManager favoritePlacesLayoutManager = new LinearLayoutManager(this);
        favoritePlacesLayoutManager.setAutoMeasureEnabled(true);
        mFavoritePlacesRecyclerView.setLayoutManager(favoritePlacesLayoutManager);
        mFavoritePlacesRecyclerView.setNestedScrollingEnabled(false);
        setupFavoritePlacesAdapter();

        // Initialize Business Profiles RecyclerView & Set Adapter
        LinearLayoutManager membershipsLayoutManager = new LinearLayoutManager(this);
        membershipsLayoutManager.setAutoMeasureEnabled(true);
        mMembershipsRecyclerView.setLayoutManager(membershipsLayoutManager);
        mMembershipsRecyclerView.setNestedScrollingEnabled(false);
        setupMembershipsAdapter();

        // Set up User's Profile Image
        updateProfileImage();
    }

    private void setupFavoritePlacesAdapter() {
        if (user == null) {
            return;
        }

        // Initialize Adapter with User's Favorites data
        favoritePlacesAdapter = new FavoritePlacesAdapter(user.getHome(), user.getWork(), user.getOtherPlaces(), this);
        mFavoritePlacesRecyclerView.setAdapter(favoritePlacesAdapter);
    }

    private void setupMembershipsAdapter() {
        if (user == null) {
            return;
        }

        // Fetch Memberships saved in DB
        List<Membership> membershipsList = user.getActiveBusinessMemberships();

        // Initialize Adapter with User's Memberships data
        membershipsAdapter = new MembershipsAdapter(this, membershipsList, this);
        mMembershipsRecyclerView.setAdapter(membershipsAdapter);
    }

    private void updateProfileImage() {
        if (user != null) {
            Bitmap bitmap = user.getImageBitmap();

            // Set Profile Picture if one exists for Current User
            if (bitmap != null) {
                profileImageView.setImageBitmap(bitmap);
                profileImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        }
    }

    private void refreshUserData() {
        // Hide the Swipe Refresh Loader
        mSwipeRefreshLayout.setRefreshing(false);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Refreshing profile data");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        UserStore.sharedStore.updatePlaces(new SuccessErrorCallback() {
            @Override
            public void OnSuccess() {
                isRefreshFavoritesCompleted = true;
                if (!SettingsScreen.this.isFinishing() && mProgressDialog != null
                        && isRefreshUserDataCompleted)
                    mProgressDialog.dismiss();

                updateFavoritesAdapter();
            }

            @Override
            public void OnError() {
                isRefreshFavoritesCompleted = true;
                if (!SettingsScreen.this.isFinishing() && mProgressDialog != null
                        && isRefreshUserDataCompleted)
                    mProgressDialog.dismiss();
            }
        });

        UserStore.sharedStore.getUserData(new UserStoreGetUserDataCallback() {
            @Override
            public void OnSuccess(User user) {
                isRefreshUserDataCompleted = true;
                if (!SettingsScreen.this.isFinishing() && mProgressDialog != null
                        && isRefreshFavoritesCompleted)
                    mProgressDialog.dismiss();

                updateMembershipsAdapter();
            }

            @Override
            public void OnError() {
                isRefreshUserDataCompleted = true;
                if (!SettingsScreen.this.isFinishing() && mProgressDialog != null
                        && isRefreshUserDataCompleted)
                    mProgressDialog.dismiss();
            }
        });
    }

    private void updateFavoritesAdapter() {
        if (user == null) {
            return;
        }

        favoritePlacesAdapter.setHome(user.getHome());
        favoritePlacesAdapter.setWork(user.getWork());
        favoritePlacesAdapter.setOtherPlaces(user.getOtherPlaces());

        favoritePlacesAdapter.notifyDataSetChanged();
    }

    private void updateMembershipsAdapter() {
        if (user == null) {
            return;
        }

        membershipsAdapter.setMembershipsList(user.getActiveBusinessMemberships());
        membershipsAdapter.notifyDataSetChanged();
    }

    public void onEditAccountClick(View view) {
        Intent editProfileIntent = new Intent(this, EditProfile.class);
        startActivityForResult(editProfileIntent, Constants.EDIT_PROFILE_REQUEST_CODE, null);

        AnalyticsStore.getLogger().tappedEditProfile();
    }

    @Override
    public void OnAddHomeClick() {
        MetaPlace newHome = new MetaPlace(MetaPlace.HOME, LocationStore.sharedStore().getCurrentLatLng());
        showAddPlaceScreen(newHome);
    }

    @Override
    public void OnEditHomeClick(MetaPlace place) {
        showAddPlaceScreen(new MetaPlace(place));
    }

    @Override
    public void OnAddWorkClick() {
        MetaPlace newWork = new MetaPlace(MetaPlace.WORK, LocationStore.sharedStore().getCurrentLatLng());
        showAddPlaceScreen(newWork);
    }

    @Override
    public void OnEditWorkClick(MetaPlace place) {
        showAddPlaceScreen(new MetaPlace(place));
    }

    @Override
    public void OnAddPlaceClick() {
        MetaPlace newPlace = new MetaPlace(LocationStore.sharedStore().getCurrentLatLng());
        showAddPlaceScreen(newPlace);
    }

    @Override
    public void OnEditPlaceClick(MetaPlace place) {
        showAddPlaceScreen(new MetaPlace(place));
    }

    private void showAddPlaceScreen(MetaPlace place) {
        // Start an intent to AddFavoritePlace with MetaPlace object as parameter
        Intent addFavPlaceIntent = new Intent(this, AddFavoritePlace.class);
        addFavPlaceIntent.putExtra("meta_place", place);
        startActivityForResult(addFavPlaceIntent, Constants.FAVORITE_PLACE_REQUEST_CODE, null);
    }

    @Override
    public void OnDeletePlace(final MetaPlace place) {

        // Create a confirmation Dialog for Deleting a User Favorite Place
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.favorite_place_delete_dialog_message);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // User agreed to proceed with Deleting Favorite Place
                mProgressDialog = new ProgressDialog(SettingsScreen.this);
                mProgressDialog.setMessage("Deleting place");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();

                // Delete User Favorite Place from DB & Server
                UserStore.sharedStore.deletePlace(new MetaPlace(place), new SuccessErrorCallback() {
                    @Override
                    public void OnSuccess() {
                        if (mProgressDialog != null && !SettingsScreen.this.isFinishing())
                            mProgressDialog.dismiss();

                        updateFavoritesAdapter();
                    }

                    @Override
                    public void OnError() {
                        if (mProgressDialog != null && !SettingsScreen.this.isFinishing())
                            mProgressDialog.dismiss();

                        Toast.makeText(SettingsScreen.this, R.string.deleting_favorite_place_failed,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User dismissed to delete Favorite Place
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onAddMembership() {
        // Start BusinessProfile Activity with no parameters
        showBusinessProfileScreen(0);
    }

    @Override
    public void onVerifyPendingMembership(Membership membership) {
        // Start BusinessProfile Activity with to be verified Membership as parameter
        showBusinessProfileScreen(membership.getAccountId());
    }

    private void showBusinessProfileScreen(int membershipAccountId) {
        Intent businessProfileIntent = new Intent(SettingsScreen.this, BusinessProfile.class);
        if (membershipAccountId != 0) {
            businessProfileIntent.putExtra(BusinessProfile.KEY_MEMBERSHIP_INVITE, true);
            businessProfileIntent.putExtra(BusinessProfile.KEY_MEMBERSHIP_ACCOUNT_ID, membershipAccountId);
        } else {
            businessProfileIntent.putExtra(BusinessProfile.KEY_MEMBERSHIP_INVITE, false);
        }
        startActivityForResult(businessProfileIntent, Constants.BUSINESS_PROFILE_REQUEST_CODE);
    }

    @Override
    public void onDeleteMembership(final Membership membership) {
        // Start BusinessProfile Activity with to be deleted Memberships as parameter
        // Create a confirmation Dialog for Deleting a User Favorite Place
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.business_profile_delete_dialog_msg);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // User agreed to proceed with Deleting Business Profile
                mProgressDialog = new ProgressDialog(SettingsScreen.this);
                mProgressDialog.setMessage("Deleting business profile");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();

                UserStore.sharedStore.deleteMembership(membership, new UserStoreDeleteMembershipCallback() {
                    @Override
                    public void OnSuccess(String accountName) {
                        if (mProgressDialog != null && !SettingsScreen.this.isFinishing())
                            mProgressDialog.dismiss();

                        if (TextUtils.isEmpty(accountName))
                            accountName = "Business";

                        Toast.makeText(SettingsScreen.this,
                                getString(R.string.business_profile_deleted_success_msg, accountName),
                                Toast.LENGTH_SHORT).show();

                        updateMembershipsAdapter();
                    }

                    @Override
                    public void OnError() {
                        if (mProgressDialog != null && !SettingsScreen.this.isFinishing())
                            mProgressDialog.dismiss();

                        Toast.makeText(SettingsScreen.this,
                                getString(R.string.business_profile_delete_error_msg, membership.getAccountName()),
                                Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User dismissed to delete Favorite Place
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.FAVORITE_PLACE_REQUEST_CODE) {
            updateFavoritesAdapter();

        } else if (requestCode == Constants.EDIT_PROFILE_REQUEST_CODE) {

            // Set Toolbar Title as User Name
            if (user != null)
                setTitle(user.getFullName());

            updateProfileImage();
        } else if (requestCode == Constants.BUSINESS_PROFILE_REQUEST_CODE) {
            // Update the Business Profile List Data on successful addition of Business Profile
            if (data != null && data.hasExtra("success")) {
                if (data.getBooleanExtra("success", false)) {
                    updateMembershipsAdapter();
                }
            }
        }
    }
}
