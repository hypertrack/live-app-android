package com.hypertrack.live.ui.tracking;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.wrappers.InstantApps;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.hypertrack.live.R;
import com.hypertrack.live.ui.LoaderDecorator;
import com.hypertrack.live.ui.places.SearchPlaceFragment;
import com.hypertrack.live.utils.AppUtils;
import com.hypertrack.sdk.TrackingError;
import com.hypertrack.sdk.views.dao.Trip;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class TrackingFragment extends SupportMapFragment
        implements TrackingPresenter.View, OnMapReadyCallback, FragmentManager.OnBackStackChangedListener {

    public static final int PERMISSIONS_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS = 616;
    public static final int AUTOCOMPLETE_REQUEST_CODE = 111;
    public static final int SET_ON_MAP_REQUEST_CODE = 112;
    public static final int SHARE_REQUEST_CODE = 113;

    private static final DateFormat DATE_FORMAT = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);

    private Snackbar turnOnLocationSnackbar;
    private View blockingView;
    private TextView trackingStatus;
    private FloatingActionButton locationButton;
    private TextView trackingStatusText;
    private View bottomHolder;
    private BottomSheetBehavior bottomHolderSheetBehavior;
    private RecyclerView tripsRecyclerView;
    private View tripInfo;
    private View tripSummaryInfo;
    private View whereAreYouGoing;
    private TextView tripsCount;
    private TextView tripTo;
    private ImageView destinationIcon;
    private TextView destinationAddress;
    private TextView destinationArrival;
    private TextView destinationArrivalTitle;
    private TextView destinationAway;
    private TextView destinationAwayTitle;
    private TextView stats;
    private TextView destination;
    private Button shareButton;
    private Button endTripButton;
    private LoaderDecorator loader;

    private GoogleMap mGoogleMap;
    private MapStyleOptions mapStyleOptions;
    private MapStyleOptions mapStyleOptionsSilver;

    private TrackingPresenter presenter;
    private TripsAdapter tripsAdapter;

    private boolean isMapStyleChanged = false;

    private Timer tripInfoUpdater;

    public static Fragment newInstance(String hyperTrackPublicKey) {
        TrackingFragment fragment = new TrackingFragment();
        Bundle bundle = new Bundle();
        bundle.putString("HYPER_TRACK_PUBLIC_KEY", hyperTrackPublicKey);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    @SuppressLint("InflateParams")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mapView = super.onCreateView(inflater, container, savedInstanceState);
        View fragmentLayout = inflater.inflate(R.layout.fragment_tracking, container, false);
        FrameLayout frameLayout = fragmentLayout.findViewById(R.id.content_frame);
        frameLayout.addView(mapView);
        return fragmentLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String hyperTrackPublicKey = getArguments() != null ?
                getArguments().getString("HYPER_TRACK_PUBLIC_KEY") : null;
        if (TextUtils.isEmpty(hyperTrackPublicKey)) {
            return;
        }

        presenter = new TrackingPresenter(view.getContext(), this, hyperTrackPublicKey);
        loader = new LoaderDecorator(view.getContext());

        mapStyleOptions = MapStyleOptions.loadRawResourceStyle(view.getContext(), R.raw.style_map);
        mapStyleOptionsSilver = MapStyleOptions.loadRawResourceStyle(view.getContext(), R.raw.style_map_silver);

        blockingView = view.findViewById(R.id.blocking_view);
        trackingStatus = view.findViewById(R.id.tracking_status);
        trackingStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (trackingStatusText.getVisibility() == View.VISIBLE) {
                    trackingStatusText.setVisibility(View.GONE);
                } else {
                    trackingStatusText.setVisibility(View.VISIBLE);
                }
            }
        });
        locationButton = view.findViewById(R.id.location_button);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                presenter.setCameraFixedEnabled(true);
                locationButton.hide();
                blockingView.setOnTouchListener(new android.view.View.OnTouchListener() {

                    @Override
                    public boolean onTouch(android.view.View view, MotionEvent motionEvent) {
                        presenter.setCameraFixedEnabled(false);
                        locationButton.show();
                        blockingView.setOnTouchListener(null);
                        return false;
                    }
                });
            }
        });
        trackingStatusText = view.findViewById(R.id.tracking_status_text);
        trackingStatusText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trackingStatusText.setVisibility(View.GONE);
            }
        });

        whereAreYouGoing = view.findViewById(R.id.where_are_you);
        whereAreYouGoing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                whereAreYouGoing.setVisibility(View.INVISIBLE);
                addFragment(new SearchPlaceFragment());
            }
        });

        bottomHolder = view.findViewById(R.id.bottom_holder);
        bottomHolderSheetBehavior = BottomSheetBehavior.from(bottomHolder);
        tripsRecyclerView = view.findViewById(R.id.recycler_view);
        tripInfo = view.findViewById(R.id.trip_info);
        tripsCount = view.findViewById(R.id.trips_count);
        tripTo = view.findViewById(R.id.trip_to);
        destinationIcon = view.findViewById(R.id.destination_icon);
        destinationAddress = view.findViewById(R.id.destination_address);
        destinationArrival = view.findViewById(R.id.destination_arrival);
        destinationArrivalTitle = view.findViewById(R.id.destination_arrival_title);
        destinationAway = view.findViewById(R.id.destination_away);
        destinationAwayTitle = view.findViewById(R.id.destination_away_title);
        tripSummaryInfo = view.findViewById(R.id.trip_summary_info);
        stats = view.findViewById(R.id.stats);
        destination = view.findViewById(R.id.destination);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        tripsRecyclerView.setHasFixedSize(true);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(view.getContext(),
                layoutManager.getOrientation());
        tripsRecyclerView.addItemDecoration(dividerItemDecoration);
        tripsRecyclerView.setLayoutManager(layoutManager);
        tripsAdapter = new TripsAdapter();
        tripsAdapter.setOnItemClickListener(new TripsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView.Adapter<?> adapter, View view, int position) {
                presenter.selectTrip(tripsAdapter.getItem(position));
            }
        });
        tripsRecyclerView.setAdapter(tripsAdapter);

        bottomHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (bottomHolderSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomHolderSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomHolderSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
        shareButton = view.findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.shareTrackMessage();
            }
        });
        endTripButton = view.findViewById(R.id.endTripButton);
        endTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final TripEndConfirmDialog dialog = new TripEndConfirmDialog(getActivity());
                dialog.setEndTripButton(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        presenter.endTrip();
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        turnOnLocationSnackbar = Snackbar.make(locationButton, "", Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.tap_to_turn_location_settings), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        presenter.actionLocationSourceSettings();
                    }
                })
                .setActionTextColor(Color.WHITE);

        getActivity().getSupportFragmentManager().addOnBackStackChangedListener(this);

        getMapAsync(this);

        if (!AppUtils.isGpsProviderEnabled(getActivity())) {
            showTurnOnLocationSnackbar();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        presenter.initMap(googleMap);
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setMapToolbarEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.resume();
    }

    @Override
    public void onTrackingStart() {
        trackingStatus.setActivated(true);
        trackingStatus.setText(R.string.active);
        trackingStatusText.setVisibility(View.GONE);
        trackingStatusText.setText(String.format(getString(R.string.tracking_is), getString(R.string.active).toLowerCase()));
    }


    @Override
    public void onTrackingStop() {
        trackingStatus.setActivated(false);
        trackingStatus.setText(R.string.inactive);
        trackingStatusText.setText(String.format(getString(R.string.tracking_is), getString(R.string.disabled).toLowerCase()));
    }

    @Override
    public void onError(TrackingError trackingError) {
        if (trackingError.code == TrackingError.GPS_PROVIDER_DISABLED_ERROR) {
            showTurnOnLocationSnackbar();
        }
        onDisabled();
    }

    @Override
    public void onActive() {
        if (turnOnLocationSnackbar.isShown()) {
            turnOnLocationSnackbar.dismiss();
        }
        if (isMapStyleChanged && mGoogleMap != null) {
            mGoogleMap.setMapStyle(mapStyleOptions);
            isMapStyleChanged = false;
        }
        shareButton.setEnabled(true);
        endTripButton.setEnabled(true);
    }

    @Override
    public void onDisabled() {
        if (mGoogleMap != null) {
            mGoogleMap.setMapStyle(mapStyleOptionsSilver);
            isMapStyleChanged = true;
        }
        shareButton.setEnabled(false);
        endTripButton.setEnabled(false);
    }

    @Override
    public void onStatusUpdateReceived(@NonNull String statusText) {
    }

    @Override
    public void onDestinationChanged(@NonNull String address) {
        if (getActivity() != null) {
            SearchPlaceFragment fragment = (SearchPlaceFragment) getActivity().getSupportFragmentManager()
                    .findFragmentByTag(SearchPlaceFragment.class.getSimpleName());
            if (fragment != null) {
                fragment.updateAddress(address);
            }
        }
    }

    @Override
    public void showSearch() {
        whereAreYouGoing.setVisibility(View.VISIBLE);
        bottomHolder.setVisibility(View.INVISIBLE);
        presenter.stopMapDestinationMode();
    }

    @Override
    public void updateTripsMenu(@NonNull List<Trip> trips, int selectedTrip) {
        if (getActivity() != null) {
            if (trips.isEmpty()) {

                bottomHolder.setVisibility(View.INVISIBLE);
                whereAreYouGoing.setVisibility(View.VISIBLE);
                stopTripInfoUpdating();
            } else {

                if (getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
                whereAreYouGoing.setVisibility(View.INVISIBLE);
                bottomHolder.setVisibility(View.VISIBLE);

                String text = getString(R.string.you_have_ongoing_trips);
                String tripValue = trips.size() == 1 ?
                        getString(R.string.trip).toLowerCase() : getString(R.string.trips).toLowerCase();
                String tripsCountText = String.format(text, trips.size(), tripValue);
                tripsCount.setText(tripsCountText);

                tripsAdapter.update(trips);
                tripsAdapter.setSelection(selectedTrip);
            }
        }
    }

    private void startTripInfoUpdating(final Trip trip) {

        stopTripInfoUpdating();

        tripInfoUpdater = new Timer();
        tripInfoUpdater.schedule(new TimerTask() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showTripInfo(trip);
                        }
                    });
                }
            }
        }, 60000, 60000);
    }

    private void stopTripInfoUpdating() {
        if (tripInfoUpdater != null) {
            tripInfoUpdater.cancel();
            tripInfoUpdater = null;
        }
    }

    @Override
    public void showTripInfo(@NonNull Trip trip) {
        popBackStack();

        if (getActivity() != null) {
            int origin = R.drawable.starting_position;
            int destination = R.drawable.destination;
            if (trip.getDestination() == null) {

                tripTo.setText(R.string.trip_started_from);
                String valueText = getString(R.string.unknown);
                if (trip.getStartDate() != null) {
                    valueText = DATE_FORMAT.format(trip.getStartDate());
                }
                destinationIcon.setImageResource(origin);
                destinationAddress.setText(valueText);

                String arrivalText = trip.getSummary() == null ?
                        "-"
                        : String.format(getString(R.string._min), TimeUnit.SECONDS.toMinutes(trip.getSummary().getDuration()));
                destinationArrival.setText(arrivalText);
                destinationArrivalTitle.setText(R.string.tracking);

                destinationAway.setText("");
                destinationAwayTitle.setVisibility(View.INVISIBLE);
                stopTripInfoUpdating();
            } else {

                tripTo.setText(R.string.trip_to);
                destinationIcon.setImageResource(destination);
                if (!TextUtils.isEmpty(trip.getDestination().getAddress())) {
                    destinationAddress.setText(trip.getDestination().getAddress());
                } else {
                    String latLng = String.format(getString(R.string.lat_lng),
                            trip.getDestination().getLatitude(), trip.getDestination().getLongitude()
                    );
                    destinationAddress.setText(latLng);
                }

                if (trip.getDestination().getArrivedDate() == null) {
                    if (trip.getEstimate() != null && trip.getEstimate().getRoute() != null
                            && trip.getEstimate().getRoute().getDuration() != null) {

                        int remainingDuration = trip.getEstimate().getRoute().getDuration();
                        Date arriveDate = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(remainingDuration));
                        destinationArrival.setText(DATE_FORMAT.format(arriveDate));
                        if (remainingDuration < 120) {
                            destinationAway.setText(getString(R.string.arriving_now));
                        } else {
                            destinationAway.setText(
                                    String.format(getString(R.string._min), TimeUnit.SECONDS.toMinutes(remainingDuration))
                            );
                        }

                        startTripInfoUpdating(trip);
                    } else {

                        destinationArrival.setText("-");
                        destinationAway.setText("-");
                    }
                    destinationArrivalTitle.setText(R.string.arrival);
                } else {

                    destinationArrival.setText(DATE_FORMAT.format(trip.getDestination().getArrivedDate()));
                    destinationAway.setText("");
                    destinationArrivalTitle.setText(R.string.arrived);
                }

                destinationAwayTitle.setVisibility(View.VISIBLE);
            }

            tripSummaryInfo.setVisibility(View.GONE);
            tripInfo.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showTripSummaryInfo(@NonNull Trip trip) {
        popBackStack();

        stopTripInfoUpdating();

        if (getActivity() != null) {
            if (trip.getDestination() == null && trip.getSummary() == null) {

                stats.setVisibility(View.GONE);
                destination.setVisibility(View.GONE);
            } else {
                if (trip.getSummary() != null) {

                    double miles = trip.getSummary().getDistance() * 0.000621371;
                    long mins = TimeUnit.SECONDS.toMinutes(trip.getSummary().getDuration());
                    String statsText = String.format(getString(R.string.miles_mins), miles, mins);
                    stats.setText(statsText);
                }
                if (trip.getDestination() != null) {

                    destination.setText(trip.getDestination().getAddress());
                }

                stats.setVisibility(View.VISIBLE);
                destination.setVisibility(View.VISIBLE);
            }

            tripInfo.setVisibility(View.GONE);

            tripSummaryInfo.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showProgressBar() {
        loader.start();
    }

    @Override
    public void hideProgressBar() {
        loader.stop();
    }

    @Override
    public void addFragment(@NonNull Fragment fragment) {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_frame, fragment, fragment.getClass().getSimpleName())
                    .addToBackStack(fragment.getClass().getSimpleName())
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public void onBackStackChanged() {
        if (getActivity() != null && getActivity().getSupportFragmentManager().getBackStackEntryCount() == 0) {
            showSearch();
        }
    }

    @Override
    public void popBackStack() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    @SuppressWarnings("ConstantConditions")
    @SuppressLint({"NewApi", "BatteryLife"})
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (getActivity() != null) {
            switch (requestCode) {
                case TrackingFragment.PERMISSIONS_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS: {
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        PowerManager pm = (PowerManager) getActivity().getSystemService(Activity.POWER_SERVICE);
                        String packageName = getActivity().getPackageName();
                        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            Uri uri = Uri.parse("package:" + packageName);
                            intent.setData(uri);
                            getActivity().startActivityForResult(intent, TrackingFragment.PERMISSIONS_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        }
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        presenter.onActivityResult(requestCode, resultCode, data);
    }

    private void showTurnOnLocationSnackbar() {
        if (getActivity() != null && !InstantApps.isInstantApp(getActivity())) {
            turnOnLocationSnackbar.show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.destroy();
        stopTripInfoUpdating();
    }

    private static class TripEndConfirmDialog extends AppCompatDialog {

        public TripEndConfirmDialog(Context context) {
            super(context);
            setContentView(R.layout.dialog_trip_confirm);
            findViewById(R.id.resume).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
        }

        void setEndTripButton(View.OnClickListener onClickListener) {
            findViewById(R.id.end_trip).setOnClickListener(onClickListener);
        }
    }
}
