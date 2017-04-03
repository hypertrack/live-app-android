package io.hypertrack.sendeta.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.hypertrack.lib.HyperTrackMapAdapter;
import com.hypertrack.lib.HyperTrackMapFragment;

import io.hypertrack.sendeta.R;

/**
 * Created by Aman Jain on 06/03/17.
 */

public class HyperTrackMapActivity extends AppCompatActivity {
    private HyperTrackMapFragment hyperTrackMapFragment;
    private MyMapAdapter mapAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_track);

        hyperTrackMapFragment = (HyperTrackMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapAdapter = new MyMapAdapter(this);
        hyperTrackMapFragment.setHTMapAdapter(mapAdapter);
    }

    private class MyMapAdapter extends HyperTrackMapAdapter {
        private Context mContext;

        public MyMapAdapter(Context mContext) {
            super(mContext);
            this.mContext = mContext;
        }

        @Override
        public boolean showHeroMarkerForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
            return true;
        }
    }
}
