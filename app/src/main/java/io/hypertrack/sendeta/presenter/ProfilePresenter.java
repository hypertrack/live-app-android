
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
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
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

import io.hypertrack.sendeta.BuildConfig;
import io.hypertrack.sendeta.network.retrofit.HyperTrackLiveService;
import io.hypertrack.sendeta.network.retrofit.HyperTrackLiveServiceGenerator;
import io.hypertrack.sendeta.store.SharedPreferenceManager;
import io.hypertrack.sendeta.view.Profile;
import io.hypertrack.sendeta.view.ProfileView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static io.hypertrack.sendeta.BuildConfig.HYPERTRACK_BASE_URL_V1;

/**
 * Created by Aman on 24/12/17.
 */
public class ProfilePresenter implements IProfilePresenter<ProfileView> {

    private static final String TAG = Profile.class.getSimpleName();
    private ProfileView view;

    @Override
    public void attachView(final ProfileView view) {
        this.view = view;
        final String[] ISOcode = {null};
        final String[] phoneNo = {null};
        if (!HTTextUtils.isEmpty(HyperTrack.getUserId())) {
            view.showProfileLoading(true);
            HyperTrack.getUser(new HyperTrackCallback() {
                @Override
                public void onSuccess(@NonNull SuccessResponse response) {
                    Log.d(TAG, "onSuccess: Data get from getUser");
                    User user = (User) response.getResponseObject();
                    if (user != null) {
                        if (!HTTextUtils.isEmpty(user.getPhone())) {
                            int index = user.getPhone().indexOf(" ");
                            ISOcode[0] = user.getPhone().substring(0, index + 1);
                            phoneNo[0] = user.getPhone().substring(index + 1);
                        }
                        view.updateViews(user, ISOcode[0], phoneNo[0]);
                        view.showProfileLoading(false);
                    }
                }

                @Override
                public void onError(@NonNull ErrorResponse errorResponse) {
                    view.showProfileLoading(false);
                }
            });
        }
    }

    @Override
    public void detachView() {
        view = null;
    }

    @Override
    public boolean isViewAttached() {
        return view != null;
    }

    private UserParams getUserParams(final Context context, final String name, final String number,
                                     String ISOCode, File profileImage)
            throws NumberParseException {

        String encodedImage = null;
        // Set user's profile image
        if (profileImage != null && profileImage.length() > 0) {
            byte[] bytes = convertFiletoByteArray(profileImage);
            if (bytes != null && bytes.length > 0)
                encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT);
        }

        String phoneNumber = getInternationalNumber(number, ISOCode);
        return new UserParams()
                .setName(name)
                .setPhone(phoneNumber)
                .setPhoto(encodedImage)
                .setUniqueId(phoneNumber);
    }

    private String getInternationalNumber(String phoneNo, String ISOCode) throws NumberParseException {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        if (HTTextUtils.isEmpty(phoneNo))
            return null;

        Phonenumber.PhoneNumber number = phoneUtil.parse(phoneNo, ISOCode);
        return phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
    }

    @Override
    public void attemptLogin(final String userName, final String phone, String ISOCode,
                             final File profileImage, final boolean verifyPhone, final Context context) {
        try {
            UserParams userParams = getUserParams(context, userName, phone, ISOCode, profileImage);
            HyperTrack.getOrCreateUser(userParams, new HyperTrackCallback() {
                @Override
                public void onSuccess(@NonNull SuccessResponse successResponse) {
                    Log.d(TAG, "onSuccess: User Created");
                    if (verifyPhone && !HTTextUtils.isEmpty(BuildConfig.isHyperTrackLive)) {
                        sendVerificationCode(context);
                    } else if (isViewAttached())
                        view.onProfileUpdateSuccess();
                    SharedPreferenceManager.setHyperTrackLiveUser(context,
                            (User) successResponse.getResponseObject());
                }

                @Override
                public void onError(@NonNull ErrorResponse errorResponse) {
                    Log.d(TAG, "onError: User Created:" + errorResponse.getErrorMessage());
                    if (isViewAttached())
                        view.showErrorMessage(errorResponse.getErrorMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (isViewAttached())
                view.showErrorMessage(e.getMessage());
        }
    }

    @Override
    public void updateProfile(final String name, final String number, String ISOCode, File profileImage,
                              final boolean verifyPhone, final Context context) {
        try {
            UserParams userParams = getUserParams(context, name, number, ISOCode, profileImage);
            HyperTrack.updateUser(userParams, new HyperTrackCallback() {
                @Override
                public void onSuccess(@NonNull SuccessResponse successResponse) {
                    Log.d(TAG, "onSuccess: User Profile Updated");
                    if (verifyPhone && !HTTextUtils.isEmpty(BuildConfig.isHyperTrackLive)) {
                        sendVerificationCode(context);
                    } else if (isViewAttached())
                        view.onProfileUpdateSuccess();
                    SharedPreferenceManager.setHyperTrackLiveUser(context,
                            (User) successResponse.getResponseObject());
                }

                @Override
                public void onError(@NonNull ErrorResponse errorResponse) {
                    Log.d(TAG, "onError: UpdateUser:" + errorResponse.getErrorMessage());
                    if (isViewAttached())
                        view.showErrorMessage(errorResponse.getErrorMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (isViewAttached())
                view.showErrorMessage(e.getMessage());
        }
    }

    private void sendVerificationCode(Context context) {
        HyperTrackLiveService getResendCodeService =
                HyperTrackLiveServiceGenerator.createService(HyperTrackLiveService.class, context, HYPERTRACK_BASE_URL_V1);
        Call<User> call = getResendCodeService.sendCode(HyperTrack.getUserId());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse: Verification Code Sent");
                    if (isViewAttached())
                        view.navigateToVerifyCodeScreen();
                } else {
                    if (isViewAttached()) {
                        try {
                            JSONObject jObjError = new JSONObject(response.errorBody().string());
                            view.showErrorMessage(jObjError.getString("message"));
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                t.printStackTrace();
                if (isViewAttached())
                    view.showErrorMessage(t.getMessage());
            }
        });
    }

    private byte[] convertFiletoByteArray(File file) {
        byte[] b = new byte[(int) file.length()];
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(b);
            return b;
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found.");
            e.printStackTrace();
        } catch (IOException e1) {
            System.out.println("Error Reading The File.");
            e1.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return b;
    }
}