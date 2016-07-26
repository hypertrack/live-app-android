package io.hypertrack.sendeta.view;

import io.hypertrack.sendeta.model.Membership;

/**
 * Created by piyush on 22/07/16.
 */
public interface BusinessProfileView {

    void handleGetMembershipSuccess(Membership membership);
    void showGetMembershipSuccess();
    void showGetMembershipError();
}
