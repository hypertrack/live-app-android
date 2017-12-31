package io.hypertrack.sendeta.presenter;

import android.content.Context;
import android.util.Log;

import com.hypertrack.lib.models.User;

import io.hypertrack.sendeta.model.AcceptInviteModel;
import io.hypertrack.sendeta.network.retrofit.HyperTrackLiveService;
import io.hypertrack.sendeta.network.retrofit.HyperTrackLiveServiceGenerator;
import io.hypertrack.sendeta.view.InviteView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Aman on 19/07/17.
 */

public class InvitePresenter implements IInvitePresenter<InviteView> {
    private static final String TAG = InvitePresenter.class.getSimpleName();
    private InviteView inviteView;

    @Override
    public void attachView(InviteView view) {
        inviteView = view;
    }

    @Override
    public void detachView() {
        inviteView = null;
    }

    @Override
    public void acceptInvite(String userID, String accountID, Context context) {
        HyperTrackLiveService getPlacelineService = HyperTrackLiveServiceGenerator.createService(HyperTrackLiveService.class,context);
        Call<User> call = getPlacelineService.acceptInvite(userID, new AcceptInviteModel(accountID, userID));
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (inviteView == null)
                    return;

                if (response.isSuccessful()) {
                    inviteView.inviteAccepted();
                } else {
                    inviteView.showError();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
                t.printStackTrace();
                if (inviteView != null)
                    inviteView.showError();
            }
        });
    }
}
