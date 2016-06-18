package io.hypertrack.meta.store;

import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;

import java.util.HashMap;

import io.hypertrack.meta.BuildConfig;
import io.hypertrack.meta.model.OnboardingUser;
import io.hypertrack.meta.network.retrofit.SendETAService;
import io.hypertrack.meta.network.retrofit.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ulhas on 18/06/16.
 */
public class OnboardingManager {

    private static String TAG = OnboardingManager.class.getSimpleName();
    private SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class, BuildConfig.API_KEY);

    private static OnboardingManager sSharedManager = null;
    private OnboardingUser onboardingUser;

    public static OnboardingManager sharedManager() {
        if (sSharedManager == null) {
            sSharedManager = new OnboardingManager();
        }

        return sSharedManager;
    }

    private OnboardingManager() {
        onboardingUser = new OnboardingUser();
    }

    public OnboardingUser getUser() {
        return onboardingUser;
    }

    public void onboardUser(final OnOnboardingCallback callback) {
        String phoneNumber;
        try {
            phoneNumber = this.onboardingUser.getInternationalNumber();
        }  catch (NumberParseException e) {
            callback.onError();
            return;
        }

        HashMap<String, String> phoneNumberMap = new HashMap<>();
        phoneNumberMap.put("phone_number", phoneNumber);

        Call<OnboardingUser> call = sendETAService.getUser(phoneNumberMap);
        call.enqueue(new Callback<OnboardingUser>() {
            @Override
            public void onResponse(Call<OnboardingUser> call, Response<OnboardingUser> response) {
                onboardingUser = response.body();
                callback.onSuccess();
            }

            @Override
            public void onFailure(Call<OnboardingUser> call, Throwable t) {
                callback.onError();
            }
        });
    }

    public void verifyCode(String code, final OnOnboardingCallback callback) {

    }

    public void updateInfo(final OnOnboardingCallback callback) {

    }

    public void resendVerificationCode(final OnOnboardingCallback callback) {

    }

    public void uploadPhoto(final OnOnboardingCallback callback) {

    }

    public void getUserImage(final OnOnboardingCallback callback) {

    }

    private void didOnbardUser() {

    }
}
