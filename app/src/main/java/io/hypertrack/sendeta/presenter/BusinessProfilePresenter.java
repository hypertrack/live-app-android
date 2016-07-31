package io.hypertrack.sendeta.presenter;

import io.hypertrack.sendeta.model.Membership;
import io.hypertrack.sendeta.model.User;
import io.hypertrack.sendeta.store.UserStore;
import io.hypertrack.sendeta.store.callback.UserStoreGetUserDataCallback;
import io.hypertrack.sendeta.store.callback.UserStoreMembershipCallback;
import io.hypertrack.sendeta.view.BusinessProfileView;

/**
 * Created by piyush on 22/07/16.
 */
public class BusinessProfilePresenter implements IBusinessProfilePresenter<BusinessProfileView> {

    private BusinessProfileView view;

    @Override
    public void attachView(BusinessProfileView view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        view = null;
    }

    @Override
    public void attemptAcceptPendingBusinessProfile(final Membership membership) {
        UserStore.sharedStore.acceptMembership(membership, new UserStoreMembershipCallback() {
            @Override
            public void OnSuccess(Membership membership) {
                if (view != null)
                    view.showMembershipActionSuccess(true, membership.getAccountName());
            }

            @Override
            public void OnError() {
                if (view != null)
                    view.showMembershipActionError(true, membership.getAccountName());
            }
        });
    }

    @Override
    public void attemptRejectPendingBusinessProfile(final Membership membership) {
        UserStore.sharedStore.rejectMembership(membership, new UserStoreMembershipCallback() {
            @Override
            public void OnSuccess(Membership membership) {
                if (view != null)
                    view.showMembershipActionSuccess(false, membership.getAccountName());
            }

            @Override
            public void OnError() {
                if (view != null)
                    view.showMembershipActionError(false, membership.getAccountName());
            }
        });
    }

    public void getMembershipsForUser(final int membershipAccountId) {
        UserStore.sharedStore.getUserData(new UserStoreGetUserDataCallback() {
            @Override
            public void OnSuccess(User user) {

                User updatedUser = UserStore.sharedStore.getUser();

                if (updatedUser != null && updatedUser.getPendingMemberships() != null
                        && updatedUser.getPendingMemberships().size() > 0) {

                    if (membershipAccountId == 0) {
                        if (updatedUser.getPendingMemberships().get(0) != null) {
                            view.handleGetMembershipSuccess(updatedUser.getPendingMemberships().get(0));
                            return;
                        }
                    } else {
                        // Check if the selected MembershipAccount Id exists in the PendingList
                        for (Membership membership : updatedUser.getPendingMemberships()) {
                            if (membership.getAccountId() == membershipAccountId) {
                                view.handleGetMembershipSuccess(membership);
                                return;
                            }
                        }
                    }
                }

                if (view != null)
                    view.handleGetMembershipError();
            }

            @Override
            public void OnError() {
                if (view != null)
                    view.handleGetMembershipError();
            }
        });
    }
}
