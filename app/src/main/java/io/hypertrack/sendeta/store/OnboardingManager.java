package io.hypertrack.sendeta.store;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.HashMap;

import io.hypertrack.sendeta.model.OnboardingUser;
import io.hypertrack.sendeta.store.callback.OnOnboardingCallback;
import io.hypertrack.sendeta.store.callback.OnOnboardingImageUploadCallback;
import io.hypertrack.sendeta.util.SharedPreferenceManager;

/**
 * Created by ulhas on 18/06/16.
 */
public class OnboardingManager {

    private static final String LOGGED_USER = "io.hypertrack.meta:LoggedUser";
    private static String TAG = OnboardingManager.class.getSimpleName();
    private static OnboardingManager sSharedManager = null;
    private OnboardingUser onboardingUser;

    private OnboardingManager() {
        this.onboardingUser = OnboardingUser.sharedOnboardingUser();
    }

    public static OnboardingManager sharedManager() {
        if (sSharedManager == null) {
            sSharedManager = new OnboardingManager();
        }

        return sSharedManager;
    }

    public boolean isUserLoggedIN() {
        if (onboardingUser == null || TextUtils.isEmpty(onboardingUser.getId()))
            return false;
        return !TextUtils.isEmpty(onboardingUser.getId());
    }

    public OnboardingUser getUser() {
        return onboardingUser;
    }

    /*public void onboardUser(final OnOnboardingCallback callback) {
        final String phoneNumber;
        try {
            phoneNumber = this.onboardingUser.getInternationalNumber();
        } catch (NumberParseException e) {
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

                    // Check if Current Phone Number is an Existing SendETA User or not
                    if (response.raw().code() == 200) {
                        onboardingUser.setExistingUser(true);
                    } else {
                        onboardingUser.setExistingUser(false);
                    }
                    OnboardingUser.setOnboardingUser();

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
    }*/

    public void verifyCode(String code, final OnOnboardingCallback callback) {
        HashMap<String, String> verificationParams = new HashMap<>();
        verificationParams.put("verification_code", code);
        verificationParams.put("phone_number", onboardingUser.getPhone());

       /* Call<VerifyResponse> call = sendETAService.verifyUser(verificationParams);
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
        });*/
    }

   /* public void updateInfo(final OnOnboardingCallback callback) {
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
    }*/

    public void resendVerificationCode(final OnOnboardingCallback callback) {
        HashMap<String, String> phoneNumber = new HashMap<>();
        phoneNumber.put("phone_number", this.onboardingUser.getPhone());

      /*  Call<Map<String, Object>> call = sendETAService.resendCode(phoneNumber);
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
        });*/
    }

    public void uploadPhoto(final Bitmap oldProfileImage, final Bitmap updatedProfileImage,
                            final OnOnboardingImageUploadCallback callback) {
        File profileImage = this.onboardingUser.getPhotoImage();

        if (profileImage != null && profileImage.length() > 0) {
            //UserStore.sharedStore.addImage(profileImage);

            // Check if the profile image has changed from the existing one
            if (updatedProfileImage != null && updatedProfileImage.getByteCount() > 0
                    && !updatedProfileImage.sameAs(oldProfileImage)) {
                this.onboardingUser.saveFileAsBitmap(profileImage);
                OnboardingUser.setOnboardingUser();
                if (callback != null) {
                    callback.onSuccess();
                }
            } else {
                // No need to upload Profile Image since there was no change in it
                if (callback != null) {
                    callback.onImageUploadNotNeeded();
                }
            }
        } else {
            if (callback != null) {
                callback.onError();
            }
        }
    }

    public void getUserImage(final OnOnboardingCallback callback) {

    }

    public void didOnboardUser(OnboardingUser user) {
        OnboardingUser.setOnboardingUser();
    }

    private void onVerifyCode(VerifyResponse response) {
        Log.v(TAG, response.toString());

        SharedPreferenceManager.setUserAuthToken(response.getToken());
        // Update OnBoardingUser Data with the data fetched after code verification
        this.onboardingUser.update(response.getUser());
    }


}
