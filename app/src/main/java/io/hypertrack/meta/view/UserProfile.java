package io.hypertrack.meta.view;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import io.hypertrack.meta.R;
import io.hypertrack.meta.adapter.SavedPlacesAdapter;
import io.hypertrack.meta.model.MetaPlace;
import io.hypertrack.meta.model.User;
import io.hypertrack.meta.store.UserStore;

public class UserProfile extends AppCompatActivity {

    private final String TAG = "UserProfile";

    private RecyclerView mRecyclerView;
    private SavedPlacesAdapter savedPlacesAdapter;

    private ArrayList<MetaPlace> savedPlaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.updateTitle();

        // Fetch User's Saved Places
        savedPlaces = new ArrayList<>();

        mRecyclerView = (RecyclerView) findViewById(R.id.user_profile_saved_places);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        mRecyclerView.setLayoutManager(layoutManager);

        savedPlacesAdapter = new SavedPlacesAdapter(savedPlaces);
        mRecyclerView.setAdapter(savedPlacesAdapter);
    }

    public void doneButtonClicked(MenuItem menuItem) {
        finish();
    }

    public void onEditAccountClick(View view) {
        // Action to be performed on EditAccount
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
}
