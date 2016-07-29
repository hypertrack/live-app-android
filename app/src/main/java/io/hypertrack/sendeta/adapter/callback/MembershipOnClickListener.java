package io.hypertrack.sendeta.adapter.callback;

import io.hypertrack.sendeta.model.Membership;

/**
 * Created by piyush on 22/07/16.
 */
public interface MembershipOnClickListener {
    void onAddMembership();
    void onDeleteMembership(Membership membership);
    void onVerifyPendingMembership(Membership membership);
}
