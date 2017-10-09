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

import com.hypertrack.lib.models.Place;

import java.util.List;

import io.hypertrack.sendeta.store.ActionManager;

/**
 * Created by piyush on 07/05/17.
 */

public interface IHomePresenter<V> extends Presenter<V> {

    void shareLiveLocation(final ActionManager actionManager,final String collectionID, final String lookupID, final Place expectedPlace);

    void stopSharing(final ActionManager actionManager, boolean fromGeofence);

    void openCustomShareCard(Context context, final ActionManager actionManager);

    void shareTrackingURL(ActionManager actionManager);

    void openNavigationForExpectedPlace(final ActionManager actionManager);

    void trackActionsOnMap(String collectionId,String lookupID, List<String> actionIDs, ActionManager actionManager,Context context);
}
