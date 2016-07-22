package io.hypertrack.sendeta.adapter.callback;

import io.hypertrack.sendeta.model.BusinessProfileModel;

/**
 * Created by piyush on 22/07/16.
 */
public interface BusinessProfileOnClickListener {
    void onAddBusinessProfile();
    void onDeleteBusinessProfile(BusinessProfileModel businessProfile);
    void onVerifyPendingBusinessProfile(BusinessProfileModel businessProfile);
}
