package io.hypertrack.sendeta.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.internal.consumer.view.Placeline.PlacelineFragment;

import io.hypertrack.sendeta.R;

/**
 * Created by Aman Jain on 24/05/17.
 */

public class Placeline extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = Placeline.class.getSimpleName();
    PlacelineFragment placelineFragment;
    FloatingActionButton floatingActionButton;
    private DrawerLayout drawer;
    NavigationView navigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeline);
        if (!HyperTrack.isTracking())
            HyperTrack.startTracking();
        initUI();
    }

    private void initUI() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        placelineFragment = (PlacelineFragment) getSupportFragmentManager().findFragmentById(R.id.placeline_fragment);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Placeline.this, Home.class);
                intent.putExtra("class_from", Placeline.class.getSimpleName());
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawer.closeDrawers();
        if (item.getItemId() == R.id.edit_profile)
            startActivity(new Intent(this, Profile.class));
        else if (item.getItemId() == R.id.edit_activity) {
            startActivity(new Intent(this, FeedbackPlaceline.class));
        }
        return true;
    }
}
