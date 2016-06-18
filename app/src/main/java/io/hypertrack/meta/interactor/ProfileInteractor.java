package io.hypertrack.meta.interactor;

import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.hypertrack.meta.BuildConfig;
import io.hypertrack.meta.MetaApplication;
import io.hypertrack.meta.interactor.callback.OnProfileUpdateCallback;
import io.hypertrack.meta.model.OnboardingUser;
import io.hypertrack.meta.model.User;
import io.hypertrack.meta.network.retrofit.SendETAService;
import io.hypertrack.meta.network.retrofit.ServiceGenerator;
import io.hypertrack.meta.store.OnOnboardingCallback;
import io.hypertrack.meta.store.OnboardingManager;
import io.hypertrack.meta.util.SharedPreferenceManager;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileInteractor {

    private static final String TAG = ProfileInteractor.class.getSimpleName();

     public void updateUserProfile(final OnProfileUpdateCallback onProfileUpdateCallback) {

         OnboardingManager.sharedManager().updateInfo(new OnOnboardingCallback() {
             @Override
             public void onSuccess() {
                onProfileUpdateCallback.OnSuccess();
             }

             @Override
             public void onError() {
                onProfileUpdateCallback.OnError();
             }
         });
    }

    public void updateUserProfilePic(File profileImage) {

        OnboardingManager.sharedManager().uploadPhoto(new OnOnboardingCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

            }
        });
    }
}
