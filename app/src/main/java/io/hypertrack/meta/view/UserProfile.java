package io.hypertrack.meta.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;

import io.hypertrack.meta.R;
import io.hypertrack.meta.adapter.FavoritePlacesAdapter;
import io.hypertrack.meta.adapter.callback.FavoritePlaceOnClickListener;
import io.hypertrack.meta.model.MetaPlace;
import io.hypertrack.meta.model.User;
import io.hypertrack.meta.store.LocationStore;
import io.hypertrack.meta.store.UserStore;
import io.hypertrack.meta.util.SuccessErrorCallback;

public class UserProfile extends AppCompatActivity implements FavoritePlaceOnClickListener {

    private final String TAG = UserProfile.class.getSimpleName();

    private ScrollView mScrollView;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FavoritePlacesAdapter favoritePlacesAdapter;
    private ProgressDialog mProgressDialog;
    private ImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        this.updateTitle();

        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mScrollView.smoothScrollTo(0, 0);

        mRecyclerView = (RecyclerView) findViewById(R.id.user_profile_saved_places);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeColors(Color.GRAY);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                UserProfile.this.refreshFavorites();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        mRecyclerView.setLayoutManager(layoutManager);
        profileImageView = (ImageView)findViewById(R.id.user_profile_image);

        this.setupFavoritePlacesAdapter();
        this.updateProfileImage();
    }

    private void setupFavoritePlacesAdapter() {
        User user = UserStore.sharedStore.getUser();
        if (user == null) {
            return;
        }

        favoritePlacesAdapter = new FavoritePlacesAdapter(user.getHome(), user.getWork(), user.getOtherPlaces(), this);
        mRecyclerView.setAdapter(favoritePlacesAdapter);
    }

    public void onEditAccountClick(View view) {
        Intent editProfileIntent = new Intent(this, EditProfile.class);
        startActivityForResult(editProfileIntent, EditProfile.EDIT_PROFILE_RESULT_CODE, null);
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
                mProgressDialog.dismiss();
                updateFavoritesAdapter();
            }

            @Override
            public void OnError() {
                mProgressDialog.dismiss();
            }
        });
    }

    private void updateTitle() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            return;
        }

        User user = UserStore.sharedStore.getUser();
        if (user == null) {
            return;
        }

        actionBar.setTitle(user.getFullName());
    }

    private void updateFavoritesAdapter(){
        User user = UserStore.sharedStore.getUser();
        if (user == null) {
            return;
        }

        favoritePlacesAdapter.setHome(user.getHome());
        favoritePlacesAdapter.setWork(user.getWork());
        favoritePlacesAdapter.setOtherPlaces(user.getOtherPlaces());

        favoritePlacesAdapter.notifyDataSetChanged();
    }

    @Override
    public void OnAddHomeClick() {
        MetaPlace newHome = new MetaPlace(MetaPlace.HOME, LocationStore.sharedStore().getCurrentLatLng());
        this.showAddPlace(newHome);
    }

    @Override
    public void OnEditHomeClick(MetaPlace place) {
        this.showAddPlace(new MetaPlace(place));
    }

    @Override
    public void OnAddWorkClick() {
        MetaPlace newWork = new MetaPlace(MetaPlace.WORK, LocationStore.sharedStore().getCurrentLatLng());
        this.showAddPlace(newWork);
    }

    @Override
    public void OnEditWorkClick(MetaPlace place) {
        this.showAddPlace(new MetaPlace(place));
    }

    @Override
    public void OnAddPlaceClick() {
        MetaPlace newPlace = new MetaPlace(LocationStore.sharedStore().getCurrentLatLng());
        this.showAddPlace(newPlace);
    }

    @Override
    public void OnEditPlaceClick(MetaPlace place) {
        this.showAddPlace(new MetaPlace(place));
    }

    @Override
    public void OnDeletePlace(MetaPlace place) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Deleting place");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        UserStore.sharedStore.deletePlace(new MetaPlace(place), new SuccessErrorCallback() {
            @Override
            public void OnSuccess() {
                mProgressDialog.dismiss();
                updateFavoritesAdapter();
            }

            @Override
            public void OnError() {
                mProgressDialog.dismiss();
            }
        });
    }

    private void showAddPlace(MetaPlace place) {
        // For Testing purpose
        Intent addPlace = new Intent(this, AddFavoritePlace.class);
        addPlace.putExtra("meta_place", place);
        startActivityForResult(addPlace, AddFavoritePlace.FAVORITE_PLACE_REQUEST_CODE, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AddFavoritePlace.FAVORITE_PLACE_REQUEST_CODE) {
            this.updateFavoritesAdapter();
        } else if (requestCode == EditProfile.EDIT_PROFILE_RESULT_CODE) {
            this.updateTitle();
            this.updateProfileImage();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //back button inside toolbar
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else
            return false;
    }

    private void updateProfileImage() {
        User user = UserStore.sharedStore.getUser();
        if (user != null) {
            Bitmap bitmap = user.getImageBitmap();
            if (bitmap != null) {
                profileImageView.setImageBitmap(bitmap);
                profileImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        }
    }
}
