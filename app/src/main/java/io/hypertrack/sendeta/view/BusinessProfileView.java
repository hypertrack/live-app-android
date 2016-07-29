package io.hypertrack.sendeta.view;

import io.hypertrack.sendeta.model.Membership;

/**
 * Created by piyush on 22/07/16.
 */
public interface BusinessProfileView {

    void handleGetMembershipSuccess(Membership membership);
    void handleGetMembershipError();
    void showMembershipActionSuccess(boolean acceptInvite, String accountName);
    void showMembershipActionError(boolean acceptInvite, String accountName);
}
