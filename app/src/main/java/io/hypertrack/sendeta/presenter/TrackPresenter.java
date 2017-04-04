package io.hypertrack.sendeta.presenter;

import java.util.List;

/**
 * Created by Aman Jain on 04/04/17.
 */

public interface TrackPresenter {
    void removeTrackingAction();

    void addTrackingAction(String id);

    void trackAction(List<String> actionsIDList);

    void destroy();

}
