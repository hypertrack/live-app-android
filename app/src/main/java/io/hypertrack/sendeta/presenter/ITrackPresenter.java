package io.hypertrack.sendeta.presenter;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.SuccessResponse;

import java.util.ArrayList;
import java.util.List;

import io.hypertrack.sendeta.util.SharedPreferenceManager;
import io.hypertrack.sendeta.view.TrackView;

/**
 * Created by Aman Jain on 04/04/17.
 */

public class ITrackPresenter implements TrackPresenter {

    private TrackView trackView;

    public ITrackPresenter(TrackView trackView) {
        this.trackView = trackView;
    }

    @Override
    public void removeTrackingAction() {
        if (!TextUtils.isEmpty(SharedPreferenceManager.getCurrentTrackingAction())) {
            HyperTrack.removeActions(new ArrayList<String>() {{
                add(SharedPreferenceManager.getCurrentTrackingAction());
            }});

        }
    }

    @Override
    public void addTrackingAction(String id) {
        SharedPreferenceManager.setCurrentTrackingAction(id);
    }

    @Override
    public void trackAction(List<String> actionsIDList) {
        if (trackView != null)
            trackView.showLoader(true);

        HyperTrack.trackActionsForUser(actionsIDList, new HyperTrackCallback() {
            @Override
            public void onSuccess(@NonNull SuccessResponse response) {
                if (trackView != null) {
                    trackView.showTrackingDetail();
                    trackView.showLoader(false);
                }
            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {
                if (trackView != null)
                    trackView.showError();
            }
        });
    }

    @Override
    public void destroy() {
        trackView = null;
    }

}
