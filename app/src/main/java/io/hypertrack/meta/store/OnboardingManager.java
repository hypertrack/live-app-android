package io.hypertrack.meta.store;

import android.content.Context;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.hypertrack.meta.BuildConfig;
import io.hypertrack.meta.MetaApplication;
import io.hypertrack.meta.model.OnboardingUser;
import io.hypertrack.meta.model.User;
import io.hypertrack.meta.network.retrofit.SendETAService;
import io.hypertrack.meta.network.retrofit.ServiceGenerator;
import io.hypertrack.meta.util.SharedPreferenceManager;
import io.hypertrack.meta.util.SuccessErrorCallback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ulhas on 18/06/16.
 */
public class OnboardingManager {

    private static String TAG = OnboardingManager.class.getSimpleName();
    private SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class);

    private static OnboardingManager sSharedManager = null;
    private OnboardingUser onboardingUser;

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
        String phoneNumber;
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

        Call<OnboardingUser> call = sendETAService.getUser(phoneNumberMap);
        call.enqueue(new Callback<OnboardingUser>() {
            @Override
            public void onResponse(Call<OnboardingUser> call, Response<OnboardingUser> response) {
                OnboardingUser candidate = response.body();
                if (candidate == null) {
                    if (callback != null) {
                        callback.onError();
                    }

                    return;
                }

                onboardingUser = candidate;
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onFailure(Call<OnboardingUser> call, Throwable t) {
                callback.onError();
            }
        });
    }

    public void verifyCode(String code, final OnOnboardingCallback callback) {
        HashMap<String, String> verificationCode = new HashMap<>();
        verificationCode.put("verification_code", code);

        Call<Map<String, Object>> call = sendETAService.verifyUser(this.onboardingUser.getId(), verificationCode);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                Map body = response.body();
                if (body == null) {
                    if (callback != null) {
                        callback.onError();
                    }

                    return;
                }

                onVerifyCode(body);
                if (callback != null) {
                    callback.onSuccess();
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

    public void updateInfo(final OnOnboardingCallback callback) {
        HashMap<String, String> user = new HashMap<>();
        user.put("first_name", this.onboardingUser.getFirstName());
        user.put("last_name", this.onboardingUser.getLastName());

        Call<User> call = sendETAService.updateUserName(this.onboardingUser.getId(), user);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(final Call<User> call, Response<User> response) {
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
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                callback.onError();
            }
        });
    }

    public void resendVerificationCode(final OnOnboardingCallback callback) {
        Call<Map<String, Object>> call = sendETAService.resendCode(this.onboardingUser.getId());
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (callback != null) {
                    callback.onSuccess();
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
        if (profileImage == null) {
            callback.onError();
            return;
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("image*//*"), profileImage);

        Map<String, RequestBody> requestBodyMap = new HashMap<>();
        String uuid = UUID.randomUUID().toString();
        String fileName = "photo\"; filename=\"" + uuid + ".jpg";
        requestBodyMap.put(fileName, requestBody);

        Call<Map<String, Object>> call = sendETAService.updateUserProfilePic(this.onboardingUser.getId(), requestBodyMap);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (callback != null) {
                    callback.onSuccess();
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

    public void getUserImage(final OnOnboardingCallback callback) {

    }

    private void didOnbardUser(User user) {
        UserStore.sharedStore.addUser(user);
    }

    private void onVerifyCode(Map<String, Object> response) {
        Log.v(TAG, response.toString());

        SharedPreferenceManager.setUserAuthToken((String)response.get("token"));
    }
}
