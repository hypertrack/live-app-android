package io.hypertrack.sendeta.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.adapter.BusinessProfilesAdapter;
import io.hypertrack.sendeta.adapter.FavoritePlacesAdapter;
import io.hypertrack.sendeta.adapter.callback.BusinessProfileOnClickListener;
import io.hypertrack.sendeta.adapter.callback.FavoritePlaceOnClickListener;
import io.hypertrack.sendeta.model.*;
import io.hypertrack.sendeta.store.AnalyticsStore;
import io.hypertrack.sendeta.store.LocationStore;
import io.hypertrack.sendeta.store.UserStore;
import io.hypertrack.sendeta.util.Constants;
import io.hypertrack.sendeta.util.SuccessErrorCallback;

public class SettingsScreen extends BaseActivity implements FavoritePlaceOnClickListener, BusinessProfileOnClickListener {

    private final String TAG = "SettingsScreen";

    private ProgressDialog mProgressDialog;
    private ImageView profileImageView;

    private RecyclerView mFavoritePlacesRecyclerView;
    private FavoritePlacesAdapter favoritePlacesAdapter;

    private RecyclerView mBusinessProfilesRecyclerView;
    private BusinessProfilesAdapter businessProfilesAdapter;

    private ScrollView mScrollView;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    private User user;

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
        mFavoritePlacesRecyclerView = (RecyclerView) findViewById(R.id.settings_saved_places);
        mBusinessProfilesRecyclerView = (RecyclerView) findViewById(R.id.settings_business_profiles);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);

        // Scroll User Favorite List to top by default
        mScrollView.smoothScrollTo(0, 0);

        // Set Swipe Refresh Layout Listener
        mSwipeRefreshLayout.setColorSchemeColors(Color.GRAY);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFavorites();
            }
        });

        // Initialize Favorite Places RecyclerView & Set Adapter
        LinearLayoutManager favoritePlacesLayoutManager = new LinearLayoutManager(this);
        favoritePlacesLayoutManager.setAutoMeasureEnabled(true);
        mFavoritePlacesRecyclerView.setLayoutManager(favoritePlacesLayoutManager);
        setupFavoritePlacesAdapter();

        // Initialize Business Profiles RecyclerView & Set Adapter
        LinearLayoutManager businessProfilesLayoutManager = new LinearLayoutManager(this);
        businessProfilesLayoutManager.setAutoMeasureEnabled(true);
        mBusinessProfilesRecyclerView.setLayoutManager(businessProfilesLayoutManager);
        setupBusinessProfilesAdapter();

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

    private void setupBusinessProfilesAdapter() {
        if (user == null) {
            return;
        }

        // Initialize Adapter with User's Business Profiles data
        businessProfilesAdapter = new BusinessProfilesAdapter(null, this);
        mBusinessProfilesRecyclerView.setAdapter(businessProfilesAdapter);
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

    private void refreshFavorites() {
        // Hide the Swipe Refresh Loader
        mSwipeRefreshLayout.setRefreshing(false);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Refresh favorite places");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        UserStore.sharedStore.updatePlaces(new SuccessErrorCallback() {
            @Override
            public void OnSuccess() {
                if (!SettingsScreen.this.isFinishing() && mProgressDialog != null)
                    mProgressDialog.dismiss();

                updateFavoritesAdapter();
            }

            @Override
            public void OnError() {
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
        builder.setMessage("Do you want to delete this favorite address?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
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
                        mProgressDialog.dismiss();
                        updateFavoritesAdapter();
                    }

                    @Override
                    public void OnError() {
                        mProgressDialog.dismiss();
                        Toast.makeText(SettingsScreen.this, R.string.deleting_favorite_place_failed,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
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
    public void onAddBusinessProfile() {
        // Start BusinessProfile Activity with no parameters
        showBusinessProfileScreen(null);
    }

    @Override
    public void onDeleteBusinessProfile(BusinessProfileModel businessProfile) {
        // Start BusinessProfile Activity with to be deleted BusinessProfileModel as parameter
        showBusinessProfileScreen(businessProfile);
    }

    @Override
    public void onVerifyPendingBusinessProfile(BusinessProfileModel businessProfile) {
        // Start BusinessProfile Activity with to be verified BusinessProfileModel as parameter
        showBusinessProfileScreen(businessProfile);
    }

    private void showBusinessProfileScreen(BusinessProfileModel businessProfile) {
        Intent businessProfileIntent = new Intent(SettingsScreen.this, BusinessProfile.class);
        businessProfileIntent.putExtra(BusinessProfile.KEY_BUSINESS_PROFILE, businessProfile);
        startActivityForResult(businessProfileIntent, Constants.BUSINESS_PROFILE_REQUEST_CODE);
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
        }
    }
}
