package io.hypertrack.sendeta.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.internal.consumer.view.Placeline.PlacelineFragment;

import io.hypertrack.sendeta.R;

/**
 * Created by Aman Jain on 24/05/17.
 */

public class Placeline extends AppCompatActivity {

    private static final String TAG = Placeline.class.getSimpleName();
    PlacelineFragment placelineFragment;
    FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeline);
        if (!HyperTrack.isTracking())
            HyperTrack.startTracking();
        placelineFragment = (PlacelineFragment) getSupportFragmentManager().findFragmentById(R.id.placeline_fragment);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Placeline.this, Home.class);
                intent.putExtra("class_from", Placeline.class.getSimpleName());
                startActivity(intent);
                //overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });
    }
}
