package io.hypertrack.sendeta.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.hypertrack.lib.internal.consumer.models.KeyPair;
import com.hypertrack.lib.placeline.PlacelineActivitySummaryView;
import com.hypertrack.lib.tracking.MapProvider.MapFragmentView;
import com.hypertrack.lib.tracking.basemvp.BaseView;
import com.hypertrack.lib.tracking.model.MarkerModel;

import io.hypertrack.sendeta.R;

public class StartView extends BaseView {

    private TextView mUserName, mSummaryTitle;
    private ImageButton mSettingButton;

    public static StartView newInstance(){
        StartView startView = new StartView();
        Bundle bundle = new Bundle();
        bundle.putInt("type", MapFragmentView.Type.CUSTOM);
        startView.setArguments(bundle);
        return startView;
    }

    @Override
    public void clearView() {

    }

    @Override
    public void moveCamera(LatLng latLng) {

    }

    @Override
    public void refreshViews() {

    }

    @Override
    public MarkerModel getInfoWindowModel(KeyPair keyPair) {
        return null;
    }

    @Override
    public void onMarkerClick(KeyPair keyPair, Marker marker) {

    }

    @Override
    public void onMarkerInfoWindowClick(KeyPair keyPair) {

    }

    @Override
    public void removeBottomItems() {

    }

    @Override
    public void detachView() {

    }

    @Override
    public boolean isViewAttached() {
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = super.onCreateView(inflater, container, savedInstanceState);
        mParent = (RelativeLayout) ((LayoutInflater)
                getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.start_view_fragment, null);
        setType(MapFragmentView.Type.CUSTOM);
        initView();
        return view;
    }

    private void initView() {
        mUserName = mParent.findViewById(R.id.user_name);
        mSummaryTitle = mParent.findViewById(R.id.summary_title);
        mSettingButton = mParent.findViewById(R.id.setting);
        mSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        addBottomView(mParent);
        setCTAButtonTitle("SHARE YOUR LOCATION");
        mUserName.setText("Howdy Aman Jain!");
        showCTAButton();
    }
}
