package io.hypertrack.meta.interactor;

import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.hypertrack.meta.BuildConfig;
import io.hypertrack.meta.MetaApplication;
import io.hypertrack.meta.interactor.callback.OnProfileUpdateCallback;
import io.hypertrack.meta.model.User;
import io.hypertrack.meta.network.retrofit.SendEtaService;
import io.hypertrack.meta.network.retrofit.ServiceGenerator;
import io.hypertrack.meta.util.SharedPreferenceManager;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileInteractor {

    private static final String TAG = ProfileInteractor.class.getSimpleName();

     public void updateUserProfileRetro(String firstName, String lastName, final int userId, final File profileImage, final OnProfileUpdateCallback onProfileUpdateCallback) {

        SendEtaService sendEtaService = ServiceGenerator.createService(SendEtaService.class, BuildConfig.API_KEY);

        User user = new User(firstName, lastName);

        Call<User> call = sendEtaService.updateUserName(String.valueOf(userId), user);
        call.enqueue(new Callback<User>() {

            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {

                    Log.v(TAG, "Response from Retrofit");
                    Log.d("Response", "User :" + response.body().toString());

                    SharedPreferenceManager sharedPreferenceManager = new SharedPreferenceManager(MetaApplication.getInstance());
                    sharedPreferenceManager.setHyperTrackDriverID(response.body().getHypertrackDriverID());
                    sharedPreferenceManager.setUserPhoto(response.body().getPhoto());
                    sharedPreferenceManager.setUserLoggedIn(true);

                    if (onProfileUpdateCallback != null) {
                        onProfileUpdateCallback.OnSuccess();
                    }

                    //Upload photo
                    if (profileImage != null)
                        updateUserProfilePic(userId, profileImage);

                } else {
                    if (onProfileUpdateCallback != null) {
                        onProfileUpdateCallback.OnError();
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.v(TAG, "Inside error block of retrofit. " + t.getMessage());
                onProfileUpdateCallback.OnError();
            }
        });

    }

    public void updateUserProfilePic(int userId, File profileImage) {

        SendEtaService sendEtaService = ServiceGenerator.createService(SendEtaService.class, BuildConfig.API_KEY);

        RequestBody requestBody =
                RequestBody.create(MediaType.parse("image*//*"), profileImage);

        Map<String, RequestBody> requestBodyMap = new HashMap<>();
        String uuid = UUID.randomUUID().toString();
        String fileName = "photo\"; filename=\"" + uuid + ".jpg";
        requestBodyMap.put(fileName, requestBody);

        Call<User> updatePicCall = sendEtaService.updateUserProfilePic(String.valueOf(userId), requestBodyMap);
        updatePicCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Log.v(TAG, "Pic updated successfully");
                    Log.v(TAG, response.headers().toString());

                    if (response.body() != null && response.body().getPhoto() != null) {
                        SharedPreferenceManager sharedPreferenceManager = new SharedPreferenceManager(MetaApplication.getInstance());
                        sharedPreferenceManager.setUserPhoto(response.body().getPhoto());
                    }

                } else {
                    Log.d(TAG, "User profile could not be uploaded");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.v(TAG, "Error while updating profile pic. " + t.getMessage());
            }
        });
    }
}
