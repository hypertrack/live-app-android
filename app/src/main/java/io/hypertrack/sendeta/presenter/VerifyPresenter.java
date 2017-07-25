package io.hypertrack.sendeta.presenter;

import android.util.Log;

import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.models.User;

import io.hypertrack.sendeta.model.VerifyCodeModel;
import io.hypertrack.sendeta.network.retrofit.HyperTrackService;
import io.hypertrack.sendeta.network.retrofit.HyperTrackServiceGenerator;
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
    public void verifyOTP(String OTP) {
        VerifyCodeModel verifyCodeModel = new VerifyCodeModel(OTP);
        HyperTrackService getVerifyCodeService = HyperTrackServiceGenerator.createService(HyperTrackService.class);
        Call<User> call = getVerifyCodeService.validateCode(HyperTrack.getUserId(), verifyCodeModel);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.d(TAG, "onResponse: Code Verified");
                if (verifyView != null) {
                    if (response.isSuccessful())
                        verifyView.codeVerified();
                    else {
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
    public void resendOTP() {
        HyperTrackService getResendCodeService = HyperTrackServiceGenerator.createService(HyperTrackService.class);
        Call<User> call = getResendCodeService.sendCode(HyperTrack.getUserId());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.d(TAG, "onResponse: Code Resend Successfully");
                if(response.isSuccessful()) {
                    if (verifyView != null)
                        verifyView.codeResent();
                }
                else {
                    if (verifyView != null)
                        verifyView.showError("Error Occured");
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
