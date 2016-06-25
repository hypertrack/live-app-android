package io.hypertrack.meta.interactor;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;

import io.hypertrack.meta.MetaApplication;
import io.hypertrack.meta.interactor.callback.OnRegisterCallback;
import io.hypertrack.meta.model.OnboardingUser;
import io.hypertrack.meta.model.User;
import io.hypertrack.meta.network.HTCustomPostRequest;
import io.hypertrack.meta.store.OnOnboardingCallback;
import io.hypertrack.meta.store.OnboardingManager;
import io.hypertrack.meta.util.Constants;
import io.hypertrack.meta.util.SharedPreferenceManager;

public class RegisterInteractor {

    public void registerPhoneNumber(final OnRegisterCallback onRegisterCallback) {
        OnboardingManager.sharedManager().onboardUser(new OnOnboardingCallback() {
            @Override
            public void onSuccess() {
                onRegisterCallback.OnSuccess();
            }

            @Override
            public void onError() {
                onRegisterCallback.OnError();
            }
        });
    }
}
