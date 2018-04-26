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

import com.hypertrack.lib.models.Action;
import com.hypertrack.lib.models.Place;
import com.hypertrack.lib.models.User;

import java.util.List;

import io.hypertrack.sendeta.store.ActionManager;

/**
 * Created by piyush on 07/05/17.
 */

public interface IHomePresenter<V> extends Presenter<V> {

    void shareLiveLocation(final User user);

    void stopSharing(boolean fromGeofence);

    void getShareMessage();

    void trackActionsOnMap(String collectionId, boolean isDeepLinkTrackingAction);

    void refreshView(List<Action> actions, boolean isDeepLinkTrackingAction);

    /*
     * Method to restore app's state in case of ongoing location sharing for current user.
     */
    boolean restoreLocationSharing();

    void setActionManager(ActionManager actionManager);

    void updateExpectedPlace(Place place);

    void clearTrackingAction();
}
