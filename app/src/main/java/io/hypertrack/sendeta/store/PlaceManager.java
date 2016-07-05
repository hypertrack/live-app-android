package io.hypertrack.sendeta.store;

import java.util.List;

import io.hypertrack.sendeta.model.MetaPlace;
import io.hypertrack.sendeta.model.PlaceDTO;
import io.hypertrack.sendeta.network.retrofit.SendETAService;
import io.hypertrack.sendeta.network.retrofit.ServiceGenerator;
import io.hypertrack.sendeta.store.callback.PlaceManagerCallback;
import io.hypertrack.sendeta.store.callback.PlaceManagerGetPlacesCallback;
import io.hypertrack.sendeta.util.SharedPreferenceManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ulhas on 21/06/16.
 */
public class PlaceManager {

    public void getPlaces(final PlaceManagerGetPlacesCallback callback) {
        SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class, SharedPreferenceManager.getUserAuthToken());

        Call<List<MetaPlace>> call = sendETAService.getPlaces();
        call.enqueue(new Callback<List<MetaPlace>>() {
            @Override
            public void onResponse(Call<List<MetaPlace>> call, Response<List<MetaPlace>> response) {
                if (response.isSuccessful()) {
                    if (callback != null) {
                        callback.OnSuccess(response.body());
                    }
                } else {
                    if (callback != null) {
                        callback.OnError();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<MetaPlace>> call, Throwable t) {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }

    public void deletePlace(final MetaPlace place, final PlaceManagerCallback callback) {
        SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class, SharedPreferenceManager.getUserAuthToken());
        PlaceDTO placeDTO = new PlaceDTO(place);

        Call<MetaPlace> call = sendETAService.deletePlace(placeDTO.getId());
        call.enqueue(new Callback<MetaPlace>() {
            @Override
            public void onResponse(Call<MetaPlace> call, Response<MetaPlace> response) {
                if (response.isSuccessful()) {
                    if (callback != null) {
                        callback.OnSuccess(place);
                    }
                } else {
                    if (callback != null) {
                        callback.OnError();
                    }
                }
            }

            @Override
            public void onFailure(Call<MetaPlace> call, Throwable t) {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }

    public void addPlace(MetaPlace place, final PlaceManagerCallback callback) {
        SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class, SharedPreferenceManager.getUserAuthToken());
        PlaceDTO placeDTO = new PlaceDTO(place);

        Call<MetaPlace> call = sendETAService.addPlace(placeDTO);
        call.enqueue(new Callback<MetaPlace>() {
            @Override
            public void onResponse(Call<MetaPlace> call, Response<MetaPlace> response) {
                if (response.isSuccessful()) {
                    if (callback != null) {
                        callback.OnSuccess(response.body());
                    }
                } else {
                    if (callback != null) {
                        callback.OnError();
                    }
                }
            }

            @Override
            public void onFailure(Call<MetaPlace> call, Throwable t) {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }

    public void editPlace(MetaPlace place, final PlaceManagerCallback callback) {
        SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class, SharedPreferenceManager.getUserAuthToken());
        PlaceDTO placeDTO = new PlaceDTO(place);

        Call<MetaPlace> call = sendETAService.editPlace(placeDTO.getId(), placeDTO);
        call.enqueue(new Callback<MetaPlace>() {
            @Override
            public void onResponse(Call<MetaPlace> call, Response<MetaPlace> response) {
                if (response.isSuccessful()) {
                    if (callback != null) {
                        callback.OnSuccess(response.body());
                    }
                } else {
                    if (callback != null) {
                        callback.OnError();
                    }
                }
            }

            @Override
            public void onFailure(Call<MetaPlace> call, Throwable t) {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }
}
