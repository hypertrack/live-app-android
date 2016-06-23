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
import io.hypertrack.meta.model.User;
import io.hypertrack.meta.store.UserStore;
import io.hypertrack.meta.util.SuccessErrorCallback;

public class UserProfile extends AppCompatActivity {

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

        favoritePlacesAdapter = new FavoritePlacesAdapter(user.getHome(), user.getWork(), user.getOtherPlaces());
        mRecyclerView.setAdapter(favoritePlacesAdapter);
    }

    public void refreshButtonClicked(MenuItem menuItem) {
        this.refreshFavorites();
    }

    public void onEditAccountClick(View view) {
        Intent editProfileIntent = new Intent(this, EditProfile.class);
        startActivity(editProfileIntent);
    }

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
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
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
}
