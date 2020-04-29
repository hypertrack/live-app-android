package com.hypertrack.live.ui.places;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

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
import com.hypertrack.backend.AbstractBackendProvider;
import com.hypertrack.live.R;
import com.hypertrack.live.models.PlaceModel;
import com.hypertrack.live.ui.LoaderDecorator;
import com.hypertrack.live.ui.MainActivity;
import com.hypertrack.live.ui.share.ShareTripFragment;
import com.hypertrack.live.utils.HTTextWatcher;

import java.util.List;

public class SearchPlaceFragment extends Fragment implements OnMapReadyCallback, SearchPlacePresenter.View {

    private final AbstractBackendProvider mBackendProvider;
    private Config config;

    private SearchPlacePresenter presenter;

    private EditText search;
    private View destinationOnMap;
    private View offlineView;
    private View home;
    private View setHome;
    private View homeInfo;
    private View setOnMap;
    private View confirm;
    private PlacesAdapter placesAdapter;
    private LoaderDecorator loader;

    public static SearchPlaceFragment newInstance(Config config, @NonNull AbstractBackendProvider backendProvider) {
        SearchPlaceFragment fragment = new SearchPlaceFragment(backendProvider);
        Bundle bundle = new Bundle();
        bundle.putParcelable("config", config);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
            config = new Config("");
        } else {
            config = getArguments().getParcelable("config");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_place, container, false);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        getActivity().setTitle(config.titleResId);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        setHasOptionsMenu(true);

        search = view.findViewById(R.id.search);
        search.setHint(config.hintResId);
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

        View noDestination = view.findViewById(R.id.no_destination);
        noDestination.setVisibility(config.isNotDecidedEnabled ? View.VISIBLE : View.INVISIBLE);
        noDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.setMapDestinationModeEnable(false);
                presenter.providePlace(null);
            }
        });
        destinationOnMap = view.findViewById(R.id.destination_on_map);

        offlineView = view.findViewById(R.id.offline);
        home = view.findViewById(R.id.home);
        setHome = view.findViewById(R.id.set_home);
        homeInfo = view.findViewById(R.id.home_info);
        View.OnClickListener onHomeAddressClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity())
                        .beginFragmentTransaction(SearchPlaceFragment.newInstance(SearchPlaceFragment.Config.HOME_ADDRESS, mBackendProvider))
                        .addToBackStack(null)
                        .commitAllowingStateLoss();
            }
        };
        setHome.setOnClickListener(onHomeAddressClickListener);
        homeInfo.findViewById(R.id.home_edit).setOnClickListener(onHomeAddressClickListener);
        homeInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.selectHome();
            }
        });

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
                presenter.selectItem(placesAdapter.getItem(position));
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

        loader = new LoaderDecorator(getContext());
        presenter = new SearchPlacePresenter(getActivity(), config.key, this, mBackendProvider);

        presenter.search(null);
        ((MainActivity) getActivity()).getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        presenter.initMap(googleMap);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search_place, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.skip).setVisible(config.isSkipEnabled);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.skip) {
            presenter.skip();
        }
        return false;
    }

    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
        search.clearFocus();
    }

    @Override
    public void updateConnectionStatus(boolean offline) {
        if (offline) {
            offlineView.setVisibility(View.VISIBLE);
            confirm.setEnabled(false);
        } else {
            offlineView.setVisibility(View.GONE);
            confirm.setEnabled(true);
        }
    }

    @Override
    public void updateAddress(String address) {
        search.setText(address);
    }

    @Override
    public void updateHomeAddress(PlaceModel home) {
        if (home == null) {
            setHome.setVisibility(View.VISIBLE);
            homeInfo.setVisibility(View.GONE);
        } else {
            setHome.setVisibility(View.GONE);
            homeInfo.setVisibility(View.VISIBLE);
            ((TextView)homeInfo.findViewById(R.id.home_text)).setText(home.address);
        }
    }

    @Override
    public void updateList(List<PlaceModel> list) {
        placesAdapter.clear();
        placesAdapter.addAll(list);
        placesAdapter.notifyDataSetChanged();
    }

    @Override
    public void showHomeAddress() {
        home.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideHomeAddress() {
        home.setVisibility(View.GONE);
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
            ((MainActivity) getActivity()).beginFragmentTransaction(ShareTripFragment.newInstance(tripId, shareUrl, mBackendProvider))
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public void finish() {
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideSoftInput();
        presenter.destroy();
    }

    private SearchPlaceFragment(@NonNull AbstractBackendProvider backendProvider) {
        mBackendProvider = backendProvider;
    }

    @SuppressWarnings("WeakerAccess")
    public static class Config implements Parcelable {

        public static final Config HOME_ADDRESS = new Config("home")
                .setTitle(R.string.add_home_address)
                .setHint(R.string.search_address)
                .setSkipEnabled(true);

        public static final Config SEARCH_PLACE = new Config("search")
                .setTitle(R.string.where_are_you_going)
                .setHint(R.string.i_m_going_to)
                .setNotDecidedEnabled(true);

        public final String key;
        private int titleResId;
        private int hintResId;
        private boolean isSkipEnabled = false;
        private boolean isNotDecidedEnabled = false;

        private Config(String key) {
            this.key = key;
        }

        public Config setTitle(int titleResId) {
            this.titleResId = titleResId;
            return this;
        }

        public Config setHint(int hintResId) {
            this.hintResId = hintResId;
            return this;
        }

        public Config setSkipEnabled(boolean skipEnabled) {
            isSkipEnabled = skipEnabled;
            return this;
        }

        public Config setNotDecidedEnabled(boolean notDecidedEnabled) {
            isNotDecidedEnabled = notDecidedEnabled;
            return this;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.key);
            dest.writeInt(this.titleResId);
            dest.writeInt(this.hintResId);
            dest.writeByte(this.isSkipEnabled ? (byte) 1 : (byte) 0);
            dest.writeByte(this.isNotDecidedEnabled ? (byte) 1 : (byte) 0);
        }

        protected Config(Parcel in) {
            this.key = in.readString();
            this.titleResId = in.readInt();
            this.hintResId = in.readInt();
            this.isSkipEnabled = in.readByte() != 0;
            this.isNotDecidedEnabled = in.readByte() != 0;
        }

        public static final Parcelable.Creator<Config> CREATOR = new Parcelable.Creator<Config>() {
            @Override
            public Config createFromParcel(Parcel source) {
                return new Config(source);
            }

            @Override
            public Config[] newArray(int size) {
                return new Config[size];
            }
        };
    }
}
