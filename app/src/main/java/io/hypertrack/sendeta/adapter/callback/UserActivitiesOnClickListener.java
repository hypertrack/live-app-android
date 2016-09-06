package io.hypertrack.sendeta.adapter.callback;

import io.hypertrack.sendeta.model.UserActivity;

/**
 * Created by piyush on 02/09/16.
 */
public interface UserActivitiesOnClickListener {
    void OnInProcessActivityClicked(UserActivity inProcessActivity);
    void OnHistoryActivityClicked(UserActivity historyActivity);
}
