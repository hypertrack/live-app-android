package com.hypertrack.live.ui.places;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.hypertrack.live.R;
import com.hypertrack.live.models.PlaceModel;
import com.hypertrack.live.ui.LoaderDecorator;
import com.hypertrack.live.ui.MainActivity;
import com.hypertrack.live.ui.share.ShareTripFragment;
import com.hypertrack.live.utils.HTTextWatcher;

import java.util.List;

public class SearchPlaceFragment extends Fragment implements OnMapReadyCallback, SearchPlacePresenter.View {

    private SearchPlacePresenter presenter;

    private EditText search;
    private View destinationOnMap;
    private View setOnMap;
    private View confirm;
    private PlacesAdapter placesAdapter;
    private LoaderDecorator loader;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_place, container, false);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter = new SearchPlacePresenter(getActivity(), this);
        loader = new LoaderDecorator(getContext());

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        getActivity().setTitle(R.string.where_are_you_going);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        search = view.findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.setMapDestinationModeEnable(false);
            }
        });
        search.addTextChangedListener(new HTTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                presenter.search(charSequence.toString());
            }
        });
        search.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);

        view.findViewById(R.id.no_destination).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.setMapDestinationModeEnable(false);
                presenter.startTrip();
            }
        });
        destinationOnMap = view.findViewById(R.id.destination_on_map);
        setOnMap = view.findViewById(R.id.set_on_map);
        setOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.setMapDestinationModeEnable(true);
            }
        });
        confirm = view.findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.confirm();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        RecyclerView locationsRecyclerView = view.findViewById(R.id.locations);
        locationsRecyclerView.setHasFixedSize(true);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(view.getContext(),
                layoutManager.getOrientation());
        locationsRecyclerView.addItemDecoration(dividerItemDecoration);
        locationsRecyclerView.setLayoutManager(layoutManager);
        placesAdapter = new PlacesAdapter();
        placesAdapter.setOnItemClickListener(new PlacesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView.Adapter<?> adapter, View view, int position) {
                presenter.selectPlace(placesAdapter.getItem(position));
            }
        });
        locationsRecyclerView.setAdapter(placesAdapter);

        View.OnTouchListener hideSoftInputOnTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideSoftInput();
                return false;
            }
        };
        view.setOnTouchListener(hideSoftInputOnTouchListener);
        locationsRecyclerView.setOnTouchListener(hideSoftInputOnTouchListener);

        presenter.search(null);
        ((MainActivity) getActivity()).getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        presenter.initMap(googleMap);
    }

    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
        search.clearFocus();
    }

    @Override
    public void updateAddress(String address) {
        search.setText(address);
    }

    @Override
    public void updateList(List<PlaceModel> list) {
        placesAdapter.clear();
        placesAdapter.addAll(list);
        placesAdapter.notifyDataSetChanged();
    }

    @Override
    public void showSetOnMap() {
        hideSoftInput();
        setOnMap.setVisibility(View.GONE);
        destinationOnMap.setVisibility(View.VISIBLE);
        confirm.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideSetOnMap() {
        setOnMap.setVisibility(View.VISIBLE);
        destinationOnMap.setVisibility(View.GONE);
        confirm.setVisibility(View.GONE);
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
    public void addShareTripFragment(String tripId, String shareUrl) {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).beginFragmentTransaction(ShareTripFragment.newInstance(tripId, shareUrl))
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.destroy();
    }
}
