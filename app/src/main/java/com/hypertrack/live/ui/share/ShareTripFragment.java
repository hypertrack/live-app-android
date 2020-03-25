package com.hypertrack.live.ui.share;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.hypertrack.live.R;
import com.hypertrack.live.ui.LoaderDecorator;
import com.hypertrack.live.ui.MainActivity;
import com.hypertrack.live.ui.OnBackPressedListener;
import com.hypertrack.sdk.views.dao.Trip;

public class ShareTripFragment extends Fragment
        implements ShareTripPresenter.View, OnBackPressedListener {

    public static final String TRIP_ID_KEY = "trip_id";
    public static final String SHARE_URL_KEY = "share_url";

    private ShareTripPresenter presenter;

    private LoaderDecorator loader;
    private View share;
    private String tripId;
    private String shareUrl;

    public static Fragment newInstance(String tripId, String shareUrl) {
        ShareTripFragment fragment = new ShareTripFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TRIP_ID_KEY, tripId);
        bundle.putString(SHARE_URL_KEY, shareUrl);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            tripId = getArguments().getString(TRIP_ID_KEY);
            shareUrl = getArguments().getString(SHARE_URL_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_share_trip, container, false);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter = new ShareTripPresenter(getActivity(), this, shareUrl);

        loader = new LoaderDecorator(getContext());
        View back = view.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        share = view.findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.shareTrackMessage();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                presenter.subscribeTripUpdates(googleMap, tripId);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.pause();
    }

    @Override
    public void showProgressBar() {
        if (getActivity() != null) {
            loader.start();
        }
    }

    @Override
    public void hideProgressBar() {
        if (getActivity() != null) {
            loader.stop();
        }
    }

    @Override
    public void onTripUpdate(@NonNull Trip trip) {
        share.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onBackPressed() {
        presenter.endTrip();
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.destroy();
    }
}
