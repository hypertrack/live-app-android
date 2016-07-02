package io.hypertrack.sendeta.store;

import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import io.hypertrack.sendeta.model.OnboardingUser;
import io.hypertrack.sendeta.model.User;
import io.hypertrack.sendeta.network.retrofit.SendETAService;
import io.hypertrack.sendeta.network.retrofit.ServiceGenerator;
import io.hypertrack.sendeta.store.callback.OnOnboardingCallback;
import io.hypertrack.sendeta.util.SharedPreferenceManager;
import io.hypertrack.sendeta.util.SuccessErrorCallback;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ulhas on 18/06/16.
 */
public class OnboardingManager {

    private static String TAG = OnboardingManager.class.getSimpleName();

    private static OnboardingManager sSharedManager = null;
    private OnboardingUser onboardingUser;
    private SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class);

    public static OnboardingManager sharedManager() {
        if (sSharedManager == null) {
            sSharedManager = new OnboardingManager();
        }

        return sSharedManager;
    }

    private OnboardingManager() {
        this.onboardingUser = new OnboardingUser();
    }

    public OnboardingUser getUser() {
        return onboardingUser;
    }

    public void onboardUser(final OnOnboardingCallback callback) {
        final String phoneNumber;
        try {
            phoneNumber = this.onboardingUser.getInternationalNumber();
        }  catch (NumberParseException e) {
            if (callback != null) {
                callback.onError();
            }
            return;
        }

        HashMap<String, String> phoneNumberMap = new HashMap<>();
        phoneNumberMap.put("phone_number", phoneNumber);

        Call<Map<String, Object>> call = sendETAService.getUser(phoneNumberMap);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    onboardingUser.setPhoneNumber(phoneNumber);
                    if (callback != null) {
                        callback.onSuccess();
                    }
                    return;
                }

                if (callback != null) {
                    callback.onError();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError();
            }
        });
    }

    public void verifyCode(String code, final OnOnboardingCallback callback) {
        HashMap<String, String> verificationParams = new HashMap<>();
        verificationParams.put("verification_code", code);
        verificationParams.put("phone_number", onboardingUser.getPhoneNumber());

        Call<VerifyResponse> call = sendETAService.verifyUser(verificationParams);
        call.enqueue(new Callback<VerifyResponse>() {
            @Override
            public void onResponse(Call<VerifyResponse> call, Response<VerifyResponse> response) {
                if (response.isSuccessful()) {
                    onVerifyCode(response.body());
                    if (callback != null) {
                        callback.onSuccess();
                    }
                    return;
                }

                if (callback != null) {
                    callback.onError();
                }
            }

            @Override
            public void onFailure(Call<VerifyResponse> call, Throwable t) {
                if (callback != null) {
                    callback.onError();
                }
            }
        });
    }

    public void updateInfo(final OnOnboardingCallback callback) {
        HashMap<String, String> user = new HashMap<>();
        user.put("first_name", this.onboardingUser.getFirstName());
        user.put("last_name", this.onboardingUser.getLastName());

        SendETAService userService = ServiceGenerator.createService(SendETAService.class, SharedPreferenceManager.getUserAuthToken());

        Call<User> call = userService.updateUserName(this.onboardingUser.getId(), user);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(final Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    didOnbardUser(response.body());
                    UserStore.sharedStore.updatePlaces(new SuccessErrorCallback() {
                        @Override
                        public void OnSuccess() {
                            if (callback != null) {
                                callback.onSuccess();
                            }
                        }

                        @Override
                        public void OnError() {
                            if (callback != null) {
                                callback.onError();
                            }
                        }
                    });
                    return;
                }

                if (callback != null) {
                    callback.onError();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                callback.onError();
            }
        });
    }

    public void resendVerificationCode(final OnOnboardingCallback callback) {
        HashMap<String, String> phoneNumber = new HashMap<>();
        phoneNumber.put("phone_number", this.onboardingUser.getPhoneNumber());

        Call<Map<String, Object>> call = sendETAService.resendCode(phoneNumber);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } else {
                    if (callback != null) {
                        callback.onError();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                if (callback != null) {
                    callback.onError();
                }
            }
        });
    }

    public void uploadPhoto(final OnOnboardingCallback callback) {
        File profileImage = this.onboardingUser.getPhotoImage();

        if (profileImage.length() > 0) {
            UserStore.sharedStore.addImage(profileImage);

            UserStore.sharedStore.updatePhoto(profileImage, new SuccessErrorCallback() {
                @Override
                public void OnSuccess() {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                }

                @Override
                public void OnError() {
                    if (callback != null) {
                        callback.onError();
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.onError();
            }
        }
    }

    public void getUserImage(final OnOnboardingCallback callback) {

    }

    private void didOnbardUser(User user) {
        UserStore.sharedStore.addUser(user);
    }

    private void onVerifyCode(VerifyResponse response) {
        Log.v(TAG, response.toString());

        SharedPreferenceManager.setUserAuthToken(response.getToken());
        this.onboardingUser = response.getUser();
    }
}
