package io.hypertrack.sendeta.adapter.callback;

import io.hypertrack.sendeta.model.AccountProfile;

/**
 * Created by piyush on 22/07/16.
 */
public interface AccountProfileOnClickListener {
    void onAddBusinessProfile();
    void onDeleteBusinessProfile(AccountProfile businessProfile);
    void onVerifyPendingBusinessProfile(AccountProfile businessProfile);
}
