package io.hypertrack.sendeta.presenter;

import android.content.Context;
import android.util.Log;

import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.models.User;

import io.hypertrack.sendeta.model.VerifyCodeModel;
import io.hypertrack.sendeta.network.retrofit.HyperTrackLiveService;
import io.hypertrack.sendeta.network.retrofit.HyperTrackLiveServiceGenerator;
import io.hypertrack.sendeta.view.VerifyView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Aman on 19/07/17.
 */

public class VerifyPresenter implements IVerifyPresenter<VerifyView> {

    private static final String TAG = VerifyPresenter.class.getSimpleName();
    private VerifyView verifyView;

    @Override
    public void attachView(VerifyView view) {
        this.verifyView = view;
    }

    @Override
    public void detachView() {
        verifyView = null;
    }

    @Override
    public void verifyOTP(String OTP, Context context) {
        VerifyCodeModel verifyCodeModel = new VerifyCodeModel(OTP);
        HyperTrackLiveService getVerifyCodeService = HyperTrackLiveServiceGenerator.createService(HyperTrackLiveService.class,context);
        Call<User> call = getVerifyCodeService.validateCode(HyperTrack.getUserId(), verifyCodeModel);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {

                if (verifyView != null) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "onResponse: Code Verified");
                        verifyView.codeVerified();
                    }
                    else {
                        Log.d(TAG, "onResponse: Entered Incorrect Code");
                        verifyView.showError(ERROR_INCORRECT_CODE);
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                t.printStackTrace();
                if (verifyView != null)
                    verifyView.showError(t.getMessage());
            }
        });
    }

    @Override
    public void resendOTP(Context context) {
        HyperTrackLiveService getResendCodeService = HyperTrackLiveServiceGenerator.createService(HyperTrackLiveService.class,context);
        Call<User> call = getResendCodeService.sendCode(HyperTrack.getUserId());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (verifyView != null) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "onResponse: Code Resend Successfully");
                        verifyView.codeResent();
                    } else {
                        verifyView.showError("There is some error occurred. Please try again");
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                t.printStackTrace();
                if (verifyView != null)
                    verifyView.showError(t.getMessage());
            }
        });
    }
}