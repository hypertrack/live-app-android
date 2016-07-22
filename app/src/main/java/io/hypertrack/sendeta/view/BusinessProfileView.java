package io.hypertrack.sendeta.view;

import io.hypertrack.sendeta.model.BusinessProfileModel;

/**
 * Created by piyush on 22/07/16.
 */
public interface BusinessProfileView {
    void updateViewsToSetUpBusinessProfile();
    void updateViewsForPendingInvite(BusinessProfileModel businessProfile);
}
