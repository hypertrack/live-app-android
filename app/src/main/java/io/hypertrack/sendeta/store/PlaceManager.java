package io.hypertrack.sendeta.store;

/**
 * Created by ulhas on 21/06/16.
 */
public class PlaceManager {

   /* public void getPlaces(final PlaceManagerGetPlacesCallback callback) {
        SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class, SharedPreferenceManager.getUserAuthToken());

        Call<List<UserPlace>> call = sendETAService.getPlaces();
        call.enqueue(new Callback<List<UserPlace>>() {
            @Override
            public void onResponse(Call<List<UserPlace>> call, Response<List<UserPlace>> response) {
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
            public void onFailure(Call<List<UserPlace>> call, Throwable t) {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }

   *//* public void deletePlace(final UserPlace place, final PlaceManagerCallback callback) {
        SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class, SharedPreferenceManager.getUserAuthToken());
        PlaceDTO placeDTO = new PlaceDTO(place);

        Call<UserPlace> call = sendETAService.deletePlace(placeDTO.getId());
        call.enqueue(new Callback<UserPlace>() {
            @Override
            public void onResponse(Call<UserPlace> call, Response<UserPlace> response) {
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
            public void onFailure(Call<UserPlace> call, Throwable t) {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }*//*

    public void addPlace(UserPlace place, final PlaceManagerCallback callback) {
        SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class, SharedPreferenceManager.getUserAuthToken());
        PlaceDTO placeDTO = new PlaceDTO(place);

        Call<UserPlace> call = sendETAService.addPlace(placeDTO);
        call.enqueue(new Callback<UserPlace>() {
            @Override
            public void onResponse(Call<UserPlace> call, Response<UserPlace> response) {
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
            public void onFailure(Call<UserPlace> call, Throwable t) {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }

    public void editPlace(UserPlace place, final PlaceManagerCallback callback) {
        SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class, SharedPreferenceManager.getUserAuthToken());
        PlaceDTO placeDTO = new PlaceDTO(place);

       *//* Call<UserPlace> call = sendETAService.editPlace(placeDTO.getId(), placeDTO);
        call.enqueue(new Callback<UserPlace>() {
            @Override
            public void onResponse(Call<UserPlace> call, Response<UserPlace> response) {
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
            public void onFailure(Call<UserPlace> call, Throwable t) {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });*//*
    }*/
}
