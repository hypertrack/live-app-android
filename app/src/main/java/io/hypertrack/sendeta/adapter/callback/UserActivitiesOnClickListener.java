package io.hypertrack.sendeta.adapter.callback;

import io.hypertrack.sendeta.model.UserActivityModel;

/**
 * Created by piyush on 02/09/16.
 */
public interface UserActivitiesOnClickListener {
    void OnInProcessActivityClicked(int position, UserActivityModel inProcessActivity);
    void OnHistoryActivityClicked(int position, UserActivityModel historyActivity);
}
