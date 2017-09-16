
/*
The MIT License (MIT)

Copyright (c) 2015-2017 HyperTrack (http://hypertrack.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package io.hypertrack.sendeta.presenter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.internal.common.util.HTTextUtils;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.SuccessResponse;
import com.hypertrack.lib.models.User;
import com.hypertrack.lib.models.UserParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import io.hypertrack.sendeta.model.HyperTrackLiveUser;
import io.hypertrack.sendeta.network.retrofit.HyperTrackService;
import io.hypertrack.sendeta.network.retrofit.HyperTrackServiceGenerator;
import io.hypertrack.sendeta.store.OnboardingManager;
import io.hypertrack.sendeta.view.Profile;
import io.hypertrack.sendeta.view.ProfileView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by suhas on 24/02/16.
 */
public class ProfilePresenter implements IProfilePresenter<ProfileView> {

    private static final String TAG = Profile.class.getSimpleName();
    private ProfileView view;
    private OnboardingManager onboardingManager = OnboardingManager.sharedManager();

    @Override
    public void attachView(final ProfileView view) {
        this.view = view;
        if (!HTTextUtils.isEmpty(HyperTrack.getUserId())) {
            view.showProfileLoading(true);
            HyperTrack.getUser(new HyperTrackCallback() {
                @Override
                public void onSuccess(@NonNull SuccessResponse response) {
                    Log.d(TAG, "onSuccess: Data get from getUser");
                    User userModel = (User) response.getResponseObject();
                    String ISOcode = null;
                    String phoneNo = null;
                    if (userModel != null) {
                        if (!HTTextUtils.isEmpty(userModel.getPhone())) {
                            int index = userModel.getPhone().indexOf(" ");
                            ISOcode = userModel.getPhone().substring(0, index + 1);
                            phoneNo = userModel.getPhone().substring(index + 1);
                        }
                        view.updateViews(userModel.getName(), phoneNo, ISOcode, userModel.getPhoto());
                        view.showProfileLoading(false);
                    }
                }

                @Override
                public void onError(@NonNull ErrorResponse errorResponse) {
                    view.showProfileLoading(false);
                }
            });
        } else {
            HyperTrackLiveUser user = this.onboardingManager.getUser();
            this.view.updateViews(user.getName(), user.getPhone(), user.getCountryCode(),
                    user.getPhoto());
        }
    }

    @Override
    public void detachView() {
        view = null;
    }

    private UserParams getUserParams(String name, final String number, String ISOCode, File profileImage)
            throws NumberParseException {
        final HyperTrackLiveUser user = this.onboardingManager.getUser();

        // Update Country Code from device's current location
        if (!HTTextUtils.isEmpty(ISOCode))
            user.setCountryCode(ISOCode);

        String encodedImage = null;
        // Set user's profile image
        if (profileImage != null && profileImage.length() > 0) {
            user.setPhotoImage(profileImage);
            byte[] bytes = convertFiletoByteArray(profileImage);
            if (bytes != null && bytes.length > 0)
                encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT);
        }

        HyperTrackLiveUser.setHyperTrackLiveUser();

        String phoneNumber = user.getInternationalNumber(number);
        return new UserParams()
                .setName(name)
                .setPhone(phoneNumber)
                .setPhoto(encodedImage)
                .setLookupId(phoneNumber);
    }

    @Override
    public void attemptLogin(final String userName, final String phone, String ISOCode,
                             final File profileImage, final boolean verifyPhone) {
        try {
            UserParams userParams = getUserParams(userName, phone, ISOCode, profileImage);
            HyperTrack.getOrCreateUser(userParams, new HyperTrackCallback() {
                @Override
                public void onSuccess(@NonNull SuccessResponse successResponse) {
                    Log.d(TAG, "onSuccess: User Created");
                    /*if (verifyPhone) {
                        sendVerificationCode();
                    } else*/
                        view.onProfileUpdateSuccess();
                }

                @Override
                public void onError(@NonNull ErrorResponse errorResponse) {
                    Log.d(TAG, "onError: User Created:" + errorResponse.getErrorMessage());
                    if (view != null) {
                        view.showErrorMessage(errorResponse.getErrorMessage());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (view != null) {
                view.showErrorMessage(e.getMessage());
            }
        }
    }

    @Override
    public void updateProfile(String name, final String number, String ISOCode, File profileImage,
                              final boolean verifyPhone) {
        try {
            UserParams userParams = getUserParams(name, number, ISOCode, profileImage);
            HyperTrack.updateUser(userParams, new HyperTrackCallback() {
                @Override
                public void onSuccess(@NonNull SuccessResponse successResponse) {
                    Log.d(TAG, "onSuccess: User Profile Updated");
                   /* if (verifyPhone) {
                        sendVerificationCode();
                    } else*/
                        view.onProfileUpdateSuccess();
                }

                @Override
                public void onError(@NonNull ErrorResponse errorResponse) {
                    Log.d(TAG, "onError: UpdateUser:" + errorResponse.getErrorMessage());
                    if (view != null) {
                        view.showErrorMessage(errorResponse.getErrorMessage());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (view != null) {
                view.showErrorMessage(e.getMessage());
            }
        }
    }

    private void sendVerificationCode(Context context) {
        HyperTrackService getResendCodeService = HyperTrackServiceGenerator.createService(HyperTrackService.class,context);
        Call<User> call = getResendCodeService.sendCode(HyperTrack.getUserId());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse: Verification Code Sent");
                    if (view != null)
                        view.navigateToVerifyCodeScreen();
                } else {
                    if (view != null) {
                        try {
                            JSONObject jObjError = new JSONObject(response.errorBody().string());
                            view.showErrorMessage(jObjError.getString("message"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                t.printStackTrace();
                if (view != null)
                    view.showErrorMessage(t.getMessage());
            }
        });
    }

    private byte[] convertFiletoByteArray(File file) {
        byte[] b = new byte[(int) file.length()];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(b);
           /* for (int i = 0; i < b.length; i++) {
                System.out.print((char) b[i]);
            }*/
            return b;
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found.");
            e.printStackTrace();
        } catch (IOException e1) {
            System.out.println("Error Reading The File.");
            e1.printStackTrace();
        }
        return b;
    }
}