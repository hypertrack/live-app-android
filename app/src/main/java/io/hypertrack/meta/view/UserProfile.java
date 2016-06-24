package io.hypertrack.meta.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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

    private RecyclerView mRecyclerView;
    private FavoritePlacesAdapter favoritePlacesAdapter;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.updateTitle();

        mRecyclerView = (RecyclerView) findViewById(R.id.user_profile_saved_places);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        mRecyclerView.setLayoutManager(layoutManager);

        this.setupFavoritePlacesAdapter();
    }

    private void setupFavoritePlacesAdapter() {
        User user = UserStore.sharedStore.getUser();
        if (user == null) {
            return;
        }

        favoritePlacesAdapter = new FavoritePlacesAdapter(user.getHome(), user.getWork(), user.getOtherPlaces(), this);
        mRecyclerView.setAdapter(favoritePlacesAdapter);
    }

    public void refreshButtonClicked(MenuItem menuItem) {
        this.refreshFavorites();
    }

    public void onEditAccountClick(View view) {
        Intent editProfileIntent = new Intent(this, EditProfile.class);
        startActivityForResult(editProfileIntent, EditProfile.EDIT_PROFILE_RESULT_CODE, null);
    }

//    public void onDoneButtonClicked(MenuItem v) {
//        // TODO: 23/06/16 Add Done Btn functionality
//        finish();
//    }

    private void refreshFavorites() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_profile, menu);
        return super.onCreateOptionsMenu(menu);
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
        }
    }
}
