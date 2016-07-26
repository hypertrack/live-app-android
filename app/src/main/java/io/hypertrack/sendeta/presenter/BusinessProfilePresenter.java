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
    public void attemptVerifyPendingBusinessProfile(final Membership membership) {
        UserStore.sharedStore.acceptMembership(membership, new UserStoreMembershipCallback() {
            @Override
            public void OnSuccess(Membership membership) {
                if (view != null)
                    view.showGetMembershipSuccess();
            }

            @Override
            public void OnError() {
                if(view != null)
                    view.showGetMembershipError();
            }
        });
    }

    public void getMembershipForAccountId() {
        UserStore.sharedStore.getUserData(new UserStoreGetUserDataCallback() {
            @Override
            public void OnSuccess(User user) {

                User updatedUser = UserStore.sharedStore.getUser();

                if (updatedUser != null && updatedUser.getPendingMemberships() != null) {
                    if (updatedUser.getMemberships().size() > 0 && updatedUser.getMemberships().get(0) != null) {
                        view.handleGetMembershipSuccess(updatedUser.getMemberships().get(0));
                        return;
                    }
                }

                if (view != null)
                    view.showGetMembershipError();
            }

            @Override
            public void OnError() {
                if (view != null)
                    view.showGetMembershipError();
            }
        });
    }
}
