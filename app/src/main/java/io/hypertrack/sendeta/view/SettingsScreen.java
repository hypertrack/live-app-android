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
import io.hypertrack.sendeta.adapter.FavoritePlacesAdapter;
import io.hypertrack.sendeta.adapter.callback.FavoritePlaceOnClickListener;
import io.hypertrack.sendeta.model.MetaPlace;
import io.hypertrack.sendeta.model.User;
import io.hypertrack.sendeta.store.AnalyticsStore;
import io.hypertrack.sendeta.store.LocationStore;
import io.hypertrack.sendeta.store.UserStore;
import io.hypertrack.sendeta.util.Constants;
import io.hypertrack.sendeta.util.SuccessErrorCallback;

public class SettingsScreen extends BaseActivity implements FavoritePlaceOnClickListener {

    private final String TAG = "SettingsScreen";

    private ScrollView mScrollView;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FavoritePlacesAdapter favoritePlacesAdapter;
    private ProgressDialog mProgressDialog;
    private ImageView profileImageView;

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
        profileImageView = (ImageView)findViewById(R.id.settings_image);
        mRecyclerView = (RecyclerView) findViewById(R.id.settings_saved_places);
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

        // Initialize RecyclerView & Set Adapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        mRecyclerView.setLayoutManager(layoutManager);

        setupFavoritePlacesAdapter();
        updateProfileImage();
    }

    private void setupFavoritePlacesAdapter() {
        if (user == null) {
            return;
        }

        // Initialize Adapter with User's Favorites data
        favoritePlacesAdapter = new FavoritePlacesAdapter(user.getHome(), user.getWork(), user.getOtherPlaces(), this);
        mRecyclerView.setAdapter(favoritePlacesAdapter);
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

    private void updateFavoritesAdapter(){
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
        startActivityForResult(editProfileIntent, EditProfile.EDIT_PROFILE_RESULT_CODE, null);

        AnalyticsStore.getLogger().tappedEditProfile();
    }

    @Override
    public void OnAddHomeClick() {
        MetaPlace newHome = new MetaPlace(MetaPlace.HOME, LocationStore.sharedStore().getCurrentLatLng());
        showAddPlace(newHome);
    }

    @Override
    public void OnEditHomeClick(MetaPlace place) {
        showAddPlace(new MetaPlace(place));
    }

    @Override
    public void OnAddWorkClick() {
        MetaPlace newWork = new MetaPlace(MetaPlace.WORK, LocationStore.sharedStore().getCurrentLatLng());
        showAddPlace(newWork);
    }

    @Override
    public void OnEditWorkClick(MetaPlace place) {
        showAddPlace(new MetaPlace(place));
    }

    @Override
    public void OnAddPlaceClick() {
        MetaPlace newPlace = new MetaPlace(LocationStore.sharedStore().getCurrentLatLng());
        showAddPlace(newPlace);
    }

    @Override
    public void OnEditPlaceClick(MetaPlace place) {
        showAddPlace(new MetaPlace(place));
    }

    private void showAddPlace(MetaPlace place) {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.FAVORITE_PLACE_REQUEST_CODE) {
            updateFavoritesAdapter();

        } else if (requestCode == EditProfile.EDIT_PROFILE_RESULT_CODE) {

            // Set Toolbar Title as User Name
            if (user != null)
                setTitle(user.getFullName());

            updateProfileImage();
        }
    }
}
