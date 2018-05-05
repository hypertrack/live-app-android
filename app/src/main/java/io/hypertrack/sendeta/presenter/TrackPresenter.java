
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

import android.support.annotation.NonNull;

import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.internal.common.util.HTTextUtils;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.SuccessResponse;

import java.util.List;

import io.hypertrack.sendeta.view.TrackView;

/**
 * Created by Aman Jain on 04/04/17.
 */

public class TrackPresenter implements ITrackPresenter {

    private TrackView trackView;

    public TrackPresenter(TrackView trackView) {
        this.trackView = trackView;
    }

    @Override
    public void removeTrackingAction() {
        HyperTrack.removeActions(null);
    }

    @Override
    public void trackAction(String collectionId, String uniqueId) {
        if (trackView != null)
            trackView.showLoader(true);

        if (!HTTextUtils.isEmpty(collectionId)) {
            HyperTrack.trackActionByCollectionId(collectionId, new HyperTrackCallback() {
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
        } else {
            HyperTrack.trackActionByUniqueId(uniqueId, new HyperTrackCallback() {
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
    }

    @Override
    public void trackAction(final List<String> actionsIDList) {
        if (trackView != null)
            trackView.showLoader(true);

        HyperTrack.trackAction(actionsIDList, new HyperTrackCallback() {
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
