package com.hypertrack.live.ui.tracking;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hypertrack.live.R;
import com.hypertrack.live.ui.LoaderDecorator;
import com.hypertrack.live.ui.MainActivity;
import com.hypertrack.live.ui.places.SearchPlaceFragment;
import com.hypertrack.live.views.Snackbar;
import com.hypertrack.sdk.views.dao.Trip;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TrackingFragment extends Fragment implements OnMapReadyCallback, TrackingPresenter.View {

    private static final DateFormat DATE_FORMAT = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);

    private Snackbar tripConfirmSnackbar;
    private View blockingView;
    private FloatingActionButton locationButton;
    private View bottomHolder;
    private BottomSheetBehavior bottomHolderSheetBehavior;
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
    private LoaderDecorator loader;

    private TrackingPresenter presenter;
    private TripsAdapter tripsAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tracking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter = new TrackingPresenter(view.getContext(), this);
        loader = new LoaderDecorator(view.getContext());

        blockingView = view.findViewById(R.id.blocking_view);
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

        whereAreYouGoing = view.findViewById(R.id.where_are_you);
        whereAreYouGoing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).beginFragmentTransaction(new SearchPlaceFragment())
                        .addToBackStack(null)
                        .commitAllowingStateLoss();
            }
        });

        bottomHolder = view.findViewById(R.id.bottom_holder);
        bottomHolderSheetBehavior = BottomSheetBehavior.from(bottomHolder);
        RecyclerView tripsRecyclerView = view.findViewById(R.id.recycler_view);
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
        Button shareButton = view.findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.shareTrackMessage();
            }
        });
        Button endTripButton = view.findViewById(R.id.endTripButton);
        endTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tripConfirmSnackbar.show();
            }
        });

        tripConfirmSnackbar = Snackbar.make(view.getRootView(), R.layout.snackbar_trip_confirm, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.id.resume, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tripConfirmSnackbar.dismiss();
                    }
                })
                .setAction(R.id.end_trip, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        presenter.endTrip();
                        tripConfirmSnackbar.dismiss();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).getMapAsync(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.pause();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        presenter.subscribeUpdates(googleMap);
    }

    @Override
    public void onStatusUpdateReceived(@NonNull String statusText) {
    }

    @Override
    public void showSearch() {
        whereAreYouGoing.setVisibility(View.VISIBLE);
        bottomHolder.setVisibility(View.INVISIBLE);
    }

    @Override
    public void updateTripsMenu(@NonNull List<Trip> trips, int selectedTrip) {
        if (getActivity() != null) {
            if (trips.isEmpty()) {

                if (bottomHolder.getVisibility() == View.VISIBLE) {
                    bottomHolder.setVisibility(View.INVISIBLE);
                    whereAreYouGoing.setVisibility(View.VISIBLE);
                }
                presenter.stopTripInfoUpdating();
            } else {

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

    @Override
    public void showTripInfo(@NonNull Trip trip) {

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
                presenter.stopTripInfoUpdating();
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

                        presenter.startTripInfoUpdating(trip);
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

        presenter.stopTripInfoUpdating();
        tripConfirmSnackbar.dismiss();

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
    public void onDestroyView() {
        super.onDestroyView();
        presenter.destroy();
    }
}
