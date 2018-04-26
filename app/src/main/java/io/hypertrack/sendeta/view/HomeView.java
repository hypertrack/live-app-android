
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
package io.hypertrack.sendeta.view;

import com.hypertrack.lib.models.Action;
import com.hypertrack.lib.models.ErrorResponse;

/**
 * Created by suhas on 25/02/16.
 */
public interface HomeView {

    void showShareLiveLocationError(ErrorResponse errorResponse);

    void showShareLiveLocationSuccess(Action action);

    void showStopSharingError();

    void showStopSharingSuccess();

    void showTrackActionsOnMapError(ErrorResponse errorResponse);

    void showLoading();

    void showLoading(String message);

    void hideLoading();

    void showShareCard(String shareMessage);

    void updateExpectedPlaceFailure(String s);

    void showUpdatePlaceLoading();

    void onActionRefreshed();
}
